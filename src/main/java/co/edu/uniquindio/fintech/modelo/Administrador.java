package co.edu.uniquindio.fintech.modelo;

/**
 * Administrador de la plataforma. No tiene billeteras, solo entra al panel.
 */
public class Administrador extends Persona {

    private String cargo;

    public Administrador(String id, String nombre, String correo, String contrasena, String cargo) {
        super(id, nombre, correo, contrasena);
        this.cargo = cargo;
    }

    @Override
    public String getRol() {
        return "Administrador";
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    @Override
    public String toString() {
        return getNombre() + " (" + cargo + ")";
    }
}
