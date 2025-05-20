import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class GUI extends Application {
    private ImageView originalImageView;
    private ImageView greyScaleImageView;
    private ImageView edgeImageView;
    private ImageView gridImageView;
    private Button selectImageBtn;
    private Button processBtn;
    private Button resetBtn;
    private Label classificationLabel;
    private Label distanceLabel;
    private File selectedFile;
    private Image_Processor imageProcessor;

    @Override
    public void start(Stage primaryStage) {
        // Initialize components
        initializeComponents();
        
        // Create layout
        HBox buttonBox = createTopButtonBox();
        GridPane imageGrid = createImageGrid();
        VBox infoBox = createInfoBox();
        
        // Main layout
        BorderPane root = new BorderPane();
        root.setTop(buttonBox);
        root.setCenter(imageGrid);
        root.setRight(infoBox);
        root.setPadding(new Insets(10));
        
        // Set up scene and stage
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Parking Lot Image Processor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeComponents() {
        // Image views
        originalImageView = createImageView();
        greyScaleImageView = createImageView();
        edgeImageView = createImageView();
        gridImageView = createImageView();
        
        // Buttons
        selectImageBtn = new Button("Select Image");
        processBtn = new Button("Process Image");
        processBtn.setDisable(true);
        resetBtn = new Button("Reset");
        
        // Info labels
        distanceLabel = new Label("Distance to exit: -");
        classificationLabel = new Label("Classification: Not processed");
        
        
        // Set button actions
        selectImageBtn.setOnAction(e -> selectImage());
        processBtn.setOnAction(e -> displayProcessedImages());
        resetBtn.setOnAction(e -> reset());
    }

    private HBox createTopButtonBox() {
        HBox box = new HBox(10, selectImageBtn, processBtn, resetBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        return box;
    }

    private GridPane createImageGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        // Add image views with labels
        grid.add(createImageBox("Original Image", originalImageView), 0, 0);
        grid.add(createImageBox("GreyScale Image", greyScaleImageView), 1, 0);
        grid.add(createImageBox("Edge Detection", edgeImageView), 0, 1);
        grid.add(createImageBox("Processed Grid", gridImageView), 1, 1);
        
        return grid;
    }

    private VBox createImageBox(String title, ImageView imageView) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold");
        VBox box = new VBox(5, label, imageView);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private ImageView createImageView() {
        ImageView view = new ImageView();
        view.setFitWidth(400);
        view.setFitHeight(300);
        view.setPreserveRatio(true);
        return view;
    }

    private VBox createInfoBox() {
        VBox box = new VBox(10, 
            new Label("Parking Spot Info:"),
            distanceLabel,classificationLabel
            );
        box.setPadding(new Insets(10));
        box.setMinWidth(200);
        return box;
    }

    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Parking Lot Image");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png"));
        
        // Set initial directory to data folder
        File dataDir = new File("data");
        if (dataDir.exists()) {
            fileChooser.setInitialDirectory(dataDir);
        }
        
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Display original image
                Image image = new Image(selectedFile.toURI().toString());
                originalImageView.setImage(image);
                processBtn.setDisable(false);
                
                // Reset classification info when new image is selected
                distanceLabel.setText("Distance to exit: -");
                classificationLabel.setText("Classification: Not processed");
                
            } catch (Exception e) {
                showAlert("Error", "Could not load image: " + e.getMessage());
            }
        }
    }

//    private void processImage() {
//        if (selectedFile == null) return;
//        
//        try {
//            // Disable process button during processing
//            processBtn.setDisable(true);
//            
//            // Process the image
//            imageProcessor = new Image_Processor();
//            String fileName = selectedFile.getName();
//            String metaFileName = fileName.replace(".jpg", "_meta.jpg");
//            
//            imageProcessor.processImage(
//                "data/" + fileName,
//                "data/" + metaFileName,
//                0); // Using 0 as index since we're processing single image
//            
//            // Display processed images
//            displayProcessedImages();
//            
//            // Update classification info
//            updateClassificationInfo();
//            
//        } catch (Exception e) {
//            showAlert("Processing Error", "Failed to process image: " + e.getMessage());
//            e.printStackTrace();
//            reset();
//        }
//    }
    
    private void displayProcessedImages() {
        if (selectedFile == null) return;
        
        try {
            // Disable process button during processing
            processBtn.setDisable(true);
            
            // Process the image
            imageProcessor = new Image_Processor();
            String fileName = selectedFile.getName(); // Get the selected file name
            String baseName = fileName.substring(0, fileName.lastIndexOf(".")); // Get the base name without extension
            
            System.out.println(fileName + baseName);
            
            //String greyScaleFile = "output/" + baseName + "_greyscale.png";
            //String edgeFile = "output/" + baseName + "_edge.png";
            String gridFile = "output/" + baseName + "_path.png"; // Assuming the grid image is named with _path

            imageProcessor.processImage(
                "data/" + fileName, // Original image
                "data/" + baseName + "_meta.jpg", // Assuming metadata follows a similar naming convention
                0); // Using 0 as index since we're processing single image
            
            // Display processed images dynamically
            //greyScaleImageView.setImage(new Image(new File(greyScaleFile).toURI().toString()));
            //edgeImageView.setImage(new Image(new File(edgeFile).toURI().toString()));
            greyScaleImageView.setImage(new Image(new File("output/image_0_greyscale.png").toURI().toString()));
            edgeImageView.setImage(new Image(new File("output/image_0_edge.png").toURI().toString()));
            gridImageView.setImage(new Image(new File(gridFile).toURI().toString()));
            
            // Update classification info
            updateClassificationInfo();
            
        } catch (Exception e) {
            showAlert("Processing Error", "Failed to process image: " + e.getMessage());
            e.printStackTrace();
            reset();
        }
    }


//    private void displayProcessedImages() {
//        try {
//            // Load and display each processed image
//            greyScaleImageView.setImage(new Image(new File("output/image_0_greyscale.png").toURI().toString()));
//            edgeImageView.setImage(new Image(new File("output/image_0_edge.png").toURI().toString()));
//            gridImageView.setImage(new Image(new File("output/image_0_path.png").toURI().toString()));
////            gridImageView.setImage(new Image(new File("output/image_1_path.png").toURI().toString()));
//            //trying to display the image with the path
//        } catch (Exception e) {
//            showAlert("Error", "Could not load processed images: " + e.getMessage());
//        }
//    }

    private void updateClassificationInfo() {
        if (imageProcessor == null || imageProcessor.getGrid() == null) {
        	distanceLabel.setText("Distance to exit: Error");
            classificationLabel.setText("Classification: Error - No grid");
            
            return;
        }
        
        try {
            // Get closest parking spot
            Dijkstra dijkstra = new Dijkstra(imageProcessor.getGrid(), imageProcessor.getEntrance());
            dijkstra.Compute();
            Node closestParking = dijkstra.getClosestParking();
            
            if (closestParking == null) {
            	distanceLabel.setText("Distance to exit: -");
                classificationLabel.setText("Classification: No parking found");
                
                return;
            }
            
            // Calculate classification
            A_Star_Classification aStar = new A_Star_Classification();
            double distance = aStar.calculateExitDistance(closestParking, imageProcessor.getExitList());
            Node.DistanceClassification classification = aStar.classifySpot(closestParking, imageProcessor.getExitList());
            
            // Update labels
            classificationLabel.setText("Classification: " + classification.getLabel());
            classificationLabel.setStyle("-fx-text-fill: " + getColorHex(classification.getColor()));
            distanceLabel.setText(String.format("Distance to exit: %.2f units", distance));
            
        } catch (Exception e) {
        	distanceLabel.setText("Distance to exit: Error");
            classificationLabel.setText("Classification: Error in calculation");
            
            e.printStackTrace();
        }
    }

    private String getColorHex(int rgb) {
        // Convert RGB integer to hex color
        return String.format("#%06X", (0xFFFFFF & rgb));
    }

    private void reset() {
        // Clear all images and selections
        originalImageView.setImage(null);
        greyScaleImageView.setImage(null);
        edgeImageView.setImage(null);
        gridImageView.setImage(null);
        
        // Reset buttons
        processBtn.setDisable(true);
        selectedFile = null;
        imageProcessor = null;
        
        // Reset info labels
        distanceLabel.setText("Distance to exit: -");
        classificationLabel.setText("Classification: Not processed");
        
        classificationLabel.setStyle(""); // Reset color
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}