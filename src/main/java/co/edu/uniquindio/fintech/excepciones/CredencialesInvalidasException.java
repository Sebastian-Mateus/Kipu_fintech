package co.edu.uniquindio.fintech.excepciones;

/**
 * Se lanza en el login cuando el usuario o la contraseña no coinciden.
 */
public class CredencialesInvalidasException extends FintechException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
