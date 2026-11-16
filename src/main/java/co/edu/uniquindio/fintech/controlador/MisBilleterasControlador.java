package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.TipoBilletera;
import co.edu.uniquindio.fintech.servicio.ServicioBilleteras;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * Lista las billeteras del cliente y deja crearlas, renombrarlas, bloquearlas
 * y cerrarlas. Con doble clic se ve el historial de una billetera.
 */
public class MisBilleterasControlador {

    @FXML
    private TableView<Billetera> tablaBilleteras;
    @FXML
    private Label etiquetaMensaje;

    private ServicioBilleteras servicioBilleteras;
    private Usuario usuario;

    @FXML
    private void initialize() {
        servicioBilleteras = Plataforma.getInstancia().getServicioBilleteras();
        usuario = Sesion.getUsuarioActual();

        UtilidadesVista.columna(tablaBilleteras, "Código", billetera -> billetera.getCodigo());
        UtilidadesVista.columna(tablaBilleteras, "Nombre", billetera -> billetera.getNombre());
        UtilidadesVista.columna(tablaBilleteras, "Tipo", billetera -> billetera.getTipo().getTexto());
        UtilidadesVista.columna(tablaBilleteras, "Saldo", billetera -> UtilidadesVista.pesos(billetera.getSaldo()));
        UtilidadesVista.columna(tablaBilleteras, "Aviso de saldo bajo",
                billetera -> UtilidadesVista.pesos(billetera.getUmbralSaldoBajo()));
        UtilidadesVista.columnaEtiqueta(tablaBilleteras, "Estado", billetera -> billetera.getEstado().getTexto(),
                billetera -> UtilidadesVista.clase("estado", billetera.getEstado()));
        tablaBilleteras.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaBilleteras.setPlaceholder(new Label("Todavía no tienes billeteras, crea la primera"));

        tablaBilleteras.setRowFactory(tabla -> {
            TableRow<Billetera> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    verHistorial(fila.getItem());
                }
            });
            return fila;
        });
        cargar();
    }

    private void cargar() {
        tablaBilleteras.setItems(UtilidadesVista.aObservable(usuario.getBilleteras()));
        tablaBilleteras.refresh();
    }

    @FXML
    private void nueva() {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Nueva billetera");
        dialogo.setHeaderText("Datos de la nueva billetera");
        UtilidadesVista.estilizar(dialogo.getDialogPane());

        TextField campoNombre = new TextField();
        ComboBox<TipoBilletera> comboTipo = new ComboBox<>();
        comboTipo.getItems().addAll(TipoBilletera.values());
        comboTipo.setValue(TipoBilletera.AHORRO);
        TextField campoUmbral = new TextField("50000");

        GridPane formulario = new GridPane();
        formulario.setHgap(16);
        formulario.setVgap(8);
        formulario.addRow(0, new Label("Nombre"), campoNombre);
        formulario.addRow(1, new Label("Tipo"), comboTipo);
        formulario.addRow(2, new Label("Avisar si el saldo baja de"), campoUmbral);
        dialogo.getDialogPane().setContent(formulario);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> respuesta = dialogo.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            try {
                double umbral = UtilidadesVista.leerMonto(campoUmbral.getText());
                Billetera billetera = servicioBilleteras.crear(usuario, campoNombre.getText(), comboTipo.getValue(),
                        umbral);
                exito("Se creó la billetera " + billetera.getCodigo());
            } catch (FintechException error) {
                UtilidadesVista.mostrarError(error.getMessage());
            }
        }
    }

    @FXML
    private void renombrar() {
        Billetera seleccionada = seleccionada();
        if (seleccionada == null) {
            return;
        }
        TextInputDialog dialogo = new TextInputDialog(seleccionada.getNombre());
        dialogo.setTitle("Renombrar billetera");
        dialogo.setHeaderText("Nuevo nombre para " + seleccionada.getCodigo());
        UtilidadesVista.estilizar(dialogo.getDialogPane());
        Optional<String> nombre = dialogo.showAndWait();
        if (nombre.isPresent()) {
            try {
                servicioBilleteras.renombrar(seleccionada.getCodigo(), nombre.get());
                exito("Billetera renombrada");
            } catch (FintechException error) {
                UtilidadesVista.mostrarError(error.getMessage());
            }
        }
    }

    @FXML
    private void bloquear() {
        Billetera seleccionada = seleccionada();
        if (seleccionada != null) {
            try {
                servicioBilleteras.bloquear(seleccionada.getCodigo());
                exito("La billetera " + seleccionada.getCodigo() + " quedó bloqueada");
            } catch (FintechException error) {
                UtilidadesVista.mostrarError(error.getMessage());
            }
        }
    }

    @FXML
    private void desbloquear() {
        Billetera seleccionada = seleccionada();
        if (seleccionada != null) {
            try {
                servicioBilleteras.desbloquear(seleccionada.getCodigo());
                exito("La billetera " + seleccionada.getCodigo() + " está activa otra vez");
            } catch (FintechException error) {
                UtilidadesVista.mostrarError(error.getMessage());
            }
        }
    }

    @FXML
    private void cerrar() {
        Billetera seleccionada = seleccionada();
        if (seleccionada != null && UtilidadesVista.confirmar("¿Seguro que quieres cerrar la billetera "
                + seleccionada.getNombre() + "? Una billetera cerrada no se puede volver a abrir.")) {
            try {
                servicioBilleteras.cerrar(seleccionada.getCodigo());
                exito("La billetera " + seleccionada.getCodigo() + " quedó cerrada");
            } catch (FintechException error) {
                UtilidadesVista.mostrarError(error.getMessage());
            }
        }
    }

    private void verHistorial(Billetera billetera) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Historial de " + billetera.getNombre());
        dialogo.setHeaderText(billetera.getNombre() + " (" + billetera.getCodigo() + ") · Saldo "
                + UtilidadesVista.pesos(billetera.getSaldo()));
        UtilidadesVista.estilizar(dialogo.getDialogPane());

        TableView<Transaccion> tabla = new TableView<>();
        UtilidadesVista.configurarTablaTransacciones(tabla);
        tabla.setItems(UtilidadesVista.aObservableInverso(billetera.getHistorial()));
        tabla.setPrefSize(1000, 480);

        dialogo.getDialogPane().setContent(tabla);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialogo.showAndWait();
    }

    private Billetera seleccionada() {
        Billetera billetera = tablaBilleteras.getSelectionModel().getSelectedItem();
        if (billetera == null) {
            UtilidadesVista.mostrarError("Primero selecciona una billetera de la tabla");
        }
        return billetera;
    }

    private void exito(String mensaje) {
        etiquetaMensaje.getStyleClass().setAll("label", "mensaje-exito");
        etiquetaMensaje.setText(mensaje);
        cargar();
        PrincipalClienteControlador.refrescarBarra();
    }
}
