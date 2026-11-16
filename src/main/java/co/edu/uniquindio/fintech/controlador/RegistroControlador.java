package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.util.NavegadorVistas;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Registro de un cliente nuevo. Si sale bien, entra de una vez a su cuenta.
 */
public class RegistroControlador {

    @FXML
    private TextField campoCedula;
    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoCorreo;
    @FXML
    private TextField campoTelefono;
    @FXML
    private PasswordField campoContrasena;
    @FXML
    private PasswordField campoConfirmacion;
    @FXML
    private Label etiquetaError;

    @FXML
    private void registrar() {
        if (!campoContrasena.getText().equals(campoConfirmacion.getText())) {
            etiquetaError.setText("Las contraseñas no coinciden");
            return;
        }
        try {
            Usuario usuario = Plataforma.getInstancia().getServicioUsuarios().registrar(campoCedula.getText(),
                    campoNombre.getText(), campoCorreo.getText(), campoContrasena.getText(),
                    campoTelefono.getText());
            Sesion.iniciar(usuario);
            NavegadorVistas.irA("principal_cliente");
        } catch (FintechException error) {
            etiquetaError.setText(error.getMessage());
        }
    }

    @FXML
    private void volver() {
        NavegadorVistas.irA("login");
    }
}
