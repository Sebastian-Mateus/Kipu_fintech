package co.edu.uniquindio.fintech.modelo.enums;

/**
 * Tipos de movimiento que maneja la plataforma.
 */
public enum TipoTransaccion {

    RECARGA("Recarga"),
    RETIRO("Retiro"),
    TRANSFERENCIA_INTERNA("Transferencia interna"),
    TRANSFERENCIA_EXTERNA("Transferencia externa"),
    PAGO_PROGRAMADO("Pago programado"),
    REVERSION("Reversión");

    private final String texto;

    TipoTransaccion(String texto) {
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
