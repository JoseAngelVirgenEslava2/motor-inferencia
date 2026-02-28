import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.io.*;
import java.util.*;


class Regla {
    ArrayList<String> antecedentes;
    ArrayList<Boolean> negaciones;
    String consecuente;

    public Regla() {
        antecedentes = new ArrayList<>();
        negaciones = new ArrayList<>();
    }

    public boolean sePuedeDisparar(ArrayList<String> hechos) {
        for (int i = 0; i < antecedentes.size(); i++) {
            String ant = antecedentes.get(i);
            boolean neg = negaciones.get(i);

            boolean contiene = hechos.contains(ant);

            if (neg && contiene) return false;
            if (!neg && !contiene) return false;
        }
        return true;
    }

    public void imprimirRegla() {
        System.out.print("SI ");
        for (int i = 0; i < antecedentes.size(); i++) {
            if (negaciones.get(i))
                System.out.print("NO ");
            System.out.print(antecedentes.get(i));
            if (i < antecedentes.size() - 1)
                System.out.print(" Y ");
        }
        System.out.println(" ENTONCES " + consecuente);
    }
}

public class MotorInferencia {

    static ArrayList<String> hechos = new ArrayList<>();
    static ArrayList<Regla> reglas = new ArrayList<>();
    static ArrayList<Regla> reglasDisparadasGrafico = new ArrayList<>();
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Scanner sc2 = new Scanner(System.in);
        System.out.println("======ELIGE PROBLEMA======");
        System.out.println("1. Link sospechoso");
        System.out.println("2. Mecánico de autos");

        
        int op = sc2.nextInt();
        sc2.nextLine();

        switch (op) {
            case 1 -> {
                cargarHechos("hechos1.txt");
                cargarReglas("reglas111.txt");
            }
            case 2 -> {
                cargarHechos("hechos2.txt");
                cargarReglas("reglas2.txt");
            }
            default -> {
                System.out.println("Opción no válida.");
                return;
            }
        }
        System.out.println("\nHECHOS INICIALES: " + hechos);
        System.out.println("\n\n======ELIGE EL TIPO DE ENCADENAMIENTO======");
        System.out.println("1. Encadenamiento hacia Adelante");
        System.out.println("2. Encadenamiento hacia Atrás");


        int opcion = sc.nextInt();
        sc.nextLine();

        System.out.println("Ingrese la META a la que quiere llegar:");
        String meta = sc.nextLine();

        if (opcion == 1) {
            encadenamientoAdelante(meta);
        } else {
            boolean resultado = encadenamientoAtras(meta);
            if(resultado) System.out.println("\n¡META [" + meta + "] CONFIRMADA!");
            else System.out.println("\nNo se pudo confirmar la meta.");
        }

        

        System.out.println("\nLista de hechos actualizada: " + hechos);

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
    static void encadenamientoAdelante(String meta) {
        System.out.println("\n--- INICIANDO PROCESO PASO A PASO ---");
        boolean cambio = true;
        HashSet<Regla> reglasUsadas = new HashSet<>();

        while (cambio && !hechos.contains(meta)) {
            cambio = false;
            for (Regla r : reglas) {
                if (reglasUsadas.contains(r)) continue;

                // Paso 1: Ver si la regla es relevante (comparte algo con nuestros hechos)
                boolean relevante = false;
                for(String ant : r.antecedentes) {
                    if(hechos.contains(ant)) {
                        relevante = true;
                        break;
                    }
                }

                if (relevante) {
                    System.out.println("-------------------------------------------------");
                    System.out.print("Regla a evaluar: \n");
                    r.imprimirRegla();

                    // Paso 2: Intentar disparar
                    if (r.sePuedeDisparar(hechos)) {
                        System.out.println("¡Regla disparada exitosamente!\n");
                        System.out.println("Se agrega '" + r.consecuente + "' a la lista de hechos.\n");
                        reglasDisparadasGrafico.add(r);

                        hechos.add(r.consecuente);
                        reglasUsadas.add(r);
                        cambio = true;

                        if (r.consecuente.equalsIgnoreCase(meta)) {
                            System.out.println("\n>>> RESULTADO: Meta '" + meta + "' alcanzada.");
                            return;
                        }
                        System.out.println("NUEVA META: " + r.consecuente);
                    } else {
                        System.out.println("Regla no disparada. Faltan hechos o  no coinciden.");
                    }
                }
            }
        }
        
        if (hechos.contains(meta)) {
            System.out.println("\n*** META [" + meta + "] CONFIRMADA ***");
        } else {
            System.out.println("\n[AVISO]: Se agotaron las reglas y no se pudo alcanzar la meta '" + meta + "'.");
        }
    }
    // -------------------------
    // ENC. HACIA ATRÁS
    // -------------------------
    static boolean encadenamientoAtras(String objetivo) {

        if (hechos.contains(objetivo)) {
            System.out.println(objetivo + " ya es un hecho.");
            return true;
        }

        System.out.println("Nueva Meta: " + objetivo + ". \nBuscando reglas que la sustenten...\n");

        for (Regla r : reglas) {

            if (r.consecuente.equals(objetivo)) {

                System.out.println("\n\nIntentando demostrar usando regla:\n");
                r.imprimirRegla();

                boolean valido = true;

                for (int i = 0; i < r.antecedentes.size(); i++) {

                    String ant = r.antecedentes.get(i);
                    boolean neg = r.negaciones.get(i);

                    boolean resultado = encadenamientoAtras(ant);

                    if (neg && resultado) valido = false;
                    if (!neg && !resultado) valido = false;
                }

                if (valido) {
                    System.out.println("Regla disparada -> " + objetivo + " agregado a hechos.");
                    hechos.add(objetivo);
                    return true;
                }
            }
        }
        return false;
    }

    // -------------------------
    // CARGA DE ARCHIVOS
    // -------------------------
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

    static void cargarReglas(String archivo) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;

            while ((linea = br.readLine()) != null) {

                Regla r = new Regla();

                String[] partes = linea.split("ENTONCES");
                String antecedenteParte = partes[0].replace("SI", "").trim();
                String consecuente = partes[1].trim();

                r.consecuente = consecuente;

                String[] antecedentes = antecedenteParte.split("Y");

                for (String ant : antecedentes) {
                    ant = ant.trim();
                    if (ant.startsWith("NO ")) {
                        r.negaciones.add(true);
                        r.antecedentes.add(ant.substring(3));
                    } else {
                        r.negaciones.add(false);
                        r.antecedentes.add(ant);
                    }
                }

                reglas.add(r);
            }

            br.close();

        } catch (Exception e) {
            System.out.println("No se pudieron cargar reglas.");
        }
    }

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

