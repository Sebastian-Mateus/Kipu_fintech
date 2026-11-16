package co.edu.uniquindio.fintech.modelo;

/**
 * Resultado de una medicion de tiempo del modulo de rendimiento:
 * que estructura, que operacion, con cuantos elementos y cuanto se demoro.
 */
public class ResultadoRendimiento {

    private String estructura;
    private String operacion;
    private int cantidadElementos;
    private long tiempoNanosegundos;

    public ResultadoRendimiento(String estructura, String operacion, int cantidadElementos, long tiempoNanosegundos) {
        this.estructura = estructura;
        this.operacion = operacion;
        this.cantidadElementos = cantidadElementos;
        this.tiempoNanosegundos = tiempoNanosegundos;
    }

    /** El mismo tiempo pero en milisegundos, que es mas facil de leer en la tabla. */
    public double getTiempoMilisegundos() {
        return tiempoNanosegundos / 1_000_000.0;
    }

    public String getEstructura() {
        return estructura;
    }

    public String getOperacion() {
        return operacion;
    }

    public int getCantidadElementos() {
        return cantidadElementos;
    }

    public long getTiempoNanosegundos() {
        return tiempoNanosegundos;
    }
}
