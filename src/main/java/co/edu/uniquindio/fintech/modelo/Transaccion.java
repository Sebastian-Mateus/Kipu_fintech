package co.edu.uniquindio.fintech.modelo;

import co.edu.uniquindio.fintech.modelo.enums.EstadoTransaccion;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.util.Validador;

import java.time.LocalDateTime;

/**
 * Un movimiento de plata. En las recargas no hay billetera de origen y en los
 * retiros no hay billetera de destino, por eso esos campos pueden venir en null.
 * El idTransaccionOriginal solo se llena en las reversiones.
 */
public class Transaccion {

    private String id;
    private LocalDateTime fecha;
    private TipoTransaccion tipo;
    private double valor;
    private double comision;
    private Usuario usuario;
    private Billetera billeteraOrigen;
    private Billetera billeteraDestino;
    private EstadoTransaccion estado;
    private int puntosGenerados;
    private NivelRiesgo nivelRiesgo;
    private String descripcion;
    private String idTransaccionOriginal;

    public Transaccion(String id, LocalDateTime fecha, TipoTransaccion tipo, double valor, double comision,
                       Usuario usuario, Billetera billeteraOrigen, Billetera billeteraDestino, String descripcion) {
        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.valor = Validador.redondear(valor);
        this.comision = Validador.redondear(comision);
        this.usuario = usuario;
        this.billeteraOrigen = billeteraOrigen;
        this.billeteraDestino = billeteraDestino;
        this.estado = EstadoTransaccion.PENDIENTE;
        this.puntosGenerados = 0;
        this.nivelRiesgo = NivelRiesgo.NINGUNO;
        this.descripcion = descripcion;
        this.idTransaccionOriginal = null;
    }

    /** Valor mas comision, que es lo que realmente sale de la billetera de origen. */
    public double getValorTotal() {
        return Validador.redondear(valor + comision);
    }

    /** Dice si la transaccion quedo exitosa. */
    public boolean esExitosa() {
        return estado == EstadoTransaccion.EXITOSA;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = Validador.redondear(comision);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Billetera getBilleteraOrigen() {
        return billeteraOrigen;
    }

    public Billetera getBilleteraDestino() {
        return billeteraDestino;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTransaccion estado) {
        this.estado = estado;
    }

    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    public void setPuntosGenerados(int puntosGenerados) {
        this.puntosGenerados = puntosGenerados;
    }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIdTransaccionOriginal() {
        return idTransaccionOriginal;
    }

    public void setIdTransaccionOriginal(String idTransaccionOriginal) {
        this.idTransaccionOriginal = idTransaccionOriginal;
    }

    // Dos transacciones son la misma si tienen el mismo id
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Transaccion)) {
            return false;
        }
        Transaccion otraTransaccion = (Transaccion) otro;
        return id.equals(otraTransaccion.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id + " - " + tipo + " - " + Validador.formatearPesos(valor);
    }
}
