package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.util.NavegadorVistas;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Marco de las pantallas del cliente: menu a la izquierda, barra de arriba con
 * la fecha simulada, el nivel y las notificaciones, y el contenido en el centro.
 */
public class PrincipalClienteControlador {

    // Referencia a la pantalla abierta para que las demas puedan refrescar la barra
    private static PrincipalClienteControlador actual;

    @FXML
    private StackPane contenido;
    @FXML
    private Label etiquetaSeccion;
    @FXML
    private Label etiquetaFecha;
    @FXML
    private Label etiquetaNivel;
    @FXML
    private Label etiquetaUsuario;
    @FXML
    private Button botonCampana;
    @FXML
    private Button botonInicio;
    @FXML
    private Button botonBilleteras;
    @FXML
    private Button botonOperaciones;
    @FXML
    private Button botonHistorial;
    @FXML
    private Button botonProgramadas;
    @FXML
    private Button botonRecompensas;
    @FXML
    private Button botonNotificaciones;
    @FXML
    private Button botonPerfil;

    private Button botonActivo;

    @FXML
    private void initialize() {
        actual = this;
        NavegadorVistas.setContenedor(contenido);
        irAInicio();
    }

    /** Vuelve a pintar la barra de arriba (despues de una operacion, un canje, etc). */
    public static void refrescarBarra() {
        if (actual != null) {
            actual.actualizarBarra();
        }
    }

    private void actualizarBarra() {
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null) {
            return;
        }
        Plataforma plataforma = Plataforma.getInstancia();
        etiquetaFecha.setText(UtilidadesVista.fecha(plataforma.getReloj().ahora()));
        etiquetaUsuario.setText(usuario.getNombre());

        etiquetaNivel.setText("Nivel " + usuario.getNivel().getTexto());
        etiquetaNivel.getStyleClass().setAll("label", "etiqueta", UtilidadesVista.clase("nivel", usuario.getNivel()));

        int sinLeer = plataforma.getServicioNotificaciones().contarNoLeidas(usuario);
        botonCampana.setText("Notificaciones (" + sinLeer + ")");
    }

    private void abrir(String vista, String titulo, Button boton) {
        if (botonActivo != null) {
            botonActivo.getStyleClass().remove("activo");
        }
        boton.getStyleClass().add("activo");
        botonActivo = boton;
        etiquetaSeccion.setText(titulo);
        NavegadorVistas.mostrarEnContenido(vista);
        actualizarBarra();
    }

    @FXML
    private void irAInicio() {
        abrir("inicio", "Inicio", botonInicio);
    }

    @FXML
    private void irABilleteras() {
        abrir("mis_billeteras", "Mis billeteras", botonBilleteras);
    }

    @FXML
    private void irAOperaciones() {
        abrir("operaciones", "Operaciones", botonOperaciones);
    }

    @FXML
    private void irAHistorial() {
        abrir("historial", "Historial", botonHistorial);
    }

    @FXML
    private void irAProgramadas() {
        abrir("programadas", "Operaciones programadas", botonProgramadas);
    }

    @FXML
    private void irARecompensas() {
        abrir("recompensas", "Recompensas", botonRecompensas);
    }

    @FXML
    private void irANotificaciones() {
        abrir("notificaciones", "Notificaciones", botonNotificaciones);
    }

    @FXML
    private void irAPerfil() {
        abrir("perfil", "Mi perfil", botonPerfil);
    }

    @FXML
    private void cerrarSesion() {
        Sesion.cerrar();
        actual = null;
        NavegadorVistas.irA("login");
    }
}
