package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.modelo.OperacionProgramada;

import java.util.Comparator;

/**
 * Orden del monticulo de programadas: primero la que tiene la fecha mas cercana.
 * Si dos tienen la misma fecha, va primero la del usuario de mayor nivel
 * (asi Platino tiene prioridad). Si sigue el empate, por id.
 */
public class ComparadorOperacionesProgramadas implements Comparator<OperacionProgramada> {

    @Override
    public int compare(OperacionProgramada primera, OperacionProgramada segunda) {
        int porFecha = primera.getFechaProximaEjecucion().compareTo(segunda.getFechaProximaEjecucion());
        if (porFecha != 0) {
            return porFecha;
        }
        int porNivel = Integer.compare(segunda.getUsuario().getNivel().ordinal(),
                primera.getUsuario().getNivel().ordinal());
        if (porNivel != 0) {
            return porNivel;
        }
        return primera.getId().compareTo(segunda.getId());
    }
}
