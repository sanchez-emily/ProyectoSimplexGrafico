import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ControladorMetodoGrafico {

    @FXML private TextField txtRutaArchivo;
    @FXML private TextArea txtResultados;
    @FXML private LineChart<Number, Number> graficaMetodoGrafico;
    @FXML private NumberAxis ejeX;
    @FXML private NumberAxis ejeY;

    static class Punto {
        double x, y, z;
        Punto(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    @FXML
    public void calcularYGraficar() {
    
    graficaMetodoGrafico.setStyle(
    ".default-color0.chart-series-line { -fx-stroke: #8A2BE2; } " + // L1 / R1 en Morado
    ".default-color0.chart-line-symbol { -fx-background-color: #8A2BE2, white; } " +
    ".default-color1.chart-series-line { -fx-stroke: #FF69B4; } " + // L2 / R2 en Rosa
    ".default-color1.chart-line-symbol { -fx-background-color: #FF69B4, white; } ");

    
        graficaMetodoGrafico.getData().clear();
        txtResultados.clear();

        String ruta = txtRutaArchivo.getText();

        try (Scanner sc = new Scanner(new File(ruta))) {
            if (!sc.hasNextInt()) {
                txtResultados.setText("Error: El archivo no contiene un formato de números válido.");
                return;
            }

            int numVars = sc.nextInt();
            int numRestricciones = sc.nextInt();

            if (numVars != 2) {
                txtResultados.setText("El Método Gráfico requiere exactamente 2 variables de decisión.");
                return;
            }

            double c1 = sc.nextDouble(); // Coeficiente b (x1)
            double c2 = sc.nextDouble(); // Coeficiente j (x2)

            StringBuilder sb = new StringBuilder();
            sb.append("=========================================\n");
            sb.append("        MODELO DE PROGRAMACIÓN LINEAL    \n");
            sb.append("=========================================\n");
            sb.append(String.format("Función Objetivo: Z = %.2fb + %.2fj\n\n", c1, c2));
            sb.append("PUNTOS DE CORTE CON LOS EJES:\n");
            sb.append("-----------------------------------------\n");

            double[][] rest = new double[numRestricciones][3];
            double maxCorteX = 0;
            double maxCorteY = 0;

            for (int i = 0; i < numRestricciones; i++) {
                rest[i][0] = sc.nextDouble();
                rest[i][1] = sc.nextDouble();
                rest[i][2] = sc.nextDouble();

                double a = rest[i][0];
                double b = rest[i][1];
                double r = rest[i][2];

                String etiqueta = String.format("L%d: %.1fb + %.1fj <= %.1f", (i + 1), a, b, r);

                if (a != 0 && b != 0) {
                    double corteY = r / b;
                    double corteX = r / a;
                    trazarLinea(etiqueta, 0, corteY, corteX, 0);
                    sb.append(String.format("• %s\n  -> Corte en Eje Y (j): (0, %.2f)\n  -> Corte en Eje X (b): (%.2f, 0)\n\n", etiqueta, corteY, corteX));
                    if (corteX > maxCorteX) maxCorteX = corteX;
                    if (corteY > maxCorteY) maxCorteY = corteY;
                } else if (b == 0) {
                    double xFijo = r / a;
                    trazarLinea(etiqueta, xFijo, 0, xFijo, 50);
                    sb.append(String.format("• %s\n  -> Recta Vertical en b = %.2f\n\n", etiqueta, xFijo));
                    if (xFijo > maxCorteX) maxCorteX = xFijo;
                } else if (a == 0) {
                    double yFijo = r / b;
                    trazarLinea(etiqueta, 0, yFijo, 50, yFijo);
                    sb.append(String.format("• %s\n  -> Recta Horizontal en j = %.2f\n\n", etiqueta, yFijo));
                    if (yFijo > maxCorteY) maxCorteY = yFijo;
                }
            }

            // Calcular vértices de la región factible
            List<Punto> vertices = new ArrayList<>();
            vertices.add(new Punto(0, 0, 0));

            for (int i = 0; i < numRestricciones; i++) {
                if (rest[i][1] != 0) {
                    double yVal = rest[i][2] / rest[i][1];
                    if (esFactible(0, yVal, rest)) vertices.add(new Punto(0, yVal, c2 * yVal));
                }
                if (rest[i][0] != 0) {
                    double xVal = rest[i][2] / rest[i][0];
                    if (esFactible(xVal, 0, rest)) vertices.add(new Punto(xVal, 0, c1 * xVal));
                }
                for (int j = i + 1; j < numRestricciones; j++) {
                    double a1 = rest[i][0], b1 = rest[i][1], r1 = rest[i][2];
                    double a2 = rest[j][0], b2 = rest[j][1], r2 = rest[j][2];
                    double det = a1 * b2 - a2 * b1;
                    if (det != 0) {
                        double ix = (r1 * b2 - r2 * b1) / det;
                        double iy = (a1 * r2 - a2 * r1) / det;
                        if (ix >= -1e-5 && iy >= -1e-5 && esFactible(ix, iy, rest)) {
                            vertices.add(new Punto(ix, iy, c1 * ix + c2 * iy));
                        }
                    }
                }
            }

            if (vertices.isEmpty()) {
                sb.append("No se encontró una región factible válida.");
                txtResultados.setText(sb.toString());
                return;
            }

            // Evaluar Máximo y Mínimo
            Punto maxPunto = vertices.get(0);
            Punto minPunto = vertices.get(0);

            for (Punto p : vertices) {
                if (p.z > maxPunto.z) maxPunto = p;
                if (p.z < minPunto.z) minPunto = p;
            }

            // Graficar Puntos
            XYChart.Series<Number, Number> ptMax = new XYChart.Series<>();
            ptMax.setName(String.format("MÁXIMO Z=%.1f (b=%.1f, j=%.1f)", maxPunto.z, maxPunto.x, maxPunto.y));
            ptMax.getData().add(new XYChart.Data<>(maxPunto.x, maxPunto.y));
            graficaMetodoGrafico.getData().add(ptMax);

            XYChart.Series<Number, Number> ptMin = new XYChart.Series<>();
            ptMin.setName(String.format("MÍNIMO Z=%.1f (b=%.1f, j=%.1f)", minPunto.z, minPunto.x, minPunto.y));
            ptMin.getData().add(new XYChart.Data<>(minPunto.x, minPunto.y));
            graficaMetodoGrafico.getData().add(ptMin);

            // Reporte Final
            sb.append("=========================================\n");
            sb.append("         RESULTADOS DE OPTIMIZACIÓN      \n");
            sb.append("=========================================\n");
            sb.append("🟢 MAXIMIZACIÓN (Ganancia Máxima):\n");
            sb.append(String.format("   • Ganancia Máxima (Z) = $%.2f\n", maxPunto.z));
            sb.append(String.format("   • Balones a producir (b) = %.0f\n", maxPunto.x));
            sb.append(String.format("   • Juegos de ajedrez (j) = %.0f\n\n", maxPunto.y));

            sb.append("🔴 MINIMIZACIÓN (Valor Mínimo):\n");
            sb.append(String.format("   • Valor Mínimo (Z) = $%.2f\n", minPunto.z));
            sb.append(String.format("   • Balones a producir (b) = %.0f\n", minPunto.x));
            sb.append(String.format("   • Juegos de ajedrez (j) = %.0f\n", minPunto.y));
            sb.append("=========================================\n");

            txtResultados.setText(sb.toString());

            // Ajuste de ejes
            ejeX.setAutoRanging(false);
            ejeX.setUpperBound((maxCorteX > 0 ? maxCorteX : 10) + 5);
            ejeY.setAutoRanging(false);
            ejeY.setUpperBound((maxCorteY > 0 ? maxCorteY : 10) + 5);

        } catch (Exception e) {
            txtResultados.setText("Error al procesar el archivo: " + e.getMessage());
        }
    }

    private boolean esFactible(double x, double y, double[][] rest) {
        if (x < -1e-5 || y < -1e-5) return false;
        for (double[] r : rest) {
            if (r[0] * x + r[1] * y > r[2] + 1e-5) return false;
        }
        return true;
    }

    private void trazarLinea(String nombre, double x1, double y1, double x2, double y2) {
        XYChart.Series<Number, Number> linea = new XYChart.Series<>();
        linea.setName(nombre);
        linea.getData().add(new XYChart.Data<>(x1, y1));
        linea.getData().add(new XYChart.Data<>(x2, y2));
        graficaMetodoGrafico.getData().add(linea);
    }
}