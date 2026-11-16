package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.modelo.ResultadoRendimiento;
import co.edu.uniquindio.fintech.servicio.ServicioRendimiento;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Corre las comparaciones de rendimiento y muestra los tiempos en una tabla y
 * en una grafica de lineas. Las pruebas se corren en otro hilo para que la
 * ventana no se congele mientras tanto.
 */
public class RendimientoControlador {

    private static final String BUSQUEDA = "Búsqueda por id";
    private static final String TOP = "Top 10 de mayor valor";
    private static final String ARBOL = "ABB ordenado vs aleatorio";

    @FXML
    private CheckBox opcionMil;
    @FXML
    private CheckBox opcionDiezMil;
    @FXML
    private CheckBox opcionCienMil;
    @FXML
    private Button botonEjecutar;
    @FXML
    private ProgressIndicator indicadorProgreso;
    @FXML
    private Label etiquetaEstado;
    @FXML
    private TableView<ResultadoRendimiento> tablaResultados;
    @FXML
    private ComboBox<String> comboComparacion;
    @FXML
    private LineChart<String, Number> graficaTiempos;

    private ListaEnlazada<ResultadoRendimiento> resultados;
    private NumberFormat formatoNumero;

    @FXML
    private void initialize() {
        resultados = new ListaEnlazada<>();
        formatoNumero = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"));

        UtilidadesVista.columna(tablaResultados, "Elementos",
                resultado -> formatoNumero.format(resultado.getCantidadElementos()));
        UtilidadesVista.columna(tablaResultados, "Estructura", resultado -> resultado.getEstructura());
        UtilidadesVista.columna(tablaResultados, "Operación", resultado -> resultado.getOperacion());
        UtilidadesVista.columna(tablaResultados, "Tiempo (ms)",
                resultado -> String.format(Locale.forLanguageTag("es-CO"), "%,.3f", resultado.getTiempoMilisegundos()));
        tablaResultados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaResultados.setPlaceholder(new Label("Elige los tamaños y pulsa \"Ejecutar comparación\""));

        comboComparacion.getItems().addAll(BUSQUEDA, TOP, ARBOL);
        comboComparacion.setValue(BUSQUEDA);
        comboComparacion.valueProperty().addListener((observable, anterior, nuevo) -> dibujarGrafica());
    }

    @FXML
    private void ejecutar() {
        int[] tamanios = tamaniosElegidos();
        if (tamanios.length == 0) {
            UtilidadesVista.mostrarError("Elige por lo menos una cantidad de elementos");
            return;
        }

        ServicioRendimiento servicio = Plataforma.getInstancia().getServicioRendimiento();
        Task<ListaEnlazada<ResultadoRendimiento>> tarea = new Task<>() {
            @Override
            protected ListaEnlazada<ResultadoRendimiento> call() {
                ListaEnlazada<ResultadoRendimiento> todos = new ListaEnlazada<>();
                for (int tamanio : tamanios) {
                    updateMessage("Midiendo con " + formatoNumero.format(tamanio) + " elementos...");
                    for (ResultadoRendimiento resultado : servicio.ejecutarComparaciones(tamanio)) {
                        todos.agregarAlFinal(resultado);
                    }
                }
                return todos;
            }
        };

        etiquetaEstado.textProperty().bind(tarea.messageProperty());
        tarea.setOnSucceeded(evento -> {
            terminar();
            resultados = tarea.getValue();
            tablaResultados.setItems(UtilidadesVista.aObservable(resultados));
            dibujarGrafica();
            etiquetaEstado.setText("Listo, " + resultados.tamanio() + " mediciones");
        });
        tarea.setOnFailed(evento -> {
            terminar();
            etiquetaEstado.setText("");
            UtilidadesVista.mostrarError("No se pudo terminar la comparación: " + tarea.getException().getMessage());
        });

        botonEjecutar.setDisable(true);
        indicadorProgreso.setVisible(true);
        Thread hilo = new Thread(tarea, "comparacion-rendimiento");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void terminar() {
        etiquetaEstado.textProperty().unbind();
        botonEjecutar.setDisable(false);
        indicadorProgreso.setVisible(false);
    }

    private int[] tamaniosElegidos() {
        int cantidad = (opcionMil.isSelected() ? 1 : 0) + (opcionDiezMil.isSelected() ? 1 : 0)
                + (opcionCienMil.isSelected() ? 1 : 0);
        int[] tamanios = new int[cantidad];
        int posicion = 0;
        if (opcionMil.isSelected()) {
            tamanios[posicion++] = 1000;
        }
        if (opcionDiezMil.isSelected()) {
            tamanios[posicion++] = 10000;
        }
        if (opcionCienMil.isSelected()) {
            tamanios[posicion] = 100000;
        }
        return tamanios;
    }

    /*
     * Una linea por estructura (y operacion en el caso del arbol). En el eje X va
     * la cantidad de elementos. Como el ABB ordenado se limita a 10.000, si ya hay
     * un punto con esa cantidad no se repite.
     */
    private void dibujarGrafica() {
        graficaTiempos.getData().clear();
        String comparacion = comboComparacion.getValue();
        ListaEnlazada<XYChart.Series<String, Number>> series = new ListaEnlazada<>();

        for (ResultadoRendimiento resultado : resultados) {
            if (!perteneceA(resultado, comparacion)) {
                continue;
            }
            String nombreSerie = nombreBase(resultado.getEstructura());
            if (ARBOL.equals(comparacion)) {
                nombreSerie = nombreSerie + " · " + resultado.getOperacion();
            }
            XYChart.Series<String, Number> serie = buscarSerie(series, nombreSerie);
            String cantidad = formatoNumero.format(resultado.getCantidadElementos());
            if (!tienePunto(serie, cantidad)) {
                serie.getData().add(new XYChart.Data<>(cantidad, resultado.getTiempoMilisegundos()));
            }
        }
        for (XYChart.Series<String, Number> serie : series) {
            graficaTiempos.getData().add(serie);
        }
    }

    private boolean perteneceA(ResultadoRendimiento resultado, String comparacion) {
        boolean esDelArbol = resultado.getEstructura().startsWith("ABB");
        if (ARBOL.equals(comparacion)) {
            return esDelArbol;
        }
        if (TOP.equals(comparacion)) {
            return resultado.getOperacion().startsWith("Top");
        }
        return !esDelArbol && resultado.getOperacion().startsWith("Buscar");
    }

    // "ABB ordenado (altura 1000)" queda "ABB ordenado"
    private String nombreBase(String estructura) {
        int parentesis = estructura.indexOf(" (");
        if (parentesis < 0) {
            return estructura;
        }
        return estructura.substring(0, parentesis);
    }

    private XYChart.Series<String, Number> buscarSerie(ListaEnlazada<XYChart.Series<String, Number>> series,
                                                       String nombre) {
        for (XYChart.Series<String, Number> serie : series) {
            if (serie.getName().equals(nombre)) {
                return serie;
            }
        }
        XYChart.Series<String, Number> nueva = new XYChart.Series<>();
        nueva.setName(nombre);
        series.agregarAlFinal(nueva);
        return nueva;
    }

    private boolean tienePunto(XYChart.Series<String, Number> serie, String cantidad) {
        for (XYChart.Data<String, Number> punto : serie.getData()) {
            if (punto.getXValue().equals(cantidad)) {
                return true;
            }
        }
        return false;
    }
}
