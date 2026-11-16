package co.edu.uniquindio.fintech.util;

import co.edu.uniquindio.fintech.modelo.Administrador;
import co.edu.uniquindio.fintech.modelo.Persona;
import co.edu.uniquindio.fintech.modelo.Usuario;

/**
 * Recuerda quien inicio sesion para que todas las pantallas lo puedan consultar.
 */
public class Sesion {

    private static Persona personaActual;

    private Sesion() {
        // Solo tiene metodos estaticos
    }

    /** Guarda a la persona que acaba de entrar. */
    public static void iniciar(Persona persona) {
        personaActual = persona;
    }

    /** Borra la sesion al salir. */
    public static void cerrar() {
        personaActual = null;
    }

    /** Dice si hay alguien con sesion abierta. */
    public static boolean haySesion() {
        return personaActual != null;
    }

    /** Dice si quien entro es el administrador. */
    public static boolean esAdministrador() {
        return personaActual instanceof Administrador;
    }

    public static Persona getPersonaActual() {
        return personaActual;
    }

    /** Devuelve el usuario cliente que entro, o null si es el administrador o no hay nadie. */
    public static Usuario getUsuarioActual() {
        if (personaActual instanceof Usuario) {
            return (Usuario) personaActual;
        }
        return null;
    }
}
