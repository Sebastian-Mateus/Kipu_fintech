package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Deja que el cliente cambie su nombre, correo, telefono y contraseña.
 * La cedula no se puede cambiar.
 */
public class PerfilControlador {

    @FXML
    private Label etiquetaResumen;
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
    private Label etiquetaMensaje;

    private Usuario usuario;

    @FXML
    private void initialize() {
        usuario = Sesion.getUsuarioActual();
        etiquetaResumen.setText("Cliente desde el " + UtilidadesVista.fecha(usuario.getFechaRegistro())
                + " · " + usuario.getBilleteras().tamanio() + " billeteras · Nivel " + usuario.getNivel().getTexto());
        campoCedula.setText(usuario.getId());
        campoNombre.setText(usuario.getNombre());
        campoCorreo.setText(usuario.getCorreo());
        campoTelefono.setText(usuario.getTelefono());
    }

    @FXML
    private void guardar() {
        try {
            Plataforma.getInstancia().getServicioUsuarios().modificar(usuario.getId(), campoNombre.getText(),
                    campoCorreo.getText(), campoTelefono.getText(), campoContrasena.getText());
            campoContrasena.clear();
            etiquetaMensaje.getStyleClass().setAll("label", "mensaje-exito");
            etiquetaMensaje.setText("Tus datos quedaron guardados");
            PrincipalClienteControlador.refrescarBarra();
        } catch (FintechException error) {
            etiquetaMensaje.getStyleClass().setAll("label", "mensaje-error");
            etiquetaMensaje.setText(error.getMessage());
        }
    }
}
