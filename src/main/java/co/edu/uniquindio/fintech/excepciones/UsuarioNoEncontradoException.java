package co.edu.uniquindio.fintech.excepciones;

/**
 * Se lanza cuando se busca un usuario por id y no aparece.
 */
public class UsuarioNoEncontradoException extends FintechException {

    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
