package co.edu.uniquindio.fintech.modelo;

import co.edu.uniquindio.fintech.modelo.enums.Frecuencia;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.util.Validador;

import java.time.LocalDateTime;

/**
 * Operacion que el usuario deja programada para una fecha futura,
 * puede ser de una sola vez o repetirse cada dia, semana o mes.
 */
public class OperacionProgramada {

    private String id;
    private Usuario usuario;
    private TipoTransaccion tipo;
    private double valor;
    private Billetera billeteraOrigen;
    private Billetera billeteraDestino;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaProximaEjecucion;
    private Frecuencia frecuencia;
    private boolean activa;
    private boolean alertaEnviada;
    private int vecesEjecutada;
    private int intentosFallidos;

    public OperacionProgramada(String id, Usuario usuario, TipoTransaccion tipo, double valor,
                               Billetera billeteraOrigen, Billetera billeteraDestino, String descripcion,
                               LocalDateTime fechaCreacion, LocalDateTime fechaProximaEjecucion,
                               Frecuencia frecuencia) {
        this.id = id;
        this.usuario = usuario;
        this.tipo = tipo;
        this.valor = Validador.redondear(valor);
        this.billeteraOrigen = billeteraOrigen;
        this.billeteraDestino = billeteraDestino;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.fechaProximaEjecucion = fechaProximaEjecucion;
        this.frecuencia = frecuencia;
        this.activa = true;
        this.alertaEnviada = false;
        this.vecesEjecutada = 0;
        this.intentosFallidos = 0;
    }

    /** Anota que se ejecuto bien. Los fallos se vuelven a contar desde cero. */
    public void registrarEjecucion() {
        vecesEjecutada++;
        intentosFallidos = 0;
    }

    /** Anota un intento fallido. */
    public void registrarFallo() {
        intentosFallidos++;
    }

    public String getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public Billetera getBilleteraOrigen() {
        return billeteraOrigen;
    }

    public Billetera getBilleteraDestino() {
        return billeteraDestino;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaProximaEjecucion() {
        return fechaProximaEjecucion;
    }

    public void setFechaProximaEjecucion(LocalDateTime fechaProximaEjecucion) {
        this.fechaProximaEjecucion = fechaProximaEjecucion;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public boolean isAlertaEnviada() {
        return alertaEnviada;
    }

    public void setAlertaEnviada(boolean alertaEnviada) {
        this.alertaEnviada = alertaEnviada;
    }

    public int getVecesEjecutada() {
        return vecesEjecutada;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    // Dos programadas son la misma si tienen el mismo id
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof OperacionProgramada)) {
            return false;
        }
        OperacionProgramada otraOperacion = (OperacionProgramada) otro;
        return id.equals(otraOperacion.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
