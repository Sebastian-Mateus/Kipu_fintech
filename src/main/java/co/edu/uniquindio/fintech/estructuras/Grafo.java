package co.edu.uniquindio.fintech.estructuras;

import java.util.Iterator;

/**
 * Grafo dirigido y ponderado guardado como lista de adyacencia:
 * cada vertice tiene su lista con las aristas que salen de el.
 * Lo usamos para el grafo de usuarios y el grafo de billeteras.
 */
public class Grafo<T> implements Iterable<T> {

    // Colores para el recorrido en profundidad que busca ciclos
    private static final int BLANCO = 0;
    private static final int GRIS = 1;
    private static final int NEGRO = 2;

    private TablaHash<T, ListaEnlazada<Arista<T>>> adyacencia;
    private int cantidadAristas;

    public Grafo() {
        this.adyacencia = new TablaHash<>();
        this.cantidadAristas = 0;
    }

    /** Agrega el vertice si todavia no esta. O(1). */
    public void agregarVertice(T vertice) {
        if (!adyacencia.contieneClave(vertice)) {
            adyacencia.insertar(vertice, new ListaEnlazada<Arista<T>>());
        }
    }

    /** Dice si el vertice esta en el grafo. O(1). */
    public boolean contieneVertice(T vertice) {
        return adyacencia.contieneClave(vertice);
    }

    /**
     * Registra una transferencia de origen a destino. Si la arista no existe la crea,
     * y si ya existe solo le suma la transferencia y el monto. O(grado del origen).
     */
    public Arista<T> registrarArista(T origen, T destino, double monto) {
        agregarVertice(origen);
        agregarVertice(destino);

        Arista<T> arista = obtenerArista(origen, destino);
        if (arista == null) {
            arista = new Arista<>(origen, destino);
            adyacencia.obtener(origen).agregarAlFinal(arista);
            cantidadAristas++;
        }
        arista.sumarTransferencia(monto);
        return arista;
    }

    /**
     * Descuenta una transferencia de la arista (se usa al revertir).
     * Si la arista se queda sin transferencias, se quita. O(grado del origen).
     */
    public boolean descontarArista(T origen, T destino, double monto) {
        Arista<T> arista = obtenerArista(origen, destino);
        if (arista == null) {
            return false;
        }
        arista.restarTransferencia(monto);
        if (arista.getCantidadTransferencias() <= 0) {
            adyacencia.obtener(origen).eliminar(arista);
            cantidadAristas--;
        }
        return true;
    }

    /** Busca la arista que va de origen a destino, o null si no hay. O(grado del origen). */
    public Arista<T> obtenerArista(T origen, T destino) {
        ListaEnlazada<Arista<T>> salientes = adyacencia.obtener(origen);
        if (salientes == null) {
            return null;
        }
        for (Arista<T> arista : salientes) {
            if (arista.getDestino().equals(destino)) {
                return arista;
            }
        }
        return null;
    }

    /** Aristas que salen del vertice (lista vacia si no existe). O(1). */
    public ListaEnlazada<Arista<T>> vecinos(T vertice) {
        ListaEnlazada<Arista<T>> salientes = adyacencia.obtener(vertice);
        if (salientes == null) {
            return new ListaEnlazada<>();
        }
        return salientes;
    }

    /** Lista con todos los vertices. O(V). */
    public ListaEnlazada<T> vertices() {
        return adyacencia.claves();
    }

    /** Lista con todas las aristas del grafo. O(V + E). */
    public ListaEnlazada<Arista<T>> aristas() {
        ListaEnlazada<Arista<T>> resultado = new ListaEnlazada<>();
        for (ListaEnlazada<Arista<T>> salientes : adyacencia.valores()) {
            for (Arista<T> arista : salientes) {
                resultado.agregarAlFinal(arista);
            }
        }
        return resultado;
    }

    /** Recorrido en anchura (BFS) desde un vertice, se apoya en la Cola. O(V + E). */
    public ListaEnlazada<T> recorridoAnchura(T inicio) {
        ListaEnlazada<T> recorrido = new ListaEnlazada<>();
        if (!contieneVertice(inicio)) {
            return recorrido;
        }

        TablaHash<T, Boolean> visitados = new TablaHash<>();
        Cola<T> cola = new Cola<>();
        cola.encolar(inicio);
        visitados.insertar(inicio, true);

        while (!cola.estaVacia()) {
            T actual = cola.desencolar();
            recorrido.agregarAlFinal(actual);
            for (Arista<T> arista : vecinos(actual)) {
                T vecino = arista.getDestino();
                if (!visitados.contieneClave(vecino)) {
                    visitados.insertar(vecino, true);
                    cola.encolar(vecino);
                }
            }
        }
        return recorrido;
    }

    /**
     * Busca un camino de origen a destino con BFS (el de menos saltos).
     * Devuelve los vertices del camino en orden, o una lista vacia si no hay. O(V + E).
     */
    public ListaEnlazada<T> buscarCamino(T origen, T destino) {
        ListaEnlazada<T> camino = new ListaEnlazada<>();
        if (!contieneVertice(origen) || !contieneVertice(destino)) {
            return camino;
        }

        // En "anteriores" anotamos desde qué vértice llegamos a cada uno
        TablaHash<T, T> anteriores = new TablaHash<>();
        TablaHash<T, Boolean> visitados = new TablaHash<>();
        Cola<T> cola = new Cola<>();
        cola.encolar(origen);
        visitados.insertar(origen, true);
        boolean encontrado = origen.equals(destino);

        while (!encontrado && !cola.estaVacia()) {
            T actual = cola.desencolar();
            for (Arista<T> arista : vecinos(actual)) {
                T vecino = arista.getDestino();
                if (!visitados.contieneClave(vecino)) {
                    visitados.insertar(vecino, true);
                    anteriores.insertar(vecino, actual);
                    cola.encolar(vecino);
                    if (vecino.equals(destino)) {
                        encontrado = true;
                    }
                }
            }
        }

        if (encontrado) {
            // Reconstruimos el camino caminando hacia atrás desde el destino
            T paso = destino;
            while (paso != null) {
                camino.agregarAlInicio(paso);
                paso = anteriores.obtener(paso);
            }
        }
        return camino;
    }

    /** Dice si se puede llegar de origen a destino. O(V + E). */
    public boolean existeCamino(T origen, T destino) {
        return !buscarCamino(origen, destino).estaVacia();
    }

    /**
     * Busca ciclos con un recorrido en profundidad (DFS) pintando los vertices
     * de blanco (sin visitar), gris (en el camino actual) y negro (ya terminado).
     * Cada ciclo se devuelve cerrado, por ejemplo A, B, C, A. O(V + E).
     */
    public ListaEnlazada<ListaEnlazada<T>> detectarCiclos() {
        ListaEnlazada<ListaEnlazada<T>> ciclos = new ListaEnlazada<>();
        TablaHash<T, Integer> colores = new TablaHash<>();
        ListaEnlazada<T> todos = vertices();

        for (T vertice : todos) {
            colores.insertar(vertice, BLANCO);
        }
        for (T vertice : todos) {
            if (colores.obtener(vertice) == BLANCO) {
                visitarEnProfundidad(vertice, colores, new ListaEnlazada<T>(), ciclos);
            }
        }
        return ciclos;
    }

    /** Las n aristas con mas transferencias, las saca con la cola de prioridad. O(E log E). */
    public ListaEnlazada<Arista<T>> aristasMasFrecuentes(int cantidad) {
        ColaPrioridad<Arista<T>> monticulo = new ColaPrioridad<>(new ComparadorAristasPorCantidad<T>());
        for (Arista<T> arista : aristas()) {
            monticulo.insertar(arista);
        }

        ListaEnlazada<Arista<T>> resultado = new ListaEnlazada<>();
        while (resultado.tamanio() < cantidad && !monticulo.estaVacia()) {
            resultado.agregarAlFinal(monticulo.extraer());
        }
        return resultado;
    }

    /** Cuantos vertices hay. O(1). */
    public int cantidadVertices() {
        return adyacencia.tamanio();
    }

    /** Cantidad de aristas. O(1). */
    public int cantidadAristas() {
        return cantidadAristas;
    }

    /** Dice si el grafo no tiene vertices. O(1). */
    public boolean estaVacio() {
        return adyacencia.estaVacia();
    }

    /** Recorre los vertices del grafo. */
    @Override
    public Iterator<T> iterator() {
        return vertices().iterator();
    }

    private void visitarEnProfundidad(T vertice, TablaHash<T, Integer> colores,
                                      ListaEnlazada<T> camino, ListaEnlazada<ListaEnlazada<T>> ciclos) {
        colores.insertar(vertice, GRIS);
        camino.agregarAlFinal(vertice);

        for (Arista<T> arista : vecinos(vertice)) {
            T destino = arista.getDestino();
            int color = colores.obtener(destino);
            if (color == GRIS) {
                // Volvimos a un vértice que está en el camino actual: hay un ciclo
                ciclos.agregarAlFinal(extraerCiclo(camino, destino));
            } else if (color == BLANCO) {
                visitarEnProfundidad(destino, colores, camino, ciclos);
            }
        }

        camino.eliminarUltimo();
        colores.insertar(vertice, NEGRO);
    }

    // Copia el pedazo del camino que va desde "inicio" hasta el final y lo cierra
    private ListaEnlazada<T> extraerCiclo(ListaEnlazada<T> camino, T inicio) {
        ListaEnlazada<T> ciclo = new ListaEnlazada<>();
        boolean dentroDelCiclo = false;
        for (T vertice : camino) {
            if (vertice.equals(inicio)) {
                dentroDelCiclo = true;
            }
            if (dentroDelCiclo) {
                ciclo.agregarAlFinal(vertice);
            }
        }
        ciclo.agregarAlFinal(inicio);
        return ciclo;
    }
}
