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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        cargarHechos("hechos1.txt");
        cargarReglas("reglas1.txt");

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

        System.out.println("¿Desea guardar los hechos? (s/n)");
        String guardar = sc.nextLine();
        if (guardar.equalsIgnoreCase("s")) {
            guardarHechos("hechos_actualizados.txt");
        }
    }

    // -------------------------
    // ENC. HACIA ADELANTE
    // -------------------------
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

        } while (cambio);

        System.out.println("\nHechos finales:");
        for (String h : hechos)
            System.out.println("- " + h);
    }

    // -------------------------
    // ENC. HACIA ATRÁS
    // -------------------------
    static boolean encadenamientoAtras(String objetivo) {

        if (hechos.contains(objetivo)) {
            System.out.println(objetivo + " ya es un hecho.");
            return true;
        }

        for (Regla r : reglas) {

            if (r.consecuente.equals(objetivo)) {

                System.out.println("Intentando demostrar usando regla:");
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
}