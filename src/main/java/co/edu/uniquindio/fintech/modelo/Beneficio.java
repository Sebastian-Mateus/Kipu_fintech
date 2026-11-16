package co.edu.uniquindio.fintech.modelo;

import co.edu.uniquindio.fintech.modelo.enums.NivelUsuario;
import co.edu.uniquindio.fintech.modelo.enums.TipoBeneficio;

/**
 * Beneficio del catalogo que se puede canjear con puntos.
 * El campo valor depende del tipo: el porcentaje de descuento en la comision,
 * el multiplicador de puntos o las transacciones extra del dia.
 */
public class Beneficio {

    private String codigo;
    private String nombre;
    private String descripcion;
    private int costoPuntos;
    private TipoBeneficio tipo;
    private double valor;
    private int usosIniciales;
    private NivelUsuario nivelMinimo;
    private boolean activo;

    public Beneficio(String codigo, String nombre, String descripcion, int costoPuntos, TipoBeneficio tipo,
                     double valor, int usosIniciales, NivelUsuario nivelMinimo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoPuntos = costoPuntos;
        this.tipo = tipo;
        this.valor = valor;
        this.usosIniciales = usosIniciales;
        this.nivelMinimo = nivelMinimo;
        this.activo = true;
    }

    /** Dice si un usuario con ese nivel puede canjear este beneficio (sin mirar los puntos). */
    public boolean estaDisponiblePara(NivelUsuario nivel) {
        return activo && nivel.esIgualOMayorQue(nivelMinimo);
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCostoPuntos() {
        return costoPuntos;
    }

    public void setCostoPuntos(int costoPuntos) {
        this.costoPuntos = costoPuntos;
    }

    public TipoBeneficio getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public int getUsosIniciales() {
        return usosIniciales;
    }

    public NivelUsuario getNivelMinimo() {
        return nivelMinimo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
