package co.edu.uniquindio.fintech.estructuras;

/**
 * Nodo de una cubeta de la tabla hash. Guarda la pareja clave-valor
 * y apunta al siguiente nodo de la misma cubeta.
 */
public class NodoHash<K, V> {

    private K clave;
    private V valor;
    private NodoHash<K, V> siguiente;

    public NodoHash(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
        this.siguiente = null;
    }

    public K getClave() {
        return clave;
    }

    public V getValor() {
        return valor;
    }

    public void setValor(V valor) {
        this.valor = valor;
    }

    public NodoHash<K, V> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoHash<K, V> siguiente) {
        this.siguiente = siguiente;
    }
}
