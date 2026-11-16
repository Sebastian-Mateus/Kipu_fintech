package co.edu.uniquindio.fintech.servicio;

import java.util.Comparator;

/**
 * Para que la cola de prioridad saque primero el numero mas grande.
 */
public class ComparadorMayorPrimero implements Comparator<Double> {

    @Override
    public int compare(Double primero, Double segundo) {
        return Double.compare(segundo, primero);
    }
}
