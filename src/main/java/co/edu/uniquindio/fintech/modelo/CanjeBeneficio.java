package co.edu.uniquindio.fintech.modelo;

import java.time.LocalDateTime;

/**
 * Registro de un beneficio que el usuario ya canjeo, con los usos que le quedan.
 */
public class CanjeBeneficio {

    private String id;
    private Beneficio beneficio;
    private LocalDateTime fecha;
    private int puntosUsados;
    private int usosRestantes;

    public CanjeBeneficio(String id, Beneficio beneficio, LocalDateTime fecha, int puntosUsados) {
        this.id = id;
        this.beneficio = beneficio;
        this.fecha = fecha;
        this.puntosUsados = puntosUsados;
        this.usosRestantes = beneficio.getUsosIniciales();
    }

    /** Dice si al canje todavia le quedan usos. */
    public boolean tieneUsos() {
        return usosRestantes > 0;
    }

    /** Gasta un uso del beneficio. */
    public void usar() {
        if (usosRestantes > 0) {
            usosRestantes--;
        }
    }

    public String getId() {
        return id;
    }

    public Beneficio getBeneficio() {
        return beneficio;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public int getPuntosUsados() {
        return puntosUsados;
    }

    public int getUsosRestantes() {
        return usosRestantes;
    }

    public void setUsosRestantes(int usosRestantes) {
        this.usosRestantes = usosRestantes;
    }
}
