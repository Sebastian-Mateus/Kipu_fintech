package co.edu.uniquindio.fintech.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Metodos de ayuda para validar datos y manejar la plata.
 * Como el dinero va en double, siempre se redondea a 2 decimales y los
 * montos se comparan con una pequeña tolerancia en vez de usar ==.
 */
public class Validador {

    // Diferencia minima para considerar que dos montos son distintos
    private static final double TOLERANCIA = 0.001;

    private Validador() {
        // Solo tiene metodos estaticos, no se crean objetos
    }

    /** Redondea el valor a 2 decimales. */
    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    /** Dice si dos montos son iguales (con la tolerancia). */
    public static boolean sonIguales(double primero, double segundo) {
        return Math.abs(primero - segundo) < TOLERANCIA;
    }

    /** Dice si el primer monto es menor que el segundo. */
    public static boolean esMenor(double primero, double segundo) {
        return primero < segundo - TOLERANCIA;
    }

    /** Dice si el primer monto es mayor que el segundo. */
    public static boolean esMayor(double primero, double segundo) {
        return primero > segundo + TOLERANCIA;
    }

    /** Dice si el primer monto es mayor o igual al segundo (sirve para ver si alcanza el saldo). */
    public static boolean esMayorOIgual(double primero, double segundo) {
        return !esMenor(primero, segundo);
    }

    /** Dice si el monto es mayor que cero despues de redondearlo. */
    public static boolean esPositivo(double valor) {
        return redondear(valor) > 0;
    }

    /** Dice si el texto es null o solo tiene espacios. */
    public static boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /** Revisa que el correo tenga la forma basica nombre@dominoi.com */
    public static boolean esCorreoValido(String correo) {
        return correo != null && correo.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    }

    /** La cedula tiene que ser solo numeros, entre 6 y 10 digitos. */
    public static boolean esCedulaValida(String cedula) {
        return cedula != null && cedula.trim().matches("\\d{6,10}");
    }

    /** El telefono tiene que ser solo numeros, entre 7 y 10 digitos. */
    public static boolean esTelefonoValido(String telefono) {
        return telefono != null && telefono.trim().matches("\\d{7,10}");
    }

    /** Muestra el monto en pesos colombianos, por ejemplo $ 1.250.000,50 */
    public static String formatearPesos(double valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"));
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return "$ " + formato.format(redondear(valor));
    }
}
