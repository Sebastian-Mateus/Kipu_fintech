package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.util.Configuracion;

import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Marca riesgo medio cuando el usuario le manda varias veces a la misma
 * billetera de otra persona en poco tiempo (por defecto 3 veces en 30 minutos).
 */
public class ReglaDestinoRepetido extends ReglaBase {

    public ReglaDestinoRepetido(Configuracion configuracion) {
        super(configuracion);
    }

    @Override
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        if (!esExterna(transaccion)) {
            return NivelRiesgo.NINGUNO;
        }

        Billetera destino = transaccion.getBilleteraDestino();
        int minutos = configuracion.obtenerEntero(Configuracion.DESTINO_MINUTOS);
        LocalDateTime inicioVentana = transaccion.getFecha().minusMinutes(minutos);
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

        if (cantidad >= configuracion.obtenerEntero(Configuracion.DESTINO_CANTIDAD)) {
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
        return configuracion.obtenerEntero(Configuracion.DESTINO_CANTIDAD)
                + " o más envíos a la misma billetera en menos de "
                + configuracion.obtenerEntero(Configuracion.DESTINO_MINUTOS) + " minutos";
    }
}
