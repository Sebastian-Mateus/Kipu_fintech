package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.excepciones.OperacionInvalidaException;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.OperacionProgramada;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.Frecuencia;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.servicio.ServicioProgramadas;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Formulario para programar operaciones y tabla con las que estan activas.
 * Segun el tipo se muestran unos campos u otros.
 */
public class ProgramadasControlador {

    @FXML
    private ComboBox<TipoTransaccion> comboTipo;
    @FXML
    private VBox cajaOrigen;
    @FXML
    private ComboBox<Billetera> comboOrigen;
    @FXML
    private VBox cajaDestinoPropia;
    @FXML
    private ComboBox<Billetera> comboDestino;
    @FXML
    private VBox cajaDestinoCodigo;
    @FXML
    private TextField campoDestinoCodigo;
    @FXML
    private TextField campoMonto;
    @FXML
    private DatePicker campoFecha;
    @FXML
    private TextField campoHora;
    @FXML
    private ComboBox<Frecuencia> comboFrecuencia;
    @FXML
    private TextField campoDescripcion;
    @FXML
    private Label etiquetaMensaje;
    @FXML
    private TableView<OperacionProgramada> tablaProgramadas;

    private ServicioProgramadas servicioProgramadas;
    private Usuario usuario;

    @FXML
    private void initialize() {
        Plataforma plataforma = Plataforma.getInstancia();
        servicioProgramadas = plataforma.getServicioProgramadas();
        usuario = Sesion.getUsuarioActual();

        comboTipo.getItems().addAll(TipoTransaccion.RECARGA, TipoTransaccion.RETIRO,
                TipoTransaccion.TRANSFERENCIA_INTERNA, TipoTransaccion.TRANSFERENCIA_EXTERNA);
        comboTipo.setValue(TipoTransaccion.TRANSFERENCIA_EXTERNA);
        comboTipo.valueProperty().addListener((observable, anterior, nuevo) -> mostrarCampos());

        ObservableList<Billetera> activas = UtilidadesVista.aObservable(
                plataforma.getServicioBilleteras().activasDe(usuario));
        comboOrigen.setItems(activas);
        comboDestino.setItems(activas);

        comboFrecuencia.getItems().addAll(Frecuencia.values());
        comboFrecuencia.setValue(Frecuencia.UNA_VEZ);
        campoFecha.setValue(plataforma.getReloj().hoy().plusDays(1));

        UtilidadesVista.columna(tablaProgramadas, "ID", operacion -> operacion.getId());
        UtilidadesVista.columna(tablaProgramadas, "Descripción",
                operacion -> UtilidadesVista.textoSeguro(operacion.getDescripcion()));
        UtilidadesVista.columna(tablaProgramadas, "Tipo", operacion -> operacion.getTipo().getTexto());
        UtilidadesVista.columna(tablaProgramadas, "Valor", operacion -> UtilidadesVista.pesos(operacion.getValor()));
        UtilidadesVista.columna(tablaProgramadas, "Próxima ejecución",
                operacion -> UtilidadesVista.fecha(operacion.getFechaProximaEjecucion()));
        UtilidadesVista.columna(tablaProgramadas, "Frecuencia", operacion -> operacion.getFrecuencia().getTexto());
        UtilidadesVista.columna(tablaProgramadas, "Ejecutada",
                operacion -> operacion.getVecesEjecutada() + " veces");
        UtilidadesVista.columna(tablaProgramadas, "Fallos", operacion -> String.valueOf(operacion.getIntentosFallidos()));
        tablaProgramadas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaProgramadas.setPlaceholder(new Label("No tienes operaciones programadas"));

        mostrarCampos();
        cargarTabla();
    }

    @FXML
    private void programar() {
        try {
            TipoTransaccion tipo = comboTipo.getValue();
            String codigoOrigen = null;
            String codigoDestino = null;
            if (tipo != TipoTransaccion.RECARGA && comboOrigen.getValue() != null) {
                codigoOrigen = comboOrigen.getValue().getCodigo();
            }
            if (tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA) {
                codigoDestino = campoDestinoCodigo.getText().trim().toUpperCase();
            } else if (tipo != TipoTransaccion.RETIRO && comboDestino.getValue() != null) {
                codigoDestino = comboDestino.getValue().getCodigo();
            }

            OperacionProgramada operacion = servicioProgramadas.programar(usuario, tipo, codigoOrigen, codigoDestino,
                    UtilidadesVista.leerMonto(campoMonto.getText()), leerFecha(), comboFrecuencia.getValue(),
                    campoDescripcion.getText());

            etiquetaMensaje.getStyleClass().setAll("label", "mensaje-exito");
            etiquetaMensaje.setText("Quedó programada (" + operacion.getId() + ") para el "
                    + UtilidadesVista.fecha(operacion.getFechaProximaEjecucion()));
            campoMonto.clear();
            campoDescripcion.clear();
            cargarTabla();
            PrincipalClienteControlador.refrescarBarra();
        } catch (FintechException error) {
            etiquetaMensaje.getStyleClass().setAll("label", "mensaje-error");
            etiquetaMensaje.setText(error.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        OperacionProgramada seleccionada = tablaProgramadas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            UtilidadesVista.mostrarError("Selecciona la operación que quieres cancelar");
            return;
        }
        if (UtilidadesVista.confirmar("¿Cancelar la operación \"" + seleccionada.getDescripcion() + "\"?")) {
            try {
                servicioProgramadas.cancelar(seleccionada.getId());
                cargarTabla();
            } catch (FintechException error) {
                UtilidadesVista.mostrarError(error.getMessage());
            }
        }
    }

    private void cargarTabla() {
        tablaProgramadas.setItems(UtilidadesVista.aObservable(servicioProgramadas.activasDe(usuario)));
    }

    private void mostrarCampos() {
        TipoTransaccion tipo = comboTipo.getValue();
        mostrar(cajaOrigen, tipo != TipoTransaccion.RECARGA);
        mostrar(cajaDestinoPropia, tipo == TipoTransaccion.RECARGA || tipo == TipoTransaccion.TRANSFERENCIA_INTERNA);
        mostrar(cajaDestinoCodigo, tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA);
    }

    private void mostrar(VBox caja, boolean visible) {
        caja.setVisible(visible);
        caja.setManaged(visible);
    }

    private LocalDateTime leerFecha() {
        if (campoFecha.getValue() == null) {
            throw new OperacionInvalidaException("Elige la fecha de ejecución");
        }
        try {
            LocalTime hora = LocalTime.parse(campoHora.getText().trim());
            return LocalDateTime.of(campoFecha.getValue(), hora);
        } catch (DateTimeParseException error) {
            throw new OperacionInvalidaException("La hora debe tener el formato HH:mm, por ejemplo 08:30");
        }
    }
}
