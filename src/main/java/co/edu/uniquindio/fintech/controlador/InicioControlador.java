package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelUsuario;
import co.edu.uniquindio.fintech.servicio.ServicioTransacciones;
import co.edu.uniquindio.fintech.util.Sesion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;

import java.util.Iterator;

/**
 * Resumen del cliente: saldo total, puntos, nivel con su barra de progreso,
 * transacciones de hoy contra el limite y los ultimos 5 movimientos.
 */
public class InicioControlador {

    private static final int ULTIMOS_MOVIMIENTOS = 5;

    @FXML
    private Label etiquetaSaludo;
    @FXML
    private Label etiquetaSaldo;
    @FXML
    private Label etiquetaPuntos;
    @FXML
    private Label etiquetaTransaccionesHoy;
    @FXML
    private Label etiquetaNivel;
    @FXML
    private ProgressBar barraNivel;
    @FXML
    private Label etiquetaProgreso;
    @FXML
    private Label etiquetaBeneficiosNivel;
    @FXML
    private TableView<Transaccion> tablaMovimientos;

    @FXML
    private void initialize() {
        Usuario usuario = Sesion.getUsuarioActual();
        ServicioTransacciones transacciones = Plataforma.getInstancia().getServicioTransacciones();
        transacciones.actualizarDia(usuario);

        etiquetaSaludo.setText("Hola, " + usuario.getNombre());
        etiquetaSaldo.setText(UtilidadesVista.pesos(usuario.saldoTotal()));
        etiquetaPuntos.setText(String.valueOf(usuario.getPuntosDisponibles()));

        int limite = transacciones.limiteDiario(usuario);
        String textoLimite = limite == NivelUsuario.SIN_LIMITE ? "sin límite" : String.valueOf(limite);
        etiquetaTransaccionesHoy.setText(usuario.getTransaccionesHoy() + " / " + textoLimite);

        mostrarNivel(usuario);
        mostrarMovimientos(usuario);
    }

    private void mostrarNivel(Usuario usuario) {
        NivelUsuario nivel = usuario.getNivel();
        etiquetaNivel.setText(nivel.getTexto());
        etiquetaNivel.getStyleClass().setAll("label", "etiqueta", UtilidadesVista.clase("nivel", nivel));

        if (nivel == NivelUsuario.PLATINO) {
            barraNivel.setProgress(1);
            etiquetaProgreso.setText("Estás en el nivel más alto con " + usuario.getPuntosAcumulados()
                    + " puntos acumulados");
        } else {
            // Progreso dentro del rango de puntos del nivel actual
            int puntosEnNivel = usuario.getPuntosAcumulados() - nivel.getPuntosMinimos();
            int tamanioNivel = nivel.getPuntosMaximos() - nivel.getPuntosMinimos() + 1;
            barraNivel.setProgress((double) puntosEnNivel / tamanioNivel);

            NivelUsuario siguiente = NivelUsuario.values()[nivel.ordinal() + 1];
            int faltan = siguiente.getPuntosMinimos() - usuario.getPuntosAcumulados();
            etiquetaProgreso.setText(usuario.getPuntosAcumulados() + " puntos acumulados · te faltan " + faltan
                    + " para " + siguiente.getTexto());
        }

        String limite = nivel.esIlimitado() ? "sin límite" : nivel.getLimiteTransaccionesDiarias() + " por día";
        etiquetaBeneficiosNivel.setText("Comisión " + nivel.getPorcentajeComision() + " % · Transacciones: "
                + limite + " · Bono de puntos: +" + (int) nivel.getBonoPuntos() + " %");
    }

    private void mostrarMovimientos(Usuario usuario) {
        UtilidadesVista.configurarTablaTransacciones(tablaMovimientos);
        ObservableList<Transaccion> ultimos = FXCollections.observableArrayList();
        Iterator<Transaccion> iterador = usuario.getHistorial().iteradorInverso();
        while (iterador.hasNext() && ultimos.size() < ULTIMOS_MOVIMIENTOS) {
            ultimos.add(iterador.next());
        }
        tablaMovimientos.setItems(ultimos);
    }
}
