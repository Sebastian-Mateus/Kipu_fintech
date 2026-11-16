package co.edu.uniquindio.fintech.modelo;

import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.excepciones.OperacionInvalidaException;
import co.edu.uniquindio.fintech.excepciones.SaldoInsuficienteException;
import co.edu.uniquindio.fintech.modelo.enums.EstadoBilletera;
import co.edu.uniquindio.fintech.modelo.enums.TipoBilletera;
import co.edu.uniquindio.fintech.util.Validador;

import java.time.LocalDateTime;

/**
 * Billetera digital de un usuario. Cada una tiene su saldo, su estado
 * y su propio historial de movimientos.
 */
public class Billetera {

    private String codigo;
    private String nombre;
    private TipoBilletera tipo;
    private double saldo;
    private double umbralSaldoBajo;
    private EstadoBilletera estado;
    private Usuario propietario;
    private LocalDateTime fechaCreacion;
    private ListaEnlazada<Transaccion> historial;

    public Billetera(String codigo, String nombre, TipoBilletera tipo, double umbralSaldoBajo,
                     Usuario propietario, LocalDateTime fechaCreacion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.saldo = 0;
        this.umbralSaldoBajo = Validador.redondear(umbralSaldoBajo);
        this.estado = EstadoBilletera.ACTIVA;
        this.propietario = propietario;
        this.fechaCreacion = fechaCreacion;
        this.historial = new ListaEnlazada<>();
    }

    /** Mete plata a la billetera. Tiene que estar activa y el monto ser mayor que cero. */
    public void depositar(double monto) {
        validarMovimiento(monto);
        saldo = Validador.redondear(saldo + monto);
    }

    /** Saca plata de la billetera. Ademas de lo de depositar, revisa que el saldo alcance. */
    public void retirar(double monto) {
        validarMovimiento(monto);
        if (Validador.esMenor(saldo, monto)) {
            throw new SaldoInsuficienteException("Saldo insuficiente en la billetera " + codigo);
        }
        saldo = Validador.redondear(saldo - monto);
    }

    /** Dice si la billetera esta activa. */
    public boolean estaActiva() {
        return estado == EstadoBilletera.ACTIVA;
    }

    /** Dice si el saldo quedo por debajo del umbral que puso el usuario. */
    public boolean tieneSaldoBajo() {
        return Validador.esMenor(saldo, umbralSaldoBajo);
    }

    /** Agrega una transaccion al historial de esta billetera. */
    public void registrarTransaccion(Transaccion transaccion) {
        historial.agregarAlFinal(transaccion);
    }

    private void validarMovimiento(double monto) {
        if (!Validador.esPositivo(monto)) {
            throw new OperacionInvalidaException("El monto debe ser mayor que cero");
        }
        if (!estaActiva()) {
            throw new OperacionInvalidaException("La billetera " + codigo + " no está activa");
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoBilletera getTipo() {
        return tipo;
    }

    public double getSaldo() {
        return saldo;
    }

    public double getUmbralSaldoBajo() {
        return umbralSaldoBajo;
    }

    public void setUmbralSaldoBajo(double umbralSaldoBajo) {
        this.umbralSaldoBajo = Validador.redondear(umbralSaldoBajo);
    }

    public EstadoBilletera getEstado() {
        return estado;
    }

    public void setEstado(EstadoBilletera estado) {
        this.estado = estado;
    }

    public Usuario getPropietario() {
        return propietario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public ListaEnlazada<Transaccion> getHistorial() {
        return historial;
    }

    // Dos billeteras son la misma si tienen el mismo codigo
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Billetera)) {
            return false;
        }
        Billetera otraBilletera = (Billetera) otro;
        return codigo.equals(otraBilletera.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}
