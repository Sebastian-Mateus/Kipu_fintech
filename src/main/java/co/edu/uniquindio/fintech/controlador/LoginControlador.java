package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Persona;
import co.edu.uniquindio.fintech.util.NavegadorVistas;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Pantalla de inicio de sesion. Segun quien entre manda al panel del
 * administrador o al del cliente.
 */
public class LoginControlador {

    @FXML
    private TextField campoUsuario;
    @FXML
    private PasswordField campoContrasena;
    @FXML
    private Label etiquetaError;

    @FXML
    private void ingresar() {
        try {
            Persona persona = Plataforma.getInstancia().getServicioUsuarios()
                    .autenticar(campoUsuario.getText(), campoContrasena.getText());
            Sesion.iniciar(persona);
            if (Sesion.esAdministrador()) {
                NavegadorVistas.irA("principal_administrador");
            } else {
                NavegadorVistas.irA("principal_cliente");
            }
        } catch (FintechException error) {
            etiquetaError.setText(error.getMessage());
            campoContrasena.clear();
        }
    }

    @FXML
    private void irARegistro() {
        NavegadorVistas.irA("registro");
    }
}
