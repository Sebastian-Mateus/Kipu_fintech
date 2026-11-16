package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.util.NavegadorVistas;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Marco de las pantallas del administrador: menu a la izquierda y arriba la
 * fecha simulada.
 */
public class PrincipalAdministradorControlador {

    private static PrincipalAdministradorControlador actual;

    @FXML
    private StackPane contenido;
    @FXML
    private Label etiquetaSeccion;
    @FXML
    private Label etiquetaFecha;
    @FXML
    private Label etiquetaUsuario;
    @FXML
    private Button botonSimulacion;
    @FXML
    private Button botonUsuarios;
    @FXML
    private Button botonTransacciones;
    @FXML
    private Button botonAnalitica;
    @FXML
    private Button botonRed;
    @FXML
    private Button botonAuditoria;
    @FXML
    private Button botonRendimiento;

    private Button botonActivo;

    @FXML
    private void initialize() {
        actual = this;
        NavegadorVistas.setContenedor(contenido);
        etiquetaUsuario.setText(Sesion.getPersonaActual().getNombre());
        irASimulacion();
    }

    /** Actualiza la fecha de arriba (se llama cuando se adelanta el reloj). */
    public static void refrescarBarra() {
        if (actual != null) {
            actual.etiquetaFecha.setText("Fecha simulada: "
                    + UtilidadesVista.fecha(Plataforma.getInstancia().getReloj().ahora()));
        }
    }

    private void abrir(String vista, String titulo, Button boton) {
        if (botonActivo != null) {
            botonActivo.getStyleClass().remove("activo");
        }
        boton.getStyleClass().add("activo");
        botonActivo = boton;
        etiquetaSeccion.setText(titulo);
        NavegadorVistas.mostrarEnContenido(vista);
        refrescarBarra();
    }

    @FXML
    private void irASimulacion() {
        abrir("simulacion", "Simulación del tiempo", botonSimulacion);
    }

    @FXML
    private void irAUsuarios() {
        abrir("usuarios", "Usuarios", botonUsuarios);
    }

    @FXML
    private void irATransacciones() {
        abrir("transacciones_admin", "Transacciones", botonTransacciones);
    }

    @FXML
    private void irAAnalitica() {
        abrir("analitica", "Analítica", botonAnalitica);
    }

    @FXML
    private void irARed() {
        abrir("red_transferencias", "Red de transferencias", botonRed);
    }

    @FXML
    private void irAAuditoria() {
        abrir("auditoria", "Auditoría de fraude", botonAuditoria);
    }

    @FXML
    private void irARendimiento() {
        abrir("rendimiento", "Rendimiento de las estructuras", botonRendimiento);
    }

    @FXML
    private void cerrarSesion() {
        Sesion.cerrar();
        actual = null;
        NavegadorVistas.irA("login");
    }
}
