package co.edu.uniquindio.fintech.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Guarda la fecha y hora simualda de la plataforma. Todo el sistema le pide
 * la hora a esta clase y no a LocalDateTime.now(), asi el administrador puede
 * adelantar el tiempo para probar las operaciones programadas.
 */
public class RelojSistema {

    private LocalDateTime fechaActual;

    /** Arranca el reloj en la fecha que se le pase. */
    public RelojSistema(LocalDateTime fechaInicial) {
        this.fechaActual = fechaInicial.withSecond(0).withNano(0);
    }

    /** Arranca el reloj en la hora real del computador (es el unico lugar donde se usa now). */
    public RelojSistema() {
        this(LocalDateTime.now());
    }

    /** Fecha y hora actual de la simulacion. */
    public LocalDateTime ahora() {
        return fechaActual;
    }

    /** Solo el dia actual de la simulacion. */
    public LocalDate hoy() {
        return fechaActual.toLocalDate();
    }

    /** Adelanta el reloj unas horas. */
    public void avanzarHoras(int horas) {
        validarCantidad(horas);
        fechaActual = fechaActual.plusHours(horas);
    }

    /** Adelanta el reloj unos dias. */
    public void avanzarDias(int dias) {
        validarCantidad(dias);
        fechaActual = fechaActual.plusDays(dias);
    }

    /** Adelanta el reloj unas semanas. */
    public void avanzarSemanas(int semanas) {
        validarCantidad(semanas);
        fechaActual = fechaActual.plusWeeks(semanas);
    }

    /** Pone el reloj en una fecha exacta, lo usa el cargador de datos de ejemplo. */
    public void establecer(LocalDateTime fecha) {
        this.fechaActual = fecha.withSecond(0).withNano(0);
    }

    /** Dice si la fecha cae en el mismo dia que la fecha simulada. */
    public boolean esHoy(LocalDateTime fecha) {
        return fecha != null && fecha.toLocalDate().equals(hoy());
    }

    // El reloj solo va hacia adelante, nunca para atras
    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("Solo se puede adelantar el reloj");
        }
    }
}
