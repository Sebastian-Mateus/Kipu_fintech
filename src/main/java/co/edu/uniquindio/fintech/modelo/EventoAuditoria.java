package co.edu.uniquindio.fintech.modelo;

import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;

import java.time.LocalDateTime;

/**
 * Queda registrado cada vez que una regla de fraude marca una transaccion.
 * Guarda que regla fue, el riesgo y una descripcion de lo que paso.
 */
public class EventoAuditoria {

    private String id;
    private LocalDateTime fecha;
    private Usuario usuario;
    private Transaccion transaccion;
    private String nombreRegla;
    private NivelRiesgo nivelRiesgo;
    private String descripcion;

    public EventoAuditoria(String id, LocalDateTime fecha, Usuario usuario, Transaccion transaccion,
                           String nombreRegla, NivelRiesgo nivelRiesgo, String descripcion) {
        this.id = id;
        this.fecha = fecha;
        this.usuario = usuario;
        this.transaccion = transaccion;
        this.nombreRegla = nombreRegla;
        this.nivelRiesgo = nivelRiesgo;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public String getNombreRegla() {
        return nombreRegla;
    }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
