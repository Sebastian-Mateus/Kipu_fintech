package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.modelo.EventoAuditoria;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.servicio.reglas.ReglaCambioFrecuencia;
import co.edu.uniquindio.fintech.servicio.reglas.ReglaDeteccion;
import co.edu.uniquindio.fintech.servicio.reglas.ReglaDestinoRepetido;
import co.edu.uniquindio.fintech.servicio.reglas.ReglaFragmentacion;
import co.edu.uniquindio.fintech.servicio.reglas.ReglaHorarioInusual;
import co.edu.uniquindio.fintech.servicio.reglas.ReglaMontoAtipico;
import co.edu.uniquindio.fintech.servicio.reglas.ReglaRafagaTransferencias;
import co.edu.uniquindio.fintech.util.Configuracion;
import co.edu.uniquindio.fintech.util.GeneradorIdentificadores;
import co.edu.uniquindio.fintech.util.Validador;

/**
 * Pasa cada transaccion por todas las reglas de fraude y se queda con el
 * riesgo mas alto. Lo que encuentre queda en la auditoria:
 * - BAJO: solo se marca y se registra
 * - MEDIO: ademas se le manda una alerta de seguridad al usuario
 * - ALTO: ademas la transaccion se rechaza (eso lo hace el servicio de transacciones)
 */
public class ServicioDeteccionFraude {

    private ListaEnlazada<ReglaDeteccion> reglas;
    private ListaEnlazada<EventoAuditoria> auditoria;
    private ServicioNotificaciones notificaciones;
    private GeneradorIdentificadores generador;

    public ServicioDeteccionFraude(ServicioNotificaciones notificaciones, GeneradorIdentificadores generador,
                                   Configuracion configuracion) {
        this.notificaciones = notificaciones;
        this.generador = generador;
        this.auditoria = new ListaEnlazada<>();
        this.reglas = new ListaEnlazada<>();
        reglas.agregarAlFinal(new ReglaRafagaTransferencias(configuracion));
        reglas.agregarAlFinal(new ReglaMontoAtipico(configuracion));
        reglas.agregarAlFinal(new ReglaDestinoRepetido(configuracion));
        reglas.agregarAlFinal(new ReglaFragmentacion(configuracion));
        reglas.agregarAlFinal(new ReglaCambioFrecuencia(configuracion));
        reglas.agregarAlFinal(new ReglaHorarioInusual(configuracion));
    }

    /** Evalua la transaccion con todas las reglas, la marca con el riesgo mayor y lo devuelve. */
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        if (transaccion.getTipo() == TipoTransaccion.REVERSION) {
            return NivelRiesgo.NINGUNO;
        }

        NivelRiesgo mayor = NivelRiesgo.NINGUNO;
        for (ReglaDeteccion regla : reglas) {
            NivelRiesgo riesgo = regla.evaluar(transaccion, usuario);
            if (riesgo != NivelRiesgo.NINGUNO) {
                auditoria.agregarAlFinal(new EventoAuditoria(generador.siguienteEvento(), transaccion.getFecha(),
                        usuario, transaccion, regla.getNombre(), riesgo, regla.getDescripcion()));
                if (riesgo.esMayorQue(mayor)) {
                    mayor = riesgo;
                }
            }
        }
        transaccion.setNivelRiesgo(mayor);

        String detalle = transaccion.getTipo().getTexto().toLowerCase() + " por "
                + Validador.formatearPesos(transaccion.getValor());
        if (mayor == NivelRiesgo.MEDIO) {
            notificaciones.generar(usuario, TipoNotificacion.ALERTA_SEGURIDAD,
                    "Detectamos actividad inusual en tu " + detalle + ". Si no fuiste tú, contáctanos.");
        } else if (mayor == NivelRiesgo.ALTO) {
            notificaciones.generar(usuario, TipoNotificacion.ALERTA_SEGURIDAD,
                    "Bloqueamos tu " + detalle + " por riesgo alto de fraude.");
        }
        return mayor;
    }

    /** Todos los eventos de auditoria en el orden en que pasaron. */
    public ListaEnlazada<EventoAuditoria> consultarAuditoria() {
        return auditoria;
    }

    /**
     * Filtra la auditoria por usuario y por nivel de riesgo.
     * Si alguno de los dos viene en null, no se filtra por ese.
     */
    public ListaEnlazada<EventoAuditoria> consultarAuditoria(String idUsuario, NivelRiesgo riesgo) {
        ListaEnlazada<EventoAuditoria> resultado = new ListaEnlazada<>();
        for (EventoAuditoria evento : auditoria) {
            boolean coincideUsuario = idUsuario == null || evento.getUsuario().getId().equals(idUsuario);
            boolean coincideRiesgo = riesgo == null || evento.getNivelRiesgo() == riesgo;
            if (coincideUsuario && coincideRiesgo) {
                resultado.agregarAlFinal(evento);
            }
        }
        return resultado;
    }

    public ListaEnlazada<ReglaDeteccion> getReglas() {
        return reglas;
    }
}
