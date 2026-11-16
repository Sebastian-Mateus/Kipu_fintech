package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;

import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Marca riesgo medio cuando el usuario hace 5 o mas transferencias en 10 minutos.
 */
public class ReglaRafagaTransferencias extends ReglaBase {

    private static final int MINUTOS_VENTANA = 10;
    private static final int CANTIDAD_MAXIMA = 5;

    @Override
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        if (!esTransferencia(transaccion)) {
            return NivelRiesgo.NINGUNO;
        }

        LocalDateTime inicioVentana = transaccion.getFecha().minusMinutes(MINUTOS_VENTANA);
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

        if (cantidad >= CANTIDAD_MAXIMA) {
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
        return "5 o más transferencias en menos de 10 minutos";
    }
}
