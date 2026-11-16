package co.edu.uniquindio.fintech.estructuras;

/**
 * Arista del grafo de transferencias. Va de un origen a un destino y lleva la
 * cuenta de cuantas transferencias hubo entre los dos y cuanta plata se movio.
 * Guardamos también el origen para poder mostrar "quién le transfiere a quién".
 */
public class Arista<T> {

    private T origen;
    private T destino;
    private int cantidadTransferencias;
    private double montoTotal;

    public Arista(T origen, T destino) {
        this.origen = origen;
        this.destino = destino;
        this.cantidadTransferencias = 0;
        this.montoTotal = 0;
    }

    /** Suma una transferencia mas con su monto. */
    public void sumarTransferencia(double monto) {
        cantidadTransferencias++;
        montoTotal = montoTotal + monto;
    }

    /** Descuenta una transferencia, esto pasa cuando se revierte. */
    public void restarTransferencia(double monto) {
        cantidadTransferencias--;
        montoTotal = montoTotal - monto;
    }

    public T getOrigen() {
        return origen;
    }

    public T getDestino() {
        return destino;
    }

    public int getCantidadTransferencias() {
        return cantidadTransferencias;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }
}
