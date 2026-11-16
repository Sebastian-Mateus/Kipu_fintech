package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;

/**
 * Lo que tiene que cumplir cada regla de deteccion de fraude.
 */
public interface ReglaDeteccion {

    /**
     * Revisa la transaccion contra el historial del usuario y devuelve el riesgo
     * que encuentra (NINGUNO si todo se ve normal). La transaccion que se evalua
     * todavia no esta en el historial.
     */
    NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario);

    /** Nombre corto de la regla para la auditoria. */
    String getNombre();

    /** Explicacion de lo que detecta la regla. */
    String getDescripcion();
}
