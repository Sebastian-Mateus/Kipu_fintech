package co.edu.uniquindio.fintech.repositorio;

import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.estructuras.TablaHash;
import co.edu.uniquindio.fintech.modelo.Billetera;

/**
 * Guarda todas las billeteras de la plataforma en una tabla hash por codigo.
 */
public class RepositorioBilleteras {

    private TablaHash<String, Billetera> billeterasPorCodigo;

    public RepositorioBilleteras() {
        this.billeterasPorCodigo = new TablaHash<>();
    }

    /** Guarda una billetera. */
    public void guardar(Billetera billetera) {
        billeterasPorCodigo.insertar(billetera.getCodigo(), billetera);
    }

    /** Busca una billetera por codigo, devuelve null si no existe. O(1). */
    public Billetera buscar(String codigo) {
        return billeterasPorCodigo.obtener(codigo);
    }

    /** Dice si existe una billetera con ese codigo. */
    public boolean existe(String codigo) {
        return billeterasPorCodigo.contieneClave(codigo);
    }

    /** Quita la billetera del repositorio. */
    public void eliminar(Billetera billetera) {
        billeterasPorCodigo.eliminar(billetera.getCodigo());
    }

    /** Todas las billeteras, sin un orden en particular. */
    public ListaEnlazada<Billetera> listar() {
        return billeterasPorCodigo.valores();
    }

    /** Cuantas billeteras hay. */
    public int cantidad() {
        return billeterasPorCodigo.tamanio();
    }
}
