package co.edu.uniquindio.fintech.excepciones;

/**
 * Se lanza cuando la billetera no tiene plata suficiente para el movimiento
 * (contando la comision).
 */
public class SaldoInsuficienteException extends FintechException {

    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
