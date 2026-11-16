package co.edu.uniquindio.fintech.modelo.enums;

/**
 * Tipos de notificacion que le pueden llegar a un usuario.
 */
public enum TipoNotificacion {

    SALDO_BAJO("Saldo bajo"),
    PROGRAMADA_PROXIMA("Operación programada próxima"),
    OPERACION_RECHAZADA("Operación rechazada"),
    ASCENSO_NIVEL("Subiste de nivel"),
    DESCENSO_NIVEL("Bajaste de nivel"),
    CANJE_BENEFICIO("Canje de beneficio"),
    ALERTA_SEGURIDAD("Alerta de seguridad");

    private final String texto;

    TipoNotificacion(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
