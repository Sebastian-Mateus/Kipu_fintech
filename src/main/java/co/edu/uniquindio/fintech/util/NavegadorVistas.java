package co.edu.uniquindio.fintech.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Se encarga de cambiar de pantalla. Hay una sola escena: para ir a otra
 * pantalla completa (login, registro, principal) se cambia la raiz, y dentro
 * de la pantalla principal se cambia solo el contenido del centro.
 */
public class NavegadorVistas {

    private static final String RUTA_VISTAS = "/vistas/";
    private static final String HOJA_ESTILOS = "/estilos/estilos.css";
    private static final double ANCHO = 1280;
    private static final double ALTO = 800;

    private static Stage escenario;
    private static Scene escena;
    private static StackPane contenedor;

    private NavegadorVistas() {
        // Solo tiene metodos estaticos
    }

    /** Crea la escena con la hoja de estilos y la pone en la ventana. */
    public static void inicializar(Stage ventana) {
        escenario = ventana;
        escena = new Scene(new StackPane(), ANCHO, ALTO);
        escena.getStylesheets().add(getHojaEstilos());
        escenario.setScene(escena);
    }

    /** Cambia toda la pantalla por la vista indicada (sin el .fxml). Devuelve su controlador. */
    public static Object irA(String nombreVista) {
        FXMLLoader cargador = crearCargador(nombreVista);
        escena.setRoot(cargar(cargador, nombreVista));
        return cargador.getController();
    }

    /** Pone la vista indicada en el centro de la pantalla principal. Devuelve su controlador. */
    public static Object mostrarEnContenido(String nombreVista) {
        FXMLLoader cargador = crearCargador(nombreVista);
        contenedor.getChildren().setAll(cargar(cargador, nombreVista));
        return cargador.getController();
    }

    /** La pantalla principal avisa cual es su panel del centro. */
    public static void setContenedor(StackPane panelCentral) {
        contenedor = panelCentral;
    }

    /** Ruta de la hoja de estilos, para los dialogos que se abren aparte. */
    public static String getHojaEstilos() {
        return NavegadorVistas.class.getResource(HOJA_ESTILOS).toExternalForm();
    }

    public static Stage getEscenario() {
        return escenario;
    }

    private static FXMLLoader crearCargador(String nombreVista) {
        URL ruta = NavegadorVistas.class.getResource(RUTA_VISTAS + nombreVista + ".fxml");
        if (ruta == null) {
            throw new IllegalArgumentException("No existe la vista " + nombreVista);
        }
        return new FXMLLoader(ruta);
    }

    private static Parent cargar(FXMLLoader cargador, String nombreVista) {
        try {
            return cargador.load();
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo cargar la vista " + nombreVista, error);
        }
    }
}
