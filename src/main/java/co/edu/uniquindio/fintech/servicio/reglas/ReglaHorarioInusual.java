package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.PerfilComportamiento;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.util.Configuracion;

/**
 * Marca riesgo bajo si la transaccion se hace de madrugada (entre la 1 y las 5)
 * o en una hora en la que el usuario casi nunca se mueve (menos del 2 % de su
 * actividad, solo si ya tiene 20 transacciones o mas).
 */
public class ReglaHorarioInusual extends ReglaBase {

    public ReglaHorarioInusual(Configuracion configuracion) {
        super(configuracion);
    }

    @Override
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        int hora = transaccion.getFecha().getHour();
        if (hora >= configuracion.obtenerEntero(Configuracion.HORARIO_INICIO_MADRUGADA)
                && hora < configuracion.obtenerEntero(Configuracion.HORARIO_FIN_MADRUGADA)) {
            return NivelRiesgo.BAJO;
        }

        PerfilComportamiento perfil = usuario.getPerfil();
        if (perfil.getCantidadTransacciones() >= configuracion.obtenerEntero(Configuracion.HORARIO_MINIMO_TRANSACCIONES)
                && perfil.porcentajeEnHora(hora) < configuracion.obtener(Configuracion.HORARIO_PORCENTAJE_MINIMO)) {
            return NivelRiesgo.BAJO;
        }
        return NivelRiesgo.NINGUNO;
    }

    @Override
    public String getNombre() {
        return "Horario inusual";
    }

    @Override
    public String getDescripcion() {
        return "Transacción de madrugada o en una hora en la que el usuario casi no tiene actividad";
    }
}
