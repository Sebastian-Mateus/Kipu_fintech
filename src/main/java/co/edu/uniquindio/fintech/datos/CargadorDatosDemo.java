package co.edu.uniquindio.fintech.datos;

import co.edu.uniquindio.fintech.Plataforma;
import co.edu.uniquindio.fintech.excepciones.FintechException;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.modelo.enums.Frecuencia;
import co.edu.uniquindio.fintech.modelo.enums.TipoBilletera;
import co.edu.uniquindio.fintech.modelo.enums.TipoTransaccion;
import co.edu.uniquindio.fintech.servicio.ServicioProgramadas;
import co.edu.uniquindio.fintech.servicio.ServicioRecompensas;
import co.edu.uniquindio.fintech.servicio.ServicioTransacciones;
import co.edu.uniquindio.fintech.util.RelojSistema;
import co.edu.uniquindio.fintech.util.Validador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Carga datos de ejemplo al arrancar la aplicacion. En vez de meter los datos
 * a mano en las estructuras, se simulan tres semanas de uso moviendo el reloj
 * y haciendo las operaciones por los servicios, asi todo queda registrado
 * igual que si la gente hubiera usado la plataforma de verdad (historiales,
 * arboles, grafos, puntos, niveles, auditoria...).
 *
 * Al final se dejan a proposito algunos casos para la demostracion: alertas
 * de fraude de cada tipo, un ciclo de transferencias, una reversion y
 * operaciones programadas.
 */
public class CargadorDatosDemo {

    private static final int DIAS_DE_HISTORIA = 21;
    private static final int HORA_INICIO = 8;
    private static final int HORA_FIN = 20;
    private static final String CONTRASENA_DEMO = "1234";

    // Los usuarios no van ordenados por puntos, asi el arbol por puntos no se degenera
    private static final String[][] DATOS_USUARIOS = {
            {"1094887321", "Laura Restrepo", "laura.restrepo@correo.com", "3104567890"},
            {"1097330458", "Andrés Cardona", "andres.cardona@correo.com", "3157894561"},
            {"1094215976", "Valentina Ospina", "valentina.ospina@correo.com", "3201239876"},
            {"1098650123", "Juan Pablo Henao", "juanpablo.henao@correo.com", "3006543210"},
            {"1094473812", "Camila Zuluaga", "camila.zuluaga@correo.com", "3112345678"},
            {"1096128745", "Santiago Quintero", "santiago.quintero@correo.com", "3168765432"},
            {"1094902567", "Mariana López", "mariana.lopez@correo.com", "3189876543"},
            {"1097745390", "Daniel Arango", "daniel.arango@correo.com", "3015678901"}
    };
    // Con el saldo inicial cada usuario arranca en un nivel distinto (1 punto por cada 10.000)
    private static final double[] SALDOS_INICIALES = {
            15_000_000, 2_500_000, 52_000_000, 7_000_000, 1_800_000, 8_000_000, 12_000_000, 3_000_000
    };
    // Monto alrededor del cual se mueve cada usuario en su dia a dia
    private static final double[] MONTOS_TIPICOS = {120_000, 45_000, 300_000, 90_000, 35_000, 100_000, 150_000, 60_000};
    private static final int[] CANTIDAD_BILLETERAS = {3, 3, 4, 3, 2, 3, 4, 2};

    private static final String[] NOMBRES_BILLETERAS = {"Ahorro", "Gastos diarios", "Transporte", "Compras"};
    private static final TipoBilletera[] TIPOS_BILLETERAS = {
            TipoBilletera.AHORRO, TipoBilletera.GASTOS_DIARIOS, TipoBilletera.TRANSPORTE, TipoBilletera.COMPRAS
    };
    private static final double[] UMBRALES = {200_000, 50_000, 20_000, 50_000};

    private static final String[] MOTIVOS = {
            "Almuerzo", "Mercado", "Regalo de cumpleaños", "Pago de deuda", "Salida a cine",
            "Servicios públicos", "Cuota del gimnasio", "Domicilio"
    };

    // Posiciones de algunos usuarios que se usan en los casos especiales
    private static final int LAURA = 0;
    private static final int ANDRES = 1;
    private static final int VALENTINA = 2;
    private static final int JUAN_PABLO = 3;
    private static final int CAMILA = 4;
    private static final int SANTIAGO = 5;
    private static final int MARIANA = 6;
    private static final int DANIEL = 7;

    private Plataforma plataforma;
    private RelojSistema reloj;
    private ServicioTransacciones transacciones;
    private ServicioProgramadas programadas;
    private Random aleatorio;
    private Usuario[] usuarios;

    private CargadorDatosDemo(Plataforma plataforma) {
        this.plataforma = plataforma;
        this.reloj = plataforma.getReloj();
        this.transacciones = plataforma.getServicioTransacciones();
        this.programadas = plataforma.getServicioProgramadas();
        // Semilla fija para que los datos salgan iguales cada vez que se abre la aplicacion
        this.aleatorio = new Random(2026);
        this.usuarios = new Usuario[DATOS_USUARIOS.length];
    }

    /** Llena la plataforma con el administrador, 8 clientes y tres semanas de movimientos. */
    public static void cargar(Plataforma plataforma) {
        new CargadorDatosDemo(plataforma).cargarTodo();
    }

    private void cargarTodo() {
        LocalDate hoy = reloj.hoy();
        LocalDate primerDia = hoy.minusDays(DIAS_DE_HISTORIA);
        reloj.establecer(primerDia.atTime(7, 0));

        plataforma.getServicioUsuarios().registrarAdministrador("admin", "Administrador Kipu", "admin@kipu.co",
                "admin123", "Administrador general");
        crearUsuarios();
        cargarSaldosIniciales();

        for (int numeroDia = 0; numeroDia < DIAS_DE_HISTORIA; numeroDia++) {
            LocalDate dia = primerDia.plusDays(numeroDia);
            if (numeroDia == 14) {
                operacionDeMadrugada(dia);
            }
            simularDia(dia);
            eventosDelDia(numeroDia, dia);
        }

        casosDeHoy(hoy);
        programarPendientes(hoy);
    }

    private void crearUsuarios() {
        for (int indice = 0; indice < DATOS_USUARIOS.length; indice++) {
            String[] datos = DATOS_USUARIOS[indice];
            Usuario usuario = plataforma.getServicioUsuarios().registrar(datos[0], datos[1], datos[2],
                    CONTRASENA_DEMO, datos[3]);
            for (int posicion = 0; posicion < CANTIDAD_BILLETERAS[indice]; posicion++) {
                plataforma.getServicioBilleteras().crear(usuario, NOMBRES_BILLETERAS[posicion],
                        TIPOS_BILLETERAS[posicion], UMBRALES[posicion]);
            }
            usuarios[indice] = usuario;
        }
    }

    // Cada usuario recarga su ahorro y reparte un poco en las otras bileteras
    private void cargarSaldosIniciales() {
        for (int indice = 0; indice < usuarios.length; indice++) {
            Usuario usuario = usuarios[indice];
            Billetera ahorro = billetera(indice, 0);
            pasarMinutos(2);
            transacciones.recargar(usuario, ahorro.getCodigo(), SALDOS_INICIALES[indice], "Saldo inicial");
            for (int posicion = 1; posicion < usuario.getBilleteras().tamanio(); posicion++) {
                pasarMinutos(1);
                transacciones.transferirInterna(usuario, ahorro.getCodigo(), billetera(indice, posicion).getCodigo(),
                        redondearMonto(SALDOS_INICIALES[indice] * 0.15), "Reparto inicial");
            }
        }
    }

    private void simularDia(LocalDate dia) {
        reloj.establecer(dia.atTime(HORA_INICIO, 0));
        programadas.procesarPendientes();

        int cantidadOperaciones = 6 + aleatorio.nextInt(6);
        for (int numero = 0; numero < cantidadOperaciones; numero++) {
            pasarMinutos(20 + aleatorio.nextInt(70));
            if (reloj.ahora().getHour() >= HORA_FIN) {
                return;
            }
            operacionAleatoria(aleatorio.nextInt(usuarios.length));
        }
    }

    /*
     * Una operacion cualquiera de un usuario: recarga, transferencia interna,
     * transferencia a otra persona o retiro. Si falla (por ejemplo por saldo)
     * queda como rechazada, igual que pasaria en la vida real.
     */
    private void operacionAleatoria(int indice) {
        Usuario usuario = usuarios[indice];
        double monto = redondearMonto(MONTOS_TIPICOS[indice] * (0.5 + aleatorio.nextDouble()));
        int opcion = aleatorio.nextInt(100);
        try {
            if (opcion < 20) {
                Billetera destino = billeteraAlAzar(usuario);
                transacciones.recargar(usuario, destino.getCodigo(), monto * 2, "Recarga desde el banco");
                return;
            }

            Billetera origen = billeteraConMasSaldo(usuario);
            monto = Math.min(monto, redondearMonto(origen.getSaldo() * 0.3));
            if (monto < 1000) {
                return;
            }
            if (opcion < 45) {
                Billetera destino = otraBilleteraPropia(usuario, origen);
                transacciones.transferirInterna(usuario, origen.getCodigo(), destino.getCodigo(), monto,
                        "Movimiento entre billeteras");
            } else if (opcion < 85) {
                int otro = (indice + 1 + aleatorio.nextInt(usuarios.length - 1)) % usuarios.length;
                Billetera destino = billeteraAlAzar(usuarios[otro]);
                transacciones.transferirExterna(usuario, origen.getCodigo(), destino.getCodigo(), monto,
                        MOTIVOS[aleatorio.nextInt(MOTIVOS.length)]);
            } else {
                transacciones.retirar(usuario, origen.getCodigo(), monto, "Retiro en cajero");
            }
        } catch (FintechException error) {
            // No pasa nada, la transaccion ya quedo registrada como rechazada
        }
    }

    // Una recarga a las 2:30 a.m. para que salga la regla de horario inusual
    private void operacionDeMadrugada(LocalDate dia) {
        reloj.establecer(dia.atTime(2, 30));
        intentar(usuarios[SANTIAGO], "recarga", null, billetera(SANTIAGO, 0), 50_000, "Recarga de madrugada");
    }

    private void eventosDelDia(int numeroDia, LocalDate dia) {
        pasarMinutos(10);
        if (numeroDia == 5) {
            // Recarga diaria del transporte: se va a ejecutar todos los dias (registro de programadas)
            programar(usuarios[VALENTINA], TipoTransaccion.RECARGA, null, billetera(VALENTINA, 2), 20_000,
                    dia.plusDays(1).atTime(7, 0), Frecuencia.DIARIA, "Recarga diaria del transporte");
        } else if (numeroDia == 10) {
            // Un retiro que no le alcanza: falla 3 veces y se desactiva solo
            programar(usuarios[ANDRES], TipoTransaccion.RETIRO, billetera(ANDRES, 2), null, 900_000,
                    dia.plusDays(1).atTime(7, 0), Frecuencia.DIARIA, "Retiro para la moto");
        } else if (numeroDia == 17) {
            // Una transferencia que despues se revierte
            Transaccion transaccion = intentar(usuarios[LAURA], "externa", billeteraConMasSaldo(usuarios[LAURA]),
                    billetera(MARIANA, 0), 150_000, "Boletas del concierto");
            if (transaccion != null) {
                pasarMinutos(45);
                plataforma.getServicioReversion().revertir(transaccion.getId());
            }
        } else if (numeroDia == 19) {
            canjear(usuarios[VALENTINA], ServicioRecompensas.PUNTOS_DOBLES);
            canjear(usuarios[LAURA], ServicioRecompensas.RETIRO_SIN_COMISION);
            canjear(usuarios[JUAN_PABLO], ServicioRecompensas.DESCUENTO_TRANSFERENCIAS);
        }
    }

    // Casos armados a proposito para mostrar en la sustentacion
    private void casosDeHoy(LocalDate hoy) {
        reloj.establecer(hoy.atTime(HORA_INICIO, 0));
        programadas.procesarPendientes();

        // Rafaga: 5 transferencias en menos de 10 minutos (riesgo medio)
        for (int numero = 0; numero < 5; numero++) {
            pasarMinutos(2);
            intentar(usuarios[JUAN_PABLO], "interna", billetera(JUAN_PABLO, 0), billetera(JUAN_PABLO, 1), 20_000,
                    "Ajuste de presupuesto");
        }

        // Destino repetido: 3 envios a la misma billetera en 30 minutos (riesgo medio)
        for (int numero = 0; numero < 3; numero++) {
            pasarMinutos(5);
            intentar(usuarios[LAURA], "externa", billetera(LAURA, 0), billetera(ANDRES, 0), 30_000,
                    "Pago de la vaca");
        }

        // Fragmentacion: 3 billeteras propias mandando al mismo destino (riesgo alto, se rechaza)
        for (int posicion = 0; posicion < 3; posicion++) {
            pasarMinutos(3);
            intentar(usuarios[MARIANA], "externa", billetera(MARIANA, posicion), billetera(CAMILA, 0), 25_000,
                    "Pago compartido");
        }

        // Monto atipico: una recarga de 6 veces su promedio (riesgo alto, se rechaza)
        pasarMinutos(15);
        Usuario andres = usuarios[ANDRES];
        double montoRaro = redondearMonto(andres.getPerfil().promedioMonto() * 6);
        intentar(andres, "recarga", null, billetera(ANDRES, 0), montoRaro, "Recarga inesperada");

        // Ciclo en el grafo: Daniel -> Santiago -> Valentina -> Daniel
        pasarMinutos(10);
        intentar(usuarios[DANIEL], "externa", billetera(DANIEL, 0), billetera(SANTIAGO, 0), 40_000, "Préstamo");
        pasarMinutos(10);
        intentar(usuarios[SANTIAGO], "externa", billetera(SANTIAGO, 0), billetera(VALENTINA, 0), 40_000,
                "Pago del préstamo");
        pasarMinutos(10);
        intentar(usuarios[VALENTINA], "externa", billetera(VALENTINA, 0), billetera(DANIEL, 0), 40_000,
                "Devolución");

        // Una recarga reciente que todavia se puede revertir en la demostracion
        pasarMinutos(10);
        intentar(usuarios[CAMILA], "recarga", null, billetera(CAMILA, 1), 100_000, "Recarga para la semana");
    }

    private void programarPendientes(LocalDate hoy) {
        // Esta se ejecuta en menos de 24 horas, por eso Laura ya tiene la alerta
        programar(usuarios[LAURA], TipoTransaccion.TRANSFERENCIA_INTERNA, billetera(LAURA, 1), billetera(LAURA, 0),
                200_000, hoy.plusDays(1).atTime(8, 0), Frecuencia.SEMANAL, "Ahorro semanal");
        programar(usuarios[JUAN_PABLO], TipoTransaccion.TRANSFERENCIA_EXTERNA, billetera(JUAN_PABLO, 0),
                billetera(LAURA, 0), 1_200_000, hoy.plusDays(2).atTime(9, 0), Frecuencia.MENSUAL, "Pago del arriendo");
        programar(usuarios[MARIANA], TipoTransaccion.RETIRO, billetera(MARIANA, 1), null, 80_000,
                hoy.plusDays(3).atTime(10, 0), Frecuencia.UNA_VEZ, "Retiro para el mercado");
    }

    // ---------- Ayudas ----------

    /** Hace una operacion y si falla devuelve null en vez de lanzar la excepcion. */
    private Transaccion intentar(Usuario usuario, String tipo, Billetera origen, Billetera destino, double monto,
                                 String descripcion) {
        try {
            if (tipo.equals("recarga")) {
                return transacciones.recargar(usuario, destino.getCodigo(), monto, descripcion);
            }
            if (tipo.equals("interna")) {
                return transacciones.transferirInterna(usuario, origen.getCodigo(), destino.getCodigo(), monto,
                        descripcion);
            }
            return transacciones.transferirExterna(usuario, origen.getCodigo(), destino.getCodigo(), monto,
                    descripcion);
        } catch (FintechException error) {
            return null;
        }
    }

    private void programar(Usuario usuario, TipoTransaccion tipo, Billetera origen, Billetera destino, double monto,
                           LocalDateTime fecha, Frecuencia frecuencia, String descripcion) {
        String codigoOrigen = origen == null ? null : origen.getCodigo();
        String codigoDestino = destino == null ? null : destino.getCodigo();
        try {
            programadas.programar(usuario, tipo, codigoOrigen, codigoDestino, monto, fecha, frecuencia, descripcion);
        } catch (FintechException error) {
            // Si algo no cuadra con los datos, simplemente no se programa
        }
    }

    private void canjear(Usuario usuario, String codigoBeneficio) {
        try {
            plataforma.getServicioRecompensas().canjear(usuario, codigoBeneficio);
        } catch (FintechException error) {
            // Si no le alcanzan los puntos no se canjea
        }
    }

    private Billetera billetera(int indiceUsuario, int posicion) {
        return usuarios[indiceUsuario].getBilleteras().obtener(posicion);
    }

    private Billetera billeteraAlAzar(Usuario usuario) {
        return usuario.getBilleteras().obtener(aleatorio.nextInt(usuario.getBilleteras().tamanio()));
    }

    private Billetera billeteraConMasSaldo(Usuario usuario) {
        Billetera mayor = usuario.getBilleteras().obtenerPrimero();
        for (Billetera billetera : usuario.getBilleteras()) {
            if (billetera.getSaldo() > mayor.getSaldo()) {
                mayor = billetera;
            }
        }
        return mayor;
    }

    private Billetera otraBilleteraPropia(Usuario usuario, Billetera distintaA) {
        Billetera elegida = billeteraAlAzar(usuario);
        while (elegida.equals(distintaA)) {
            elegida = billeteraAlAzar(usuario);
        }
        return elegida;
    }

    private void pasarMinutos(int minutos) {
        reloj.establecer(reloj.ahora().plusMinutes(minutos));
    }

    // Montos redondos a la centena, como los que escribe una persona
    private double redondearMonto(double monto) {
        return Validador.redondear(Math.round(monto / 100) * 100.0);
    }
}
