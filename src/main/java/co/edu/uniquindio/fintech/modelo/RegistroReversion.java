package co.edu.uniquindio.fintech.modelo;

import java.time.LocalDateTime;

/**
 * Lo que se guarda en la pila de reversiones: la transaccion, los puntos que
 * dio (para quitarlos si se revierte) y hasta cuando se puede revertir.
 */
public class RegistroReversion {

    // Una transaccion se puede revertir hasta 24 horas despues
    private static final int HORAS_PARA_REVERTIR = 24;

    private Transaccion transaccion;
    private int puntosOtorgados;
    private LocalDateTime fechaLimite;

    public RegistroReversion(Transaccion transaccion, int puntosOtorgados) {
        this.transaccion = transaccion;
        this.puntosOtorgados = puntosOtorgados;
        this.fechaLimite = transaccion.getFecha().plusHours(HORAS_PARA_REVERTIR);
    }

    /** Dice si todavia esta dentro de las 24 horas para revertir. */
    public boolean estaVigente(LocalDateTime ahora) {
        return !ahora.isAfter(fechaLimite);
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public int getPuntosOtorgados() {
        return puntosOtorgados;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }
}
