package co.edu.uniquindio.fintech.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Recorre la lista enlazada. Puede ir de la cabeza a la cola o al reves,
 * esto ultimo sirve para ver primero lo mas reciente.
 */
public class IteradorLista<T> implements Iterator<T> {

    private NodoLista<T> actual;
    private boolean haciaAtras;

    public IteradorLista(NodoLista<T> inicio, boolean haciaAtras) {
        this.actual = inicio;
        this.haciaAtras = haciaAtras;
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
        if (haciaAtras) {
            actual = actual.getAnterior();
        } else {
            actual = actual.getSiguiente();
        }
        return dato;
    }
}
