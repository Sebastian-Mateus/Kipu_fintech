package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.PerfilComportamiento;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;
import co.edu.uniquindio.fintech.util.Validador;

/**
 * Compara el monto con el promedio del usuario: mas de 3 veces el promedio es
 * riesgo medio y mas de 5 veces es alto. Solo se revisa cuando el usuario ya
 * tiene por lo menos 5 transacciones, si no el promedio no dice mucho.
 */
public class ReglaMontoAtipico extends ReglaBase {

    private static final int MINIMO_TRANSACCIONES = 5;
    private static final double VECES_MEDIO = 3;
    private static final double VECES_ALTO = 5;

    @Override
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        PerfilComportamiento perfil = usuario.getPerfil();
        if (perfil.getCantidadTransacciones() < MINIMO_TRANSACCIONES) {
            return NivelRiesgo.NINGUNO;
        }

        double promedio = perfil.promedioMonto();
        if (Validador.esMayor(transaccion.getValor(), promedio * VECES_ALTO)) {
            return NivelRiesgo.ALTO;
        }
        if (Validador.esMayor(transaccion.getValor(), promedio * VECES_MEDIO)) {
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
        return "Monto mayor a 3 veces (medio) o 5 veces (alto) el promedio del usuario";
    }
}
