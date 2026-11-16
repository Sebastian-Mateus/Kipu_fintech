package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.estructuras.Cola;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.modelo.Notificacion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.fintech.util.GeneradorIdentificadores;
import co.edu.uniquindio.fintech.util.RelojSistema;

import java.util.Iterator;

/**
 * Maneja las notificaciones. Cuando se genera una queda esperando en una cola,
 * y al despachar pasa a la lista del usuario. Cada usuario guarda maximo 20,
 * si se pasa se borra la mas vieja.
 */
public class ServicioNotificaciones {

    private static final int MAXIMO_RECIENTES = 20;

    private Cola<Notificacion> pendientes;
    private RelojSistema reloj;
    private GeneradorIdentificadores generador;

    public ServicioNotificaciones(RelojSistema reloj, GeneradorIdentificadores generador) {
        this.pendientes = new Cola<>();
        this.reloj = reloj;
        this.generador = generador;
    }

    /** Crea una notificaicon y la deja en la cola de pendientes. */
    public Notificacion generar(Usuario usuario, TipoNotificacion tipo, String mensaje) {
        Notificacion notificacion = new Notificacion(generador.siguienteNotificacion(), usuario, tipo, mensaje,
                reloj.ahora());
        pendientes.encolar(notificacion);
        return notificacion;
    }

    /** Pasa todas las pendientes a la lista de su usuario. Devuelve cuantas despacho. */
    public int despacharPendientes() {
        int despachadas = 0;
        while (!pendientes.estaVacia()) {
            Notificacion notificacion = pendientes.desencolar();
            notificacion.setDespachada(true);

            ListaEnlazada<Notificacion> lista = notificacion.getUsuario().getNotificaciones();
            lista.agregarAlFinal(notificacion);
            if (lista.tamanio() > MAXIMO_RECIENTES) {
                lista.eliminarPrimero();
            }
            despachadas++;
        }
        return despachadas;
    }

    /** Marca una notificacion como leida. */
    public void marcarLeida(Notificacion notificacion) {
        notificacion.marcarLeida();
    }

    /** Marca como leidas todas las notificaciones del usuario. */
    public void marcarTodasLeidas(Usuario usuario) {
        for (Notificacion notificacion : usuario.getNotificaciones()) {
            notificacion.marcarLeida();
        }
    }

    /** Cuantas notificaciones sin leer tiene el usuario (para la campana). */
    public int contarNoLeidas(Usuario usuario) {
        int cantidad = 0;
        for (Notificacion notificacion : usuario.getNotificaciones()) {
            if (!notificacion.isLeida()) {
                cantidad++;
            }
        }
        return cantidad;
    }

    /** Las notificaciones del usuario empezando por la mas nueva. */
    public ListaEnlazada<Notificacion> recientes(Usuario usuario) {
        ListaEnlazada<Notificacion> resultado = new ListaEnlazada<>();
        Iterator<Notificacion> iterador = usuario.getNotificaciones().iteradorInverso();
        while (iterador.hasNext()) {
            resultado.agregarAlFinal(iterador.next());
        }
        return resultado;
    }

    /** Cuantas notificaciones estan esperando en la cola. */
    public int cantidadPendientes() {
        return pendientes.tamanio();
    }
}
