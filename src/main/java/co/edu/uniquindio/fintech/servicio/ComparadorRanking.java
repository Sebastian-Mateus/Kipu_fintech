package co.edu.uniquindio.fintech.servicio;

import java.util.Comparator;

/**
 * Pone primero los elementos con el contador mas alto.
 */
public class ComparadorRanking<T> implements Comparator<ElementoRanking<T>> {

    @Override
    public int compare(ElementoRanking<T> primero, ElementoRanking<T> segundo) {
        return Integer.compare(segundo.getCantidad(), primero.getCantidad());
    }
}
