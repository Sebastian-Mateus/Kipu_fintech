package co.edu.uniquindio.fintech.excepciones;

/**
 * Se lanza cuando el codigo de billetera no existe.
 */
public class BilleteraNoEncontradaException extends FintechException {

    public BilleteraNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
