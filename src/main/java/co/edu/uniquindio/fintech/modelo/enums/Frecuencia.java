package co.edu.uniquindio.fintech.modelo.enums;

import java.time.LocalDateTime;

/**
 * Cada cuanto se repite una operacion programada.
 */
public enum Frecuencia {

    UNA_VEZ("Una vez"),
    DIARIA("Diaria"),
    SEMANAL("Semanal"),
    MENSUAL("Mensual");

    private final String texto;

    Frecuencia(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    /** Dice si la operacion se vuelve a programar despues de ejecutarse. */
    public boolean esRecurrente() {
        return this != UNA_VEZ;
    }

    /** Calcula la fecha de la siguiente ejecucion. Para UNA_VEZ devuelve null. */
    public LocalDateTime siguienteFecha(LocalDateTime fecha) {
        switch (this) {
            case DIARIA:
                return fecha.plusDays(1);
            case SEMANAL:
                return fecha.plusWeeks(1);
            case MENSUAL:
                return fecha.plusMonths(1);
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return texto;
    }
}
