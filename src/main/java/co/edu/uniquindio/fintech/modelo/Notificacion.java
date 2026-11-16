package co.edu.uniquindio.fintech.modelo;

import co.edu.uniquindio.fintech.modelo.enums.TipoNotificacion;

import java.time.LocalDateTime;

/**
 * Aviso para un usuario. Primero queda pendiente en la cola y cuando se
 * despacha pasa a la lista de notificaciones del usuario.
 */
public class Notificacion {

    private String id;
    private Usuario usuario;
    private TipoNotificacion tipo;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;
    private boolean despachada;

    public Notificacion(String id, Usuario usuario, TipoNotificacion tipo, String mensaje, LocalDateTime fecha) {
        this.id = id;
        this.usuario = usuario;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.leida = false;
        this.despachada = false;
    }

    /** Marca la notificacion como leida. */
    public void marcarLeida() {
        this.leida = true;
    }

    public String getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public boolean isDespachada() {
        return despachada;
    }

    public void setDespachada(boolean despachada) {
        this.despachada = despachada;
    }
}
