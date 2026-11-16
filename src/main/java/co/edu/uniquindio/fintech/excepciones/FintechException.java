package co.edu.uniquindio.fintech.excepciones;

/**
 * Excepcion base de la plataforma. Todas las demas heredan de esta, asi
 * en la interfaz se pueden atrapar juntas y mostrar el mensaje al usuario.
 */
public class FintechException extends RuntimeException {

    public FintechException(String mensaje) {
        super(mensaje);
    }
}
