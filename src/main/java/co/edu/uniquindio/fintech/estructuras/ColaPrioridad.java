package co.edu.uniquindio.fintech.estructuras;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Cola de prioridad hecha con un monticulo binario metido en un arreglo.
 * El comparador decide quien va primero: si compare(a, b) da negativo, a sale antes que b.
 * La usamos para las operaciones programadas y para sacar los top N de los rankings.
 */
public class ColaPrioridad<T> implements Iterable<T> {

    private static final int CAPACIDAD_INICIAL = 16;

    private T[] elementos;
    private int tamanio;
    private Comparator<T> comparador;

    @SuppressWarnings("unchecked")
    public ColaPrioridad(Comparator<T> comparador) {
        this.elementos = (T[]) new Object[CAPACIDAD_INICIAL];
        this.tamanio = 0;
        this.comparador = comparador;
    }

    /** Agrega un elemento y lo sube hasta su lugar. O(log n). */
    public void insertar(T elemento) {
        if (tamanio == elementos.length) {
            crecer();
        }
        elementos[tamanio] = elemento;
        subir(tamanio);
        tamanio++;
    }

    /** Quita y devuelve el elemento de mayor prioridad. O(log n). */
    public T extraer() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola de prioridad está vacía");
        }
        T primero = elementos[0];
        // El ultimo pasa a la raiz y se va hundiendo hasta donde le toca
        tamanio--;
        elementos[0] = elementos[tamanio];
        elementos[tamanio] = null;
        if (tamanio > 0) {
            bajar(0);
        }
        return primero;
    }

    /** Devuelve el elemento de mayor prioridad sin quitarlo. O(1). */
    public T verPrimero() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola de prioridad está vacía");
        }
        return elementos[0];
    }

    /**
     * Quita un elemento cualquiera, por ejemplo una programada que se cancelo.
     * Toca buscarlo en el arrelgo, asi que es O(n).
     */
    public boolean eliminar(T elemento) {
        int posicion = -1;
        for (int indice = 0; indice < tamanio && posicion == -1; indice++) {
            if (elementos[indice].equals(elemento)) {
                posicion = indice;
            }
        }
        if (posicion == -1) {
            return false;
        }

        tamanio--;
        elementos[posicion] = elementos[tamanio];
        elementos[tamanio] = null;
        if (posicion < tamanio) {
            // El que pusimos en ese hueco puede necesitar subir o bajar
            subir(posicion);
            bajar(posicion);
        }
        return true;
    }

    /** Cantidad de elementos guardados. O(1). */
    public int tamanio() {
        return tamanio;
    }

    /** Dice si no hay elementos. O(1). */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /** Recorre el arreglo tal cual esta guardado, no en orden de prioridad. O(n). */
    @Override
    public Iterator<T> iterator() {
        return new IteradorColaPrioridad<>(elementos, tamanio);
    }

    // Sube el elemento mientras tenga mas prioridad que su padre
    private void subir(int indice) {
        int actual = indice;
        while (actual > 0) {
            int padre = (actual - 1) / 2;
            if (comparador.compare(elementos[actual], elementos[padre]) < 0) {
                intercambiar(actual, padre);
                actual = padre;
            } else {
                return;
            }
        }
    }

    // Baja el elemento mientras alguno de los hijos tenga mas prioridad que el
    private void bajar(int indice) {
        int actual = indice;
        boolean enSuLugar = false;
        while (!enSuLugar) {
            int izquierdo = 2 * actual + 1;
            int derecho = 2 * actual + 2;
            int mejor = actual;

            if (izquierdo < tamanio && comparador.compare(elementos[izquierdo], elementos[mejor]) < 0) {
                mejor = izquierdo;
            }
            if (derecho < tamanio && comparador.compare(elementos[derecho], elementos[mejor]) < 0) {
                mejor = derecho;
            }

            if (mejor == actual) {
                enSuLugar = true;
            } else {
                intercambiar(actual, mejor);
                actual = mejor;
            }
        }
    }

    private void intercambiar(int primero, int segundo) {
        T auxiliar = elementos[primero];
        elementos[primero] = elementos[segundo];
        elementos[segundo] = auxiliar;
    }

    // Cuando el arreglo se llena, pasamos todo a uno del doble de tamaño
    @SuppressWarnings("unchecked")
    private void crecer() {
        T[] nuevo = (T[]) new Object[elementos.length * 2];
        for (int indice = 0; indice < tamanio; indice++) {
            nuevo[indice] = elementos[indice];
        }
        elementos = nuevo;
    }
}
