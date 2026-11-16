package co.edu.uniquindio.fintech.modelo;

import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.estructuras.Pila;
import co.edu.uniquindio.fintech.modelo.enums.NivelUsuario;
import co.edu.uniquindio.fintech.util.Validador;

import java.time.LocalDateTime;

/**
 * Cliente de la plataforma. Tiene sus billeteras, su historial, los puntos,
 * el nivel y la pila de operaciones que todavia puede revertir.
 *
 * Los puntos acumualdos son historicos y son los que definen el nivel.
 * Los disponibles son los que se gastan al canjear beneficios.
 */
public class Usuario extends Persona {

    private String telefono;
    private LocalDateTime fechaRegistro;
    private boolean activo;
    private int puntosDisponibles;
    private int puntosAcumulados;
    private NivelUsuario nivel;
    private int transaccionesHoy;
    private LocalDateTime fechaUltimoMovimiento;
    private PerfilComportamiento perfil;
    private ListaEnlazada<Billetera> billeteras;
    private ListaEnlazada<Transaccion> historial;
    private Pila<RegistroReversion> pilaReversiones;
    private ListaEnlazada<Notificacion> notificaciones;
    private ListaEnlazada<CanjeBeneficio> canjes;

    public Usuario(String id, String nombre, String correo, String contrasena, String telefono,
                   LocalDateTime fechaRegistro) {
        super(id, nombre, correo, contrasena);
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
        this.activo = true;
        this.puntosDisponibles = 0;
        this.puntosAcumulados = 0;
        this.nivel = NivelUsuario.BRONCE;
        this.transaccionesHoy = 0;
        this.fechaUltimoMovimiento = null;
        this.perfil = new PerfilComportamiento();
        this.billeteras = new ListaEnlazada<>();
        this.historial = new ListaEnlazada<>();
        this.pilaReversiones = new Pila<>();
        this.notificaciones = new ListaEnlazada<>();
        this.canjes = new ListaEnlazada<>();
    }

    @Override
    public String getRol() {
        return "Cliente";
    }

    /** Agrega una billetera a la lista del usuario. */
    public void agregarBilletera(Billetera billetera) {
        billeteras.agregarAlFinal(billetera);
    }

    /** Suma la plata de todas sus billeteras. */
    public double saldoTotal() {
        double total = 0;
        for (Billetera billetera : billeteras) {
            total = total + billetera.getSaldo();
        }
        return Validador.redondear(total);
    }

    /** Suma puntos a los acumulados y a los disponibles. */
    public void sumarPuntos(int puntos) {
        puntosAcumulados = puntosAcumulados + puntos;
        puntosDisponibles = puntosDisponibles + puntos;
    }

    /** Resta puntos de los dos contadores (cuando se revierte algo). Ninguno baja de cero. */
    public void restarPuntos(int puntos) {
        puntosAcumulados = Math.max(0, puntosAcumulados - puntos);
        puntosDisponibles = Math.max(0, puntosDisponibles - puntos);
    }

    /** Gasta puntos disponibles en un canje. Los acumulados no se tocan, asi no baja de nivel. */
    public void gastarPuntos(int puntos) {
        puntosDisponibles = Math.max(0, puntosDisponibles - puntos);
    }

    /** Cuenta una transaccion mas en el dia. */
    public void sumarTransaccionHoy() {
        transaccionesHoy++;
    }

    /** Vuelve a cero el contador del dia (cuando el reloj cambia de dia). */
    public void reiniciarTransaccionesHoy() {
        transaccionesHoy = 0;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getPuntosDisponibles() {
        return puntosDisponibles;
    }

    public int getPuntosAcumulados() {
        return puntosAcumulados;
    }

    public NivelUsuario getNivel() {
        return nivel;
    }

    public void setNivel(NivelUsuario nivel) {
        this.nivel = nivel;
    }

    public int getTransaccionesHoy() {
        return transaccionesHoy;
    }

    public LocalDateTime getFechaUltimoMovimiento() {
        return fechaUltimoMovimiento;
    }

    public void setFechaUltimoMovimiento(LocalDateTime fechaUltimoMovimiento) {
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
    }

    public PerfilComportamiento getPerfil() {
        return perfil;
    }

    public ListaEnlazada<Billetera> getBilleteras() {
        return billeteras;
    }

    public ListaEnlazada<Transaccion> getHistorial() {
        return historial;
    }

    public Pila<RegistroReversion> getPilaReversiones() {
        return pilaReversiones;
    }

    public void setPilaReversiones(Pila<RegistroReversion> pilaReversiones) {
        this.pilaReversiones = pilaReversiones;
    }

    public ListaEnlazada<Notificacion> getNotificaciones() {
        return notificaciones;
    }

    public ListaEnlazada<CanjeBeneficio> getCanjes() {
        return canjes;
    }

    @Override
    public String toString() {
        return getNombre() + " (" + getId() + ")";
    }
}
