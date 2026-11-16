package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;

import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Marca riesgo medio cuando el usuario le manda 3 o mas veces a la misma
 * billetera de otra persona en 30 minutos.
 */
public class ReglaDestinoRepetido extends ReglaBase {

    private static final int MINUTOS_VENTANA = 30;
    private static final int CANTIDAD_MAXIMA = 3;

    @Override
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        if (!esExterna(transaccion)) {
            return NivelRiesgo.NINGUNO;
        }

        Billetera destino = transaccion.getBilleteraDestino();
        LocalDateTime inicioVentana = transaccion.getFecha().minusMinutes(MINUTOS_VENTANA);
        int cantidad = 1;

        Iterator<Transaccion> iterador = usuario.getHistorial().iteradorInverso();
        boolean dentroDeLaVentana = true;
        while (iterador.hasNext() && dentroDeLaVentana) {
            Transaccion anterior = iterador.next();
            if (anterior.getFecha().isBefore(inicioVentana)) {
                dentroDeLaVentana = false;
            } else if (cuentaParaElUsuario(anterior, usuario) && esExterna(anterior)
                    && anterior.getBilleteraDestino().equals(destino)) {
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
        return "Destino repetido";
    }

    @Override
    public String getDescripcion() {
        return "3 o más envíos a la misma billetera en menos de 30 minutos";
    }
}
