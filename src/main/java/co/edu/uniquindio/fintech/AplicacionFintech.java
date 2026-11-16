package co.edu.uniquindio.fintech;

import co.edu.uniquindio.fintech.util.NavegadorVistas;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Arranque de la aplicacion: prepara la plataforma y muestra el login.
 */
public class AplicacionFintech extends Application {

    @Override
    public void start(Stage ventana) {
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
