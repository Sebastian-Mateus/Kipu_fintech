package co.edu.uniquindio.fintech.modelo.enums;

/**
 * Categorias de billetera que puede crear un usuario.
 * El texto es el que se muestra en la interfaz.
 */
public enum TipoBilletera {

    AHORRO("Ahorro"),
    GASTOS_DIARIOS("Gastos diarios"),
    COMPRAS("Compras"),
    TRANSPORTE("Transporte"),
    INVERSION("Inversión"),
    SUSCRIPCIONES("Suscripciones");

    private final String texto;

    TipoBilletera(String texto) {
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
