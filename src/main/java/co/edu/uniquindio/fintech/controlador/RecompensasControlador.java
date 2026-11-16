package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Beneficio;
import co.edu.uniquindio.fintech.modelo.CanjeBeneficio;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.servicio.ServicioRecompensas;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Catalogo de beneficios con su boton de canjear (bloqueado si no alcanza el
 * nivel o los puntos) y la tabla de canjes que ya hizo el usuario.
 */
public class RecompensasControlador {

    @FXML
    private Label etiquetaPuntos;
    @FXML
    private Label etiquetaAcumulados;
    @FXML
    private Label etiquetaNivel;
    @FXML
    private FlowPane panelCatalogo;
    @FXML
    private Label etiquetaMensaje;
    @FXML
    private TableView<CanjeBeneficio> tablaCanjes;

    private ServicioRecompensas servicioRecompensas;
    private Usuario usuario;

    @FXML
    private void initialize() {
        servicioRecompensas = Plataforma.getInstancia().getServicioRecompensas();
        usuario = Sesion.getUsuarioActual();

        UtilidadesVista.columna(tablaCanjes, "Beneficio", canje -> canje.getBeneficio().getNombre());
        UtilidadesVista.columna(tablaCanjes, "Fecha", canje -> UtilidadesVista.fecha(canje.getFecha()));
        UtilidadesVista.columna(tablaCanjes, "Puntos usados", canje -> String.valueOf(canje.getPuntosUsados()));
        UtilidadesVista.columna(tablaCanjes, "Usos restantes", canje -> String.valueOf(canje.getUsosRestantes()));
        tablaCanjes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaCanjes.setPlaceholder(new Label("Todavía no has canjeado beneficios"));
        cargar();
    }

    private void cargar() {
        etiquetaPuntos.setText(String.valueOf(usuario.getPuntosDisponibles()));
        etiquetaAcumulados.setText(String.valueOf(usuario.getPuntosAcumulados()));
        etiquetaNivel.setText(usuario.getNivel().getTexto());
        etiquetaNivel.getStyleClass().setAll("label", "etiqueta", UtilidadesVista.clase("nivel", usuario.getNivel()));

        panelCatalogo.getChildren().clear();
        for (Beneficio beneficio : servicioRecompensas.getCatalogo()) {
            panelCatalogo.getChildren().add(crearTarjeta(beneficio));
        }
        tablaCanjes.setItems(UtilidadesVista.aObservableInverso(usuario.getCanjes()));
        tablaCanjes.refresh();
    }

    private VBox crearTarjeta(Beneficio beneficio) {
        Label nombre = new Label(beneficio.getNombre());
        nombre.getStyleClass().add("subtitulo");
        nombre.setWrapText(true);

        Label descripcion = new Label(beneficio.getDescripcion());
        descripcion.getStyleClass().add("texto-secundario");
        descripcion.setWrapText(true);

        HBox requisitos = new HBox(8, new Label(beneficio.getCostoPuntos() + " puntos"),
                UtilidadesVista.etiqueta("Desde " + beneficio.getNivelMinimo().getTexto(),
                        UtilidadesVista.clase("nivel", beneficio.getNivelMinimo())));

        Button botonCanjear = new Button("Canjear");
        botonCanjear.getStyleClass().add("boton-principal");
        botonCanjear.setMaxWidth(Double.MAX_VALUE);
        botonCanjear.setDisable(!servicioRecompensas.puedeCanjear(usuario, beneficio));
        botonCanjear.setOnAction(evento -> canjear(beneficio));

        Label motivo = new Label(motivoBloqueo(beneficio));
        motivo.getStyleClass().add("texto-secundario");
        motivo.setWrapText(true);

        VBox tarjeta = new VBox(8, nombre, descripcion, requisitos, botonCanjear, motivo);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.setPrefWidth(240);
        return tarjeta;
    }

    // Explica por que no se puede canjear, asi el usuario no se queda con la duda
    private String motivoBloqueo(Beneficio beneficio) {
        if (!usuario.getNivel().esIgualOMayorQue(beneficio.getNivelMinimo())) {
            return "Necesitas nivel " + beneficio.getNivelMinimo().getTexto();
        }
        int faltan = beneficio.getCostoPuntos() - usuario.getPuntosDisponibles();
        if (faltan > 0) {
            return "Te faltan " + faltan + " puntos";
        }
        return "";
    }

    private void canjear(Beneficio beneficio) {
        if (!UtilidadesVista.confirmar("¿Canjear \"" + beneficio.getNombre() + "\" por " + beneficio.getCostoPuntos()
                + " puntos?")) {
            return;
        }
        try {
            servicioRecompensas.canjear(usuario, beneficio.getCodigo());
            etiquetaMensaje.getStyleClass().setAll("label", "mensaje-exito");
            etiquetaMensaje.setText("¡Listo! Canjeaste " + beneficio.getNombre());
        } catch (FintechException error) {
            etiquetaMensaje.getStyleClass().setAll("label", "mensaje-error");
            etiquetaMensaje.setText(error.getMessage());
        }
        cargar();
        PrincipalClienteControlador.refrescarBarra();
    }
}
