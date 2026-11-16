package co.edu.uniquindio.fintech.repositorio;

import co.edu.uniquindio.fintech.estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.estructuras.TablaHash;
import co.edu.uniquindio.fintech.modelo.Administrador;
import co.edu.uniquindio.fintech.modelo.Usuario;

/**
 * Guarda los usuarios y administradores. Los usuarios estan en una tabla hash
 * por cedula (para buscarlos rapido) y en un arbol por puntos acumulados
 * (para el ranking y para sacar los usuarios de cada nivel).
 */
public class RepositorioUsuarios {

    private TablaHash<String, Usuario> usuariosPorId;
    private TablaHash<String, Administrador> administradoresPorId;
    private ArbolBinarioBusqueda<Integer, Usuario> usuariosPorPuntos;

    public RepositorioUsuarios() {
        this.usuariosPorId = new TablaHash<>();
        this.administradoresPorId = new TablaHash<>();
        this.usuariosPorPuntos = new ArbolBinarioBusqueda<>();
    }

    /** Guarda un usuario nuevo en la tabla y en el arbol. */
    public void guardar(Usuario usuario) {
        usuariosPorId.insertar(usuario.getId(), usuario);
        usuariosPorPuntos.insertar(usuario.getPuntosAcumulados(), usuario);
    }

    /** Busca un usuario por cedula, devuelve null si no existe. O(1). */
    public Usuario buscar(String id) {
        return usuariosPorId.obtener(id);
    }

    /** Dice si ya hay alguien (usuario o administrador) con ese id. */
    public boolean existe(String id) {
        return usuariosPorId.contieneClave(id) || administradoresPorId.contieneClave(id);
    }

    /** Quita al usuario de la tabla y del arbol. */
    public void eliminar(Usuario usuario) {
        usuariosPorId.eliminar(usuario.getId());
        usuariosPorPuntos.eliminar(usuario.getPuntosAcumulados(), usuario);
    }

    /**
     * Cuando le cambian los puntos a un usuario hay que sacarlo del arbol con
     * la clave vieja y volverlo a meter con la nueva.
     */
    public void actualizarPuntos(Usuario usuario, int puntosAnteriores) {
        usuariosPorPuntos.eliminar(puntosAnteriores, usuario);
        usuariosPorPuntos.insertar(usuario.getPuntosAcumulados(), usuario);
    }

    /** Todos los usuarios, sin un orden en particular. */
    public ListaEnlazada<Usuario> listar() {
        return usuariosPorId.valores();
    }

    /** Usuarios de mas puntos a menos puntos (recorrido inorden inverso). */
    public ListaEnlazada<Usuario> rankingPorPuntos() {
        return usuariosPorPuntos.recorridoInordenInverso();
    }

    /** Usuarios con puntos acumulados entre minimo y maximo, de menor a mayor. */
    public ListaEnlazada<Usuario> buscarPorRangoPuntos(int minimo, int maximo) {
        return usuariosPorPuntos.buscarRango(minimo, maximo);
    }

    /** Cuantos usuarios hay registrados. */
    public int cantidad() {
        return usuariosPorId.tamanio();
    }

    /** Guarda un administrador. */
    public void guardarAdministrador(Administrador administrador) {
        administradoresPorId.insertar(administrador.getId(), administrador);
    }

    /** Busca un administrador por id, o null si no existe. */
    public Administrador buscarAdministrador(String id) {
        return administradoresPorId.obtener(id);
    }
}
