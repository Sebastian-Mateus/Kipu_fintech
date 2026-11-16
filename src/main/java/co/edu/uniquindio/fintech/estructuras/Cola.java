package co.edu.uniquindio.fintech.estructuras;

import java.util.Iterator;

/**
 * Cola enlazada, el primero que entra es el primero que sale.
 * La usamos para las notificaciones que esperan a ser despachadas y en el BFS del grafo.
 */
public class Cola<T> implements Iterable<T> {

    private NodoCola<T> frente;
    private NodoCola<T> fin;
    private int tamanio;

    public Cola() {
        this.frente = null;
        this.fin = null;
        this.tamanio = 0;
    }

    /** Agrega un elemento al final de la cola. O(1). */
    public void encolar(T elemento) {
        NodoCola<T> nuevo = new NodoCola<>(elemento);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        tamanio++;
    }

    /** Quita y devuelve el elemento del frente. O(1). */
    public T desencolar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola está vacía");
        }
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) {
            // Si se vacio, el fin tambien tiene que quedar en null
            fin = null;
        }
        tamanio--;
        return dato;
    }

    /** Devuelve el elemento del frente sin quitarlo. O(1). */
    public T verFrente() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola está vacía");
        }
        return frente.getDato();
    }

    /** Cantidad de elementos guardados. O(1). */
    public int tamanio() {
        return tamanio;
    }

    /** Dice si la cola no tiene elementos. O(1). */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /** Recorre la cola desde el frente hasta el final. */
    @Override
    public Iterator<T> iterator() {
        return new IteradorCola<>(frente);
    }
}
