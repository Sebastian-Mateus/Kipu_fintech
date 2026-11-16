package co.edu.uniquindio.fintech.excepciones;

/**
 * Se lanza cuando los datos de una operacion no son validos, por ejemplo
 * un monto negativo, una billetera bloqueada o un campo vacio.
 */
public class OperacionInvalidaException extends FintechException {

    public OperacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
