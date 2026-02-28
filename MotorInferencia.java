import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.io.*;
import java.util.*;


/*
 * CLASE REGLA
 * Representa una regla de producción del tipo:
 *      SI A Y NO B Y C ENTONCES D
 * Cada regla contiene:
 *  - Lista de antecedentes
 *  - Lista paralela de negaciones (true si es NO antecedente)
 *  - Un consecuente
 */
class Regla {

    // Lista de antecedentes
    ArrayList<String> antecedentes;

    // Lista paralela que indica si el antecedente está negado
    ArrayList<Boolean> negaciones;

    // Consecuente de la regla
    String consecuente;

    // Constructor: inicializa las listas dinámicas
    public Regla() {
        antecedentes = new ArrayList<>();
        negaciones = new ArrayList<>();
    }

    /*
     * Método: sePuedeDisparar
     * Verifica si la regla puede activarse dado un conjunto
     * actual de hechos.
     *
     * Retorna:
     *   true  -> si todos los antecedentes se cumplen
     *   false -> si al menos uno no se cumple
     *
     * Lógica:
     *   - Si antecedente es normal entonces debe existir en hechos
     *   - Si antecedente es negado entonces no debe existir en hechos
     */
    public boolean sePuedeDisparar(ArrayList<String> hechos) {

        for (int i = 0; i < antecedentes.size(); i++) {
            // Se obtiene el antecedente y el negado inmediato
            String ant = antecedentes.get(i);
            boolean neg = negaciones.get(i);

            // Verdadero o falso, representa
            // si la base de hechos contiene el
            // antecedente
            boolean contiene = hechos.contains(ant);

            // Si está negado pero sí existe -> falla
            if (neg && contiene) return false;

            // Si no está negado pero no existe -> falla
            if (!neg && !contiene) return false;
        }

        // Todos los antecedentes se cumplen
        return true;
    }

    /*
     * Método: imprimirRegla
     * Muestra la regla en formato legible:
     *   SI A Y NO B ENTONCES C
     *
     * Se utiliza para mostrar qué regla se está evaluando
     * durante el proceso de inferencia.
     */
    public void imprimirRegla() {

        System.out.print("SI ");

        for (int i = 0; i < antecedentes.size(); i++) {

            // Si es true, es decir, es un antecedente
            // en forma negativa, se imprime NO , lo que
            // ayuda a formar la regla en la terminal
            if (negaciones.get(i))
                System.out.print("NO ");

            System.out.print(antecedentes.get(i));

            // Si antecedentes tiene mas de un elemento
            // esto sera verdadero, por lo que se agrega
            // la letra Y a modo de conjuncion
            if (i < antecedentes.size() - 1)
                System.out.print(" Y ");
        }
        // Finalmente se agrega el ENTONCES cuando
        // ya no hay antecedentes y solo queda
        // agregar el consecuente
        System.out.println(" ENTONCES " + consecuente);
    }
}

/*
 * CLASE PRINCIPAL: MotorInferencia
 * ------------------------------------------------
 * Implementa un sistema basado en reglas que permite:
 * 1) Encadenamiento hacia Adelante
 * 2) Encadenamiento hacia Atrás
 *
 * La base de conocimiento se compone de:
 *   - Base de hechos
 *   - Base de reglas
 */
public class MotorInferencia {

    // Base de hechos
    static ArrayList<String> hechos = new ArrayList<>();

    // Base de reglas
    static ArrayList<Regla> reglas = new ArrayList<>();

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        cargarHechos("hechos1.txt");
        cargarReglas("reglas1.txt");

        System.out.println("1. Encadenamiento hacia Adelante");
        System.out.println("2. Encadenamiento hacia Atrás");
        int opcion = sc.nextInt();
        sc.nextLine();

        System.out.println("Ingrese la META a la que quiere llegar:");
        String meta = sc.nextLine();

        if (opcion == 1) {
            encadenamientoAdelante();
        } else {
            System.out.println("Ingrese el objetivo:");
            String objetivo = sc.nextLine();
            boolean resultado = encadenamientoAtras(objetivo);
            System.out.println("Resultado: " + resultado);
        }

        // Guardar nueva base si se desea
        System.out.println("¿Desea guardar los hechos? (s/n)");
        String guardar = sc.nextLine();

        if (guardar.equalsIgnoreCase("s")) {
            guardarHechos("hechos_actualizados.txt");
        }

        if (!reglasDisparadasGrafico.isEmpty()) {
            mostrarGrafico();
        }
    }

    // -------------------------
    // ENC. HACIA ADELANTE
    // -------------------------
    static void encadenamientoAdelante() {
        boolean cambio;

        while (cambio && !hechos.contains(meta)) {
            cambio = false;
            for (Regla r : reglas) {
                System.out.println("Evaluando regla:");
                r.imprimirRegla();

                if (r.sePuedeDisparar(hechos)) {
                    if (!hechos.contains(r.consecuente)) {
                        System.out.println("Regla disparada -> Se agrega: " + r.consecuente);
                        hechos.add(r.consecuente);
                        reglasUsadas.add(r);
                        cambio = true;
                    }
                }
            }

        } while (cambio);

        System.out.println("\nHechos finales:");
        for (String h : hechos)
            System.out.println("- " + h);
    }

    // -------------------------
    // ENC. HACIA ATRÁS
    // -------------------------
    static boolean encadenamientoAtras(String objetivo) {

        // Si ya es un hecho conocido
        if (hechos.contains(objetivo)) {
            System.out.println(objetivo + " ya es un hecho.");
            return true;
        }

        System.out.println("Nueva Meta: " + objetivo + ". \nBuscando reglas que la sustenten...\n");

        // Buscar reglas que produzcan el objetivo
        for (Regla r : reglas) {

            if (r.consecuente.equals(objetivo)) {

                System.out.println("\n\nIntentando demostrar usando regla:\n");
                r.imprimirRegla();

                boolean valido = true;

                // Verificar recursivamente cada antecedente
                for (int i = 0; i < r.antecedentes.size(); i++) {

                    String ant = r.antecedentes.get(i);
                    boolean neg = r.negaciones.get(i);

                    boolean resultado = encadenamientoAtras(ant);

                    if (neg && resultado) valido = false;
                    if (!neg && !resultado) valido = false;
                }

                // Si todos los antecedentes se demostraron
                if (valido) {
                    System.out.println("Regla disparada -> " + objetivo + " agregado a hechos.");
                    hechos.add(objetivo);
                    return true;
                }
            }
        }

        return false; // No se pudo demostrar
    }

    /*
     * MÉTODOS DE CARGA Y GUARDADO
     */

    // Carga hechos desde archivo
    static void cargarHechos(String archivo) {

        try {

            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;

            while ((linea = br.readLine()) != null) {
                hechos.add(linea.trim());
            }

            br.close();

        } catch (Exception e) {
            System.out.println("No se pudieron cargar hechos.");
        }
    }

    // Carga reglas desde archivo con formato:
    // SI A Y NO B ENTONCES C
    static void cargarReglas(String archivo) {

        try {

            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;

            while ((linea = br.readLine()) != null) {

                Regla r = new Regla();

                // Se divide la linea entre antecedentes y consecuentes
                String[] partes = linea.split("ENTONCES");

                // Se reemplazan los SI de la parte de los antecedentes por ""
                String antecedenteParte = partes[0].replace("SI", "").trim();
                String consecuente = partes[1].trim();

                // Se agrega el consecuente a la regla
                r.consecuente = consecuente;

                // Los antecedentes se dividen mediante la letra Y
                String[] antecedentes = antecedenteParte.split("Y");

                // Iteramos sobre cada uno de los antecedentes
                for (String ant : antecedentes) {

                    ant = ant.trim();
                    
                    // Verificacion para antecedentes negados
                    if (ant.startsWith("NO ")) {
                        // Si el antecedente es de la forma NO un_antecedente,
                        // se agrega true a la lista de negaciones
                        r.negaciones.add(true);
                        // Se agrega el antecendente desde la posicion 3
                        // de la cadena pues: {0:'N', 1:'O', 2:' '}, segun
                        // los indices
                        r.antecedentes.add(ant.substring(3));
                    } else {
                        r.negaciones.add(false);
                        r.antecedentes.add(ant);
                    }
                }

                // A la lista de reglas se agrega la regla
                // que se acaba de formar
                reglas.add(r);
            }

            br.close();

        } catch (Exception e) {
            System.out.println("No se pudieron cargar reglas.");
        }
    }

    // Guarda hechos en archivo
    static void guardarHechos(String archivo) {

        try {

            PrintWriter pw = new PrintWriter(new FileWriter(archivo));

            for (String h : hechos)
                pw.println(h);

            pw.close();

        } catch (Exception e) {
            System.out.println("Error al guardar.");
        }

    }

    static void mostrarGrafico() {
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try {
            Map<String, Object> vMap = new HashMap<>();

            for (Regla r : reglasDisparadasGrafico) {
                // Crear nodo del consecuente (conclusión)
                if (!vMap.containsKey(r.consecuente)) {
                    vMap.put(r.consecuente, graph.insertVertex(parent, null, r.consecuente, 20, 20, 100, 30));
                }

                // Crear nodos de antecedentes y conectarlos
                for (String ant : r.antecedentes) {
                    if (!vMap.containsKey(ant)) {
                        vMap.put(ant, graph.insertVertex(parent, null, ant, 20, 20, 100, 30));
                    }
                    // Dibujar la flecha: Antecedente -> Consecuente
                    graph.insertEdge(parent, null, "Dispara", vMap.get(ant), vMap.get(r.consecuente));
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }

        // Configurar la ventana de visualización
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        JFrame frame = new JFrame("Árbol de Inferencia - Paso a Paso");
        frame.getContentPane().add(graphComponent);
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}

