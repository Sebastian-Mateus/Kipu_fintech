package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.servicio.ServicioReversion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Todas las transacciones de la plataforma, el top N de mayor valor (sale del
 * arbol por valor), las de un periodo y la opcion de revertir.
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

    @FXML
    private DatePicker campoDesde;
    @FXML
    private DatePicker campoHasta;

    private Plataforma plataforma;
    private ServicioReversion servicioReversion;
    // Lo que se esta mostrando, para volver a cargarlo despues de revertir
    private String vistaActual;

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
        // Son muchas columnas, mejor que cada una tenga su ancho y la tabla se pueda mover de lado
        tablaTransacciones.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        tablaTransacciones.getSelectionModel().selectedItemProperty().addListener((observable, anterior, nueva) ->
                botonRevertir.setDisable(nueva == null || !servicioReversion.esReversible(nueva)));
        botonRevertir.setDisable(true);
        campoDesde.setValue(plataforma.getReloj().hoy().minusDays(7));
        campoHasta.setValue(plataforma.getReloj().hoy());
        verTodas();
    }

    @FXML
    private void verTodas() {
        vistaActual = "todas";
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
        vistaActual = "top";
        tablaTransacciones.setItems(UtilidadesVista.aObservable(
                plataforma.getServicioAnalitica().transaccionesMayorValor(cantidad)));
        etiquetaListado.setText("Top " + cantidad + " transacciones exitosas de mayor valor "
                + "(recorrido inorden inverso del árbol por valor)");
    }

    @FXML
    private void verPeriodo() {
        if (campoDesde.getValue() == null || campoHasta.getValue() == null) {
            UtilidadesVista.mostrarError("Elige las dos fechas del período");
            return;
        }
        LocalDateTime desde = campoDesde.getValue().atStartOfDay();
        LocalDateTime hasta = campoHasta.getValue().atTime(LocalTime.MAX);
        if (hasta.isBefore(desde)) {
            UtilidadesVista.mostrarError("La fecha final no puede ser antes de la inicial");
            return;
        }
        vistaActual = "periodo";
        ListaEnlazada<Transaccion> enPeriodo = plataforma.getServicioAnalitica().transaccionesEnPeriodo(desde, hasta);
        tablaTransacciones.setItems(UtilidadesVista.aObservableInverso(enPeriodo));
        etiquetaListado.setText("Transacciones del " + UtilidadesVista.fecha(desde).substring(0, 10) + " al "
                + UtilidadesVista.fecha(hasta).substring(0, 10) + " · " + enPeriodo.tamanio()
                + " (recorrido de la lista global, que está ordenada por fecha)");
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
        if ("top".equals(vistaActual)) {
            verTop();
        } else if ("periodo".equals(vistaActual)) {
            verPeriodo();
        } else {
            verTodas();
        }
    }
}
