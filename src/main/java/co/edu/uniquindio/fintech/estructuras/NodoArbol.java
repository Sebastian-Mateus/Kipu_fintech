package co.edu.uniquindio.fintech.estructuras;

/**
 * Nodo del arbol binario de busqueda. Guarda una clave y una lista con
 * todos los valores que tienen esa misma clave (por ejemplo varios
 * usuarios con los mismos puntos).
 */
public class NodoArbol<K, V> {

    private K clave;
    private ListaEnlazada<V> valores;
    private NodoArbol<K, V> izquierdo;
    private NodoArbol<K, V> derecho;

    public NodoArbol(K clave) {
        this.clave = clave;
        this.valores = new ListaEnlazada<>();
        this.izquierdo = null;
        this.derecho = null;
    }

    public K getClave() {
        return clave;
    }

    public void setClave(K clave) {
        this.clave = clave;
    }

    public ListaEnlazada<V> getValores() {
        return valores;
    }

    public void setValores(ListaEnlazada<V> valores) {
        this.valores = valores;
    }

    public NodoArbol<K, V> getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoArbol<K, V> izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoArbol<K, V> getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoArbol<K, V> derecho) {
        this.derecho = derecho;
    }
}
