package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.util.Configuracion;

import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Marca riesgo medio cuando el usuario hace muchas transferencias seguidas
 * (por defecto 5 o mas en 10 minutos).
 */
public class ReglaRafagaTransferencias extends ReglaBase {

    public ReglaRafagaTransferencias(Configuracion configuracion) {
        super(configuracion);
    }

    @Override
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        if (!esTransferencia(transaccion)) {
            return NivelRiesgo.NINGUNO;
        }

        int minutos = configuracion.obtenerEntero(Configuracion.RAFAGA_MINUTOS);
        LocalDateTime inicioVentana = transaccion.getFecha().minusMinutes(minutos);
        // Empieza en 1 porque la transaccion nueva tambien cuenta
        int cantidad = 1;

        // Se recore el historial desde lo mas nuevo y se para al salir de la ventana
        Iterator<Transaccion> iterador = usuario.getHistorial().iteradorInverso();
        boolean dentroDeLaVentana = true;
        while (iterador.hasNext() && dentroDeLaVentana) {
            Transaccion anterior = iterador.next();
            if (anterior.getFecha().isBefore(inicioVentana)) {
                dentroDeLaVentana = false;
            } else if (cuentaParaElUsuario(anterior, usuario) && esTransferencia(anterior)) {
                cantidad++;
            }
        }

        if (cantidad >= configuracion.obtenerEntero(Configuracion.RAFAGA_CANTIDAD)) {
            return NivelRiesgo.MEDIO;
        }
        return NivelRiesgo.NINGUNO;
    }

    @Override
    public String getNombre() {
        return "Ráfaga de transferencias";
    }

    @Override
    public String getDescripcion() {
        return configuracion.obtenerEntero(Configuracion.RAFAGA_CANTIDAD) + " o más transferencias en menos de "
                + configuracion.obtenerEntero(Configuracion.RAFAGA_MINUTOS) + " minutos";
    }
}
