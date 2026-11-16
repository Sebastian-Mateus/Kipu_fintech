package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.excepciones.CredencialesInvalidasException;
import co.edu.uniquindio.fintech.excepciones.OperacionInvalidaException;
import co.edu.uniquindio.fintech.excepciones.UsuarioNoEncontradoException;
import co.edu.uniquindio.fintech.modelo.Administrador;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Persona;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.EstadoBilletera;
import co.edu.uniquindio.fintech.modelo.enums.NivelUsuario;
import co.edu.uniquindio.fintech.repositorio.RepositorioBilleteras;
import co.edu.uniquindio.fintech.repositorio.RepositorioUsuarios;
import co.edu.uniquindio.fintech.util.RelojSistema;
import co.edu.uniquindio.fintech.util.Validador;

/**
 * Registro, edicion, busqueda y eliminacion de usuarios, y el login.
 */
public class ServicioUsuarios {

    private RepositorioUsuarios repositorioUsuarios;
    private RepositorioBilleteras repositorioBilleteras;
    private ServicioProgramadas programadas;
    private RelojSistema reloj;

    public ServicioUsuarios(RepositorioUsuarios repositorioUsuarios, RepositorioBilleteras repositorioBilleteras,
                            ServicioProgramadas programadas, RelojSistema reloj) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioBilleteras = repositorioBilleteras;
        this.programadas = programadas;
        this.reloj = reloj;
    }

    /** Registra un cliente nuevo despues de validar los datos. */
    public Usuario registrar(String id, String nombre, String correo, String contrasena, String telefono) {
        if (!Validador.esCedulaValida(id)) {
            throw new OperacionInvalidaException("La cédula debe tener solo números (entre 6 y 10 dígitos)");
        }
        if (repositorioUsuarios.existe(id.trim())) {
            throw new OperacionInvalidaException("Ya existe un usuario con la cédula " + id);
        }
        validarDatos(nombre, correo, telefono);
        if (Validador.estaVacio(contrasena)) {
            throw new OperacionInvalidaException("La contraseña no puede estar vacía");
        }

        Usuario usuario = new Usuario(id.trim(), nombre.trim(), correo.trim(), contrasena, telefono.trim(),
                reloj.ahora());
        repositorioUsuarios.guardar(usuario);
        return usuario;
    }

    /** Registra un administrador (lo usa el cargador de datos de ejemplo). */
    public Administrador registrarAdministrador(String id, String nombre, String correo, String contrasena,
                                                String cargo) {
        if (repositorioUsuarios.existe(id)) {
            throw new OperacionInvalidaException("Ya existe alguien con el id " + id);
        }
        Administrador administrador = new Administrador(id, nombre, correo, contrasena, cargo);
        repositorioUsuarios.guardarAdministrador(administrador);
        return administrador;
    }

    /** Cambia los datos del usuario. Si la contraseña nueva viene vacia se deja la anterior. */
    public Usuario modificar(String id, String nombre, String correo, String telefono, String contrasenaNueva) {
        Usuario usuario = buscar(id);
        validarDatos(nombre, correo, telefono);
        usuario.setNombre(nombre.trim());
        usuario.setCorreo(correo.trim());
        usuario.setTelefono(telefono.trim());
        if (!Validador.estaVacio(contrasenaNueva)) {
            usuario.setContrasena(contrasenaNueva);
        }
        return usuario;
    }

    /** Busca un usuario por cedula, y si no existe lanza excepcion. */
    public Usuario buscar(String id) {
        Usuario usuario = null;
        if (id != null) {
            usuario = repositorioUsuarios.buscar(id.trim());
        }
        if (usuario == null) {
            throw new UsuarioNoEncontradoException("No existe un usuario con la cédula " + id);
        }
        return usuario;
    }

    /**
     * Elimina un usuario. Solo se puede si todas sus billeteras estan en cero
     * y no tiene operaciones programadas activas.
     */
    public void eliminar(String id) {
        Usuario usuario = buscar(id);
        for (Billetera billetera : usuario.getBilleteras()) {
            if (!Validador.sonIguales(billetera.getSaldo(), 0)) {
                throw new OperacionInvalidaException("No se puede eliminar: la billetera " + billetera.getCodigo()
                        + " todavía tiene saldo");
            }
        }
        if (programadas.tieneActivas(usuario)) {
            throw new OperacionInvalidaException("No se puede eliminar: tiene operaciones programadas activas");
        }

        for (Billetera billetera : usuario.getBilleteras()) {
            billetera.setEstado(EstadoBilletera.CERRADA);
            repositorioBilleteras.eliminar(billetera);
        }
        usuario.setActivo(false);
        repositorioUsuarios.eliminar(usuario);
    }

    /** Activa o desactiva a un usuario (uno inactivo no puede hacer transacciones). */
    public void cambiarEstado(String id, boolean activo) {
        buscar(id).setActivo(activo);
    }

    /** Revisa el usuario y la contraseña. Devuelve la persona que entro (cliente o administrador). */
    public Persona autenticar(String id, String contrasena) {
        if (Validador.estaVacio(id) || contrasena == null) {
            throw new CredencialesInvalidasException("Escribe tu usuario y tu contraseña");
        }
        String idLimpio = id.trim();

        Administrador administrador = repositorioUsuarios.buscarAdministrador(idLimpio);
        if (administrador != null && administrador.verificarContrasena(contrasena)) {
            return administrador;
        }

        Usuario usuario = repositorioUsuarios.buscar(idLimpio);
        if (usuario == null || !usuario.verificarContrasena(contrasena)) {
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos");
        }
        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException("Tu cuenta está inactiva, comunícate con el administrador");
        }
        return usuario;
    }

    /** Todos los usuarios registrados. */
    public ListaEnlazada<Usuario> listar() {
        return repositorioUsuarios.listar();
    }

    /** Usuarios ordenados de mas puntos acumulados a menos. */
    public ListaEnlazada<Usuario> ranking() {
        return repositorioUsuarios.rankingPorPuntos();
    }

    /** Usuarios de un nivel, buscando en el arbol por el rango de puntos del nivel. */
    public ListaEnlazada<Usuario> usuariosPorNivel(NivelUsuario nivel) {
        return repositorioUsuarios.buscarPorRangoPuntos(nivel.getPuntosMinimos(), nivel.getPuntosMaximos());
    }

    private void validarDatos(String nombre, String correo, String telefono) {
        if (Validador.estaVacio(nombre)) {
            throw new OperacionInvalidaException("El nombre no puede estar vacío");
        }
        if (!Validador.esCorreoValido(correo)) {
            throw new OperacionInvalidaException("El correo no es válido");
        }
        if (!Validador.esTelefonoValido(telefono)) {
            throw new OperacionInvalidaException("El teléfono debe tener solo números (entre 7 y 10 dígitos)");
        }
    }
}
