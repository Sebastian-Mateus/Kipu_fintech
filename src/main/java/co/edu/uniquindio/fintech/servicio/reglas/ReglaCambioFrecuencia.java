package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.PerfilComportamiento;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;

import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Marca riesgo bajo cuando en las ultimas 24 horas el usuario hizo mas de
 * 3 veces las transacciones que hace en un dia normal.
 * Igual que en el monto atipico, se necesitan 5 transacciones previas
 * para que el promedio tenga sentido.
 */
public class ReglaCambioFrecuencia extends ReglaBase {

    private static final int MINIMO_TRANSACCIONES = 5;
    private static final int HORAS_VENTANA = 24;
    private static final double VECES_PROMEDIO = 3;

    @Override
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        PerfilComportamiento perfil = usuario.getPerfil();
        if (perfil.getCantidadTransacciones() < MINIMO_TRANSACCIONES) {
            return NivelRiesgo.NINGUNO;
        }

        LocalDateTime inicioVentana = transaccion.getFecha().minusHours(HORAS_VENTANA);
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
        if (cantidad > promedioDiario * VECES_PROMEDIO) {
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
        return "Más de 3 veces su promedio diario de transacciones en las últimas 24 horas";
    }
}
