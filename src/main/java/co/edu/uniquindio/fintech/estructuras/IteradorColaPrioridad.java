package co.edu.uniquindio.fintech.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Recorre el arreglo del monticulo en el orden en que esta guardado.
 * Ojo: ese orden no es el de prioridad, lo unico seguro es que el primero si es el mayor.
 */
public class IteradorColaPrioridad<T> implements Iterator<T> {

    private T[] elementos;
    private int cantidad;
    private int posicion;

    public IteradorColaPrioridad(T[] elementos, int cantidad) {
        this.elementos = elementos;
        this.cantidad = cantidad;
        this.posicion = 0;
    }

    @Override
    public boolean hasNext() {
        return posicion < cantidad;
    }

    @Override
    public T next() {
        if (posicion >= cantidad) {
            throw new NoSuchElementException("Ya no quedan elementos por recorrer");
        }
        T dato = elementos[posicion];
        posicion++;
        return dato;
    }
}
