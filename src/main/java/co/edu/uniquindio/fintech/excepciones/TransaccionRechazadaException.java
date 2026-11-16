package co.edu.uniquindio.fintech.excepciones;

/**
 * Se lanza cuando la deteccion de fraude marca la transaccion con riesgo alto
 * y la rechaza sola.
 */
public class TransaccionRechazadaException extends FintechException {

    public TransaccionRechazadaException(String mensaje) {
        super(mensaje);
    }
}
