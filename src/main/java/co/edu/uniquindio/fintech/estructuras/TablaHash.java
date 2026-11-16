package co.edu.uniquindio.fintech.estructuras;

import java.util.Iterator;

/**
 * Tabla hash con encadenamiento separado: un arreglo de cubetas donde cada
 * cubeta es una cadena de nodos. Cuando se pasa del 75 % de ocupacion
 * duplicamos el arreglo y volvemos a ubicar todo (el rehash).
 */
public class TablaHash<K, V> implements Iterable<K> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA_MAXIMO = 0.75;

    private NodoHash<K, V>[] cubetas;
    private int tamanio;

    public TablaHash() {
        this.cubetas = crearCubetas(CAPACIDAD_INICIAL);
        this.tamanio = 0;
    }

    /** Guarda el valor con su clave. Si la clave ya estaba le cambia el valor. O(1) promedio. */
    public void insertar(K clave, V valor) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser nula");
        }

        int posicion = calcularPosicion(clave, cubetas.length);
        NodoHash<K, V> actual = cubetas[posicion];
        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                actual.setValor(valor);
                return;
            }
            actual = actual.getSiguiente();
        }

        // No estaba, entonces lo ponemos de primero en la cubtea
        NodoHash<K, V> nuevo = new NodoHash<>(clave, valor);
        nuevo.setSiguiente(cubetas[posicion]);
        cubetas[posicion] = nuevo;
        tamanio++;

        if ((double) tamanio / cubetas.length > FACTOR_CARGA_MAXIMO) {
            rehash();
        }
    }

    /** Devuelve el valor de la clave, o null si no existe. O(1) promedio. */
    public V obtener(K clave) {
        NodoHash<K, V> nodo = buscarNodo(clave);
        if (nodo == null) {
            return null;
        }
        return nodo.getValor();
    }

    /** Quita la clave y devuelve su valor, o null si no existia. O(1) promedio. */
    public V eliminar(K clave) {
        if (clave == null) {
            return null;
        }

        int posicion = calcularPosicion(clave, cubetas.length);
        NodoHash<K, V> anterior = null;
        NodoHash<K, V> actual = cubetas[posicion];
        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                if (anterior == null) {
                    cubetas[posicion] = actual.getSiguiente();
                } else {
                    anterior.setSiguiente(actual.getSiguiente());
                }
                tamanio--;
                return actual.getValor();
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        return null;
    }

    /** Dice si la clave existe en la tabla. O(1) promedio. */
    public boolean contieneClave(K clave) {
        return buscarNodo(clave) != null;
    }

    /** Lista con todas las claves (sin un orden en particular). O(n). */
    public ListaEnlazada<K> claves() {
        ListaEnlazada<K> resultado = new ListaEnlazada<>();
        for (int indice = 0; indice < cubetas.length; indice++) {
            NodoHash<K, V> actual = cubetas[indice];
            while (actual != null) {
                resultado.agregarAlFinal(actual.getClave());
                actual = actual.getSiguiente();
            }
        }
        return resultado;
    }

    /** Lista con todos los valores (sin un orden en particular). O(n). */
    public ListaEnlazada<V> valores() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        for (int indice = 0; indice < cubetas.length; indice++) {
            NodoHash<K, V> actual = cubetas[indice];
            while (actual != null) {
                resultado.agregarAlFinal(actual.getValor());
                actual = actual.getSiguiente();
            }
        }
        return resultado;
    }

    /** Cantidad de parejas guardadas. O(1). */
    public int tamanio() {
        return tamanio;
    }

    /** Dice si la tabla no tiene nada. O(1). */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /** Recorre las claves de la tabla. */
    @Override
    public Iterator<K> iterator() {
        return claves().iterator();
    }

    private NodoHash<K, V> buscarNodo(K clave) {
        if (clave == null) {
            return null;
        }
        NodoHash<K, V> actual = cubetas[calcularPosicion(clave, cubetas.length)];
        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                return actual;
            }
            actual = actual.getSiguiente();
        }
        return null;
    }

    // Quitamos el signo del hashCode para que la posición nunca sea negativa
    private int calcularPosicion(K clave, int capacidad) {
        return (clave.hashCode() & 0x7fffffff) % capacidad;
    }

    // Duplica el arreglo y vuelve a ubicar cada nodo en su nueva cubeta
    private void rehash() {
        NodoHash<K, V>[] viejas = cubetas;
        cubetas = crearCubetas(viejas.length * 2);

        for (int indice = 0; indice < viejas.length; indice++) {
            NodoHash<K, V> actual = viejas[indice];
            while (actual != null) {
                NodoHash<K, V> siguiente = actual.getSiguiente();
                int posicion = calcularPosicion(actual.getClave(), cubetas.length);
                actual.setSiguiente(cubetas[posicion]);
                cubetas[posicion] = actual;
                actual = siguiente;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private NodoHash<K, V>[] crearCubetas(int capacidad) {
        return (NodoHash<K, V>[]) new NodoHash[capacidad];
    }
}
