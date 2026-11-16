package co.edu.uniquindio.fintech.servicio;

import co.edu.uniquindio.fintech.estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.fintech.estructuras.ColaPrioridad;
import co.edu.uniquindio.fintech.estructuras.ListaEnlazada;
import co.edu.uniquindio.fintech.estructuras.TablaHash;
import co.edu.uniquindio.fintech.modelo.ResultadoRendimiento;

import java.util.Random;

/**
 * Mide con System.nanoTime() cuanto se demoran nuestras estructuras con
 * datos de prueba, para comparar en la practica lo que dice la teoria.
 * Los datos se generan aqui mismo, no se usan los de la plataforma.
 */
public class ServicioRendimiento {

    // Cuantas busquedas se hacen en cada prueba de busqueda
    private static final int BUSQUEDAS = 1000;
    private static final int TOP = 10;

    // Con 100.000 claves ordenadas el arbol degenerado se demoraba mas de un minuto
    // en llenarse, por eso esa comparacion se deja en maximo 10.000 elementos
    private static final int MAXIMO_ARBOL_ORDENADO = 10000;

    // Semilla fija para que los resultados se puedan repetir
    private Random aleatorio;

    public ServicioRendimiento() {
        this.aleatorio = new Random(2026);
    }

    /** Corre las tres comparaciones con esa cantidad de elementos. */
    public ListaEnlazada<ResultadoRendimiento> ejecutarComparaciones(int cantidad) {
        ListaEnlazada<ResultadoRendimiento> resultados = new ListaEnlazada<>();
        agregarTodos(resultados, compararBusqueda(cantidad));
        agregarTodos(resultados, compararTopN(cantidad));
        agregarTodos(resultados, compararArbolOrdenadoVsAleatorio(cantidad));
        return resultados;
    }

    /**
     * Busca 1000 ids al azar en una lista, un arbol y una tabla hash con los
     * mismos datos. Lo esperado: lista O(n), arbol O(log n), tabla O(1).
     */
    public ListaEnlazada<ResultadoRendimiento> compararBusqueda(int cantidad) {
        int[] numeros = numerosDesordenados(cantidad);
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        ArbolBinarioBusqueda<String, String> arbol = new ArbolBinarioBusqueda<>();
        TablaHash<String, String> tabla = new TablaHash<>();
        for (int indice = 0; indice < cantidad; indice++) {
            String id = "ID-" + numeros[indice];
            lista.agregarAlFinal(id);
            arbol.insertar(id, id);
            tabla.insertar(id, id);
        }

        String[] buscados = new String[BUSQUEDAS];
        for (int indice = 0; indice < BUSQUEDAS; indice++) {
            buscados[indice] = "ID-" + aleatorio.nextInt(cantidad);
        }

        long inicio = System.nanoTime();
        for (String id : buscados) {
            lista.contiene(id);
        }
        long tiempoLista = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        for (String id : buscados) {
            arbol.contiene(id);
        }
        long tiempoArbol = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        for (String id : buscados) {
            tabla.contieneClave(id);
        }
        long tiempoTabla = System.nanoTime() - inicio;

        String operacion = "Buscar " + BUSQUEDAS + " ids";
        ListaEnlazada<ResultadoRendimiento> resultados = new ListaEnlazada<>();
        resultados.agregarAlFinal(new ResultadoRendimiento("ListaEnlazada", operacion, cantidad, tiempoLista));
        resultados.agregarAlFinal(new ResultadoRendimiento("ArbolBinarioBusqueda", operacion, cantidad, tiempoArbol));
        resultados.agregarAlFinal(new ResultadoRendimiento("TablaHash", operacion, cantidad, tiempoTabla));
        return resultados;
    }

    /**
     * Saca los 10 valores mas grandes de tres formas: recorriendo toda la lista,
     * con el inorden inverso del arbol y extrayendo del monticulo.
     * Las estructuras se llenan antes y solo se mide la consulta.
     */
    public ListaEnlazada<ResultadoRendimiento> compararTopN(int cantidad) {
        ListaEnlazada<Double> lista = new ListaEnlazada<>();
        ArbolBinarioBusqueda<Double, Double> arbol = new ArbolBinarioBusqueda<>();
        ColaPrioridad<Double> monticulo = new ColaPrioridad<>(new ComparadorMayorPrimero());
        for (int indice = 0; indice < cantidad; indice++) {
            double valor = Math.round(aleatorio.nextDouble() * 10_000_000) / 100.0;
            lista.agregarAlFinal(valor);
            arbol.insertar(valor, valor);
            monticulo.insertar(valor);
        }

        long inicio = System.nanoTime();
        topRecorriendoLista(lista);
        long tiempoLista = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        arbol.obtenerMayores(TOP);
        long tiempoArbol = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        for (int contador = 0; contador < TOP && !monticulo.estaVacia(); contador++) {
            monticulo.extraer();
        }
        long tiempoMonticulo = System.nanoTime() - inicio;

        String operacion = "Top " + TOP + " de mayor valor";
        ListaEnlazada<ResultadoRendimiento> resultados = new ListaEnlazada<>();
        resultados.agregarAlFinal(new ResultadoRendimiento("ListaEnlazada", operacion, cantidad, tiempoLista));
        resultados.agregarAlFinal(new ResultadoRendimiento("ArbolBinarioBusqueda", operacion, cantidad, tiempoArbol));
        resultados.agregarAlFinal(new ResultadoRendimiento("ColaPrioridad", operacion, cantidad, tiempoMonticulo));
        return resultados;
    }

    /**
     * Llena un arbol con claves en orden (se degenera en una lista) y otro con
     * las mismas claves en desorden, y mide insertar todo y buscar 1000 claves.
     * Si piden mas de 10.000 elementos se usan solo 10.000 (ver MAXIMO_ARBOL_ORDENADO).
     */
    public ListaEnlazada<ResultadoRendimiento> compararArbolOrdenadoVsAleatorio(int cantidadPedida) {
        int cantidad = Math.min(cantidadPedida, MAXIMO_ARBOL_ORDENADO);
        int[] ordenados = new int[cantidad];
        for (int indice = 0; indice < cantidad; indice++) {
            ordenados[indice] = indice;
        }
        int[] desordenados = numerosDesordenados(cantidad);

        ArbolBinarioBusqueda<Integer, Integer> arbolOrdenado = new ArbolBinarioBusqueda<>();
        long inicio = System.nanoTime();
        for (int numero : ordenados) {
            arbolOrdenado.insertar(numero, numero);
        }
        long insertarOrdenado = System.nanoTime() - inicio;

        ArbolBinarioBusqueda<Integer, Integer> arbolAleatorio = new ArbolBinarioBusqueda<>();
        inicio = System.nanoTime();
        for (int numero : desordenados) {
            arbolAleatorio.insertar(numero, numero);
        }
        long insertarAleatorio = System.nanoTime() - inicio;

        int[] buscados = new int[BUSQUEDAS];
        for (int indice = 0; indice < BUSQUEDAS; indice++) {
            buscados[indice] = aleatorio.nextInt(cantidad);
        }

        inicio = System.nanoTime();
        for (int numero : buscados) {
            arbolOrdenado.contiene(numero);
        }
        long buscarOrdenado = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        for (int numero : buscados) {
            arbolAleatorio.contiene(numero);
        }
        long buscarAleatorio = System.nanoTime() - inicio;

        // En el nombre va la altura, que es lo que explica la diferencia
        String nombreOrdenado = "ABB ordenado (altura " + arbolOrdenado.altura() + ")";
        String nombreAleatorio = "ABB aleatorio (altura " + arbolAleatorio.altura() + ")";

        ListaEnlazada<ResultadoRendimiento> resultados = new ListaEnlazada<>();
        resultados.agregarAlFinal(new ResultadoRendimiento(nombreOrdenado, "Insertar todo", cantidad,
                insertarOrdenado));
        resultados.agregarAlFinal(new ResultadoRendimiento(nombreAleatorio, "Insertar todo", cantidad,
                insertarAleatorio));
        resultados.agregarAlFinal(new ResultadoRendimiento(nombreOrdenado, "Buscar " + BUSQUEDAS + " claves",
                cantidad, buscarOrdenado));
        resultados.agregarAlFinal(new ResultadoRendimiento(nombreAleatorio, "Buscar " + BUSQUEDAS + " claves",
                cantidad, buscarAleatorio));
        return resultados;
    }

    /*
     * Top 10 sin estructura especial: se recorre toda la lista y se mantiene
     * un arreglo pequeño con los 10 mayores vistos hasta el momento.
     */
    private double[] topRecorriendoLista(ListaEnlazada<Double> lista) {
        double[] mayores = new double[TOP];
        int ocupados = 0;
        for (double valor : lista) {
            if (ocupados < TOP) {
                mayores[ocupados] = valor;
                ocupados++;
                subirEnArreglo(mayores, ocupados - 1);
            } else if (valor > mayores[TOP - 1]) {
                mayores[TOP - 1] = valor;
                subirEnArreglo(mayores, TOP - 1);
            }
        }
        return mayores;
    }

    // Mueve el valor hacia adelante mientras sea mayor que el de su izquierda (insercion)
    private void subirEnArreglo(double[] arreglo, int posicion) {
        int actual = posicion;
        while (actual > 0 && arreglo[actual] > arreglo[actual - 1]) {
            double auxiliar = arreglo[actual];
            arreglo[actual] = arreglo[actual - 1];
            arreglo[actual - 1] = auxiliar;
            actual--;
        }
    }

    // Numeros del 0 a cantidad-1 revueltos con el algoritmo de Fisher-Yates
    private int[] numerosDesordenados(int cantidad) {
        int[] numeros = new int[cantidad];
        for (int indice = 0; indice < cantidad; indice++) {
            numeros[indice] = indice;
        }
        for (int indice = cantidad - 1; indice > 0; indice--) {
            int otro = aleatorio.nextInt(indice + 1);
            int auxiliar = numeros[indice];
            numeros[indice] = numeros[otro];
            numeros[otro] = auxiliar;
        }
        return numeros;
    }

    private void agregarTodos(ListaEnlazada<ResultadoRendimiento> destino,
                              ListaEnlazada<ResultadoRendimiento> origen) {
        for (ResultadoRendimiento resultado : origen) {
            destino.agregarAlFinal(resultado);
        }
    }
}
