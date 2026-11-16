package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.servicio.ServicioReversion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Todas las transacciones de la plataforma, el top N de mayor valor (sale del
 * arbol por valor) y la opcion de revertir.
 */
public class TransaccionesAdminControlador {

    @FXML
    private TextField campoCantidad;
    @FXML
    private Button botonRevertir;
    @FXML
    private Label etiquetaListado;
    @FXML
    private TableView<Transaccion> tablaTransacciones;
    @FXML
    private Label etiquetaMensaje;

    private Plataforma plataforma;
    private ServicioReversion servicioReversion;
    private boolean mostrandoTop;

    @FXML
    private void initialize() {
        plataforma = Plataforma.getInstancia();
        servicioReversion = plataforma.getServicioReversion();

        UtilidadesVista.configurarTablaTransacciones(tablaTransacciones);
        // Al admin le sirve ver de quien es cada transaccion, se pone de segunda columna
        TableColumn<Transaccion, String> columnaUsuario = UtilidadesVista.columna(tablaTransacciones, "Usuario",
                transaccion -> transaccion.getUsuario().getNombre());
        tablaTransacciones.getColumns().remove(columnaUsuario);
        tablaTransacciones.getColumns().add(1, columnaUsuario);

        tablaTransacciones.getSelectionModel().selectedItemProperty().addListener((observable, anterior, nueva) ->
                botonRevertir.setDisable(nueva == null || !servicioReversion.esReversible(nueva)));
        botonRevertir.setDisable(true);
        verTodas();
    }

    @FXML
    private void verTodas() {
        mostrandoTop = false;
        tablaTransacciones.setItems(UtilidadesVista.aObservableInverso(
                plataforma.getRepositorioTransacciones().listar()));
        etiquetaListado.setText("Todas las transacciones, de la más reciente a la más antigua · "
                + tablaTransacciones.getItems().size() + " en total");
    }

    @FXML
    private void verTop() {
        int cantidad;
        try {
            cantidad = Integer.parseInt(campoCantidad.getText().trim());
        } catch (NumberFormatException error) {
            UtilidadesVista.mostrarError("La cantidad debe ser un número entero");
            return;
        }
        if (cantidad <= 0) {
            UtilidadesVista.mostrarError("La cantidad debe ser mayor que cero");
            return;
        }
        mostrandoTop = true;
        tablaTransacciones.setItems(UtilidadesVista.aObservable(
                plataforma.getServicioAnalitica().transaccionesMayorValor(cantidad)));
        etiquetaListado.setText("Top " + cantidad + " transacciones exitosas de mayor valor "
                + "(recorrido inorden inverso del árbol por valor)");
    }

    @FXML
    private void revertir() {
        Transaccion seleccionada = tablaTransacciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null || !UtilidadesVista.confirmar("¿Revertir la transacción " + seleccionada.getId()
                + " de " + seleccionada.getUsuario().getNombre() + "?")) {
            return;
        }
        try {
            Transaccion reversion = servicioReversion.revertir(seleccionada.getId());
            etiquetaMensaje.getStyleClass().setAll("label", "mensaje-exito");
            etiquetaMensaje.setText("Se revirtió " + seleccionada.getId() + " con " + reversion.getId());
        } catch (FintechException error) {
            etiquetaMensaje.getStyleClass().setAll("label", "mensaje-error");
            etiquetaMensaje.setText(error.getMessage());
        }
        if (mostrandoTop) {
            verTop();
        } else {
            verTodas();
        }
    }
}
