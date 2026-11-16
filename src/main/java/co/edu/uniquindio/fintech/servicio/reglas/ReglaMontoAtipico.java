package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.PerfilComportamiento;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.util.Configuracion;
import co.edu.uniquindio.fintech.util.Validador;

/**
 * Compara el monto de los retiros y envios con el promedio del usuario: mas de
 * 3 veces el promedio es riesgo medio y mas de 5 veces es alto. Solo se revisa
 * cuando el usuario ya tiene por lo menos 5 transacciones, si no el promedio
 * no dice mucho. Las recargas no se revisan porque no sacan plata de la cuenta.
 */
public class ReglaMontoAtipico extends ReglaBase {

    public ReglaMontoAtipico(Configuracion configuracion) {
        super(configuracion);
    }

    @Override
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        // Sin billetera de origen es una recarga, esa no aplica
        if (transaccion.getBilleteraOrigen() == null) {
            return NivelRiesgo.NINGUNO;
        }
        PerfilComportamiento perfil = usuario.getPerfil();
        if (perfil.getCantidadTransacciones() < configuracion.obtenerEntero(Configuracion.ATIPICO_MINIMO_TRANSACCIONES)) {
            return NivelRiesgo.NINGUNO;
        }

        double promedio = perfil.promedioMonto();
        if (Validador.esMayor(transaccion.getValor(), promedio * configuracion.obtener(Configuracion.ATIPICO_VECES_ALTO))) {
            return NivelRiesgo.ALTO;
        }
        if (Validador.esMayor(transaccion.getValor(), promedio * configuracion.obtener(Configuracion.ATIPICO_VECES_MEDIO))) {
            return NivelRiesgo.MEDIO;
        }
        return NivelRiesgo.NINGUNO;
    }

    @Override
    public String getNombre() {
        return "Monto atípico";
    }

    @Override
    public String getDescripcion() {
        return "Retiro o envío mayor a " + configuracion.obtenerEntero(Configuracion.ATIPICO_VECES_MEDIO)
                + " veces (medio) o " + configuracion.obtenerEntero(Configuracion.ATIPICO_VECES_ALTO)
                + " veces (alto) el promedio del usuario";
    }
}
