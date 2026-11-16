package co.edu.uniquindio.fintech.estructuras;

import java.util.Iterator;

/**
 * Lista doblemente enlazada. Le guardamos la cabeza y la cola para poder
 * meter y sacar cosas por los dos lados sin tener que recorrerla toda.
 * La usamos en los historiales, las notificaciones recientes, la auditoría y otras cosas más.
 */
public class ListaEnlazada<T> implements Iterable<T> {

    private NodoLista<T> cabeza;
    private NodoLista<T> cola;
    private int tamanio;

    public ListaEnlazada() {
        this.cabeza = null;
        this.cola = null;
        this.tamanio = 0;
    }

    /** Agrega el elemento de ultimas en la lista. O(1). */
    public void agregarAlFinal(T elemento) {
        NodoLista<T> nuevo = new NodoLista<>(elemento);
        if (estaVacia()) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            nuevo.setAnterior(cola);
            cola.setSiguiente(nuevo);
            cola = nuevo;
        }
        tamanio++;
    }

    /** Agrega el elemento de primeras en la lista. O(1). */
    public void agregarAlInicio(T elemento) {
        NodoLista<T> nuevo = new NodoLista<>(elemento);
        if (estaVacia()) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            nuevo.setSiguiente(cabeza);
            cabeza.setAnterior(nuevo);
            cabeza = nuevo;
        }
        tamanio++;
    }

    /** Atajo para agregar al final, que es lo que más hacemos. O(1). */
    public void agregar(T elemento) {
        agregarAlFinal(elemento);
    }

    /** Quita y devuelve el primer elemento. O(1). */
    public T eliminarPrimero() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La lista está vacía");
        }
        T dato = cabeza.getDato();
        desenlazar(cabeza);
        return dato;
    }

    /** Quita y devuelve el ultimo elemento. O(1). */
    public T eliminarUltimo() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La lista está vacía");
        }
        T dato = cola.getDato();
        desenlazar(cola);
        return dato;
    }

    /** Devuelve el primer elemento sin quitarlo. O(1). */
    public T obtenerPrimero() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La lista está vacía");
        }
        return cabeza.getDato();
    }

    /** Devuelve el ultimo sin sacarlo de la lista. O(1). */
    public T obtenerUltimo() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La lista está vacía");
        }
        return cola.getDato();
    }

    /** Devuelve el elemento que esta en esa posicion, contando desde 0. O(n). */
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Posición fuera de la lista: " + indice);
        }
        // Si la posicion cae en la segunda mitad sale mas rapido llegar desde la cola
        NodoLista<T> actual;
        if (indice < tamanio / 2) {
            actual = cabeza;
            for (int posicion = 0; posicion < indice; posicion++) {
                actual = actual.getSiguiente();
            }
        } else {
            actual = cola;
            for (int posicion = tamanio - 1; posicion > indice; posicion--) {
                actual = actual.getAnterior();
            }
        }
        return actual.getDato();
    }

    /** Borra la primera vez que aparece el elemento y devuelve true si lo encontro. O(n). */
    public boolean eliminar(T elemento) {
        NodoLista<T> actual = cabeza;
        while (actual != null) {
            if (sonIguales(actual.getDato(), elemento)) {
                desenlazar(actual);
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    /** Dice si el elemento esta en la lista. O(n). */
    public boolean contiene(T elemento) {
        NodoLista<T> actual = cabeza;
        while (actual != null) {
            if (sonIguales(actual.getDato(), elemento)) {
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    /** Cantidad de elementos guardados. O(1). */
    public int tamanio() {
        return tamanio;
    }

    /** Dice si la lista no tiene elementos. O(1). */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /** Deja la lista vacia otra vez. */
    public void limpiar() {
        cabeza = null;
        cola = null;
        tamanio = 0;
    }

    /** Recorre la lista del primero al ultimo. */
    @Override
    public Iterator<T> iterator() {
        return new IteradorLista<>(cabeza, false);
    }

    /** Recorre la lista al reves, osea del ultimo al primero (lo mas reciente sale primero). */
    public Iterator<T> iteradorInverso() {
        return new IteradorLista<>(cola, true);
    }

    // Saca el nodo y arregla los enlaces de los vecinos para que no quede un hueco
    private void desenlazar(NodoLista<T> nodo) {
        NodoLista<T> anterior = nodo.getAnterior();
        NodoLista<T> siguiente = nodo.getSiguiente();

        if (anterior == null) {
            cabeza = siguiente;
        } else {
            anterior.setSiguiente(siguiente);
        }

        if (siguiente == null) {
            cola = anterior;
        } else {
            siguiente.setAnterior(anterior);
        }

        nodo.setAnterior(null);
        nodo.setSiguiente(null);
        tamanio--;
    }

    // Compara los dos elementos con cuidado por si alguno viene en null
    private boolean sonIguales(T primero, T segundo) {
        if (primero == null) {
            return segundo == null;
        }
        return primero.equals(segundo);
    }
}
