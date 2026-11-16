package co.edu.uniquindio.fintech.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Recorre la pila desde el tope hacia abajo, sin sacar nada.
 */
public class IteradorPila<T> implements Iterator<T> {

    private NodoPila<T> actual;

    public IteradorPila(NodoPila<T> tope) {
        this.actual = tope;
    }

    @Override
    public boolean hasNext() {
        return actual != null;
    }

    @Override
    public T next() {
        if (actual == null) {
            throw new NoSuchElementException("Ya no quedan elementos por recorrer");
        }
        T dato = actual.getDato();
        actual = actual.getDebajo();
        return dato;
    }
}
