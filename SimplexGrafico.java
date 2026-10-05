/*
javac SimplexGrafico.java
java SimplexGrafico < matriz.txt
*/


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class SimplexGrafico {

    FastReader reader;
    double[][] tabla;
    int filas;
    int columnas;
    int filaZ;

    static class FastReader {

        BufferedReader b;
        StringTokenizer s;

        public FastReader() {
            b = new BufferedReader(new InputStreamReader(System.in));
        }

        String next() {
            while (s == null || !s.hasMoreElements()) {
                try {
                    s = new StringTokenizer(b.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return s.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        double nextDouble() {
            return Double.parseDouble(next());
        }
    }

    //lee los datos
    public void leerDatos() {
        reader = new FastReader();

    //la columna z no se pone y la fila z pasa hasta abajo
        filas = reader.nextInt();    // Lee filas como 3
        columnas = reader.nextInt(); // Este las columnas (5)
        filaZ = filas - 1;

        tabla = new double[filas][columnas];

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                tabla[i][j] = reader.nextDouble();
            }
        }
    }

    public void resuelve() {
        while (true) {
            int colPivote = -1;
            double menor = 0;

            for (int j = 0; j < columnas - 1; j++) {
                if (tabla[filaZ][j] < menor) {
                    menor = tabla[filaZ][j];
                    colPivote = j;
                }
            }

            if (colPivote == -1) break;

            int filaPivote = -1;
            double menorRazon = Double.MAX_VALUE;

            for (int i = 0; i < filaZ; i++) {
                double elem = tabla[i][colPivote];
                if (elem > 0) {
                    double razon = tabla[i][columnas - 1] / elem;
                    if (razon < menorRazon) {
                        menorRazon = razon;
                        filaPivote = i;
                    }
                }
            }

            double valorPivote = tabla[filaPivote][colPivote];
            for (int j = 0; j < columnas; j++) {
                tabla[filaPivote][j] /= valorPivote;
            }

            for (int i = 0; i < filas; i++) {
                if (i != filaPivote) {
                    double factor = tabla[i][colPivote];
                    for (int j = 0; j < columnas; j++) {
                        tabla[i][j] -= factor * tabla[filaPivote][j];
                    }
                }
            }
        }
    }

public void mostrarDatos() {
    System.out.println("Resultado: ");
    System.out.println("Z/ganacia máxima = " + Math.round(tabla[filaZ][columnas - 1]));

    // Cantidad de variables de decisión (columnas menos holguras y constante RHS)
    int numVarsDecision = columnas - filaZ - 1;

    for (int v = 0; v < numVarsDecision; v++) {
        double val = obtenerValorVariable(v);
        System.out.println("x" + (v + 1) + " = " + Math.round(val));
    }
}

// Método auxiliar que verifica si la columna es básica (un 1 y puros 0s)
private double obtenerValorVariable(int colVar) {
    int filasConUno = 0;
    int filaUno = -1;

    for (int i = 0; i < filaZ; i++) {
        if (Math.abs(tabla[i][colVar] - 1.0) < 1e-5) {
            filasConUno++;
            filaUno = i;
        } else if (Math.abs(tabla[i][colVar]) > 1e-5) {
            // Si hay un número distinto de 0 y de 1, es variable no básica (vale 0)
            return 0.0;
        }
    }

    // Si tiene exactamente un '1' y el resto ceros, su valor es el valor de la RHS en esa fila
    return (filasConUno == 1) ? tabla[filaUno][columnas - 1] : 0.0;
}

    public static void main(String[] args) {
        SimplexGrafico p = new SimplexGrafico();
        p.leerDatos();
        p.resuelve();
        p.mostrarDatos();
    }
}