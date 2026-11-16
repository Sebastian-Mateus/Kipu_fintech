package co.edu.uniquindio.fintech.modelo.enums;

/**
 * Nivel de riesgo que le asigna la deteccion de fraude a una transaccion.
 * Estan en orden de menor a mayor, eso se aprovecha para comparar.
 */
public enum NivelRiesgo {

    NINGUNO("Ninguno"),
    BAJO("Bajo"),
    MEDIO("Medio"),
    ALTO("Alto");

    private final String texto;

    NivelRiesgo(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    /** Dice si este riesgo es mas grave que el otro. */
    public boolean esMayorQue(NivelRiesgo otro) {
        return this.ordinal() > otro.ordinal();
    }

    @Override
    public String toString() {
        return texto;
    }
}
