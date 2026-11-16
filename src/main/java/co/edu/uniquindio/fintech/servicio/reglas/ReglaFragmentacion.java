package co.edu.uniquindio.fintech.servicio.reglas;

import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelRiesgo;

import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Detecta cuando alguien parte un monto grande en pedazos y lo manda desde
 * varias billeteras suyas al mismo destino: 3 o mas billeteras distintas
 * en 15 minutos es riesgo alto.
 */
public class ReglaFragmentacion extends ReglaBase {

    private static final int MINUTOS_VENTANA = 15;
    private static final int BILLETERAS_MAXIMAS = 3;

    @Override
    public NivelRiesgo evaluar(Transaccion transaccion, Usuario usuario) {
        if (!esExterna(transaccion)) {
            return NivelRiesgo.NINGUNO;
        }

        Billetera destino = transaccion.getBilleteraDestino();
        LocalDateTime inicioVentana = transaccion.getFecha().minusMinutes(MINUTOS_VENTANA);

        // Se van anotando las billeteras de origen distintas que le mandaron a ese destino
        ListaEnlazada<Billetera> origenes = new ListaEnlazada<>();
        origenes.agregarAlFinal(transaccion.getBilleteraOrigen());

        Iterator<Transaccion> iterador = usuario.getHistorial().iteradorInverso();
        boolean dentroDeLaVentana = true;
        while (iterador.hasNext() && dentroDeLaVentana) {
            Transaccion anterior = iterador.next();
            if (anterior.getFecha().isBefore(inicioVentana)) {
                dentroDeLaVentana = false;
            } else if (cuentaParaElUsuario(anterior, usuario) && esExterna(anterior)
                    && anterior.getBilleteraDestino().equals(destino)
                    && !origenes.contiene(anterior.getBilleteraOrigen())) {
                origenes.agregarAlFinal(anterior.getBilleteraOrigen());
            }
        }

        if (origenes.tamanio() >= BILLETERAS_MAXIMAS) {
            return NivelRiesgo.ALTO;
        }
        return NivelRiesgo.NINGUNO;
    }

    @Override
    public String getNombre() {
        return "Fragmentación de montos";
    }

    @Override
    public String getDescripcion() {
        return "3 o más billeteras propias enviando al mismo destino en menos de 15 minutos";
    }
}
