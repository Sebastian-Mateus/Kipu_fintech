package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.estructuras.Pila;
import co.edu.uniquindio.fintech.excepciones.OperacionInvalidaException;
import co.edu.uniquindio.fintech.excepciones.SaldoInsuficienteException;
import co.edu.uniquindio.fintech.excepciones.TransaccionNoEncontradaException;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.RegistroReversion;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.EstadoTransaccion;
import co.edu.uniquindio.fintech.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.repositorio.RepositorioTransacciones;
import co.edu.uniquindio.fintech.util.GeneradorIdentificadores;
import co.edu.uniquindio.fintech.util.RelojSistema;
import co.edu.uniquindio.fintech.util.Validador;

import java.time.LocalDateTime;

/**
 * Revierte transacciones por id. No se guardan saldos viejos: la reversion es
 * una operacion compensatoria, osea hace el movimiento contrario y devuelve
 * la comision. Asi no se pierden los movimientos que pasaron despues.
 */
public class ServicioReversion {

    private static final int HORAS_PARA_REVERTIR = 24;

    private RepositorioTransacciones repositorioTransacciones;
    private RelojSistema reloj;
    private GeneradorIdentificadores generador;
    private ServicioNotificaciones notificaciones;
    private ServicioRecompensas recompensas;
    private ServicioAnalitica analitica;

    public ServicioReversion(RepositorioTransacciones repositorioTransacciones, RelojSistema reloj,
                             GeneradorIdentificadores generador, ServicioNotificaciones notificaciones,
                             ServicioRecompensas recompensas, ServicioAnalitica analitica) {
        this.repositorioTransacciones = repositorioTransacciones;
        this.reloj = reloj;
        this.generador = generador;
        this.notificaciones = notificaciones;
        this.recompensas = recompensas;
        this.analitica = analitica;
    }

    /**
     * Revierte la transaccion con ese id. Tiene que ser de un tipo reversible,
     * estar exitosa, no tener mas de 24 horas y el destino tiene que tener
     * plata para devolver. Devuelve la transaccion de reversion.
     */
    public Transaccion revertir(String idTransaccion) {
        Transaccion original = repositorioTransacciones.buscar(idTransaccion);
        if (original == null) {
            throw new TransaccionNoEncontradaException("No existe la transacción " + idTransaccion);
        }
        if (!esTipoReversible(original.getTipo())) {
            throw new OperacionInvalidaException("Las transacciones de tipo " + original.getTipo().getTexto()
                    + " no se pueden revertir");
        }
        if (original.getEstado() != EstadoTransaccion.EXITOSA) {
            throw new OperacionInvalidaException("Solo se pueden revertir transacciones exitosas");
        }

        LocalDateTime ahora = reloj.ahora();
        Usuario usuario = original.getUsuario();
        RegistroReversion registro = sacarDeLaPila(usuario, original, ahora);
        if (registro == null || !registro.estaVigente(ahora)) {
            throw new OperacionInvalidaException("Ya pasaron más de 24 horas, la transacción no se puede revertir");
        }

        Billetera origen = original.getBilleteraOrigen();
        Billetera destino = original.getBilleteraDestino();
        try {
            validarDevolucion(original, origen, destino);
        } catch (RuntimeException error) {
            // No se pudo, el registro vuelve a la pila para intentarlo mas tarde
            usuario.getPilaReversiones().apilar(registro);
            throw error;
        }

        // Movimiento contrario: el destino devuelve el valor y el origen recibe valor mas comision
        if (destino != null) {
            destino.retirar(original.getValor());
        }
        if (origen != null) {
            origen.depositar(original.getValorTotal());
        }

        Transaccion reversion = new Transaccion(generador.siguienteTransaccion(), ahora, TipoTransaccion.REVERSION,
                original.getValor(), 0, usuario, destino, origen, "Reversión de " + original.getId());
        reversion.setIdTransaccionOriginal(original.getId());
        reversion.setEstado(EstadoTransaccion.EXITOSA);
        registrarReversion(reversion);

        original.setEstado(EstadoTransaccion.REVERTIDA);
        repositorioTransacciones.quitarDeIndices(original);
        recompensas.descontar(usuario, registro.getPuntosOtorgados());

        if (destino != null && destino.tieneSaldoBajo()) {
            notificaciones.generar(destino.getPropietario(), TipoNotificacion.SALDO_BAJO, "Tu billetera "
                    + destino.getNombre() + " quedó con saldo bajo: " + Validador.formatearPesos(destino.getSaldo()));
        }
        notificaciones.despacharPendientes();
        return reversion;
    }

    /** Dice si se le puede mostrar el boton de revertir a una transaccion (para la interfaz). */
    public boolean esReversible(Transaccion transaccion) {
        return esTipoReversible(transaccion.getTipo())
                && transaccion.getEstado() == EstadoTransaccion.EXITOSA
                && !reloj.ahora().isAfter(transaccion.getFecha().plusHours(HORAS_PARA_REVERTIR));
    }

    private boolean esTipoReversible(TipoTransaccion tipo) {
        return tipo == TipoTransaccion.RECARGA
                || tipo == TipoTransaccion.TRANSFERENCIA_INTERNA
                || tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA
                || tipo == TipoTransaccion.PAGO_PROGRAMADO;
    }

    /*
     * Desapila a una pila auxiliar hasta encontrar la transaccion. Despues se
     * vuelven a apilar las demas en el mismo orden, y de paso se botan las que
     * ya se vencieron o que ya no estan exitosas.
     */
    private RegistroReversion sacarDeLaPila(Usuario usuario, Transaccion transaccion, LocalDateTime ahora) {
        Pila<RegistroReversion> pila = usuario.getPilaReversiones();
        Pila<RegistroReversion> auxiliar = new Pila<>();
        RegistroReversion encontrado = null;

        while (!pila.estaVacia() && encontrado == null) {
            RegistroReversion registro = pila.desapilar();
            if (registro.getTransaccion().equals(transaccion)) {
                encontrado = registro;
            } else {
                auxiliar.apilar(registro);
            }
        }

        while (!auxiliar.estaVacia()) {
            RegistroReversion registro = auxiliar.desapilar();
            if (registro.estaVigente(ahora) && registro.getTransaccion().esExitosa()) {
                pila.apilar(registro);
            }
        }
        return encontrado;
    }

    private void validarDevolucion(Transaccion original, Billetera origen, Billetera destino) {
        if (destino != null) {
            if (!destino.estaActiva()) {
                throw new OperacionInvalidaException("La billetera " + destino.getCodigo()
                        + " no está activa, no puede devolver la plata");
            }
            if (Validador.esMenor(destino.getSaldo(), original.getValor())) {
                throw new SaldoInsuficienteException("La billetera " + destino.getCodigo()
                        + " ya no tiene saldo suficiente para devolver "
                        + Validador.formatearPesos(original.getValor()));
            }
        }
        if (origen != null && !origen.estaActiva()) {
            throw new OperacionInvalidaException("La billetera " + origen.getCodigo()
                    + " no está activa, no puede recibir la devolución");
        }
    }

    private void registrarReversion(Transaccion reversion) {
        Usuario usuario = reversion.getUsuario();
        Billetera origen = reversion.getBilleteraOrigen();
        Billetera destino = reversion.getBilleteraDestino();

        usuario.getHistorial().agregarAlFinal(reversion);
        // Si la plata sale de la billetera de otra persona, ella tambien ve la reversion
        if (origen != null && !origen.getPropietario().equals(usuario)) {
            origen.getPropietario().getHistorial().agregarAlFinal(reversion);
        }
        if (origen != null) {
            origen.registrarTransaccion(reversion);
        }
        if (destino != null) {
            destino.registrarTransaccion(reversion);
        }
        repositorioTransacciones.guardar(reversion);
        analitica.registrar(reversion);
    }
}
