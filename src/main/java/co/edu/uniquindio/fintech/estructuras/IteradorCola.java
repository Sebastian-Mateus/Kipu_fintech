package co.edu.uniquindio.fintech.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Recorre la cola desde el frente hasta el final, sin sacar nada.
 */
public class IteradorCola<T> implements Iterator<T> {

    private NodoCola<T> actual;

    public IteradorCola(NodoCola<T> frente) {
        this.actual = frente;
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
        actual = actual.getSiguiente();
        return dato;
    }
}
