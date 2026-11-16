package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.TipoBilletera;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.servicio.ElementoRanking;
import co.edu.uniquindio.fintech.servicio.ServicioAnalitica;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;

/**
 * Tablero de analitica: tarjetas con los totales, monto y usuario mas activo
 * en un rango de fechas, y las graficas de barras.
 */
public class AnaliticaControlador {

    private static final int TOP_BILLETERAS = 8;
    private static final int DIAS_POR_DEFECTO = 30;

    @FXML
    private Label etiquetaUsuarios;
    @FXML
    private Label etiquetaBilleteras;
    @FXML
    private Label etiquetaTransacciones;
    @FXML
    private Label etiquetaMonto;
    @FXML
    private DatePicker campoDesde;
    @FXML
    private DatePicker campoHasta;
    @FXML
    private Label etiquetaMontoRango;
    @FXML
    private Label etiquetaUsuarioActivo;
    @FXML
    private BarChart<String, Number> graficaBilleteras;
    @FXML
    private BarChart<Number, String> graficaCategorias;
    @FXML
    private BarChart<String, Number> graficaTipos;

    private Plataforma plataforma;
    private ServicioAnalitica analitica;

    @FXML
    private void initialize() {
        plataforma = Plataforma.getInstancia();
        analitica = plataforma.getServicioAnalitica();

        etiquetaUsuarios.setText(String.valueOf(plataforma.getRepositorioUsuarios().cantidad()));
        etiquetaBilleteras.setText(String.valueOf(plataforma.getRepositorioBilleteras().cantidad()));
        etiquetaTransacciones.setText(String.valueOf(analitica.cantidadTransaccionesExitosas()));
        etiquetaMonto.setText(UtilidadesVista.pesos(analitica.montoTotalMovilizado()));

        LocalDate hoy = plataforma.getReloj().hoy();
        campoDesde.setValue(hoy.minusDays(DIAS_POR_DEFECTO));
        campoHasta.setValue(hoy);
        calcularRango();

        llenarGraficaBilleteras();
        llenarGraficaCategorias();
        llenarGraficaTipos();
    }

    @FXML
    private void calcularRango() {
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

        etiquetaMontoRango.setText("Monto movilizado en el período: "
                + UtilidadesVista.pesos(analitica.montoMovilizado(desde, hasta)));
        ElementoRanking<Usuario> masActivo = analitica.usuarioMasActivo(desde, hasta);
        if (masActivo == null) {
            etiquetaUsuarioActivo.setText("No hubo movimientos en ese período");
        } else {
            etiquetaUsuarioActivo.setText("Usuario más activo: " + masActivo.getElemento().getNombre() + " con "
                    + masActivo.getCantidad() + " transacciones");
        }
    }

    private void llenarGraficaBilleteras() {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (ElementoRanking<Billetera> elemento : analitica.billeterasMasUsadas(TOP_BILLETERAS)) {
            Billetera billetera = elemento.getElemento();
            serie.getData().add(new XYChart.Data<>(billetera.getCodigo() + "\n"
                    + billetera.getPropietario().getNombre().split(" ")[0], elemento.getCantidad()));
        }
        graficaBilleteras.getData().setAll(serie);
    }

    private void llenarGraficaCategorias() {
        // Las barras horizontales se pintan de abajo hacia arriba, asi que se agregan
        // al reves para que la mas activa quede arriba
        XYChart.Series<Number, String> serie = new XYChart.Series<>();
        Iterator<ElementoRanking<TipoBilletera>> iterador = analitica.categoriasMasActivas().iteradorInverso();
        while (iterador.hasNext()) {
            ElementoRanking<TipoBilletera> elemento = iterador.next();
            serie.getData().add(new XYChart.Data<>(elemento.getCantidad(), elemento.getElemento().getTexto()));
        }
        graficaCategorias.getData().setAll(serie);
    }

    private void llenarGraficaTipos() {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (ElementoRanking<TipoTransaccion> elemento : analitica.frecuenciaPorTipo()) {
            serie.getData().add(new XYChart.Data<>(elemento.getElemento().getTexto(), elemento.getCantidad()));
        }
        graficaTipos.getData().setAll(serie);
    }
}
