package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.estructuras.ColaPrioridad;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.estructuras.TablaHash;
import co.edu.uniquindio.fintech.excepciones.BilleteraNoEncontradaException;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.excepciones.OperacionInvalidaException;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.OperacionProgramada;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.Frecuencia;
import co.edu.uniquindio.fintech.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.repositorio.RepositorioBilleteras;
import co.edu.uniquindio.fintech.util.GeneradorIdentificadores;
import co.edu.uniquindio.fintech.util.RelojSistema;
import co.edu.uniquindio.fintech.util.Validador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Operaciones programadas. Las activas viven en un monticulo ordenado por fecha,
 * asi la proxima a ejecutar siempre esta arriba. Se procesan cada vez que el
 * administrador adelanta el reloj.
 */
public class ServicioProgramadas {

    private static final int MAXIMO_INTENTOS = 3;
    private static final int HORAS_PARA_ALERTA = 24;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ColaPrioridad<OperacionProgramada> monticulo;
    private TablaHash<String, OperacionProgramada> programadasPorId;
    private ListaEnlazada<String> bitacora;

    private RelojSistema reloj;
    private GeneradorIdentificadores generador;
    private ServicioTransacciones transacciones;
    private ServicioNotificaciones notificaciones;
    private RepositorioBilleteras repositorioBilleteras;

    public ServicioProgramadas(RelojSistema reloj, GeneradorIdentificadores generador,
                               ServicioTransacciones transacciones, ServicioNotificaciones notificaciones,
                               RepositorioBilleteras repositorioBilleteras) {
        this.reloj = reloj;
        this.generador = generador;
        this.transacciones = transacciones;
        this.notificaciones = notificaciones;
        this.repositorioBilleteras = repositorioBilleteras;
        this.monticulo = new ColaPrioridad<>(new ComparadorOperacionesProgramadas());
        this.programadasPorId = new TablaHash<>();
        this.bitacora = new ListaEnlazada<>();
    }

    /** Programa una operacion para una fecha futura. */
    public OperacionProgramada programar(Usuario usuario, TipoTransaccion tipo, String codigoOrigen,
                                         String codigoDestino, double valor, LocalDateTime fechaEjecucion,
                                         Frecuencia frecuencia, String descripcion) {
        if (tipo == TipoTransaccion.PAGO_PROGRAMADO || tipo == TipoTransaccion.REVERSION) {
            throw new OperacionInvalidaException("Ese tipo de operación no se puede programar");
        }
        if (!Validador.esPositivo(valor)) {
            throw new OperacionInvalidaException("El monto debe ser mayor que cero");
        }
        if (fechaEjecucion == null || !fechaEjecucion.isAfter(reloj.ahora())) {
            throw new OperacionInvalidaException("La fecha de ejecución tiene que ser en el futuro");
        }
        if (frecuencia == null) {
            throw new OperacionInvalidaException("Debes elegir la frecuencia");
        }

        Billetera origen = buscarOpcional(codigoOrigen);
        Billetera destino = buscarOpcional(codigoDestino);
        validarBilleteras(usuario, tipo, origen, destino);

        OperacionProgramada operacion = new OperacionProgramada(generador.siguienteProgramada(), usuario, tipo,
                valor, origen, destino, descripcion, reloj.ahora(), fechaEjecucion, frecuencia);
        monticulo.insertar(operacion);
        programadasPorId.insertar(operacion.getId(), operacion);

        revisarAlertas();
        notificaciones.despacharPendientes();
        return operacion;
    }

    /** Cancela una programada: la desactiva y la saca del monticulo. */
    public void cancelar(String idOperacion) {
        OperacionProgramada operacion = programadasPorId.obtener(idOperacion);
        if (operacion == null || !operacion.isActiva()) {
            throw new OperacionInvalidaException("La operación programada no existe o ya no está activa");
        }
        operacion.setActiva(false);
        monticulo.eliminar(operacion);
        anotar(operacion.getId() + " cancelada por el usuario");
    }

    /**
     * Ejecuta todas las programadas cuya fecha ya llego. Mientras la de arriba
     * del monticulo este vencida se saca y se ejecuta. Devuelve cuantas proceso.
     */
    public int procesarPendientes() {
        int procesadas = 0;
        while (!monticulo.estaVacia() && !monticulo.verPrimero().getFechaProximaEjecucion().isAfter(reloj.ahora())) {
            OperacionProgramada operacion = monticulo.extraer();
            ejecutar(operacion);
            procesadas++;
        }
        revisarAlertas();
        notificaciones.despacharPendientes();
        return procesadas;
    }

    /** Dice si el usuario tiene programadas activas (no se puede eliminar si tiene). */
    public boolean tieneActivas(Usuario usuario) {
        for (OperacionProgramada operacion : monticulo) {
            if (operacion.getUsuario().equals(usuario)) {
                return true;
            }
        }
        return false;
    }

    /** Las programadas activas de un usuario. */
    public ListaEnlazada<OperacionProgramada> activasDe(Usuario usuario) {
        ListaEnlazada<OperacionProgramada> resultado = new ListaEnlazada<>();
        for (OperacionProgramada operacion : monticulo) {
            if (operacion.getUsuario().equals(usuario)) {
                resultado.agregarAlFinal(operacion);
            }
        }
        return resultado;
    }

    /** Todas las programadas activas de la plataforma. */
    public ListaEnlazada<OperacionProgramada> activas() {
        ListaEnlazada<OperacionProgramada> resultado = new ListaEnlazada<>();
        for (OperacionProgramada operacion : monticulo) {
            resultado.agregarAlFinal(operacion);
        }
        return resultado;
    }

    /** Lo que ha ido pasando con las programadas, para la pantalla de simulacion. */
    public ListaEnlazada<String> getBitacora() {
        return bitacora;
    }

    private void ejecutar(OperacionProgramada operacion) {
        try {
            Transaccion transaccion = transacciones.ejecutarProgramada(operacion);
            operacion.registrarEjecucion();
            anotar(operacion.getId() + " ejecutada (" + transaccion.getId() + ")");

            LocalDateTime siguiente = operacion.getFrecuencia().siguienteFecha(operacion.getFechaProximaEjecucion());
            if (siguiente != null) {
                operacion.setFechaProximaEjecucion(siguiente);
                operacion.setAlertaEnviada(false);
                monticulo.insertar(operacion);
            } else {
                operacion.setActiva(false);
            }
        } catch (FintechException error) {
            operacion.registrarFallo();
            if (operacion.getIntentosFallidos() >= MAXIMO_INTENTOS) {
                operacion.setActiva(false);
                anotar(operacion.getId() + " desactivada después de " + MAXIMO_INTENTOS + " intentos fallidos");
                notificaciones.generar(operacion.getUsuario(), TipoNotificacion.OPERACION_RECHAZADA,
                        "Tu operación programada \"" + operacion.getDescripcion()
                                + "\" se desactivó después de " + MAXIMO_INTENTOS + " intentos fallidos.");
            } else {
                // Se vuelve a intentar en una hora
                operacion.setFechaProximaEjecucion(reloj.ahora().plusHours(1));
                monticulo.insertar(operacion);
                anotar(operacion.getId() + " falló (" + error.getMessage() + "), se reintenta en 1 hora");
            }
        }
    }

    /*
     * Avisa de las programadas que se ejecutan en menos de 24 horas, una sola vez
     * por ejecucion. Toca recorrer todo el arreglo del monticulo, asi que es O(n).
     */
    private void revisarAlertas() {
        LocalDateTime limite = reloj.ahora().plusHours(HORAS_PARA_ALERTA);
        for (OperacionProgramada operacion : monticulo) {
            if (!operacion.isAlertaEnviada() && operacion.getFechaProximaEjecucion().isBefore(limite)) {
                operacion.setAlertaEnviada(true);
                notificaciones.generar(operacion.getUsuario(), TipoNotificacion.PROGRAMADA_PROXIMA,
                        "Tu operación \"" + operacion.getDescripcion() + "\" por "
                                + Validador.formatearPesos(operacion.getValor()) + " se ejecuta el "
                                + operacion.getFechaProximaEjecucion().format(FORMATO_FECHA));
            }
        }
    }

    private void validarBilleteras(Usuario usuario, TipoTransaccion tipo, Billetera origen, Billetera destino) {
        boolean necesitaOrigen = tipo != TipoTransaccion.RECARGA;
        boolean necesitaDestino = tipo != TipoTransaccion.RETIRO;
        if (necesitaOrigen && origen == null) {
            throw new OperacionInvalidaException("Debes elegir la billetera de origen");
        }
        if (necesitaDestino && destino == null) {
            throw new OperacionInvalidaException("Debes elegir la billetera destino");
        }
        if (origen != null && !origen.getPropietario().equals(usuario)) {
            throw new OperacionInvalidaException("La billetera de origen no es tuya");
        }
        if (destino != null && tipo != TipoTransaccion.TRANSFERENCIA_EXTERNA
                && !destino.getPropietario().equals(usuario)) {
            throw new OperacionInvalidaException("La billetera destino no es tuya");
        }
        if (destino != null && tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA
                && destino.getPropietario().equals(usuario)) {
            throw new OperacionInvalidaException("La billetera destino es tuya, usa una transferencia interna");
        }
        if (origen != null && origen.equals(destino)) {
            throw new OperacionInvalidaException("La billetera de origen y la de destino no pueden ser la misma");
        }
    }

    private Billetera buscarOpcional(String codigo) {
        if (codigo == null) {
            return null;
        }
        Billetera billetera = repositorioBilleteras.buscar(codigo);
        if (billetera == null) {
            throw new BilleteraNoEncontradaException("No existe la billetera " + codigo);
        }
        return billetera;
    }

    private void anotar(String mensaje) {
        bitacora.agregarAlFinal(reloj.ahora().format(FORMATO_FECHA) + "  " + mensaje);
    }
}
