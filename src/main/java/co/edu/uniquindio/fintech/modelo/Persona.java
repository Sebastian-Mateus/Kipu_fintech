package co.edu.uniquindio.fintech.modelo;

/**
 * Datos comunes de cualquiera que entra a la plataforma, sea cliente o administrador.
 * El id es la cedula, por eso no tiene setter.
 */
public abstract class Persona {

    private String id;
    private String nombre;
    private String correo;
    private String contrasena;

    public Persona(String id, String nombre, String correo, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    /** Nombre del rol para mostrar en la interfaz. */
    public abstract String getRol();

    /** Dice si la contraseña que escribio coincide con la guardada. */
    public boolean verificarContrasena(String contrasenaIngresada) {
        return contrasena.equals(contrasenaIngresada);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    // Dos personas son la misma si tienen la misma cedula
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Persona)) {
            return false;
        }
        Persona otraPersona = (Persona) otro;
        return id.equals(otraPersona.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
