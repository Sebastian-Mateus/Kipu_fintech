package co.edu.uniquindio.fintech.modelo.enums;

/**
 * Clases de beneficio que se pueden canjear con puntos.
 */
public enum TipoBeneficio {

    DESCUENTO_COMISION("Descuento en comisión"),
    PUNTOS_DOBLES("Puntos dobles"),
    AUMENTO_LIMITE("Aumento de límite");

    private final String texto;

    TipoBeneficio(String texto) {
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
