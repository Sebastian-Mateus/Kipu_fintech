package co.edu.uniquindio.fintech.repositorio;

import co.edu.uniquindio.fintech.estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.fintech.estructuras.Arista;
import co.edu.uniquindio.fintech.estructuras.Grafo;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.estructuras.TablaHash;
import co.edu.uniquindio.fintech.modelo.Billetera;
import co.edu.uniquindio.fintech.modelo.Transaccion;
import co.edu.uniquindio.fintech.modelo.Usuario;
import co.edu.uniquindio.fintech.util.Validador;

/**
 * Guarda todas las transacciones. Cada una puede quedar en varios lugares:
 * - una tabla hash por id, para encontrarla rapido (por ejemplo al revertir)
 * - la lista global, que queda ordenada por fecha porque se agregan en orden
 * - un arbol por valor, para sacar las de mayor valor
 * - los grafos de usuarios y de billeteras, para ver quien le manda plata a quien
 *
 * Al arbol y a los grafos solo entran las transacciones exitosas.
 */
public class RepositorioTransacciones {

    private TablaHash<String, Transaccion> transaccionesPorId;
    private ListaEnlazada<Transaccion> todas;
    private ArbolBinarioBusqueda<Double, Transaccion> transaccionesPorValor;
    private Grafo<Usuario> grafoUsuarios;
    private Grafo<Billetera> grafoBilleteras;

    public RepositorioTransacciones() {
        this.transaccionesPorId = new TablaHash<>();
        this.todas = new ListaEnlazada<>();
        this.transaccionesPorValor = new ArbolBinarioBusqueda<>();
        this.grafoUsuarios = new Grafo<>();
        this.grafoBilleteras = new Grafo<>();
    }

    /** Guarda la transaccion en la tabla y al final de la lista global (sirve para cualquier estado). */
    public void guardar(Transaccion transaccion) {
        transaccionesPorId.insertar(transaccion.getId(), transaccion);
        todas.agregarAlFinal(transaccion);
    }

    /** Mete una transaccion exitosa en el arbol por valor y en los grafos. */
    public void indexar(Transaccion transaccion) {
        transaccionesPorValor.insertar(transaccion.getValor(), transaccion);

        Billetera origen = transaccion.getBilleteraOrigen();
        Billetera destino = transaccion.getBilleteraDestino();
        if (origen != null && destino != null) {
            Arista<Billetera> aristaBilleteras = grafoBilleteras.registrarArista(origen, destino, transaccion.getValor());
            aristaBilleteras.setMontoTotal(Validador.redondear(aristaBilleteras.getMontoTotal()));

            // Entre billeteras del mismo dueño no hay arista en el grafo de usuarios
            Usuario remitente = origen.getPropietario();
            Usuario receptor = destino.getPropietario();
            if (!remitente.equals(receptor)) {
                Arista<Usuario> aristaUsuarios = grafoUsuarios.registrarArista(remitente, receptor, transaccion.getValor());
                aristaUsuarios.setMontoTotal(Validador.redondear(aristaUsuarios.getMontoTotal()));
            }
        }
    }

    /** Saca una transaccion del arbol y descuenta su paso por los grafos (cuando se revierte). */
    public void quitarDeIndices(Transaccion transaccion) {
        transaccionesPorValor.eliminar(transaccion.getValor(), transaccion);

        Billetera origen = transaccion.getBilleteraOrigen();
        Billetera destino = transaccion.getBilleteraDestino();
        if (origen != null && destino != null) {
            descontarArista(grafoBilleteras, origen, destino, transaccion.getValor());
            Usuario remitente = origen.getPropietario();
            Usuario receptor = destino.getPropietario();
            if (!remitente.equals(receptor)) {
                descontarArista(grafoUsuarios, remitente, receptor, transaccion.getValor());
            }
        }
    }

    /** Busca una transaccion por id, o null si no existe. O(1). */
    public Transaccion buscar(String id) {
        return transaccionesPorId.obtener(id);
    }

    /** La lista global de transacciones, en orden de fecha. */
    public ListaEnlazada<Transaccion> listar() {
        return todas;
    }

    /** Cuantas transacciones hay guardadas (de todos los estados). */
    public int cantidad() {
        return todas.tamanio();
    }

    /** Las n transacciones exitosas de mayor valor, sacadas del arbol. */
    public ListaEnlazada<Transaccion> mayorValor(int cantidad) {
        return transaccionesPorValor.obtenerMayores(cantidad);
    }

    public Grafo<Usuario> getGrafoUsuarios() {
        return grafoUsuarios;
    }

    public Grafo<Billetera> getGrafoBilleteras() {
        return grafoBilleteras;
    }

    private <T> void descontarArista(Grafo<T> grafo, T origen, T destino, double monto) {
        Arista<T> arista = grafo.obtenerArista(origen, destino);
        if (arista != null) {
            grafo.descontarArista(origen, destino, monto);
            arista.setMontoTotal(Validador.redondear(arista.getMontoTotal()));
        }
    }
}
