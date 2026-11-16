package co.edu.uniquindio.fintech.excepciones;

/**
 * Se lanza cuando el usuario quiere canjear un beneficio y no le alcanzan
 * los puntos disponibles.
 */
public class PuntosInsuficientesException extends FintechException {

    public PuntosInsuficientesException(String mensaje) {
        super(mensaje);
    }
}
