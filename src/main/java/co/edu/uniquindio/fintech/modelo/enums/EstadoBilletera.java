package co.edu.uniquindio.fintech.modelo.enums;

/**
 * Estados por los que pasa una billetera.
 */
public enum EstadoBilletera {

    ACTIVA("Activa"),
    BLOQUEADA("Bloqueada"),
    CERRADA("Cerrada");

    private final String texto;

    EstadoBilletera(String texto) {
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
