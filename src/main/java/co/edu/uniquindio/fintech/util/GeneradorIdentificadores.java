package co.edu.uniquindio.fintech.util;

/**
 * Lleva los consecutivos de cada cosa que necesita un codigo unico.
 * Por ejemplo BIL-0001 para billeteras y TX-000001 para transacciones.
 */
public class GeneradorIdentificadores {

    private int contadorBilleteras;
    private int contadorTransacciones;
    private int contadorProgramadas;
    private int contadorNotificaciones;
    private int contadorEventos;
    private int contadorCanjes;

    public GeneradorIdentificadores() {
        this.contadorBilleteras = 0;
        this.contadorTransacciones = 0;
        this.contadorProgramadas = 0;
        this.contadorNotificaciones = 0;
        this.contadorEventos = 0;
        this.contadorCanjes = 0;
    }

    /** Siguiente codigo de billetera, tipo BIL-0001. */
    public String siguienteBilletera() {
        contadorBilleteras++;
        return String.format("BIL-%04d", contadorBilleteras);
    }

    /** Siguiente id de transaccion, tipo TX-000001. */
    public String siguienteTransaccion() {
        contadorTransacciones++;
        return String.format("TX-%06d", contadorTransacciones);
    }

    /** Siguiente id de operacion programada, tipo PRG-0001. */
    public String siguienteProgramada() {
        contadorProgramadas++;
        return String.format("PRG-%04d", contadorProgramadas);
    }

    /** Siguiente id de notificacion, tipo NOT-000001. */
    public String siguienteNotificacion() {
        contadorNotificaciones++;
        return String.format("NOT-%06d", contadorNotificaciones);
    }

    /** Siguiente id de evento de auditoria, tipo AUD-0001. */
    public String siguienteEvento() {
        contadorEventos++;
        return String.format("AUD-%04d", contadorEventos);
    }

    /** Siguiente id de canje de beneficio, tipo CAN-0001. */
    public String siguienteCanje() {
        contadorCanjes++;
        return String.format("CAN-%04d", contadorCanjes);
    }
}
