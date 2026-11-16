package co.edu.uniquindio.fintech;

import co.edu.uniquindio.fintech.repositorio.RepositorioBilleteras;
import co.edu.uniquindio.fintech.repositorio.RepositorioTransacciones;
import co.edu.uniquindio.fintech.repositorio.RepositorioUsuarios;
import co.edu.uniquindio.fintech.servicio.ServicioAnalitica;
import co.edu.uniquindio.fintech.servicio.ServicioBilleteras;
import co.edu.uniquindio.fintech.servicio.ServicioDeteccionFraude;
import co.edu.uniquindio.fintech.servicio.ServicioNotificaciones;
import co.edu.uniquindio.fintech.servicio.ServicioProgramadas;
import co.edu.uniquindio.fintech.servicio.ServicioRecompensas;
import co.edu.uniquindio.fintech.servicio.ServicioRendimiento;
import co.edu.uniquindio.fintech.servicio.ServicioReversion;
import co.edu.uniquindio.fintech.servicio.ServicioTransacciones;
import co.edu.uniquindio.fintech.servicio.ServicioUsuarios;
import co.edu.uniquindio.fintech.util.GeneradorIdentificadores;
import co.edu.uniquindio.fintech.util.RelojSistema;

/**
 * Punto central de la aplicacion. Crea el reloj, los repositorios y los
 * servicios una sola vez y los conecta entre si. Los controladores le piden
 * todo a Plataforma.getInstancia().
 */
public class Plataforma {

    private static Plataforma instancia;

    private RelojSistema reloj;
    private GeneradorIdentificadores generador;

    private RepositorioUsuarios repositorioUsuarios;
    private RepositorioBilleteras repositorioBilleteras;
    private RepositorioTransacciones repositorioTransacciones;

    private ServicioNotificaciones servicioNotificaciones;
    private ServicioRecompensas servicioRecompensas;
    private ServicioDeteccionFraude servicioDeteccionFraude;
    private ServicioAnalitica servicioAnalitica;
    private ServicioTransacciones servicioTransacciones;
    private ServicioReversion servicioReversion;
    private ServicioProgramadas servicioProgramadas;
    private ServicioUsuarios servicioUsuarios;
    private ServicioBilleteras servicioBilleteras;
    private ServicioRendimiento servicioRendimiento;

    private Plataforma() {
        reloj = new RelojSistema();
        generador = new GeneradorIdentificadores();

        repositorioUsuarios = new RepositorioUsuarios();
        repositorioBilleteras = new RepositorioBilleteras();
        repositorioTransacciones = new RepositorioTransacciones();

        // El orden importa: cada servicio necesita que ya existan los que usa
        servicioNotificaciones = new ServicioNotificaciones(reloj, generador);
        servicioRecompensas = new ServicioRecompensas(repositorioUsuarios, servicioNotificaciones, reloj, generador);
        servicioDeteccionFraude = new ServicioDeteccionFraude(servicioNotificaciones, generador);
        servicioAnalitica = new ServicioAnalitica(repositorioTransacciones, repositorioBilleteras, repositorioUsuarios);
        servicioTransacciones = new ServicioTransacciones(repositorioTransacciones, repositorioBilleteras, reloj,
                generador, servicioNotificaciones, servicioRecompensas, servicioDeteccionFraude, servicioAnalitica);
        servicioReversion = new ServicioReversion(repositorioTransacciones, reloj, generador, servicioNotificaciones,
                servicioRecompensas, servicioAnalitica);
        servicioProgramadas = new ServicioProgramadas(reloj, generador, servicioTransacciones, servicioNotificaciones,
                repositorioBilleteras);
        servicioUsuarios = new ServicioUsuarios(repositorioUsuarios, repositorioBilleteras, servicioProgramadas, reloj);
        servicioBilleteras = new ServicioBilleteras(repositorioBilleteras, generador, reloj);
        servicioRendimiento = new ServicioRendimiento();
    }

    /** Devuelve la unica instancia de la plataforma, la crea la primera vez. */
    public static Plataforma getInstancia() {
        if (instancia == null) {
            instancia = new Plataforma();
        }
        return instancia;
    }

    /** Adelanta el reloj una hora y ejecuta las programadas que se vencieron. Devuelve cuantas proceso. */
    public int avanzarHora() {
        reloj.avanzarHoras(1);
        return servicioProgramadas.procesarPendientes();
    }

    /** Adelanta el reloj un dia y ejecuta las programadas que se vencieron. */
    public int avanzarDia() {
        reloj.avanzarDias(1);
        return servicioProgramadas.procesarPendientes();
    }

    /** Adelanta el reloj una semana y ejecuta las programadas que se vencieron. */
    public int avanzarSemana() {
        reloj.avanzarSemanas(1);
        return servicioProgramadas.procesarPendientes();
    }

    public RelojSistema getReloj() {
        return reloj;
    }

    public GeneradorIdentificadores getGenerador() {
        return generador;
    }

    public RepositorioUsuarios getRepositorioUsuarios() {
        return repositorioUsuarios;
    }

    public RepositorioBilleteras getRepositorioBilleteras() {
        return repositorioBilleteras;
    }

    public RepositorioTransacciones getRepositorioTransacciones() {
        return repositorioTransacciones;
    }

    public ServicioNotificaciones getServicioNotificaciones() {
        return servicioNotificaciones;
    }

    public ServicioRecompensas getServicioRecompensas() {
        return servicioRecompensas;
    }

    public ServicioDeteccionFraude getServicioDeteccionFraude() {
        return servicioDeteccionFraude;
    }

    public ServicioAnalitica getServicioAnalitica() {
        return servicioAnalitica;
    }

    public ServicioTransacciones getServicioTransacciones() {
        return servicioTransacciones;
    }

    public ServicioReversion getServicioReversion() {
        return servicioReversion;
    }

    public ServicioProgramadas getServicioProgramadas() {
        return servicioProgramadas;
    }

    public ServicioUsuarios getServicioUsuarios() {
        return servicioUsuarios;
    }

    public ServicioBilleteras getServicioBilleteras() {
        return servicioBilleteras;
    }

    public ServicioRendimiento getServicioRendimiento() {
        return servicioRendimiento;
    }
}
