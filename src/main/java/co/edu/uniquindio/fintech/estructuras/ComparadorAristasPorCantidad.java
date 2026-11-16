package co.edu.uniquindio.fintech.estructuras;

import java.util.Comparator;

/**
 * Pone primero las aristas con mas transferencias.
 * Si empatan va primero la que movio mas plata.
 */
public class ComparadorAristasPorCantidad<T> implements Comparator<Arista<T>> {

    @Override
    public int compare(Arista<T> primera, Arista<T> segunda) {
        int porCantidad = Integer.compare(segunda.getCantidadTransferencias(), primera.getCantidadTransferencias());
        if (porCantidad != 0) {
            return porCantidad;
        }
        return Double.compare(segunda.getMontoTotal(), primera.getMontoTotal());
    }
}
