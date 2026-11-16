package co.edu.uniquindio.fintech.estructuras;

/**
 * Nodo de la pila. Cada uno apunta al que le quedo debajo.
 */
public class NodoPila<T> {

    private T dato;
    private NodoPila<T> debajo;

    public NodoPila(T dato) {
        this.dato = dato;
        this.debajo = null;
    }

    public T getDato() {
        return dato;
    }

    public NodoPila<T> getDebajo() {
        return debajo;
    }

    public void setDebajo(NodoPila<T> debajo) {
        this.debajo = debajo;
    }
}
