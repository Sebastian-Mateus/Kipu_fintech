package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.excepciones.OperacionInvalidaException;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.fintech.util.NavegadorVistas;
import co.edu.uniquindio.fintech.util.Validador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Cosas que se repiten en varios controladores: armar columnas de tablas,
 * mostrar mensajes, dar formato a fechas y plata, etc.
 *
 * Las tablas de JavaFX nesecitan una ObservableList para mostrar datos, asi que
 * aqui se pasan nuestras listas a ese formato solo para pintarlas en pantalla.
 */
public class UtilidadesVista {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private UtilidadesVista() {
    }

    public static String fecha(LocalDateTime fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATO_FECHA);
    }

    public static String pesos(double valor) {
        return Validador.formatearPesos(valor);
    }

    /** Copia nuestra lista a una lista de JavaFX en el mismo orden. */
    public static <T> ObservableList<T> aObservable(ListaEnlazada<T> lista) {
        ObservableList<T> resultado = FXCollections.observableArrayList();
        for (T elemento : lista) {
            resultado.add(elemento);
        }
        return resultado;
    }

    /** Copia nuestra lista al reves (lo mas reciente primero). */
    public static <T> ObservableList<T> aObservableInverso(ListaEnlazada<T> lista) {
        ObservableList<T> resultado = FXCollections.observableArrayList();
        Iterator<T> iterador = lista.iteradorInverso();
        while (iterador.hasNext()) {
            resultado.add(iterador.next());
        }
        return resultado;
    }

    /** Agrega una columna de texto a la tabla. */
    public static <T> TableColumn<T, String> columna(TableView<T> tabla, String titulo, Function<T, String> valor) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(celda -> new SimpleStringProperty(valor.apply(celda.getValue())));
        columna.setSortable(false);
        tabla.getColumns().add(columna);
        return columna;
    }

    /** Agrega una columna que muestra una etiqueta de color con texto (estado, nivel, riesgo). */
    public static <T> void columnaEtiqueta(TableView<T> tabla, String titulo, Function<T, String> texto,
                                          Function<T, String> claseEstilo) {
        TableColumn<T, T> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(celda -> new SimpleObjectProperty<>(celda.getValue()));
        columna.setCellFactory(columnaTabla -> new TableCell<T, T>() {
            @Override
            protected void updateItem(T elemento, boolean vacia) {
                super.updateItem(elemento, vacia);
                setText(null);
                if (vacia || elemento == null) {
                    setGraphic(null);
                } else {
                    setGraphic(etiqueta(texto.apply(elemento), claseEstilo.apply(elemento)));
                }
            }
        });
        columna.setSortable(false);
        tabla.getColumns().add(columna);
    }

    /** Etiqueta redondeada con color de fondo. */
    public static Label etiqueta(String texto, String claseEstilo) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().addAll("etiqueta", claseEstilo);
        return etiqueta;
    }

    /** Clase de CSS para un enum, por ejemplo NivelUsuario.ORO queda "nivel-oro". */
    public static String clase(String prefijo, Enum<?> valor) {
        return prefijo + "-" + valor.name().toLowerCase();
    }

    /** Columnas de siempre para mostrar transacciones. */
    public static void configurarTablaTransacciones(TableView<Transaccion> tabla) {
        tabla.getColumns().clear();
        columna(tabla, "ID", transaccion -> transaccion.getId());
        columna(tabla, "Fecha", transaccion -> fecha(transaccion.getFecha()));
        columna(tabla, "Tipo", transaccion -> transaccion.getTipo().getTexto());
        columna(tabla, "Valor", transaccion -> pesos(transaccion.getValor()));
        columna(tabla, "Comisión", transaccion -> pesos(transaccion.getComision()));
        columna(tabla, "Origen", transaccion -> nombreBilletera(transaccion.getBilleteraOrigen()));
        columna(tabla, "Destino", transaccion -> nombreBilletera(transaccion.getBilleteraDestino()));
        columnaEtiqueta(tabla, "Estado", transaccion -> transaccion.getEstado().getTexto(),
                transaccion -> clase("estado", transaccion.getEstado()));
        columnaEtiqueta(tabla, "Riesgo", transaccion -> transaccion.getNivelRiesgo().getTexto(),
                transaccion -> clase("riesgo", transaccion.getNivelRiesgo()));
        columna(tabla, "Puntos", transaccion -> String.valueOf(transaccion.getPuntosGenerados()));
        columna(tabla, "Descripción", transaccion -> textoSeguro(transaccion.getDescripcion()));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("No hay movimientos para mostrar"));
    }

    public static String nombreBilletera(Billetera billetera) {
        if (billetera == null) {
            return "—";
        }
        return billetera.getCodigo() + " · " + billetera.getPropietario().getNombre();
    }

    public static String textoSeguro(String texto) {
        if (texto == null) {
            return "";
        }
        return texto;
    }

    /** Icono sencillo para cada tipo de notificacion (siempre va con el texto del tipo al lado). */
    public static String iconoNotificacion(TipoNotificacion tipo) {
        switch (tipo) {
            case SALDO_BAJO:
                return "▽";
            case PROGRAMADA_PROXIMA:
                return "◷";
            case OPERACION_RECHAZADA:
                return "✕";
            case ASCENSO_NIVEL:
                return "▲";
            case DESCENSO_NIVEL:
                return "▼";
            case CANJE_BENEFICIO:
                return "★";
            default:
                return "⚠";
        }
    }

    /**
     * Lee un monto escrito por el usuario. Acepta "150000", "150000.50",
     * "150.000" o "150.000,50".
     */
    public static double leerMonto(String texto) {
        if (Validador.estaVacio(texto)) {
            throw new OperacionInvalidaException("Escribe el monto");
        }
        String limpio = texto.replace("$", "").replace(" ", "").trim();
        if (limpio.contains(",")) {
            limpio = limpio.replace(".", "").replace(",", ".");
        } else if (limpio.indexOf('.') != limpio.lastIndexOf('.')) {
            limpio = limpio.replace(".", "");
        } else if (limpio.matches("\\d{1,3}\\.\\d{3}")) {
            // Algo como 150.000 es separador de miles, no decimales
            limpio = limpio.replace(".", "");
        }
        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException error) {
            throw new OperacionInvalidaException("Escribe un monto válido, por ejemplo 150000");
        }
    }

    public static void mostrarError(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Algo salió mal", mensaje);
    }

    public static void mostrarInfo(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Listo", mensaje);
    }

    /** Pregunta si o no antes de hacer algo delicado. */
    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar");
        alerta.setHeaderText(null);
        estilizar(alerta.getDialogPane());
        return alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    /** Le pone la hoja de estilos a un dialogo. */
    public static void estilizar(DialogPane panel) {
        panel.getStylesheets().add(NavegadorVistas.getHojaEstilos());
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        estilizar(alerta.getDialogPane());
        alerta.showAndWait();
    }
}
