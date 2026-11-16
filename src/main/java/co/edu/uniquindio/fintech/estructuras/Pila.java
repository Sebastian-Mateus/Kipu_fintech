package co.edu.uniquindio.fintech.estructuras;

import java.util.Iterator;

/**
 * Pila enlazada, el ultimo que entra es el primero que sale.
 * Cada usuario tiene una con las operaciones que todavia puede revertir.
 */
public class Pila<T> implements Iterable<T> {

    private NodoPila<T> tope;
    private int tamanio;

    public Pila() {
        this.tope = null;
        this.tamanio = 0;
    }

    /** Pone un elemento encima de la pila. O(1). */
    public void apilar(T elemento) {
        NodoPila<T> nuevo = new NodoPila<>(elemento);
        nuevo.setDebajo(tope);
        tope = nuevo;
        tamanio++;
    }

    /** Saca y devuelve el elemnto de encima. O(1). */
    public T desapilar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La pila está vacía");
        }
        T dato = tope.getDato();
        tope = tope.getDebajo();
        tamanio--;
        return dato;
    }

    /** Devuelve el de encima pero sin sacarlo. O(1). */
    public T verTope() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La pila está vacía");
        }
        return tope.getDato();
    }

    /** Cantidad de elementos guardados. O(1). */
    public int tamanio() {
        return tamanio;
    }

    /** Dice si la pila no tiene elementos. O(1). */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /** Recorre la pila desde el tope hacia abajo. */
    @Override
    public Iterator<T> iterator() {
        return new IteradorPila<>(tope);
    }
}
