package co.edu.uniquindio.fintech.modelo;

import co.edu.uniquindio.fintech.util.Validador;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Resumen de como se mueve un usuario: cuantas transacciones hace por hora del
 * dia, cuanto mueve en promedio y desde cuando. Con esto las reglas de fraude
 * se pueden revisar en O(1) sin recorrer todo el historial.
 */
public class PerfilComportamiento {

    private int[] transaccionesPorHora;
    private int cantidadTransacciones;
    private double sumaMontos;
    private LocalDateTime primeraTransaccion;
    private LocalDateTime ultimaTransaccion;

    public PerfilComportamiento() {
        this.transaccionesPorHora = new int[24];
        this.cantidadTransacciones = 0;
        this.sumaMontos = 0;
        this.primeraTransaccion = null;
        this.ultimaTransaccion = null;
    }

    /** Suma una transaccion nueva al perfil. */
    public void registrar(double monto, LocalDateTime fecha) {
        transaccionesPorHora[fecha.getHour()]++;
        cantidadTransacciones++;
        sumaMontos = Validador.redondear(sumaMontos + monto);
        if (primeraTransaccion == null) {
            primeraTransaccion = fecha;
        }
        ultimaTransaccion = fecha;
    }

    /** Promedio de lo que mueve el usuario por transaccion. */
    public double promedioMonto() {
        if (cantidadTransacciones == 0) {
            return 0;
        }
        return Validador.redondear(sumaMontos / cantidadTransacciones);
    }

    /** Que porcentaje de sus transacciones las hizo en esa hora del dia (0 a 23). */
    public double porcentajeEnHora(int hora) {
        if (cantidadTransacciones == 0) {
            return 0;
        }
        return transaccionesPorHora[hora] * 100.0 / cantidadTransacciones;
    }

    /** Cuantas transacciones hace en promedio por dia, contando desde la primera hasta la fecha dada. */
    public double promedioDiario(LocalDateTime ahora) {
        if (cantidadTransacciones == 0) {
            return 0;
        }
        // Se suma 1 para que el primer dia tambien cuente y no se divida entre cero
        long dias = ChronoUnit.DAYS.between(primeraTransaccion.toLocalDate(), ahora.toLocalDate()) + 1;
        return (double) cantidadTransacciones / dias;
    }

    public int getTransaccionesEnHora(int hora) {
        return transaccionesPorHora[hora];
    }

    public int getCantidadTransacciones() {
        return cantidadTransacciones;
    }

    public double getSumaMontos() {
        return sumaMontos;
    }

    public LocalDateTime getPrimeraTransaccion() {
        return primeraTransaccion;
    }

    public LocalDateTime getUltimaTransaccion() {
        return ultimaTransaccion;
    }
}
