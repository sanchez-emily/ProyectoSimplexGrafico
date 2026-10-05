import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//javac --module-path "C:\Program Files\JavaFx\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml *.java
//java --module-path "C:\Program Files\JavaFx\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml MainFX

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("vista.fxml"));
        primaryStage.setTitle("Solucionador - Método Gráfico");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}