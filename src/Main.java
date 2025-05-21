import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application{
	/**
     * Launches the JavaFX application.
     * @param args command-line arguments
     */
	public static void main(String[] args){
		launch(args);
    }

    /**
     * The entry point for the JavaFX application.
     * @param primaryStage the primary window of the application.
     */
	@Override
	public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        GUI gui = new GUI(root);
		//Set up and display the scene
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Parking Lot Image Processor");
        primaryStage.setScene(scene);
        primaryStage.show();
	}
}
