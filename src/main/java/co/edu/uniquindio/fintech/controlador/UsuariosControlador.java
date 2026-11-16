package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelUsuario;
import co.edu.uniquindio.fintech.servicio.ServicioUsuarios;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Administracion de usuarios: buscar por cedula (tabla hash), ranking por puntos
 * (arbol en inorden inverso), filtro por nivel (buscarRango del arbol) y el
 * formulario para crear, editar, desactivar o eliminar.
 */
public class UsuariosControlador {

    private static final String TODOS = "Todos los niveles";

    @FXML
    private TextField campoBuscar;
    @FXML
    private ComboBox<String> filtroNivel;
    @FXML
    private Label etiquetaListado;
    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private Label etiquetaFormulario;
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
    private Button botonEstado;
    @FXML
    private Label etiquetaMensaje;

    private ServicioUsuarios servicioUsuarios;
    private Usuario enEdicion;

    @FXML
    private void initialize() {
        servicioUsuarios = Plataforma.getInstancia().getServicioUsuarios();

        filtroNivel.getItems().add(TODOS);
        for (NivelUsuario nivel : NivelUsuario.values()) {
            filtroNivel.getItems().add(nivel.getTexto());
        }
        filtroNivel.setValue(TODOS);
        filtroNivel.valueProperty().addListener((observable, anterior, nuevo) -> filtrarPorNivel());

        UtilidadesVista.columna(tablaUsuarios, "#",
                usuario -> String.valueOf(tablaUsuarios.getItems().indexOf(usuario) + 1));
        UtilidadesVista.columna(tablaUsuarios, "Cédula", usuario -> usuario.getId());
        UtilidadesVista.columna(tablaUsuarios, "Nombre", usuario -> usuario.getNombre());
        UtilidadesVista.columna(tablaUsuarios, "Correo", usuario -> usuario.getCorreo());
        UtilidadesVista.columnaEtiqueta(tablaUsuarios, "Nivel", usuario -> usuario.getNivel().getTexto(),
                usuario -> UtilidadesVista.clase("nivel", usuario.getNivel()));
        UtilidadesVista.columna(tablaUsuarios, "Puntos acumulados",
                usuario -> String.valueOf(usuario.getPuntosAcumulados()));
        UtilidadesVista.columna(tablaUsuarios, "Disponibles", usuario -> String.valueOf(usuario.getPuntosDisponibles()));
        UtilidadesVista.columna(tablaUsuarios, "Saldo total", usuario -> UtilidadesVista.pesos(usuario.saldoTotal()));
        UtilidadesVista.columnaEtiqueta(tablaUsuarios, "Estado", usuario -> usuario.isActivo() ? "Activo" : "Inactivo",
                usuario -> usuario.isActivo() ? "estado-activa" : "estado-cerrada");
        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaUsuarios.setPlaceholder(new Label("No hay usuarios para mostrar"));
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, nuevo) -> editar(nuevo));

        verRanking();
        nuevo();
    }

    @FXML
    private void buscar() {
        try {
            Usuario usuario = servicioUsuarios.buscar(campoBuscar.getText());
            tablaUsuarios.setItems(FXCollections.observableArrayList(usuario));
            etiquetaListado.setText("Resultado de la búsqueda por cédula (tabla hash)");
            tablaUsuarios.getSelectionModel().selectFirst();
        } catch (FintechException error) {
            UtilidadesVista.mostrarError(error.getMessage());
        }
    }

    @FXML
    private void verRanking() {
        filtroNivel.setValue(TODOS);
        mostrar(servicioUsuarios.ranking(), "Ranking por puntos acumulados (recorrido inorden inverso del árbol)");
    }

    private void filtrarPorNivel() {
        if (TODOS.equals(filtroNivel.getValue())) {
            verRanking();
            return;
        }
        for (NivelUsuario nivel : NivelUsuario.values()) {
            if (nivel.getTexto().equals(filtroNivel.getValue())) {
                mostrar(servicioUsuarios.usuariosPorNivel(nivel), "Usuarios " + nivel.getTexto()
                        + " (buscarRango del árbol entre " + nivel.getPuntosMinimos() + " y "
                        + (nivel == NivelUsuario.PLATINO ? "∞" : String.valueOf(nivel.getPuntosMaximos()))
                        + " puntos)");
            }
        }
    }

    private void mostrar(ListaEnlazada<Usuario> usuarios, String descripcion) {
        tablaUsuarios.setItems(UtilidadesVista.aObservable(usuarios));
        tablaUsuarios.refresh();
        etiquetaListado.setText(descripcion + " · " + usuarios.tamanio() + " usuarios");
    }

    @FXML
    private void nuevo() {
        enEdicion = null;
        tablaUsuarios.getSelectionModel().clearSelection();
        etiquetaFormulario.setText("Nuevo usuario");
        campoCedula.setDisable(false);
        campoCedula.clear();
        campoNombre.clear();
        campoCorreo.clear();
        campoTelefono.clear();
        campoContrasena.clear();
        botonEstado.setDisable(true);
    }

    private void editar(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        enEdicion = usuario;
        etiquetaFormulario.setText("Editar usuario");
        campoCedula.setText(usuario.getId());
        campoCedula.setDisable(true);
        campoNombre.setText(usuario.getNombre());
        campoCorreo.setText(usuario.getCorreo());
        campoTelefono.setText(usuario.getTelefono());
        campoContrasena.clear();
        botonEstado.setDisable(false);
        botonEstado.setText(usuario.isActivo() ? "Desactivar" : "Activar");
    }

    @FXML
    private void guardar() {
        try {
            if (enEdicion == null) {
                Usuario creado = servicioUsuarios.registrar(campoCedula.getText(), campoNombre.getText(),
                        campoCorreo.getText(), campoContrasena.getText(), campoTelefono.getText());
                exito("Se creó el usuario " + creado.getNombre());
            } else {
                servicioUsuarios.modificar(enEdicion.getId(), campoNombre.getText(), campoCorreo.getText(),
                        campoTelefono.getText(), campoContrasena.getText());
                exito("Se guardaron los cambios de " + enEdicion.getNombre());
            }
        } catch (FintechException error) {
            fallo(error.getMessage());
        }
    }

    @FXML
    private void cambiarEstado() {
        if (enEdicion != null) {
            servicioUsuarios.cambiarEstado(enEdicion.getId(), !enEdicion.isActivo());
            exito(enEdicion.getNombre() + (enEdicion.isActivo() ? " quedó activo" : " quedó inactivo"));
        }
    }

    @FXML
    private void eliminar() {
        if (enEdicion == null) {
            fallo("Selecciona un usuario de la tabla");
            return;
        }
        if (!UtilidadesVista.confirmar("¿Eliminar a " + enEdicion.getNombre() + "? Esto no se puede deshacer.")) {
            return;
        }
        try {
            servicioUsuarios.eliminar(enEdicion.getId());
            exito("Se eliminó a " + enEdicion.getNombre());
            nuevo();
        } catch (FintechException error) {
            fallo(error.getMessage());
        }
    }

    private void exito(String mensaje) {
        etiquetaMensaje.getStyleClass().setAll("label", "mensaje-exito");
        etiquetaMensaje.setText(mensaje);
        verRanking();
    }

    private void fallo(String mensaje) {
        etiquetaMensaje.getStyleClass().setAll("label", "mensaje-error");
        etiquetaMensaje.setText(mensaje);
    }
}
