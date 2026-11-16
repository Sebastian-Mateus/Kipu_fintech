package co.edu.uniquindio.fintech;

import co.edu.uniquindio.fintech.util.NavegadorVistas;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Locale;

/**
 * Arranque de la aplicacion: prepara la plataforma y muestra el login.
 */
public class AplicacionFintech extends Application {

    @Override
    public void start(Stage ventana) {
        // Para que los calendarios y numeros salgan en formato colombiano (dd/MM/yyyy)
        Locale.setDefault(Locale.forLanguageTag("es-CO"));
        Plataforma.getInstancia();

        ventana.setTitle("Kipu Fintech");
        ventana.setMinWidth(1100);
        ventana.setMinHeight(700);
        NavegadorVistas.inicializar(ventana);
        NavegadorVistas.irA("login");
        ventana.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
