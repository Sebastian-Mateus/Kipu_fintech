package co.edu.uniquindio.fintech.modelo.enums;

/**
 * Niveles del programa de puntos. Cada nivel define su rango de puntos
 * acumulados, la comision, cuantas transacciones se pueden hacer al dia
 * y el bono extra de puntos.
 *
 * La comision y el bono estan en porcentaje (1.0 es 1 %), toca dividir
 * entre 100 a la hora de hacer las cuentas.
 */
public enum NivelUsuario {

    BRONCE("Bronce", 0, 500, 1.0, 10, 0),
    PLATA("Plata", 501, 1000, 0.75, 50, 5),
    ORO("Oro", 1001, 5000, 0.5, 100, 10),
    PLATINO("Platino", 5001, Integer.MAX_VALUE, 0, NivelUsuario.SIN_LIMITE, 20);

    /** Se usa en limiteTransaccionesDiarias cuando el nivel no tiene tope. */
    public static final int SIN_LIMITE = -1;

    private final String texto;
    private final int puntosMinimos;
    private final int puntosMaximos;
    private final double porcentajeComision;
    private final int limiteTransaccionesDiarias;
    private final double bonoPuntos;

    NivelUsuario(String texto, int puntosMinimos, int puntosMaximos, double porcentajeComision,
                 int limiteTransaccionesDiarias, double bonoPuntos) {
        this.texto = texto;
        this.puntosMinimos = puntosMinimos;
        this.puntosMaximos = puntosMaximos;
        this.porcentajeComision = porcentajeComision;
        this.limiteTransaccionesDiarias = limiteTransaccionesDiarias;
        this.bonoPuntos = bonoPuntos;
    }

    /** Busca el nivel que le corresponde a esa cantidad de puntos acumulados. */
    public static NivelUsuario desdePuntos(int puntos) {
        for (NivelUsuario nivel : values()) {
            if (puntos >= nivel.puntosMinimos && puntos <= nivel.puntosMaximos) {
                return nivel;
            }
        }
        // Puntos negativos no deberian pasar, pero por si acaso queda en bronce
        return BRONCE;
    }

    /** Dice si este nivel es igual o superior al otro (sirve para el nivel minimo de los beneficios). */
    public boolean esIgualOMayorQue(NivelUsuario otro) {
        return this.ordinal() >= otro.ordinal();
    }

    /** Dice si el nivel no tiene tope de transacciones por dia. */
    public boolean esIlimitado() {
        return limiteTransaccionesDiarias == SIN_LIMITE;
    }

    public String getTexto() {
        return texto;
    }

    public int getPuntosMinimos() {
        return puntosMinimos;
    }

    public int getPuntosMaximos() {
        return puntosMaximos;
    }

    public double getPorcentajeComision() {
        return porcentajeComision;
    }

    public int getLimiteTransaccionesDiarias() {
        return limiteTransaccionesDiarias;
    }

    public double getBonoPuntos() {
        return bonoPuntos;
    }

    @Override
    public String toString() {
        return texto;
    }
}
