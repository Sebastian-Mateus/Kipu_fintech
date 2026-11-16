package co.edu.uniquindio.fintech.excepciones;

/**
 * Se lanza cuando se busca una transaccion por id y no existe.
 */
public class TransaccionNoEncontradaException extends FintechException {

    public TransaccionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
