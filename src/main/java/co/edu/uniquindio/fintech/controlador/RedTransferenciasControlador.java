package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.estructuras.Arista;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.servicio.ServicioAnalitica;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

import java.util.function.Function;

/**
 * Lo que sale de los grafos de transferencias, mostrado solo con tablas y listas.
 * Una pestaña es el grafo de usuarios (rutas, ciclos, caminos y relaciones) y
 * la otra el grafo de billeteras.
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
    @FXML
    private TableView<Arista<Billetera>> tablaRutasBilleteras;
    @FXML
    private ListView<String> listaCiclosBilleteras;
    @FXML
    private TableView<Arista<Billetera>> tablaRelacionesBilleteras;

    private ServicioAnalitica analitica;

    @FXML
    private void initialize() {
        Plataforma plataforma = Plataforma.getInstancia();
        analitica = plataforma.getServicioAnalitica();

        // Grafo de usuarios
        configurarTablaAristas(tablaRutas, usuario -> usuario.getNombre());
        configurarTablaAristas(tablaRelaciones, usuario -> usuario.getNombre());
        tablaRutas.setItems(UtilidadesVista.aObservable(analitica.rutasMasFrecuentesUsuarios(TOP_RUTAS)));
        tablaRelaciones.setItems(UtilidadesVista.aObservable(analitica.transferenciasEntreUsuarios()));
        mostrarCiclos(listaCiclos, analitica.ciclosEntreUsuarios(), usuario -> usuario.getNombre());

        ObservableList<Usuario> usuarios = UtilidadesVista.aObservable(plataforma.getServicioUsuarios().listar());
        comboOrigen.setItems(usuarios);
        comboDestino.setItems(usuarios);

        // Grafo de billeteras
        configurarTablaAristas(tablaRutasBilleteras, billetera -> nombreCorto(billetera));
        configurarTablaAristas(tablaRelacionesBilleteras, billetera -> nombreCorto(billetera));
        tablaRutasBilleteras.setItems(UtilidadesVista.aObservable(analitica.rutasMasFrecuentesBilleteras(TOP_RUTAS)));
        tablaRelacionesBilleteras.setItems(UtilidadesVista.aObservable(analitica.transferenciasEntreBilleteras()));
        mostrarCiclos(listaCiclosBilleteras, analitica.ciclosEntreBilleteras(), billetera -> nombreCorto(billetera));
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
            etiquetaCamino.setText("Camino más corto (" + (camino.tamanio() - 1) + " saltos): "
                    + unir(camino, usuario -> usuario.getNombre()));
        }
    }

    private <T> void configurarTablaAristas(TableView<Arista<T>> tabla, Function<T, String> nombre) {
        UtilidadesVista.columna(tabla, "De", arista -> nombre.apply(arista.getOrigen()));
        UtilidadesVista.columna(tabla, "Para", arista -> nombre.apply(arista.getDestino()));
        UtilidadesVista.columna(tabla, "Transferencias", arista -> String.valueOf(arista.getCantidadTransferencias()));
        UtilidadesVista.columna(tabla, "Monto total", arista -> UtilidadesVista.pesos(arista.getMontoTotal()));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("Todavía no hay transferencias"));
    }

    private <T> void mostrarCiclos(ListView<String> lista, ListaEnlazada<ListaEnlazada<T>> ciclos,
                                   Function<T, String> nombre) {
        ObservableList<String> textos = FXCollections.observableArrayList();
        for (ListaEnlazada<T> ciclo : ciclos) {
            textos.add(unir(ciclo, nombre));
        }
        lista.setItems(textos);
        lista.setPlaceholder(new Label("No se encontraron ciclos"));
    }

    private <T> String unir(ListaEnlazada<T> elementos, Function<T, String> nombre) {
        StringBuilder texto = new StringBuilder();
        for (T elemento : elementos) {
            if (texto.length() > 0) {
                texto.append("  →  ");
            }
            texto.append(nombre.apply(elemento));
        }
        return texto.toString();
    }

    // Por ejemplo "BIL-0003 (Laura)"
    private String nombreCorto(Billetera billetera) {
        return billetera.getCodigo() + " (" + billetera.getPropietario().getNombre().split(" ")[0] + ")";
    }
}
