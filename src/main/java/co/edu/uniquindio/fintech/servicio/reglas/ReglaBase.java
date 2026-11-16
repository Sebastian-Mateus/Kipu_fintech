package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.EstadoTransaccion;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;

/**
 * Metodos que comparten varias reglas, para no repetirlos en cada una.
 */
public abstract class ReglaBase implements ReglaDeteccion {

    /** Una transferencia es cualquier movimiento con origen y destino (tambien las programadas). */
    protected boolean esTransferencia(Transaccion transaccion) {
        return transaccion.getTipo() != TipoTransaccion.REVERSION
                && transaccion.getBilleteraOrigen() != null
                && transaccion.getBilleteraDestino() != null;
    }

    /** Es externa cuando el destino es de otro usuario. */
    protected boolean esExterna(Transaccion transaccion) {
        return esTransferencia(transaccion)
                && !transaccion.getBilleteraDestino().getPropietario()
                .equals(transaccion.getBilleteraOrigen().getPropietario());
    }

    /**
     * En el historial tambien quedan las transferencias que le llegan de otros
     * y las rechazadas, esas no cuentan para las reglas.
     */
    protected boolean cuentaParaElUsuario(Transaccion transaccion, Usuario usuario) {
        return transaccion.getUsuario().equals(usuario)
                && transaccion.getEstado() != EstadoTransaccion.RECHAZADA
                && transaccion.getTipo() != TipoTransaccion.REVERSION;
    }
}
