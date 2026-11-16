package co.edu.uniquindio.fintech.estructuras;

/**
 * Se lanza cuando alguien intenta sacar o mirar un elemento
 * de una estructura que esta vacia.
 */
public class EstructuraVaciaException extends RuntimeException {

    public EstructuraVaciaException(String mensaje) {
        super(mensaje);
    }
}
