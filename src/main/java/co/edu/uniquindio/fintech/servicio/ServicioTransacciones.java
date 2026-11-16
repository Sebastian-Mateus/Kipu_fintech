package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.excepciones.BilleteraNoEncontradaException;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.excepciones.LimiteDiarioExcedidoException;
import co.edu.uniquindio.fintech.excepciones.OperacionInvalidaException;
import co.edu.uniquindio.fintech.excepciones.SaldoInsuficienteException;
import co.edu.uniquindio.fintech.excepciones.TransaccionRechazadaException;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.CanjeBeneficio;
import co.edu.uniquindio.fintech.modelo.OperacionProgramada;
import co.edu.uniquindio.fintech.modelo.RegistroReversion;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.EstadoTransaccion;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.modelo.enums.NivelUsuario;
import co.edu.uniquindio.fintech.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.repositorio.RepositorioBilleteras;
import co.edu.uniquindio.fintech.repositorio.RepositorioTransacciones;
import co.edu.uniquindio.fintech.util.GeneradorIdentificadores;
import co.edu.uniquindio.fintech.util.RelojSistema;
import co.edu.uniquindio.fintech.util.Validador;

import java.time.LocalDateTime;

/**
 * Recargas, retiros y transferencias. Todas pasan por el mismo flujo:
 * 1. validar, 2. evaluar fraude, 3. mover los saldos, 4. registrar en todas
 * las estructuras, 5. dar puntos, 6. apilar para poder revertir,
 * 7. actualizar el perfil y 8. avisarle al usuario.
 */
public class ServicioTransacciones {

    // Bono extra de puntos cuando una programada se ejecuta bien
    private static final int BONO_PROGRAMADA = 10;

    private RepositorioTransacciones repositorioTransacciones;
    private RepositorioBilleteras repositorioBilleteras;
    private RelojSistema reloj;
    private GeneradorIdentificadores generador;
    private ServicioNotificaciones notificaciones;
    private ServicioRecompensas recompensas;
    private ServicioDeteccionFraude deteccionFraude;
    private ServicioAnalitica analitica;

    public ServicioTransacciones(RepositorioTransacciones repositorioTransacciones,
                                 RepositorioBilleteras repositorioBilleteras, RelojSistema reloj,
                                 GeneradorIdentificadores generador, ServicioNotificaciones notificaciones,
                                 ServicioRecompensas recompensas, ServicioDeteccionFraude deteccionFraude,
                                 ServicioAnalitica analitica) {
        this.repositorioTransacciones = repositorioTransacciones;
        this.repositorioBilleteras = repositorioBilleteras;
        this.reloj = reloj;
        this.generador = generador;
        this.notificaciones = notificaciones;
        this.recompensas = recompensas;
        this.deteccionFraude = deteccionFraude;
        this.analitica = analitica;
    }

    /** Mete plata desde afuera a una billetera del usuario. */
    public Transaccion recargar(Usuario usuario, String codigoDestino, double valor, String descripcion) {
        return procesar(usuario, TipoTransaccion.RECARGA, TipoTransaccion.RECARGA, null, codigoDestino, valor,
                descripcion, true, 0);
    }

    /** Saca plata de una billetera del usuario. Cobra comision segun el nivel. */
    public Transaccion retirar(Usuario usuario, String codigoOrigen, double valor, String descripcion) {
        return procesar(usuario, TipoTransaccion.RETIRO, TipoTransaccion.RETIRO, codigoOrigen, null, valor,
                descripcion, true, 0);
    }

    /** Mueve plata entre dos billeteras del mismo usuario. No cobra comision ni da puntos. */
    public Transaccion transferirInterna(Usuario usuario, String codigoOrigen, String codigoDestino, double valor,
                                         String descripcion) {
        return procesar(usuario, TipoTransaccion.TRANSFERENCIA_INTERNA, TipoTransaccion.TRANSFERENCIA_INTERNA,
                codigoOrigen, codigoDestino, valor, descripcion, true, 0);
    }

    /** Manda plata a la billetera de otro usuario. Cobra comision segun el nivel. */
    public Transaccion transferirExterna(Usuario usuario, String codigoOrigen, String codigoDestino, double valor,
                                         String descripcion) {
        return procesar(usuario, TipoTransaccion.TRANSFERENCIA_EXTERNA, TipoTransaccion.TRANSFERENCIA_EXTERNA,
                codigoOrigen, codigoDestino, valor, descripcion, true, 0);
    }

    /**
     * Ejecuta una operacion programada. Queda como PAGO_PROGRAMADO, no cuenta para
     * el limite diario y da 10 puntos de bono ademas de los de su tipo.
     */
    public Transaccion ejecutarProgramada(OperacionProgramada operacion) {
        String codigoOrigen = null;
        String codigoDestino = null;
        if (operacion.getBilleteraOrigen() != null) {
            codigoOrigen = operacion.getBilleteraOrigen().getCodigo();
        }
        if (operacion.getBilleteraDestino() != null) {
            codigoDestino = operacion.getBilleteraDestino().getCodigo();
        }
        String descripcion = "Programada " + operacion.getId() + ": " + operacion.getDescripcion();
        return procesar(operacion.getUsuario(), TipoTransaccion.PAGO_PROGRAMADO, operacion.getTipo(), codigoOrigen,
                codigoDestino, operacion.getValor(), descripcion, false, BONO_PROGRAMADA);
    }

    /** Comision que se cobraria, para la vista previa (no gasta usos de beneficios). */
    public double calcularComision(Usuario usuario, TipoTransaccion tipo, double valor) {
        CanjeBeneficio descuento = buscarDescuento(usuario, tipo, Validador.redondear(valor));
        return calcularComision(usuario, tipo, Validador.redondear(valor), descuento);
    }

    /** Puntos que daria la transaccion, para la vista previa. */
    public int estimarPuntos(Usuario usuario, TipoTransaccion tipo, double valor) {
        int puntos = recompensas.calcularPuntos(tipo, valor, usuario.getNivel());
        if (puntos > 0 && recompensas.buscarCanjeVigente(usuario, ServicioRecompensas.PUNTOS_DOBLES) != null) {
            puntos = puntos * 2;
        }
        return puntos;
    }

    /** Limite de transacciones de hoy, contando los beneficios. Devuelve SIN_LIMITE si no tiene tope. */
    public int limiteDiario(Usuario usuario) {
        if (usuario.getNivel().esIlimitado()) {
            return NivelUsuario.SIN_LIMITE;
        }
        return usuario.getNivel().getLimiteTransaccionesDiarias() + recompensas.limiteExtraHoy(usuario);
    }

    /** Si el reloj ya cambio de dia desde el ultimo movimiento, el contador del dia vuelve a cero. */
    public void actualizarDia(Usuario usuario) {
        if (usuario.getFechaUltimoMovimiento() == null || !reloj.esHoy(usuario.getFechaUltimoMovimiento())) {
            usuario.reiniciarTransaccionesHoy();
        }
    }

    /**
     * Aqui pasa todo. El tipo es con el que queda guardada la transacion y el
     * tipoBase es el movimiento real (en las programadas son distintos).
     */
    private Transaccion procesar(Usuario usuario, TipoTransaccion tipo, TipoTransaccion tipoBase, String codigoOrigen,
                                 String codigoDestino, double valor, String descripcion, boolean cuentaLimite,
                                 int bonoPuntos) {
        LocalDateTime ahora = reloj.ahora();
        double monto = Validador.redondear(valor);
        actualizarDia(usuario);

        // 1. Validar. Si algo falla queda una transaccion rechazada y se avisa
        Billetera origen = null;
        Billetera destino = null;
        double comision = 0;
        CanjeBeneficio descuento = null;
        try {
            if (codigoOrigen != null) {
                origen = buscarBilletera(codigoOrigen);
            }
            if (codigoDestino != null) {
                destino = buscarBilletera(codigoDestino);
            }
            descuento = buscarDescuento(usuario, tipoBase, monto);
            comision = calcularComision(usuario, tipoBase, monto, descuento);
            validar(usuario, tipoBase, origen, destino, monto, comision, cuentaLimite);
        } catch (FintechException error) {
            Transaccion rechazada = new Transaccion(generador.siguienteTransaccion(), ahora, tipo, monto, comision,
                    usuario, origen, destino, descripcion);
            registrarRechazo(rechazada, error.getMessage());
            throw error;
        }

        Transaccion transaccion = new Transaccion(generador.siguienteTransaccion(), ahora, tipo, monto, comision,
                usuario, origen, destino, descripcion);

        // 2. Fraude. Con riesgo alto se rechaza sola
        NivelRiesgo riesgo = deteccionFraude.evaluar(transaccion, usuario);
        if (riesgo == NivelRiesgo.ALTO) {
            registrarRechazo(transaccion, "riesgo alto de fraude");
            throw new TransaccionRechazadaException("La operación fue rechazada automáticamente por riesgo alto de fraude");
        }

        // 3. Mover la plata
        if (origen != null) {
            origen.retirar(Validador.redondear(monto + comision));
        }
        if (destino != null) {
            destino.depositar(monto);
        }
        transaccion.setEstado(EstadoTransaccion.EXITOSA);

        // 4. Registrar en todas partes
        registrarExitosa(transaccion);

        // 5. Puntos y nivel
        int puntos = recompensas.calcularPuntos(tipoBase, monto, usuario.getNivel());
        puntos = recompensas.aplicarPuntosDobles(usuario, puntos);
        puntos = puntos + bonoPuntos;
        transaccion.setPuntosGenerados(puntos);
        recompensas.otorgar(usuario, puntos);
        if (descuento != null) {
            descuento.usar();
        }
        if (cuentaLimite) {
            usuario.sumarTransaccionHoy();
        }
        usuario.setFechaUltimoMovimiento(ahora);

        // 6. Apilar para poder revertir (los retiros no se revierten)
        if (tipo != TipoTransaccion.RETIRO) {
            usuario.getPilaReversiones().apilar(new RegistroReversion(transaccion, puntos));
        }

        // 7. Perfil para las reglas de fraude
        usuario.getPerfil().registrar(monto, ahora);

        // 8. Avisos
        if (origen != null && origen.tieneSaldoBajo()) {
            notificaciones.generar(usuario, TipoNotificacion.SALDO_BAJO, "Tu billetera " + origen.getNombre()
                    + " quedó con saldo bajo: " + Validador.formatearPesos(origen.getSaldo()));
        }
        notificaciones.despacharPendientes();
        return transaccion;
    }

    private void validar(Usuario usuario, TipoTransaccion tipoBase, Billetera origen, Billetera destino, double monto,
                         double comision, boolean cuentaLimite) {
        if (!Validador.esPositivo(monto)) {
            throw new OperacionInvalidaException("El monto debe ser mayor que cero");
        }
        if (!usuario.isActivo()) {
            throw new OperacionInvalidaException("El usuario está inactivo");
        }

        // Que billeteras necesita cada tipo y de quien tienen que ser
        switch (tipoBase) {
            case RECARGA:
                exigirBilletera(destino, "destino");
                exigirPropia(destino, usuario);
                break;
            case RETIRO:
                exigirBilletera(origen, "origen");
                exigirPropia(origen, usuario);
                break;
            case TRANSFERENCIA_INTERNA:
                exigirBilletera(origen, "origen");
                exigirBilletera(destino, "destino");
                exigirPropia(origen, usuario);
                exigirPropia(destino, usuario);
                break;
            case TRANSFERENCIA_EXTERNA:
                exigirBilletera(origen, "origen");
                exigirBilletera(destino, "destino");
                exigirPropia(origen, usuario);
                if (destino.getPropietario().equals(usuario)) {
                    throw new OperacionInvalidaException(
                            "La billetera destino es tuya, usa una transferencia interna");
                }
                break;
            default:
                throw new OperacionInvalidaException("Tipo de operación no válido");
        }

        if (origen != null && !origen.estaActiva()) {
            throw new OperacionInvalidaException("La billetera de origen " + origen.getCodigo() + " no está activa");
        }
        if (destino != null && !destino.estaActiva()) {
            throw new OperacionInvalidaException("La billetera destino " + destino.getCodigo() + " no está activa");
        }
        if (origen != null && destino != null && origen.equals(destino)) {
            throw new OperacionInvalidaException("La billetera de origen y la de destino no pueden ser la misma");
        }
        if (origen != null && Validador.esMenor(origen.getSaldo(), Validador.redondear(monto + comision))) {
            throw new SaldoInsuficienteException("Saldo insuficiente: necesitas "
                    + Validador.formatearPesos(monto + comision) + " (incluye "
                    + Validador.formatearPesos(comision) + " de comisión)");
        }
        if (cuentaLimite) {
            int limite = limiteDiario(usuario);
            if (limite != NivelUsuario.SIN_LIMITE && usuario.getTransaccionesHoy() >= limite) {
                throw new LimiteDiarioExcedidoException("Ya hiciste las " + limite
                        + " transacciones que permite tu nivel hoy");
            }
        }
    }

    private void exigirBilletera(Billetera billetera, String cual) {
        if (billetera == null) {
            throw new OperacionInvalidaException("Debes elegir la billetera de " + cual);
        }
    }

    private void exigirPropia(Billetera billetera, Usuario usuario) {
        if (!billetera.getPropietario().equals(usuario)) {
            throw new OperacionInvalidaException("La billetera " + billetera.getCodigo() + " no es tuya");
        }
    }

    private Billetera buscarBilletera(String codigo) {
        Billetera billetera = repositorioBilleteras.buscar(codigo);
        if (billetera == null) {
            throw new BilleteraNoEncontradaException("No existe la billetera " + codigo);
        }
        return billetera;
    }

    // Solo cobran comision los retiros y las transferencias externas
    private double calcularComision(Usuario usuario, TipoTransaccion tipo, double monto, CanjeBeneficio descuento) {
        if (tipo != TipoTransaccion.RETIRO && tipo != TipoTransaccion.TRANSFERENCIA_EXTERNA) {
            return 0;
        }
        double comision = monto * usuario.getNivel().getPorcentajeComision() / 100;
        if (descuento != null) {
            comision = comision * (1 - descuento.getBeneficio().getValor() / 100);
        }
        return Validador.redondear(comision);
    }

    // El descuento solo se busca si de verdad hay comision, asi no se gasta un uso en vano
    private CanjeBeneficio buscarDescuento(Usuario usuario, TipoTransaccion tipo, double monto) {
        if (calcularComision(usuario, tipo, monto, null) <= 0) {
            return null;
        }
        return recompensas.buscarDescuentoComision(usuario, tipo);
    }

    private void registrarExitosa(Transaccion transaccion) {
        Usuario usuario = transaccion.getUsuario();
        Billetera origen = transaccion.getBilleteraOrigen();
        Billetera destino = transaccion.getBilleteraDestino();

        usuario.getHistorial().agregarAlFinal(transaccion);
        // El que recibe una transferencia externa tambien la ve en su historial
        if (destino != null && !destino.getPropietario().equals(usuario)) {
            destino.getPropietario().getHistorial().agregarAlFinal(transaccion);
        }
        if (origen != null) {
            origen.registrarTransaccion(transaccion);
        }
        if (destino != null) {
            destino.registrarTransaccion(transaccion);
        }
        repositorioTransacciones.guardar(transaccion);
        repositorioTransacciones.indexar(transaccion);
        analitica.registrar(transaccion);
    }

    private void registrarRechazo(Transaccion transaccion, String motivo) {
        transaccion.setEstado(EstadoTransaccion.RECHAZADA);
        transaccion.getUsuario().getHistorial().agregarAlFinal(transaccion);
        repositorioTransacciones.guardar(transaccion);
        notificaciones.generar(transaccion.getUsuario(), TipoNotificacion.OPERACION_RECHAZADA,
                "Tu " + transaccion.getTipo().getTexto().toLowerCase() + " por "
                        + Validador.formatearPesos(transaccion.getValor()) + " fue rechazada: " + motivo);
        notificaciones.despacharPendientes();
    }
}
