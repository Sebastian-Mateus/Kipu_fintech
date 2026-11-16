package co.edu.uniquindio.fintech.util;

import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.estructuras.TablaHash;

/**
 * Valores de configuracion de la plataforma: umbrales de las reglas de fraude,
 * horas para revertir, intentos de las programadas, etc. Se guardan en una
 * tabla hash por nombre, asi se consultan en O(1) y se pueden cambiar sin
 * tocar el codigo de las reglas ni de los servicios.
 */
public class Configuracion {

    // Nombres de cada valor, para no escribir los textos a mano en cada clase
    public static final String RAFAGA_MINUTOS = "fraude.rafaga.minutos";
    public static final String RAFAGA_CANTIDAD = "fraude.rafaga.cantidad";
    public static final String ATIPICO_MINIMO_TRANSACCIONES = "fraude.atipico.minimoTransacciones";
    public static final String ATIPICO_VECES_MEDIO = "fraude.atipico.vecesMedio";
    public static final String ATIPICO_VECES_ALTO = "fraude.atipico.vecesAlto";
    public static final String DESTINO_MINUTOS = "fraude.destino.minutos";
    public static final String DESTINO_CANTIDAD = "fraude.destino.cantidad";
    public static final String FRAGMENTACION_MINUTOS = "fraude.fragmentacion.minutos";
    public static final String FRAGMENTACION_BILLETERAS = "fraude.fragmentacion.billeteras";
    public static final String FRECUENCIA_MINIMO_TRANSACCIONES = "fraude.frecuencia.minimoTransacciones";
    public static final String FRECUENCIA_HORAS = "fraude.frecuencia.horas";
    public static final String FRECUENCIA_VECES = "fraude.frecuencia.veces";
    public static final String HORARIO_INICIO_MADRUGADA = "fraude.horario.inicioMadrugada";
    public static final String HORARIO_FIN_MADRUGADA = "fraude.horario.finMadrugada";
    public static final String HORARIO_MINIMO_TRANSACCIONES = "fraude.horario.minimoTransacciones";
    public static final String HORARIO_PORCENTAJE_MINIMO = "fraude.horario.porcentajeMinimo";
    public static final String HORAS_REVERSION = "reversion.horas";
    public static final String PROGRAMADAS_MAXIMO_INTENTOS = "programadas.maximoIntentos";
    public static final String PROGRAMADAS_HORAS_ALERTA = "programadas.horasAlerta";
    public static final String NOTIFICACIONES_MAXIMO = "notificaciones.maximoRecientes";

    private TablaHash<String, Double> valores;

    public Configuracion() {
        this.valores = new TablaHash<>();
        cargarValoresIniciales();
    }

    /** Devuelve el valor guardado con ese nombre. O(1) promedio. */
    public double obtener(String clave) {
        Double valor = valores.obtener(clave);
        if (valor == null) {
            throw new IllegalArgumentException("No existe la configuración " + clave);
        }
        return valor;
    }

    /** Lo mismo que obtener pero como numero entero (minutos, cantidades, horas...). */
    public int obtenerEntero(String clave) {
        return (int) obtener(clave);
    }

    /** Cambia un valor que ya existe. */
    public void cambiar(String clave, double valor) {
        if (!valores.contieneClave(clave)) {
            throw new IllegalArgumentException("No existe la configuración " + clave);
        }
        valores.insertar(clave, valor);
    }

    /** Nombres de todos los valores de configuracion. */
    public ListaEnlazada<String> claves() {
        return valores.claves();
    }

    // Los valores que se acordaron en el diseño
    private void cargarValoresIniciales() {
        valores.insertar(RAFAGA_MINUTOS, 10.0);
        valores.insertar(RAFAGA_CANTIDAD, 5.0);
        valores.insertar(ATIPICO_MINIMO_TRANSACCIONES, 5.0);
        valores.insertar(ATIPICO_VECES_MEDIO, 3.0);
        valores.insertar(ATIPICO_VECES_ALTO, 5.0);
        valores.insertar(DESTINO_MINUTOS, 30.0);
        valores.insertar(DESTINO_CANTIDAD, 3.0);
        valores.insertar(FRAGMENTACION_MINUTOS, 15.0);
        valores.insertar(FRAGMENTACION_BILLETERAS, 3.0);
        valores.insertar(FRECUENCIA_MINIMO_TRANSACCIONES, 5.0);
        valores.insertar(FRECUENCIA_HORAS, 24.0);
        valores.insertar(FRECUENCIA_VECES, 3.0);
        valores.insertar(HORARIO_INICIO_MADRUGADA, 1.0);
        valores.insertar(HORARIO_FIN_MADRUGADA, 5.0);
        valores.insertar(HORARIO_MINIMO_TRANSACCIONES, 20.0);
        valores.insertar(HORARIO_PORCENTAJE_MINIMO, 2.0);
        valores.insertar(HORAS_REVERSION, 24.0);
        valores.insertar(PROGRAMADAS_MAXIMO_INTENTOS, 3.0);
        valores.insertar(PROGRAMADAS_HORAS_ALERTA, 24.0);
        valores.insertar(NOTIFICACIONES_MAXIMO, 20.0);
    }
}
