package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.estructuras.Arista;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.servicio.ServicioAnalitica;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

/**
 * Lo que sale del grafo de usuarios, mostrado solo con tablas y listas:
 * rutas mas frecuentes, ciclos, caminos entre dos usuarios y todas las relaciones.
 */
public class RedTransferenciasControlador {

    private static final int TOP_RUTAS = 10;

    @FXML
    private TableView<Arista<Usuario>> tablaRutas;
    @FXML
    private ListView<String> listaCiclos;
    @FXML
    private ComboBox<Usuario> comboOrigen;
    @FXML
    private ComboBox<Usuario> comboDestino;
    @FXML
    private Label etiquetaCamino;
    @FXML
    private TableView<Arista<Usuario>> tablaRelaciones;

    private ServicioAnalitica analitica;

    @FXML
    private void initialize() {
        Plataforma plataforma = Plataforma.getInstancia();
        analitica = plataforma.getServicioAnalitica();

        configurarTablaAristas(tablaRutas);
        configurarTablaAristas(tablaRelaciones);
        tablaRutas.setItems(UtilidadesVista.aObservable(analitica.rutasMasFrecuentesUsuarios(TOP_RUTAS)));
        tablaRelaciones.setItems(UtilidadesVista.aObservable(analitica.transferenciasEntreUsuarios()));

        ObservableList<String> ciclos = FXCollections.observableArrayList();
        for (ListaEnlazada<Usuario> ciclo : analitica.ciclosEntreUsuarios()) {
            ciclos.add(unirNombres(ciclo));
        }
        listaCiclos.setItems(ciclos);
        listaCiclos.setPlaceholder(new Label("No se encontraron ciclos"));

        ObservableList<Usuario> usuarios = UtilidadesVista.aObservable(plataforma.getServicioUsuarios().listar());
        comboOrigen.setItems(usuarios);
        comboDestino.setItems(usuarios);
    }

    @FXML
    private void buscarCamino() {
        Usuario origen = comboOrigen.getValue();
        Usuario destino = comboDestino.getValue();
        if (origen == null || destino == null) {
            UtilidadesVista.mostrarError("Elige los dos usuarios");
            return;
        }
        ListaEnlazada<Usuario> camino = analitica.buscarCamino(origen, destino);
        if (camino.estaVacia()) {
            etiquetaCamino.getStyleClass().setAll("label", "mensaje-error");
            etiquetaCamino.setText("No hay ningún camino de transferencias de " + origen.getNombre() + " a "
                    + destino.getNombre());
        } else {
            etiquetaCamino.getStyleClass().setAll("label", "mensaje-exito");
            etiquetaCamino.setText("Camino más corto (" + (camino.tamanio() - 1) + " saltos): " + unirNombres(camino));
        }
    }

    private void configurarTablaAristas(TableView<Arista<Usuario>> tabla) {
        UtilidadesVista.columna(tabla, "De", arista -> arista.getOrigen().getNombre());
        UtilidadesVista.columna(tabla, "Para", arista -> arista.getDestino().getNombre());
        UtilidadesVista.columna(tabla, "Transferencias", arista -> String.valueOf(arista.getCantidadTransferencias()));
        UtilidadesVista.columna(tabla, "Monto total", arista -> UtilidadesVista.pesos(arista.getMontoTotal()));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("Todavía no hay transferencias entre usuarios"));
    }

    private String unirNombres(ListaEnlazada<Usuario> usuarios) {
        StringBuilder texto = new StringBuilder();
        for (Usuario usuario : usuarios) {
            if (texto.length() > 0) {
                texto.append("  →  ");
            }
            texto.append(usuario.getNombre());
        }
        return texto.toString();
    }
}
