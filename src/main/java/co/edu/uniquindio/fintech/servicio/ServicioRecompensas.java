package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.excepciones.OperacionInvalidaException;
import co.edu.uniquindio.fintech.excepciones.PuntosInsuficientesException;
import co.edu.uniquindio.fintech.modelo.Beneficio;
import co.edu.uniquindio.fintech.modelo.CanjeBeneficio;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.NivelUsuario;
import co.edu.uniquindio.fintech.modelo.enums.TipoBeneficio;
import co.edu.uniquindio.fintech.modelo.enums.TipoNotificacion;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.repositorio.RepositorioUsuarios;
import co.edu.uniquindio.fintech.util.GeneradorIdentificadores;
import co.edu.uniquindio.fintech.util.RelojSistema;

/**
 * Todo lo de puntos, niveles y beneficios: calcula cuantos puntos da cada
 * transaccion, los suma o los quita, sube o baja de nivel y maneja los canjes.
 */
public class ServicioRecompensas {

    // Codigos de los beneficios del catalogo, los usan las transacciones para saber cual aplica
    public static final String RETIRO_SIN_COMISION = "BEN-01";
    public static final String DESCUENTO_TRANSFERENCIAS = "BEN-02";
    public static final String PUNTOS_DOBLES = "BEN-03";
    public static final String LIMITE_EXTRA = "BEN-04";

    // Cada cuantos pesos se gana la base de puntos
    private static final int PESOS_POR_PUNTO = 10000;

    private ListaEnlazada<Beneficio> catalogo;
    private RepositorioUsuarios repositorioUsuarios;
    private ServicioNotificaciones notificaciones;
    private RelojSistema reloj;
    private GeneradorIdentificadores generador;

    public ServicioRecompensas(RepositorioUsuarios repositorioUsuarios, ServicioNotificaciones notificaciones,
                               RelojSistema reloj, GeneradorIdentificadores generador) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.notificaciones = notificaciones;
        this.reloj = reloj;
        this.generador = generador;
        this.catalogo = new ListaEnlazada<>();
        cargarCatalogo();
    }

    /**
     * Puntos que da una transaccion: (valor / 10.000) por el factor del tipo,
     * con division entera, y luego se le suma el bono del nivel.
     */
    public int calcularPuntos(TipoTransaccion tipo, double valor, NivelUsuario nivel) {
        int factor;
        switch (tipo) {
            case RECARGA:
                factor = 1;
                break;
            case RETIRO:
                factor = 2;
                break;
            case TRANSFERENCIA_EXTERNA:
                factor = 3;
                break;
            default:
                // Las internas no dan puntos para que nadie gane moviendo plata entre sus billeteras
                factor = 0;
                break;
        }
        int puntosBase = ((int) (valor / PESOS_POR_PUNTO)) * factor;
        int bono = (int) (puntosBase * nivel.getBonoPuntos() / 100);
        return puntosBase + bono;
    }

    /** Si el usuario tiene puntos dobles vigentes, duplica los puntos y gasta un uso. */
    public int aplicarPuntosDobles(Usuario usuario, int puntos) {
        if (puntos <= 0) {
            return puntos;
        }
        CanjeBeneficio canje = buscarCanjeVigente(usuario, PUNTOS_DOBLES);
        if (canje == null) {
            return puntos;
        }
        canje.usar();
        return (int) (puntos * canje.getBeneficio().getValor());
    }

    /** Suma puntos al usuario y revisa si cambia de nivel. */
    public void otorgar(Usuario usuario, int puntos) {
        if (puntos <= 0) {
            return;
        }
        int puntosAnteriores = usuario.getPuntosAcumulados();
        usuario.sumarPuntos(puntos);
        repositorioUsuarios.actualizarPuntos(usuario, puntosAnteriores);
        actualizarNivel(usuario);
    }

    /** Quita puntos al usuario (al revertir) y revisa si baja de nivel. */
    public void descontar(Usuario usuario, int puntos) {
        if (puntos <= 0) {
            return;
        }
        int puntosAnteriores = usuario.getPuntosAcumulados();
        usuario.restarPuntos(puntos);
        repositorioUsuarios.actualizarPuntos(usuario, puntosAnteriores);
        actualizarNivel(usuario);
    }

    /** Recalcula el nivel segun los puntos acumulados y avisa si subio o bajo. */
    public void actualizarNivel(Usuario usuario) {
        NivelUsuario anterior = usuario.getNivel();
        NivelUsuario nuevo = NivelUsuario.desdePuntos(usuario.getPuntosAcumulados());
        if (nuevo == anterior) {
            return;
        }
        usuario.setNivel(nuevo);
        if (nuevo.esIgualOMayorQue(anterior)) {
            notificaciones.generar(usuario, TipoNotificacion.ASCENSO_NIVEL,
                    "¡Felicitaciones! Subiste al nivel " + nuevo.getTexto() + ".");
        } else {
            notificaciones.generar(usuario, TipoNotificacion.DESCENSO_NIVEL,
                    "Bajaste al nivel " + nuevo.getTexto() + " por una reversión de puntos.");
        }
    }

    /** Canjea un beneficio del catalogo con los puntos disponibles del usuario. */
    public CanjeBeneficio canjear(Usuario usuario, String codigoBeneficio) {
        Beneficio beneficio = buscarBeneficio(codigoBeneficio);
        if (beneficio == null || !beneficio.isActivo()) {
            throw new OperacionInvalidaException("El beneficio no existe o no está disponible");
        }
        if (!usuario.getNivel().esIgualOMayorQue(beneficio.getNivelMinimo())) {
            throw new OperacionInvalidaException("Necesitas nivel " + beneficio.getNivelMinimo().getTexto()
                    + " para canjear este beneficio");
        }
        if (usuario.getPuntosDisponibles() < beneficio.getCostoPuntos()) {
            throw new PuntosInsuficientesException("No tienes puntos suficientes: necesitas "
                    + beneficio.getCostoPuntos() + " y tienes " + usuario.getPuntosDisponibles());
        }

        usuario.gastarPuntos(beneficio.getCostoPuntos());
        CanjeBeneficio canje = new CanjeBeneficio(generador.siguienteCanje(), beneficio, reloj.ahora(),
                beneficio.getCostoPuntos());
        usuario.getCanjes().agregarAlFinal(canje);
        notificaciones.generar(usuario, TipoNotificacion.CANJE_BENEFICIO,
                "Canjeaste \"" + beneficio.getNombre() + "\" por " + beneficio.getCostoPuntos() + " puntos.");
        notificaciones.despacharPendientes();
        return canje;
    }

    /** Dice si el usuario puede canjear el beneficio ahora mismo (nivel y puntos). Sirve para la interfaz. */
    public boolean puedeCanjear(Usuario usuario, Beneficio beneficio) {
        return beneficio.estaDisponiblePara(usuario.getNivel())
                && usuario.getPuntosDisponibles() >= beneficio.getCostoPuntos();
    }

    /**
     * Busca un canje del usuario que todavia sirva para ese beneficio.
     * El de transacciones extra solo vale el mismo dia en que se canjeo.
     */
    public CanjeBeneficio buscarCanjeVigente(Usuario usuario, String codigoBeneficio) {
        for (CanjeBeneficio canje : usuario.getCanjes()) {
            if (canje.getBeneficio().getCodigo().equals(codigoBeneficio) && canje.tieneUsos()) {
                if (canje.getBeneficio().getTipo() != TipoBeneficio.AUMENTO_LIMITE) {
                    return canje;
                }
                if (reloj.esHoy(canje.getFecha())) {
                    return canje;
                }
                // Ya paso el dia, ese canje se vencio
                canje.setUsosRestantes(0);
            }
        }
        return null;
    }

    /** Devuelve el canje de descuento en comision que aplica para ese tipo, o null si no hay. */
    public CanjeBeneficio buscarDescuentoComision(Usuario usuario, TipoTransaccion tipo) {
        if (tipo == TipoTransaccion.RETIRO) {
            return buscarCanjeVigente(usuario, RETIRO_SIN_COMISION);
        }
        if (tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA) {
            return buscarCanjeVigente(usuario, DESCUENTO_TRANSFERENCIAS);
        }
        return null;
    }

    /** Transacciones extra que tiene hoy el usuario por haber canjeado el aumento de limite. */
    public int limiteExtraHoy(Usuario usuario) {
        CanjeBeneficio canje = buscarCanjeVigente(usuario, LIMITE_EXTRA);
        if (canje == null) {
            return 0;
        }
        return (int) canje.getBeneficio().getValor();
    }

    /** Busca un beneficio del catalogo por codigo. */
    public Beneficio buscarBeneficio(String codigo) {
        for (Beneficio beneficio : catalogo) {
            if (beneficio.getCodigo().equals(codigo)) {
                return beneficio;
            }
        }
        return null;
    }

    public ListaEnlazada<Beneficio> getCatalogo() {
        return catalogo;
    }

    // Catalogo inicial que se acordo en el diseño
    private void cargarCatalogo() {
        catalogo.agregarAlFinal(new Beneficio(RETIRO_SIN_COMISION, "Retiros sin comisión",
                "Tus próximos 3 retiros no pagan comisión", 300, TipoBeneficio.DESCUENTO_COMISION,
                100, 3, NivelUsuario.BRONCE));
        catalogo.agregarAlFinal(new Beneficio(DESCUENTO_TRANSFERENCIAS, "50 % menos comisión en transferencias",
                "Tus próximas 5 transferencias externas pagan la mitad de la comisión", 500,
                TipoBeneficio.DESCUENTO_COMISION, 50, 5, NivelUsuario.PLATA));
        catalogo.agregarAlFinal(new Beneficio(PUNTOS_DOBLES, "Puntos dobles",
                "Tus próximas 5 transacciones que den puntos dan el doble", 800, TipoBeneficio.PUNTOS_DOBLES,
                2, 5, NivelUsuario.ORO));
        catalogo.agregarAlFinal(new Beneficio(LIMITE_EXTRA, "+10 transacciones por un día",
                "Hoy puedes hacer 10 transacciones más de lo que permite tu nivel", 1000,
                TipoBeneficio.AUMENTO_LIMITE, 10, 1, NivelUsuario.ORO));
    }
}
