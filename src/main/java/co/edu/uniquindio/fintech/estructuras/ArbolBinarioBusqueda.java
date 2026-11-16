package co.edu.uniquindio.fintech.estructuras;

import java.util.Iterator;

/**
 * Arbol binario de busqueda simple, sin balanceo.
 * Cada nodo tiene una clave y una lista de valores porque las claves se pueden repetir.
 *
 * Los O(log n) son en promedio. Si las claves llegan ordenadas el arbol se vuelve
 * practicamente una lista y todo pasa a O(n). Por eso los metodos estan hechos con
 * ciclos y no con recursion, con un arbol degenerado de miles de nodos la
 * recursion se quedaria sin espacio en la pila de Java.
 */
public class ArbolBinarioBusqueda<K extends Comparable<K>, V> implements Iterable<V> {

    private NodoArbol<K, V> raiz;
    private int tamanio;
    private int cantidadClaves;

    public ArbolBinarioBusqueda() {
        this.raiz = null;
        this.tamanio = 0;
        this.cantidadClaves = 0;
    }

    /** Guarda el valor con su clave, si la clave ya existia lo mete en la lista de ese nodo. O(log n) promedio. */
    public void insertar(K clave, V valor) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser nula");
        }

        if (raiz == null) {
            raiz = new NodoArbol<>(clave);
            raiz.getValores().agregarAlFinal(valor);
            cantidadClaves++;
            tamanio++;
            return;
        }

        NodoArbol<K, V> actual = raiz;
        boolean insertado = false;
        while (!insertado) {
            int comparacion = clave.compareTo(actual.getClave());
            if (comparacion == 0) {
                actual.getValores().agregarAlFinal(valor);
                insertado = true;
            } else if (comparacion < 0) {
                if (actual.getIzquierdo() == null) {
                    NodoArbol<K, V> nuevo = new NodoArbol<>(clave);
                    nuevo.getValores().agregarAlFinal(valor);
                    actual.setIzquierdo(nuevo);
                    cantidadClaves++;
                    insertado = true;
                } else {
                    actual = actual.getIzquierdo();
                }
            } else {
                if (actual.getDerecho() == null) {
                    NodoArbol<K, V> nuevo = new NodoArbol<>(clave);
                    nuevo.getValores().agregarAlFinal(valor);
                    actual.setDerecho(nuevo);
                    cantidadClaves++;
                    insertado = true;
                } else {
                    actual = actual.getDerecho();
                }
            }
        }
        tamanio++;
    }

    /**
     * Quita un valor de esa clave. Si la clave se queda sin valores se borra
     * el nodo del arbol. Devuelve true si lo encontro. O(log n) promedio.
     */
    public boolean eliminar(K clave, V valor) {
        if (clave == null) {
            return false;
        }

        // Buscamos el nodo recordando quién es su padre
        NodoArbol<K, V> padre = null;
        NodoArbol<K, V> actual = raiz;
        while (actual != null && clave.compareTo(actual.getClave()) != 0) {
            padre = actual;
            if (clave.compareTo(actual.getClave()) < 0) {
                actual = actual.getIzquierdo();
            } else {
                actual = actual.getDerecho();
            }
        }

        if (actual == null) {
            return false;
        }
        if (!actual.getValores().eliminar(valor)) {
            return false;
        }
        tamanio--;

        if (actual.getValores().estaVacia()) {
            eliminarNodo(actual, padre);
            cantidadClaves--;
        }
        return true;
    }

    /** Devuelve una copia de los valores con esa clave (vacia si no existe). O(log n) promedio. */
    public ListaEnlazada<V> buscar(K clave) {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        NodoArbol<K, V> nodo = buscarNodo(clave);
        if (nodo != null) {
            for (V valor : nodo.getValores()) {
                resultado.agregarAlFinal(valor);
            }
        }
        return resultado;
    }

    /** Dice si existe la clave en el arbol. O(log n) promedio. */
    public boolean contiene(K clave) {
        return buscarNodo(clave) != null;
    }

    /**
     * Devuelve, de menor a mayor, los valores con clave entre minimo y maximo (incluidos).
     * Solo baja por las ramas que pueden tener claves del rango. O(log n + k).
     */
    public ListaEnlazada<V> buscarRango(K minimo, K maximo) {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        Pila<NodoArbol<K, V>> pendientes = new Pila<>();
        NodoArbol<K, V> actual = raiz;
        boolean terminado = false;

        while (!terminado && (actual != null || !pendientes.estaVacia())) {
            // Bajamos a la izquierda, pero si la clave es menor que el mínimo
            // toda su rama izquierda sobra y saltamos a la derecha
            while (actual != null) {
                if (actual.getClave().compareTo(minimo) < 0) {
                    actual = actual.getDerecho();
                } else {
                    pendientes.apilar(actual);
                    actual = actual.getIzquierdo();
                }
            }

            if (!pendientes.estaVacia()) {
                actual = pendientes.desapilar();
                if (actual.getClave().compareTo(maximo) > 0) {
                    // De aqui en adelante todas las claves son mas grandes, no hay que seguir
                    terminado = true;
                } else {
                    for (V valor : actual.getValores()) {
                        resultado.agregarAlFinal(valor);
                    }
                    actual = actual.getDerecho();
                }
            }
        }
        return resultado;
    }

    /** Todos los valores ordenados de menor a mayor clave. O(n). */
    public ListaEnlazada<V> recorridoInorden() {
        return recorrer(false, -1);
    }

    /** Todos los valores ordenados de mayor a menor clave. O(n). */
    public ListaEnlazada<V> recorridoInordenInverso() {
        return recorrer(true, -1);
    }

    /** Los "cantidad" valores con las claves mas grandes (para los top N). Para apenas junta los que se pidieron. */
    public ListaEnlazada<V> obtenerMayores(int cantidad) {
        return recorrer(true, cantidad);
    }

    /** La clave mas pequeña del arbol. O(log n) promedio. */
    public K minimo() {
        if (raiz == null) {
            throw new EstructuraVaciaException("El árbol está vacío");
        }
        NodoArbol<K, V> actual = raiz;
        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }
        return actual.getClave();
    }

    /** La clave mas grande del arbol. O(log n) promedio. */
    public K maximo() {
        if (raiz == null) {
            throw new EstructuraVaciaException("El árbol está vacío");
        }
        NodoArbol<K, V> actual = raiz;
        while (actual.getDerecho() != null) {
            actual = actual.getDerecho();
        }
        return actual.getClave();
    }

    /**
     * Cuantos niveles tiene el arbol (0 si esta vacio, 1 si solo tiene la raiz).
     * Se cuenta nivel por nivel usando una cola. O(n).
     */
    public int altura() {
        if (raiz == null) {
            return 0;
        }
        Cola<NodoArbol<K, V>> cola = new Cola<>();
        cola.encolar(raiz);
        int altura = 0;

        while (!cola.estaVacia()) {
            int nodosDelNivel = cola.tamanio();
            for (int contador = 0; contador < nodosDelNivel; contador++) {
                NodoArbol<K, V> nodo = cola.desencolar();
                if (nodo.getIzquierdo() != null) {
                    cola.encolar(nodo.getIzquierdo());
                }
                if (nodo.getDerecho() != null) {
                    cola.encolar(nodo.getDerecho());
                }
            }
            altura++;
        }
        return altura;
    }

    /** Cantidad total de valores guardados (contando repetidos). O(1). */
    public int tamanio() {
        return tamanio;
    }

    /** Cantidad de claves distintas (nodos del arbol). O(1). */
    public int cantidadClaves() {
        return cantidadClaves;
    }

    /** Dice si el arbol esta vacio. O(1). */
    public boolean estaVacia() {
        return raiz == null;
    }

    /** Recorre los valores en inorden (de menor a mayor clave). */
    @Override
    public Iterator<V> iterator() {
        return recorridoInorden().iterator();
    }

    private NodoArbol<K, V> buscarNodo(K clave) {
        if (clave == null) {
            return null;
        }
        NodoArbol<K, V> actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.getClave());
            if (comparacion == 0) {
                return actual;
            } else if (comparacion < 0) {
                actual = actual.getIzquierdo();
            } else {
                actual = actual.getDerecho();
            }
        }
        return null;
    }

    /*
     * Recorido inorden usando una pila en vez de recursion.
     * Si inverso es true se va primero por la derecha (de mayor a menor).
     * Con limite en -1 recorre todo, si no para cuando ya junto esa cantidad de valores.
     */
    private ListaEnlazada<V> recorrer(boolean inverso, int limite) {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        Pila<NodoArbol<K, V>> pendientes = new Pila<>();
        NodoArbol<K, V> actual = raiz;
        boolean terminado = false;

        while (!terminado && (actual != null || !pendientes.estaVacia())) {
            while (actual != null) {
                pendientes.apilar(actual);
                if (inverso) {
                    actual = actual.getDerecho();
                } else {
                    actual = actual.getIzquierdo();
                }
            }

            actual = pendientes.desapilar();
            for (V valor : actual.getValores()) {
                if (limite >= 0 && resultado.tamanio() >= limite) {
                    terminado = true;
                    break;
                }
                resultado.agregarAlFinal(valor);
            }
            if (limite >= 0 && resultado.tamanio() >= limite) {
                terminado = true;
            }

            if (inverso) {
                actual = actual.getIzquierdo();
            } else {
                actual = actual.getDerecho();
            }
        }
        return resultado;
    }

    // Quita el nodo del arbol dependiendo de cuantos hijos tenga
    private void eliminarNodo(NodoArbol<K, V> nodo, NodoArbol<K, V> padre) {
        if (nodo.getIzquierdo() != null && nodo.getDerecho() != null) {
            // Con dos hijos: buscamos el sucesor (el menor de la rama derecha),
            // copiamos sus datos en este nodo y luego quitamos el sucesor
            NodoArbol<K, V> padreSucesor = nodo;
            NodoArbol<K, V> sucesor = nodo.getDerecho();
            while (sucesor.getIzquierdo() != null) {
                padreSucesor = sucesor;
                sucesor = sucesor.getIzquierdo();
            }
            nodo.setClave(sucesor.getClave());
            nodo.setValores(sucesor.getValores());
            // El sucesor nunca tiene hijo izquierdo, asi que su lugar lo toma el hijo derecho
            reemplazarHijo(padreSucesor, sucesor, sucesor.getDerecho());
        } else {
            // Con un hijo o ninguno, el hijo (o null) se queda con su puesto
            NodoArbol<K, V> hijo;
            if (nodo.getIzquierdo() != null) {
                hijo = nodo.getIzquierdo();
            } else {
                hijo = nodo.getDerecho();
            }
            reemplazarHijo(padre, nodo, hijo);
        }
    }

    private void reemplazarHijo(NodoArbol<K, V> padre, NodoArbol<K, V> viejo, NodoArbol<K, V> nuevo) {
        if (padre == null) {
            raiz = nuevo;
        } else if (padre.getIzquierdo() == viejo) {
            padre.setIzquierdo(nuevo);
        } else {
            padre.setDerecho(nuevo);
        }
    }
}
