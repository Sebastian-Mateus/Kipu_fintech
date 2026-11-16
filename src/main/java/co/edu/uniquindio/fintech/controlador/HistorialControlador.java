package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.servicio.ServicioReversion;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.Iterator;

/**
 * Historial del cliente con filtros por tipo y fechas. El boton de revertir
 * solo se activa cuando la transaccion seleccionada se puede revertir.
 */
public class HistorialControlador {

    private static final String TODOS = "Todos los tipos";

    @FXML
    private ComboBox<String> filtroTipo;
    @FXML
    private DatePicker filtroDesde;
    @FXML
    private DatePicker filtroHasta;
    @FXML
    private TableView<Transaccion> tablaHistorial;
    @FXML
    private Button botonRevertir;
    @FXML
    private Label etiquetaMensaje;

    private ServicioReversion servicioReversion;
    private Usuario usuario;

    @FXML
    private void initialize() {
        servicioReversion = Plataforma.getInstancia().getServicioReversion();
        usuario = Sesion.getUsuarioActual();

        filtroTipo.getItems().add(TODOS);
        for (TipoTransaccion tipo : TipoTransaccion.values()) {
            filtroTipo.getItems().add(tipo.getTexto());
        }
        filtroTipo.setValue(TODOS);

        UtilidadesVista.configurarTablaTransacciones(tablaHistorial);
        tablaHistorial.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, nueva) -> botonRevertir.setDisable(!sePuedeRevertir(nueva)));
        botonRevertir.setDisable(true);
        filtrar();
    }

    @FXML
    private void filtrar() {
        LocalDate desde = filtroDesde.getValue();
        LocalDate hasta = filtroHasta.getValue();
        ObservableList<Transaccion> filas = FXCollections.observableArrayList();

        Iterator<Transaccion> iterador = usuario.getHistorial().iteradorInverso();
        while (iterador.hasNext()) {
            Transaccion transaccion = iterador.next();
            LocalDate dia = transaccion.getFecha().toLocalDate();
            boolean coincideTipo = TODOS.equals(filtroTipo.getValue())
                    || transaccion.getTipo().getTexto().equals(filtroTipo.getValue());
            boolean despuesDeDesde = desde == null || !dia.isBefore(desde);
            boolean antesDeHasta = hasta == null || !dia.isAfter(hasta);
            if (coincideTipo && despuesDeDesde && antesDeHasta) {
                filas.add(transaccion);
            }
        }
        tablaHistorial.setItems(filas);
        tablaHistorial.refresh();
    }

    @FXML
    private void limpiar() {
        filtroTipo.setValue(TODOS);
        filtroDesde.setValue(null);
        filtroHasta.setValue(null);
        filtrar();
    }

    @FXML
    private void revertir() {
        Transaccion seleccionada = tablaHistorial.getSelectionModel().getSelectedItem();
        if (seleccionada == null || !UtilidadesVista.confirmar("¿Revertir la transacción " + seleccionada.getId()
                + " por " + UtilidadesVista.pesos(seleccionada.getValor()) + "?")) {
            return;
        }
        try {
            Transaccion reversion = servicioReversion.revertir(seleccionada.getId());
            etiquetaMensaje.getStyleClass().setAll("label", "mensaje-exito");
            etiquetaMensaje.setText("Se revirtió " + seleccionada.getId() + " con la transacción " + reversion.getId());
        } catch (FintechException error) {
            etiquetaMensaje.getStyleClass().setAll("label", "mensaje-error");
            etiquetaMensaje.setText(error.getMessage());
        }
        filtrar();
        PrincipalClienteControlador.refrescarBarra();
    }

    // Solo quien hizo la transaccion la puede revertir (es la que esta en su pila)
    private boolean sePuedeRevertir(Transaccion transaccion) {
        return transaccion != null
                && transaccion.getUsuario().equals(usuario)
                && servicioReversion.esReversible(transaccion);
    }
}
