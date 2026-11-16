package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.modelo.Notificacion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.servicio.ServicioNotificaciones;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Lista de notificaciones del cliente, la mas nueva arriba. Las que no se han
 * leido salen resaltadas con una franja azul a la izquierda.
 */
public class NotificacionesControlador {

    @FXML
    private ListView<Notificacion> listaNotificaciones;

    private ServicioNotificaciones servicioNotificaciones;
    private Usuario usuario;

    @FXML
    private void initialize() {
        servicioNotificaciones = Plataforma.getInstancia().getServicioNotificaciones();
        usuario = Sesion.getUsuarioActual();
        listaNotificaciones.setPlaceholder(new Label("No tienes notificaciones"));
        listaNotificaciones.setCellFactory(lista -> new CeldaNotificacion());
        cargar();
    }

    @FXML
    private void marcarLeida() {
        Notificacion seleccionada = listaNotificaciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            UtilidadesVista.mostrarError("Selecciona una notificación");
            return;
        }
        servicioNotificaciones.marcarLeida(seleccionada);
        cargar();
    }

    @FXML
    private void marcarTodas() {
        servicioNotificaciones.marcarTodasLeidas(usuario);
        cargar();
    }

    private void cargar() {
        listaNotificaciones.setItems(UtilidadesVista.aObservable(servicioNotificaciones.recientes(usuario)));
        listaNotificaciones.refresh();
        PrincipalClienteControlador.refrescarBarra();
    }

    /** Celda con icono, tipo, mensaje y fecha. */
    private static class CeldaNotificacion extends ListCell<Notificacion> {

        @Override
        protected void updateItem(Notificacion notificacion, boolean vacia) {
            super.updateItem(notificacion, vacia);
            getStyleClass().remove("notificacion-no-leida");
            setText(null);
            if (vacia || notificacion == null) {
                setGraphic(null);
                return;
            }

            Label icono = new Label(UtilidadesVista.iconoNotificacion(notificacion.getTipo()));
            icono.getStyleClass().add("icono-notificacion");

            Label titulo = new Label(notificacion.getTipo().getTexto());
            titulo.getStyleClass().add("titulo-notificacion");
            Label mensaje = new Label(notificacion.getMensaje());
            mensaje.setWrapText(true);
            Label fecha = new Label(UtilidadesVista.fecha(notificacion.getFecha())
                    + (notificacion.isLeida() ? "  ·  Leída" : "  ·  Sin leer"));
            fecha.getStyleClass().add("texto-secundario");

            VBox textos = new VBox(2, titulo, mensaje, fecha);
            HBox.setHgrow(textos, Priority.ALWAYS);
            textos.setMaxWidth(Double.MAX_VALUE);
            textos.prefWidthProperty().bind(getListView().widthProperty().subtract(96));

            setGraphic(new HBox(12, icono, textos));
            if (!notificacion.isLeida()) {
                getStyleClass().add("notificacion-no-leida");
            }
        }
    }
}
