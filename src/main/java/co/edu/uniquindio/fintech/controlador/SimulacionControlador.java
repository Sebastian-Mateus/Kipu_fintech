package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.modelo.OperacionProgramada;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Aqui el administrador adelanta el reloj. Cada vez que lo mueve se ejecutan
 * las operaciones programadas que se vencieron (no hay modo automatico).
 */
public class SimulacionControlador {

    private static final DateTimeFormatter FORMATO_LARGO =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy, HH:mm", Locale.forLanguageTag("es-CO"));

    @FXML
    private Label etiquetaFecha;
    @FXML
    private Label etiquetaResultado;
    @FXML
    private TableView<OperacionProgramada> tablaProgramadas;
    @FXML
    private ListView<String> listaBitacora;

    private Plataforma plataforma;

    @FXML
    private void initialize() {
        plataforma = Plataforma.getInstancia();

        UtilidadesVista.columna(tablaProgramadas, "ID", operacion -> operacion.getId());
        UtilidadesVista.columna(tablaProgramadas, "Usuario", operacion -> operacion.getUsuario().getNombre());
        UtilidadesVista.columna(tablaProgramadas, "Descripción",
                operacion -> UtilidadesVista.textoSeguro(operacion.getDescripcion()));
        UtilidadesVista.columna(tablaProgramadas, "Valor", operacion -> UtilidadesVista.pesos(operacion.getValor()));
        UtilidadesVista.columna(tablaProgramadas, "Próxima ejecución",
                operacion -> UtilidadesVista.fecha(operacion.getFechaProximaEjecucion()));
        tablaProgramadas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaProgramadas.setPlaceholder(new Label("No hay operaciones programadas activas"));
        listaBitacora.setPlaceholder(new Label("Todavía no se ha ejecutado ninguna programada"));
        refrescar();
    }

    @FXML
    private void avanzarHora() {
        mostrarResultado(plataforma.avanzarHora(), "1 hora");
    }

    @FXML
    private void avanzarDia() {
        mostrarResultado(plataforma.avanzarDia(), "1 día");
    }

    @FXML
    private void avanzarSemana() {
        mostrarResultado(plataforma.avanzarSemana(), "1 semana");
    }

    private void mostrarResultado(int procesadas, String cuanto) {
        etiquetaResultado.setText("El reloj avanzó " + cuanto + ". Se procesaron " + procesadas
                + " operaciones programadas.");
        refrescar();
        PrincipalAdministradorControlador.refrescarBarra();
    }

    private void refrescar() {
        etiquetaFecha.setText(plataforma.getReloj().ahora().format(FORMATO_LARGO));
        tablaProgramadas.setItems(UtilidadesVista.aObservable(plataforma.getServicioProgramadas().activas()));
        tablaProgramadas.getItems().sort((primera, segunda) ->
                primera.getFechaProximaEjecucion().compareTo(segunda.getFechaProximaEjecucion()));
        listaBitacora.setItems(UtilidadesVista.aObservableInverso(plataforma.getServicioProgramadas().getBitacora()));
    }
}
