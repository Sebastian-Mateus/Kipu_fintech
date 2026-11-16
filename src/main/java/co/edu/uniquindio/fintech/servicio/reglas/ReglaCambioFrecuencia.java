package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.PerfilComportamiento;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.util.Configuracion;

import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Marca riesgo bajo cuando en las ultimas 24 horas el usuario hizo mas de
 * 3 veces las transacciones que hace en un dia normal.
 * Igual que en el monto atipico, se necesitan 5 transacciones previas
 * para que el promedio tenga sentido.
 */
public class ReglaCambioFrecuencia extends ReglaBase {

    public ReglaCambioFrecuencia(Configuracion configuracion) {
        super(configuracion);
    }

    @Override
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        PerfilComportamiento perfil = usuario.getPerfil();
        if (perfil.getCantidadTransacciones()
                < configuracion.obtenerEntero(Configuracion.FRECUENCIA_MINIMO_TRANSACCIONES)) {
            return NivelRiesgo.NINGUNO;
        }

        int horas = configuracion.obtenerEntero(Configuracion.FRECUENCIA_HORAS);
        LocalDateTime inicioVentana = transaccion.getFecha().minusHours(horas);
        int cantidad = 1;

        Iterator<Transaccion> iterador = usuario.getHistorial().iteradorInverso();
        boolean dentroDeLaVentana = true;
        while (iterador.hasNext() && dentroDeLaVentana) {
            Transaccion anterior = iterador.next();
            if (anterior.getFecha().isBefore(inicioVentana)) {
                dentroDeLaVentana = false;
            } else if (cuentaParaElUsuario(anterior, usuario)) {
                cantidad++;
            }
        }

        double promedioDiario = perfil.promedioDiario(transaccion.getFecha());
        if (cantidad > promedioDiario * configuracion.obtener(Configuracion.FRECUENCIA_VECES)) {
            return NivelRiesgo.BAJO;
        }
        return NivelRiesgo.NINGUNO;
    }

    @Override
    public String getNombre() {
        return "Cambio de frecuencia";
    }

    @Override
    public String getDescripcion() {
        return "Más de " + configuracion.obtenerEntero(Configuracion.FRECUENCIA_VECES)
                + " veces su promedio diario de transacciones en las últimas "
                + configuracion.obtenerEntero(Configuracion.FRECUENCIA_HORAS) + " horas";
    }
}
