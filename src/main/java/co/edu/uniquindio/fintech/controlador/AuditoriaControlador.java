package co.edu.uniquindio.fintech.controlador;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.modelo.EventoAuditoria;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.servicio.ServicioDeteccionFraude;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

/**
 * Eventos de fraude que fueron registrando las reglas, con filtros por usuario
 * y por nivel de riesgo. El riesgo sale como etiqueta de color con su texto.
 */
public class AuditoriaControlador {

    private static final String TODOS_USUARIOS = "Todos los usuarios";
    private static final String TODOS_RIESGOS = "Todos los riesgos";

    @FXML
    private ComboBox<String> filtroUsuario;
    @FXML
    private ComboBox<String> filtroRiesgo;
    @FXML
    private Label etiquetaResumen;
    @FXML
    private TableView<EventoAuditoria> tablaEventos;

    private ServicioDeteccionFraude servicioFraude;
    // Los usuarios en el mismo orden del combo (la posicion 0 del combo es "todos")
    private ObservableList<Usuario> usuarios;

    @FXML
    private void initialize() {
        Plataforma plataforma = Plataforma.getInstancia();
        servicioFraude = plataforma.getServicioDeteccionFraude();

        usuarios = UtilidadesVista.aObservable(plataforma.getServicioUsuarios().listar());
        filtroUsuario.getItems().add(TODOS_USUARIOS);
        for (Usuario usuario : usuarios) {
            filtroUsuario.getItems().add(usuario.toString());
        }
        filtroRiesgo.getItems().addAll(TODOS_RIESGOS, NivelRiesgo.BAJO.getTexto(), NivelRiesgo.MEDIO.getTexto(),
                NivelRiesgo.ALTO.getTexto());

        UtilidadesVista.columna(tablaEventos, "ID", evento -> evento.getId());
        UtilidadesVista.columna(tablaEventos, "Fecha", evento -> UtilidadesVista.fecha(evento.getFecha()));
        UtilidadesVista.columna(tablaEventos, "Usuario", evento -> evento.getUsuario().getNombre());
        UtilidadesVista.columna(tablaEventos, "Transacción", evento -> evento.getTransaccion().getId() + " · "
                + UtilidadesVista.pesos(evento.getTransaccion().getValor()));
        UtilidadesVista.columna(tablaEventos, "Regla", evento -> evento.getNombreRegla());
        UtilidadesVista.columnaEtiqueta(tablaEventos, "Riesgo", evento -> evento.getNivelRiesgo().getTexto(),
                evento -> UtilidadesVista.clase("riesgo", evento.getNivelRiesgo()));
        UtilidadesVista.columna(tablaEventos, "Estado de la transacción",
                evento -> evento.getTransaccion().getEstado().getTexto());
        UtilidadesVista.columna(tablaEventos, "Descripción", evento -> evento.getDescripcion());
        tablaEventos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaEventos.setPlaceholder(new Label("No hay eventos de fraude con esos filtros"));
        limpiar();
    }

    @FXML
    private void filtrar() {
        String idUsuario = null;
        int posicionUsuario = filtroUsuario.getSelectionModel().getSelectedIndex();
        if (posicionUsuario > 0) {
            idUsuario = usuarios.get(posicionUsuario - 1).getId();
        }

        NivelRiesgo riesgo = null;
        for (NivelRiesgo nivel : NivelRiesgo.values()) {
            if (nivel.getTexto().equals(filtroRiesgo.getValue())) {
                riesgo = nivel;
            }
        }

        ListaEnlazada<EventoAuditoria> eventos = servicioFraude.consultarAuditoria(idUsuario, riesgo);
        tablaEventos.setItems(UtilidadesVista.aObservableInverso(eventos));
        etiquetaResumen.setText(eventos.tamanio() + " eventos");
    }

    @FXML
    private void limpiar() {
        filtroUsuario.getSelectionModel().selectFirst();
        filtroRiesgo.getSelectionModel().selectFirst();
        filtrar();
    }
}
