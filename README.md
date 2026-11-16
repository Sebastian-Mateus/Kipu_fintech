# Kipu Fintech

Plataforma de billeteras digitales con analítica de transacciones. Proyecto final de **Estructuras de Datos (2026-2)**, Universidad del Quindío.

Cada cliente puede tener varias billeteras (ahorro, gastos diarios, transporte…), hacer recargas, retiros y transferencias, programar pagos, ganar puntos, subir de nivel y canjear beneficios. El administrador controla el tiempo de la plataforma, revisa la analítica, la red de transferencias, la auditoría de fraude y compara el rendimiento de las estructuras de datos.

Todas las estructuras de datos son **propias** (lista enlazada, pila, cola, cola de prioridad, árbol binario de búsqueda, tabla hash y grafo). No se usan las colecciones de `java.util` para guardar datos.

## Requisitos

- JDK 21 o superior
- Maven 3.9 o superior (o un IDE con Maven integrado, como IntelliJ IDEA)

JavaFX se descarga solo con Maven, no hay que instalarlo aparte.

## Cómo ejecutar

Desde la carpeta del proyecto:

```bash
mvn javafx:run
```

Para solo compilar:

```bash
mvn compile
```

En IntelliJ IDEA también se puede abrir la pestaña de Maven y ejecutar `Plugins > javafx > javafx:run`.

## Usuarios de prueba

Al arrancar se cargan datos de ejemplo: el administrador y 8 clientes con tres semanas de movimientos.

| Rol | Usuario (cédula) | Contraseña | Nivel inicial |
|---|---|---|---|
| Administrador | `admin` | `admin123` | — |
| Cliente | `1094215976` (Valentina Ospina) | `1234` | Platino |
| Cliente | `1094887321` (Laura Restrepo) | `1234` | Oro |
| Cliente | `1094902567` (Mariana López) | `1234` | Oro |
| Cliente | `1096128745` (Santiago Quintero) | `1234` | Oro |
| Cliente | `1098650123` (Juan Pablo Henao) | `1234` | Plata |
| Cliente | `1097745390` (Daniel Arango) | `1234` | Plata |
| Cliente | `1097330458` (Andrés Cardona) | `1234` | Bronce |
| Cliente | `1094473812` (Camila Zuluaga) | `1234` | Bronce |

Los datos ya traen casos listos para mostrar: alertas de fraude de los tres niveles de riesgo (incluidas dos transacciones rechazadas automáticamente), una reversión, operaciones programadas (una que se ejecuta todos los días y otra que se desactivó después de tres intentos fallidos), canjes de beneficios y ciclos en la red de transferencias.

## El reloj simulado

La plataforma no usa la hora real del computador para la lógica: todo le pide la hora a `RelojSistema`. El administrador adelanta el reloj desde la pantalla **Simulación** con los botones **+1 hora**, **+1 día** y **+1 semana**, y en ese momento se ejecutan las operaciones programadas que ya se vencieron.

## Estructura del proyecto

```
src/main/java/co/edu/uniquindio/fintech
├── estructuras     Estructuras de datos propias, sus nodos e iteradores
├── modelo          Entidades (Usuario, Billetera, Transaccion…) y enums
├── excepciones     Excepciones de la plataforma
├── repositorio     Guardan las entidades usando las estructuras propias
├── servicio        Lógica de negocio
│   └── reglas      Una clase por cada regla de detección de fraude
├── controlador     Un controlador por pantalla de JavaFX
├── datos           Cargador de datos de ejemplo
├── util            Reloj simulado, generador de códigos, validaciones, sesión y navegación
├── AplicacionFintech.java   Arranque de la aplicación
└── Plataforma.java          Crea y conecta repositorios y servicios

src/main/resources
├── vistas          Archivos FXML de cada pantalla
└── estilos         Hoja de estilos (estilos.css)
```

## Estructuras de datos y dónde se usan

| Estructura | Uso en la plataforma |
|---|---|
| `ListaEnlazada` (doble) | Historiales, billeteras de cada usuario, notificaciones recientes, auditoría, lista global de transacciones |
| `Pila` | Operaciones que cada usuario todavía puede revertir |
| `Cola` | Notificaciones pendientes de despacho y recorrido en anchura (BFS) |
| `ColaPrioridad` (montículo) | Operaciones programadas ordenadas por fecha y top N de los rankings |
| `ArbolBinarioBusqueda` | Usuarios por puntos (ranking y filtro por nivel) y transacciones por valor |
| `TablaHash` | Usuarios por cédula, billeteras por código, transacciones por id y contadores de analítica |
| `Grafo` (dirigido y ponderado) | Red de transferencias entre usuarios y entre billeteras: rutas frecuentes, caminos y ciclos |

## Notas

- Los datos viven solo en memoria: al cerrar la aplicación se pierden y al abrirla se vuelven a cargar los de ejemplo.
- El dinero se maneja en pesos colombianos con `double`, redondeado siempre a dos decimales.
- En la comparación de rendimiento, la prueba del árbol con datos ordenados usa como máximo 10.000 elementos: con 100.000 el árbol degenerado tarda más de un minuto en llenarse.

## Integrantes

- Sebastian Mateus
- Michael Alvarez
