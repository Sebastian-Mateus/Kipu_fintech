package co.edu.uniquindio.fintech.servicio;

/**
 * Pareja de algo (una billetera, un usuario, un tipo...) con un contador.
 * La usa la analitica para armar los rankings.
 */
public class ElementoRanking<T> {

    private T elemento;
    private int cantidad;

    public ElementoRanking(T elemento, int cantidad) {
        this.elemento = elemento;
        this.cantidad = cantidad;
    }

    public T getElemento() {
        return elemento;
    }

    public int getCantidad() {
        return cantidad;
    }

    @Override
    public String toString() {
        return elemento + ": " + cantidad;
    }
}
