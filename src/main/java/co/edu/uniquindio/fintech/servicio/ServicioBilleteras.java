package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.excepciones.BilleteraNoEncontradaException;
import co.edu.uniquindio.fintech.excepciones.OperacionInvalidaException;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.EstadoBilletera;
import co.edu.uniquindio.fintech.modelo.enums.TipoBilletera;
import co.edu.uniquindio.fintech.repositorio.RepositorioBilleteras;
import co.edu.uniquindio.fintech.util.GeneradorIdentificadores;
import co.edu.uniquindio.fintech.util.RelojSistema;
import co.edu.uniquindio.fintech.util.Validador;

/**
 * Crear, renombrar, bloquear, desbloquear y cerrar billeteras.
 */
public class ServicioBilleteras {

    private RepositorioBilleteras repositorioBilleteras;
    private GeneradorIdentificadores generador;
    private RelojSistema reloj;

    public ServicioBilleteras(RepositorioBilleteras repositorioBilleteras, GeneradorIdentificadores generador,
                              RelojSistema reloj) {
        this.repositorioBilleteras = repositorioBilleteras;
        this.generador = generador;
        this.reloj = reloj;
    }

    /** Crea una billetera nueva para el usuario, arranca en cero y activa. */
    public Billetera crear(Usuario usuario, String nombre, TipoBilletera tipo, double umbralSaldoBajo) {
        if (Validador.estaVacio(nombre)) {
            throw new OperacionInvalidaException("El nombre de la billetera no puede estar vacío");
        }
        if (tipo == null) {
            throw new OperacionInvalidaException("Debes elegir el tipo de billetera");
        }
        if (umbralSaldoBajo < 0) {
            throw new OperacionInvalidaException("El umbral de saldo bajo no puede ser negativo");
        }

        Billetera billetera = new Billetera(generador.siguienteBilletera(), nombre.trim(), tipo, umbralSaldoBajo,
                usuario, reloj.ahora());
        usuario.agregarBilletera(billetera);
        repositorioBilleteras.guardar(billetera);
        return billetera;
    }

    /** Le cambia el nombre a la billetera. */
    public void renombrar(String codigo, String nuevoNombre) {
        if (Validador.estaVacio(nuevoNombre)) {
            throw new OperacionInvalidaException("El nombre de la billetera no puede estar vacío");
        }
        Billetera billetera = buscar(codigo);
        if (billetera.getEstado() == EstadoBilletera.CERRADA) {
            throw new OperacionInvalidaException("La billetera está cerrada");
        }
        billetera.setNombre(nuevoNombre.trim());
    }

    /** Bloquea una billetera activa (no se puede mover plata hasta desbloquearla). */
    public void bloquear(String codigo) {
        Billetera billetera = buscar(codigo);
        if (billetera.getEstado() != EstadoBilletera.ACTIVA) {
            throw new OperacionInvalidaException("Solo se pueden bloquear billeteras activas");
        }
        billetera.setEstado(EstadoBilletera.BLOQUEADA);
    }

    /** Vuelve a activar una billetera bloqueada. */
    public void desbloquear(String codigo) {
        Billetera billetera = buscar(codigo);
        if (billetera.getEstado() != EstadoBilletera.BLOQUEADA) {
            throw new OperacionInvalidaException("La billetera no está bloqueada");
        }
        billetera.setEstado(EstadoBilletera.ACTIVA);
    }

    /** Cierra la billetera, solo si ya no tiene plata. Una cerrada no se puede volver a abrir. */
    public void cerrar(String codigo) {
        Billetera billetera = buscar(codigo);
        if (billetera.getEstado() == EstadoBilletera.CERRADA) {
            throw new OperacionInvalidaException("La billetera ya está cerrada");
        }
        if (!Validador.sonIguales(billetera.getSaldo(), 0)) {
            throw new OperacionInvalidaException("Solo se puede cerrar una billetera con saldo en cero");
        }
        billetera.setEstado(EstadoBilletera.CERRADA);
    }

    /** Saldo actual de la billetera. */
    public double consultarSaldo(String codigo) {
        return buscar(codigo).getSaldo();
    }

    /** Busca una billetera por codigo, y si no existe lanza excepcion. */
    public Billetera buscar(String codigo) {
        Billetera billetera = null;
        if (codigo != null) {
            billetera = repositorioBilleteras.buscar(codigo.trim());
        }
        if (billetera == null) {
            throw new BilleteraNoEncontradaException("No existe la billetera " + codigo);
        }
        return billetera;
    }

    /** Billeteras activas del usuario (para los combos de las operaciones). */
    public ListaEnlazada<Billetera> activasDe(Usuario usuario) {
        ListaEnlazada<Billetera> resultado = new ListaEnlazada<>();
        for (Billetera billetera : usuario.getBilleteras()) {
            if (billetera.estaActiva()) {
                resultado.agregarAlFinal(billetera);
            }
        }
        return resultado;
    }

    /** Todas las billeteras de la plataforma. */
    public ListaEnlazada<Billetera> listar() {
        return repositorioBilleteras.listar();
    }
}
