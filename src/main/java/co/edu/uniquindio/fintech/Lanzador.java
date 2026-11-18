package co.edu.uniquindio.fintech;

/**
 * Clase para arrancar el programa desde el IDE.
 * Si se ejecuta directamente AplicacionFintech sale el error
 * "JavaFX runtime components are missing", porque esa clase hereda de Application
 * y el proyecto no usa module-info. Arrancando desde aca ese error no aparece.
 */
public class Lanzador {

    public static void main(String[] args) {
        AplicacionFintech.main(args);
    }
}
