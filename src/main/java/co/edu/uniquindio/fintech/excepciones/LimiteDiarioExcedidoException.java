package co.edu.uniquindio.fintech.excepciones;

/**
 * Se lanza cuando el usuario ya gasto todas las transacciones que su nivel
 * le deja hacer en el dia.
 */
public class LimiteDiarioExcedidoException extends FintechException {

    public LimiteDiarioExcedidoException(String mensaje) {
        super(mensaje);
    }
}
