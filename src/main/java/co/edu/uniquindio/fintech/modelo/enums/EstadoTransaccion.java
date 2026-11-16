package co.edu.uniquindio.fintech.modelo.enums;

/**
 * Estados de una transaccion.
 */
public enum EstadoTransaccion {

    PENDIENTE("Pendiente"),
    EXITOSA("Exitosa"),
    RECHAZADA("Rechazada"),
    REVERTIDA("Revertida");

    private final String texto;

    EstadoTransaccion(String texto) {
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
