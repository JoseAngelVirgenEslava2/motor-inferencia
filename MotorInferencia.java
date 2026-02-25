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

    /*
     * MÉTODO MAIN
     * Flujo general:
     * 1) Solicita rutas de archivos
     * 2) Carga hechos y reglas
     * 3) Permite elegir tipo de encadenamiento
     * 4) Ejecuta el motor
     * 5) Pregunta si se desea guardar la nueva base
     */
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("\t\t=========Programa de un motor de inferencia========");
        System.out.println("\t\t=============Realizado por equipo 5================");

        // Cargar base de hechos
        System.out.println("Ingresar ruta absoluta de la base de hechos: ");
        String rutaHechos = sc.nextLine();
        cargarHechos(rutaHechos);

        // Cargar base de reglas
        System.out.println("Ingresar ruta absoulta de la base de reglas: ");
        String rutaReglas = sc.nextLine();
        cargarReglas(rutaReglas);

        // Selección del tipo de inferencia
        System.out.println("1. Encadenamiento hacia Adelante");
        System.out.println("2. Encadenamiento hacia Atrás");

        int opcion = sc.nextInt();
        sc.nextLine();

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
    }

    /*
     * ENCANDENAMIENTO HACIA ADELANTE (Forward Chaining)
     * ------------------------------------------------
     * Parte de los hechos iniciales.
     * Evalúa todas las reglas repetidamente.
     * Cada vez que una regla se activa, agrega su consecuente.
     * El proceso continúa hasta que no haya más cambios.
     *
     */
    static void encadenamientoAdelante() {

        boolean cambio;

        do {
            cambio = false;

            for (Regla r : reglas) {

                System.out.println("Evaluando regla:");
                r.imprimirRegla();

                if (r.sePuedeDisparar(hechos)) {

                    if (!hechos.contains(r.consecuente)) {

                        System.out.println("Regla disparada -> Se agrega: " + r.consecuente);

                        hechos.add(r.consecuente);
                        cambio = true;
                    }
                }
            }

        } while (cambio);  // Se repite mientras haya nuevas inferencias

        System.out.println("\nHechos finales:");

        for (String h : hechos)
            System.out.println("- " + h);
    }

    /*
     * ENCANDENAMIENTO HACIA ATRÁS
     * ------------------------------------------------
     * Parte de un objetivo.
     * Intenta demostrarlo buscando reglas cuyo consecuente
     * coincida con el objetivo.
     *
     * Funciona de forma recursiva.
     * Si logra demostrar todos los antecedentes,
     * el objetivo se agrega a los hechos.
     *
     */
    static boolean encadenamientoAtras(String objetivo) {

        // Si ya es un hecho conocido
        if (hechos.contains(objetivo)) {
            System.out.println(objetivo + " ya es un hecho.");
            return true;
        }

        // Buscar reglas que produzcan el objetivo
        for (Regla r : reglas) {

            if (r.consecuente.equals(objetivo)) {

                System.out.println("Intentando demostrar usando regla:");
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
}