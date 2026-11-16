package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.estructuras.Arista;
import co.edu.uniquindio.fintech.estructuras.ColaPrioridad;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.estructuras.TablaHash;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.EstadoTransaccion;
import co.edu.uniquindio.fintech.modelo.enums.TipoBilletera;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.repositorio.RepositorioBilleteras;
import co.edu.uniquindio.fintech.repositorio.RepositorioTransacciones;
import co.edu.uniquindio.fintech.repositorio.RepositorioUsuarios;
import co.edu.uniquindio.fintech.util.Validador;

import java.time.LocalDateTime;

/**
 * Informes para el panel del administrador. Los contadores se guardan en tablas
 * hash y se actualizan en O(1) con cada transaccion exitosa; para los top N se
 * usa la cola de prioridad. Lo que depende de fechas se saca de la lista global
 * y lo de rutas y ciclos sale de los grafos.
 */
public class ServicioAnalitica {

    private TablaHash<String, Integer> usosPorBilletera;
    private TablaHash<String, Integer> transferenciasPorUsuario;
    private TablaHash<TipoBilletera, Integer> actividadPorCategoria;
    private TablaHash<TipoTransaccion, Integer> frecuenciaPorTipo;

    private RepositorioTransacciones repositorioTransacciones;
    private RepositorioBilleteras repositorioBilleteras;
    private RepositorioUsuarios repositorioUsuarios;

    public ServicioAnalitica(RepositorioTransacciones repositorioTransacciones,
                             RepositorioBilleteras repositorioBilleteras,
                             RepositorioUsuarios repositorioUsuarios) {
        this.repositorioTransacciones = repositorioTransacciones;
        this.repositorioBilleteras = repositorioBilleteras;
        this.repositorioUsuarios = repositorioUsuarios;
        this.usosPorBilletera = new TablaHash<>();
        this.transferenciasPorUsuario = new TablaHash<>();
        this.actividadPorCategoria = new TablaHash<>();
        this.frecuenciaPorTipo = new TablaHash<>();
    }

    /** Actualiza los contadores con una transaccion exitosa. O(1). */
    public void registrar(Transaccion transaccion) {
        sumarUno(frecuenciaPorTipo, transaccion.getTipo());

        Billetera origen = transaccion.getBilleteraOrigen();
        Billetera destino = transaccion.getBilleteraDestino();
        if (origen != null) {
            sumarUno(usosPorBilletera, origen.getCodigo());
            sumarUno(actividadPorCategoria, origen.getTipo());
        }
        if (destino != null) {
            sumarUno(usosPorBilletera, destino.getCodigo());
            sumarUno(actividadPorCategoria, destino.getTipo());
        }
        if (origen != null && destino != null && transaccion.getTipo() != TipoTransaccion.REVERSION) {
            sumarUno(transferenciasPorUsuario, transaccion.getUsuario().getId());
        }
    }

    /** Las n billeteras con mas movimientos. */
    public ListaEnlazada<ElementoRanking<Billetera>> billeterasMasUsadas(int cantidad) {
        ColaPrioridad<ElementoRanking<Billetera>> monticulo =
                new ColaPrioridad<>(new ComparadorRanking<Billetera>());
        for (String codigo : usosPorBilletera.claves()) {
            Billetera billetera = repositorioBilleteras.buscar(codigo);
            if (billetera != null) {
                monticulo.insertar(new ElementoRanking<>(billetera, usosPorBilletera.obtener(codigo)));
            }
        }
        return sacarPrimeros(monticulo, cantidad);
    }

    /** Los n usuarios que mas transferencias han hecho. */
    public ListaEnlazada<ElementoRanking<Usuario>> usuariosConMasTransferencias(int cantidad) {
        ColaPrioridad<ElementoRanking<Usuario>> monticulo = new ColaPrioridad<>(new ComparadorRanking<Usuario>());
        for (String id : transferenciasPorUsuario.claves()) {
            Usuario usuario = repositorioUsuarios.buscar(id);
            if (usuario != null) {
                monticulo.insertar(new ElementoRanking<>(usuario, transferenciasPorUsuario.obtener(id)));
            }
        }
        return sacarPrimeros(monticulo, cantidad);
    }

    /** Categorias de billetera ordenadas de la mas activa a la menos activa. */
    public ListaEnlazada<ElementoRanking<TipoBilletera>> categoriasMasActivas() {
        ColaPrioridad<ElementoRanking<TipoBilletera>> monticulo =
                new ColaPrioridad<>(new ComparadorRanking<TipoBilletera>());
        for (TipoBilletera tipo : TipoBilletera.values()) {
            monticulo.insertar(new ElementoRanking<>(tipo, contador(actividadPorCategoria, tipo)));
        }
        return sacarPrimeros(monticulo, TipoBilletera.values().length);
    }

    /** Cuantas transacciones exitosas hay de cada tipo (en el orden del enum, para la grafica). */
    public ListaEnlazada<ElementoRanking<TipoTransaccion>> frecuenciaPorTipo() {
        ListaEnlazada<ElementoRanking<TipoTransaccion>> resultado = new ListaEnlazada<>();
        for (TipoTransaccion tipo : TipoTransaccion.values()) {
            resultado.agregarAlFinal(new ElementoRanking<>(tipo, contador(frecuenciaPorTipo, tipo)));
        }
        return resultado;
    }

    /**
     * Plata movida por transacciones exitosas entre dos fechas.
     * Como la lista global esta ordenada por fecha, se para apenas se pasa de "hasta".
     */
    public double montoMovilizado(LocalDateTime desde, LocalDateTime hasta) {
        double total = 0;
        for (Transaccion transaccion : repositorioTransacciones.listar()) {
            if (transaccion.getFecha().isAfter(hasta)) {
                break;
            }
            if (!transaccion.getFecha().isBefore(desde) && cuentaComoMovimiento(transaccion)) {
                total = total + transaccion.getValor();
            }
        }
        return Validador.redondear(total);
    }

    /**
     * Todas las transacciones (de cualquier estado) hechas entre dos fechas, en orden.
     * Como la lista global esta ordenada por fecha, se para apenas se pasa de "hasta".
     */
    public ListaEnlazada<Transaccion> transaccionesEnPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        ListaEnlazada<Transaccion> resultado = new ListaEnlazada<>();
        for (Transaccion transaccion : repositorioTransacciones.listar()) {
            if (transaccion.getFecha().isAfter(hasta)) {
                break;
            }
            if (!transaccion.getFecha().isBefore(desde)) {
                resultado.agregarAlFinal(transaccion);
            }
        }
        return resultado;
    }

    /** El usuario que mas transacciones hizo entre dos fechas, o null si nadie hizo nada. */
    public ElementoRanking<Usuario> usuarioMasActivo(LocalDateTime desde, LocalDateTime hasta) {
        TablaHash<Usuario, Integer> conteo = new TablaHash<>();
        for (Transaccion transaccion : repositorioTransacciones.listar()) {
            if (transaccion.getFecha().isAfter(hasta)) {
                break;
            }
            if (!transaccion.getFecha().isBefore(desde) && cuentaComoMovimiento(transaccion)) {
                sumarUno(conteo, transaccion.getUsuario());
            }
        }

        ElementoRanking<Usuario> masActivo = null;
        for (Usuario usuario : conteo.claves()) {
            int cantidad = conteo.obtener(usuario);
            if (masActivo == null || cantidad > masActivo.getCantidad()) {
                masActivo = new ElementoRanking<>(usuario, cantidad);
            }
        }
        return masActivo;
    }

    /** Las n transacciones de mayor valor (inorden inverso del arbol). */
    public ListaEnlazada<Transaccion> transaccionesMayorValor(int cantidad) {
        return repositorioTransacciones.mayorValor(cantidad);
    }

    /** Cuantas transacciones exitosas hay en total (para las tarjetas del panel). */
    public int cantidadTransaccionesExitosas() {
        int cantidad = 0;
        for (Transaccion transaccion : repositorioTransacciones.listar()) {
            if (cuentaComoMovimiento(transaccion)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    /** Toda la plata movida desde el principio. */
    public double montoTotalMovilizado() {
        double total = 0;
        for (Transaccion transaccion : repositorioTransacciones.listar()) {
            if (cuentaComoMovimiento(transaccion)) {
                total = total + transaccion.getValor();
            }
        }
        return Validador.redondear(total);
    }

    // ---------- Red de transferencias (grafos) ----------

    /** Las n relaciones entre usuarios con mas transferencias. */
    public ListaEnlazada<Arista<Usuario>> rutasMasFrecuentesUsuarios(int cantidad) {
        return repositorioTransacciones.getGrafoUsuarios().aristasMasFrecuentes(cantidad);
    }

    /** Las n relaciones entre billeteras con mas transferencias. */
    public ListaEnlazada<Arista<Billetera>> rutasMasFrecuentesBilleteras(int cantidad) {
        return repositorioTransacciones.getGrafoBilleteras().aristasMasFrecuentes(cantidad);
    }

    /** Ciclos de plata entre usuarios, por ejemplo A le manda a B, B a C y C otra vez a A. */
    public ListaEnlazada<ListaEnlazada<Usuario>> ciclosEntreUsuarios() {
        return repositorioTransacciones.getGrafoUsuarios().detectarCiclos();
    }

    /** Ciclos de plata entre billeteras. */
    public ListaEnlazada<ListaEnlazada<Billetera>> ciclosEntreBilleteras() {
        return repositorioTransacciones.getGrafoBilleteras().detectarCiclos();
    }

    /** Camino de transferencias de un usuario a otro (vacio si no hay). */
    public ListaEnlazada<Usuario> buscarCamino(Usuario origen, Usuario destino) {
        return repositorioTransacciones.getGrafoUsuarios().buscarCamino(origen, destino);
    }

    /** Todas las relaciones de quien le transfiere a quien. */
    public ListaEnlazada<Arista<Usuario>> transferenciasEntreUsuarios() {
        return repositorioTransacciones.getGrafoUsuarios().aristas();
    }

    /** Todas las relaciones entre billeteras (incluye las transferencias internas). */
    public ListaEnlazada<Arista<Billetera>> transferenciasEntreBilleteras() {
        return repositorioTransacciones.getGrafoBilleteras().aristas();
    }

    // Las reversiones y las rechazadas no se cuentan como plata movida
    private boolean cuentaComoMovimiento(Transaccion transaccion) {
        return transaccion.getEstado() == EstadoTransaccion.EXITOSA
                && transaccion.getTipo() != TipoTransaccion.REVERSION;
    }

    private <K> void sumarUno(TablaHash<K, Integer> tabla, K clave) {
        tabla.insertar(clave, contador(tabla, clave) + 1);
    }

    private <K> int contador(TablaHash<K, Integer> tabla, K clave) {
        Integer valor = tabla.obtener(clave);
        if (valor == null) {
            return 0;
        }
        return valor;
    }

    private <T> ListaEnlazada<ElementoRanking<T>> sacarPrimeros(ColaPrioridad<ElementoRanking<T>> monticulo,
                                                               int cantidad) {
        ListaEnlazada<ElementoRanking<T>> resultado = new ListaEnlazada<>();
        while (resultado.tamanio() < cantidad && !monticulo.estaVacia()) {
            resultado.agregarAlFinal(monticulo.extraer());
        }
        return resultado;
    }
}
