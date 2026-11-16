package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.servicio.ServicioTransacciones;
import co.edu.uniquindio.fintech.util.Sesion;
import co.edu.uniquindio.fintech.util.Validador;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Recargar, retirar y transferir. Mientras se escribe el monto se muestra
 * una vista previa de la comision y los puntos que se ganarian.
 */
public class OperacionesControlador {

    @FXML
    private ComboBox<Billetera> recargaDestino;
    @FXML
    private TextField recargaMonto;
    @FXML
    private TextField recargaDescripcion;
    @FXML
    private Label recargaVista;

    @FXML
    private ComboBox<Billetera> retiroOrigen;
    @FXML
    private TextField retiroMonto;
    @FXML
    private TextField retiroDescripcion;
    @FXML
    private Label retiroVista;

    @FXML
    private RadioButton opcionInterna;
    @FXML
    private RadioButton opcionExterna;
    @FXML
    private ComboBox<Billetera> transferenciaOrigen;
    @FXML
    private VBox cajaDestinoPropia;
    @FXML
    private ComboBox<Billetera> transferenciaDestinoPropia;
    @FXML
    private VBox cajaDestinoCodigo;
    @FXML
    private TextField transferenciaDestinoCodigo;
    @FXML
    private Label transferenciaDueno;
    @FXML
    private TextField transferenciaMonto;
    @FXML
    private TextField transferenciaDescripcion;
    @FXML
    private Label transferenciaVista;

    @FXML
    private Label etiquetaMensaje;

    private ServicioTransacciones transacciones;
    private Usuario usuario;

    @FXML
    private void initialize() {
        transacciones = Plataforma.getInstancia().getServicioTransacciones();
        usuario = Sesion.getUsuarioActual();
        cargarBilleteras();

        // Vistas previas que se recalculan con cada tecla
        recargaMonto.textProperty().addListener((observable, anterior, nuevo) ->
                recargaVista.setText(vistaPrevia(TipoTransaccion.RECARGA, nuevo)));
        retiroMonto.textProperty().addListener((observable, anterior, nuevo) ->
                retiroVista.setText(vistaPrevia(TipoTransaccion.RETIRO, nuevo)));
        transferenciaMonto.textProperty().addListener((observable, anterior, nuevo) -> actualizarVistaTransferencia());
        opcionInterna.selectedProperty().addListener((observable, anterior, nuevo) -> cambiarTipoTransferencia());
        transferenciaDestinoCodigo.textProperty().addListener((observable, anterior, nuevo) -> mostrarDueno(nuevo));
        cambiarTipoTransferencia();
    }

    private void cargarBilleteras() {
        ObservableList<Billetera> activas = UtilidadesVista.aObservable(
                Plataforma.getInstancia().getServicioBilleteras().activasDe(usuario));
        recargaDestino.setItems(activas);
        retiroOrigen.setItems(activas);
        transferenciaOrigen.setItems(activas);
        transferenciaDestinoPropia.setItems(activas);
    }

    @FXML
    private void recargar() {
        try {
            Transaccion transaccion = transacciones.recargar(usuario, codigo(recargaDestino),
                    UtilidadesVista.leerMonto(recargaMonto.getText()), recargaDescripcion.getText());
            exito("Recarga exitosa", transaccion);
            recargaMonto.clear();
            recargaDescripcion.clear();
        } catch (FintechException error) {
            fallo(error);
        }
    }

    @FXML
    private void retirar() {
        try {
            Transaccion transaccion = transacciones.retirar(usuario, codigo(retiroOrigen),
                    UtilidadesVista.leerMonto(retiroMonto.getText()), retiroDescripcion.getText());
            exito("Retiro exitoso", transaccion);
            retiroMonto.clear();
            retiroDescripcion.clear();
        } catch (FintechException error) {
            fallo(error);
        }
    }

    @FXML
    private void transferir() {
        try {
            double monto = UtilidadesVista.leerMonto(transferenciaMonto.getText());
            Transaccion transaccion;
            if (opcionInterna.isSelected()) {
                transaccion = transacciones.transferirInterna(usuario, codigo(transferenciaOrigen),
                        codigo(transferenciaDestinoPropia), monto, transferenciaDescripcion.getText());
            } else {
                transaccion = transacciones.transferirExterna(usuario, codigo(transferenciaOrigen),
                        transferenciaDestinoCodigo.getText().trim().toUpperCase(), monto,
                        transferenciaDescripcion.getText());
            }
            exito("Transferencia exitosa", transaccion);
            transferenciaMonto.clear();
            transferenciaDescripcion.clear();
        } catch (FintechException error) {
            fallo(error);
        }
    }

    private void cambiarTipoTransferencia() {
        boolean interna = opcionInterna.isSelected();
        cajaDestinoPropia.setVisible(interna);
        cajaDestinoPropia.setManaged(interna);
        cajaDestinoCodigo.setVisible(!interna);
        cajaDestinoCodigo.setManaged(!interna);
        actualizarVistaTransferencia();
    }

    private void actualizarVistaTransferencia() {
        TipoTransaccion tipo = opcionInterna.isSelected()
                ? TipoTransaccion.TRANSFERENCIA_INTERNA : TipoTransaccion.TRANSFERENCIA_EXTERNA;
        transferenciaVista.setText(vistaPrevia(tipo, transferenciaMonto.getText()));
    }

    // Muestra de quien es la billetera destino para que el usuario confirme que es la correcta
    private void mostrarDueno(String codigo) {
        Billetera destino = Plataforma.getInstancia().getRepositorioBilleteras().buscar(codigo.trim().toUpperCase());
        if (destino == null) {
            transferenciaDueno.setText(codigo.isBlank() ? "" : "No encontramos esa billetera");
        } else {
            transferenciaDueno.setText("Le llegará a " + destino.getPropietario().getNombre()
                    + " (" + destino.getNombre() + ")");
        }
    }

    private String vistaPrevia(TipoTransaccion tipo, String textoMonto) {
        if (Validador.estaVacio(textoMonto)) {
            return "";
        }
        try {
            double monto = UtilidadesVista.leerMonto(textoMonto);
            double comision = transacciones.calcularComision(usuario, tipo, monto);
            int puntos = transacciones.estimarPuntos(usuario, tipo, monto);
            String texto = "Comisión: " + UtilidadesVista.pesos(comision) + "  ·  Puntos que ganarías: " + puntos;
            if (tipo != TipoTransaccion.RECARGA) {
                texto = texto + "  ·  Total a descontar: " + UtilidadesVista.pesos(monto + comision);
            }
            return texto;
        } catch (FintechException error) {
            return error.getMessage();
        }
    }

    private String codigo(ComboBox<Billetera> combo) {
        if (combo.getValue() == null) {
            return null;
        }
        return combo.getValue().getCodigo();
    }

    private void exito(String titulo, Transaccion transaccion) {
        String mensaje = titulo + " (" + transaccion.getId() + "). Ganaste " + transaccion.getPuntosGenerados()
                + " puntos.";
        if (transaccion.getNivelRiesgo() != NivelRiesgo.NINGUNO) {
            mensaje = mensaje + " Quedó marcada con riesgo " + transaccion.getNivelRiesgo().getTexto().toLowerCase()
                    + ", revisa tus notificaciones.";
        }
        etiquetaMensaje.getStyleClass().setAll("label", "mensaje-exito");
        etiquetaMensaje.setText(mensaje);
        cargarBilleteras();
        PrincipalClienteControlador.refrescarBarra();
    }

    private void fallo(FintechException error) {
        etiquetaMensaje.getStyleClass().setAll("label", "mensaje-error");
        etiquetaMensaje.setText(error.getMessage());
        PrincipalClienteControlador.refrescarBarra();
    }
}
