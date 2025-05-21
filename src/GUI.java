/**
 * @author Y Fakir 222114205
 * @version Mini_Project
 */

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;


/**
 * GUI is a JavaFX application for visualizing parking lot image processing of the path to the closest 
 * available parking spot to the entrance.
 * It allows users to select an image, process it using the Image_Processor class,
 * and display intermediate and final results including classification (using A*) 
 * and distance information to the exit.
 */
public class GUI {
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
    
    private int fileCounter = 0;

    /**
     * Parameterized constructor to set up the GUI
     * @param root the BorderPane root for the scene
     */
    public GUI(BorderPane root) {
    	//Initializing the UI components
        initializeComponents();
        
        //Setting up layout components
        HBox buttonBox = createTopButtonBox();
        GridPane imageGrid = createImageGrid();
        VBox infoBox = createInfoBox();
        
        //Main layout container and setting its positions
        root.setTop(buttonBox);
        root.setCenter(imageGrid);
        root.setRight(infoBox);
        root.setPadding(new Insets(10));
    }

    /**
     * Initializes all GUI components and their actions
     */
    private void initializeComponents() {
        //Setting up image views for displaying the steps of the processing
    	
        originalImageView = createImageView();
        greyScaleImageView = createImageView();
        edgeImageView = createImageView();
        gridImageView = createImageView();
        
        //Setting up the buttons for controls for user interaction 
        selectImageBtn = new Button("Select Image");
        processBtn = new Button("Process Image");
        processBtn.setDisable(true); //the process button is disabled until image is selected
        resetBtn = new Button("Reset");
        
        //Set up info labels
        distanceLabel = new Label("Distance to exit: -");
        classificationLabel = new Label("Classification: Not processed");
        
        //Assign button event handlers 
        selectImageBtn.setOnAction(e -> selectImage());
        processBtn.setOnAction(e -> displayProcessedImages());
        resetBtn.setOnAction(e -> reset());
    }

    /**
     * Creates a horizontal box for the top control buttons
     * @return configured HBox
     */
    private HBox createTopButtonBox() {
        HBox box = new HBox(10, selectImageBtn, processBtn, resetBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        return box;
    }

    /**
     * Creates a grid to display image views
     * @return configured GridPane
     */
    private GridPane createImageGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        //Adding image views with relevant titles based on the image processing stage
        grid.add(createImageBox("Original Image", originalImageView), 0, 0);
        grid.add(createImageBox("GreyScale Image", greyScaleImageView), 1, 0);
        grid.add(createImageBox("Edge Detection", edgeImageView), 0, 1);
        grid.add(createImageBox("Processed Grid", gridImageView), 1, 1);
        
        return grid;
    }

    /**
     * Creates a VBox containing an image label and its ImageView
     * @param title the label title
     * @param imageView the associated ImageView
     * @return configured VBox
     */
    private VBox createImageBox(String title, ImageView imageView) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold");
        VBox box = new VBox(5, label, imageView);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /**
     * Creates a configured ImageView with specific sizes for the images being displayed
     * @return new ImageView
     */
    private ImageView createImageView() {
        ImageView view = new ImageView();
        view.setFitWidth(400);
        view.setFitHeight(300);
        view.setPreserveRatio(true);
        return view;
    }

    /**
     * Creates a VBox on the right to display parking classification and distance info
     * @return configured VBox
     */
    private VBox createInfoBox() {
        VBox box = new VBox(10, 
            new Label("Parking Spot Info:"),
            distanceLabel,
            classificationLabel
        );
        box.setPadding(new Insets(10));
        box.setMinWidth(200);
        return box;
    }

    /**
     * Opens a file chooser to allow the user to select an image for processing.
     */
    private void selectImage() {
    	
    	//if there is an old output, it will be removed
    	File oldOutput = new File("output/image_0_path.png");
    	oldOutput.delete();
    	
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Parking Lot Image");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png"));

        //open from data directory if available (for ease of access)
        File dataDir = new File("data");
        if (dataDir.exists()) {
            fileChooser.setInitialDirectory(dataDir);
        }

        //Show file chooser dialog and set the image if a file is chosen
        
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                originalImageView.setImage(image);
                processBtn.setDisable(false);
                distanceLabel.setText("Distance to exit: -");
                classificationLabel.setText("Classification: Not processed");
            } catch (Exception e) {
                showAlert("Error", "Could not load image: " + e.getMessage());
            }
        }
    }

    /**
     * Processes the selected image and displays the output images and classification
     */
    private void displayProcessedImages() {
        if (selectedFile == null) return;

        try {
            processBtn.setDisable(true); //Prevent multiple clicks during processing
            imageProcessor = new Image_Processor();

            //Extract base file name
            String fileName = selectedFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf("."));

            //Output file for processed grid (path)
            String gridFile = "output/" + baseName + "_path.png";

            //Call processor on selected image and corresponding metadata
            imageProcessor.processImage(
                "data/" + fileName,
                "data/" + baseName + "_meta.jpg",
                fileCounter);

            //Display processed outputs
            System.out.println("Displaying the images before path");
            
            greyScaleImageView.setImage(new Image(new File("output/image_0_greyscale.png").toURI().toString()));
            edgeImageView.setImage(new Image(new File("output/image_0_edge.png").toURI().toString()));
            
            System.out.println("Displaying the path image");
            
//            gridImageView.setImage(new Image(new File("output/image_0_path.png").toURI().toString()));
            

            //Update classification details based on processed image
            updateClassificationInfo();
            gridImageView.setImage(new Image(new File("output/image_0_path.png").toURI().toString()));

        } catch (Exception e) {
            showAlert("Processing Error", "Failed to process image: " + e.getMessage());
            e.printStackTrace();
            reset();
        }
    }

    /**
     * Updates classification and distance information based on processed image
     */
    private void updateClassificationInfo() {
        if (imageProcessor == null || imageProcessor.getGrid() == null) {
            distanceLabel.setText("Distance to exit: Error");
            classificationLabel.setText("Classification: Error - No grid");
            return;
        }

        try {
            Node entrance = imageProcessor.getEntrance();
            if (entrance == null) {
                distanceLabel.setText("Distance to exit: Error");
                classificationLabel.setText("Classification: Error - No entrance");
                return;
            }

            Dijkstra dijkstra = new Dijkstra(imageProcessor.getGrid(), entrance);
            dijkstra.Compute();
            Node closestParking = dijkstra.getClosestParking();

            if (closestParking == null) {
                distanceLabel.setText("Distance to exit: -");
                classificationLabel.setText("Classification: No parking found");
                return;
            }

            List<Node> exits = imageProcessor.getExitList();
            if (exits.isEmpty()) {
                distanceLabel.setText("Distance to exit: Error");
                classificationLabel.setText("Classification: Error - No exits");
                return;
            }

            // Use A* to classify the parking spot
            A_Star_Classification aStar = new A_Star_Classification();
            double distance = aStar.calculateExitDistance(closestParking, exits);

            if (distance < 0) {
                // Fallback using Dijkstra path
                List<Node> dijkstraPath = dijkstra.getPathClosest();
                if (!dijkstraPath.isEmpty()) {
                    distance = 0;
                    for (int i = 0; i < dijkstraPath.size() - 1; i++) {
                        distance += dijkstraPath.get(i).distanceTo(dijkstraPath.get(i + 1));
                    }
                    distanceLabel.setText(String.format("Distance to exit: ~%.2f units (approx)", distance));
                } else {
                    distanceLabel.setText("Distance to exit: No path found");
                }
                classificationLabel.setText("Classification: Unreachable");
                classificationLabel.setStyle("-fx-text-fill: #F44336");
                return;
            }

            //Classification label and color based on the classification
            Node.DistanceClassification classification = aStar.classifySpot(closestParking, exits);
            classificationLabel.setText("Classification: " + classification.getLabel());
            classificationLabel.setStyle("-fx-text-fill: " + getColorHex(classification.getColor()));
            distanceLabel.setText(String.format("Distance to exit: %.2f units", distance));
            
            System.out.println("Adding Path!!!!!");
            imageProcessor.updateGrid(imageProcessor.getGrid(), dijkstra.getPathClosest(), fileCounter);

        } catch (Exception e) {
            distanceLabel.setText("Distance to exit: Error");
            classificationLabel.setText("Classification: Error in calculation");
            e.printStackTrace();
        }
    }

    /**
     * Converts an RGB int value to hex color string. This method required research on RGB and HEX colours.
     * @param rgb integer RGB value
     * @return hex string representation
     */
    private String getColorHex(int rgb) {
        return String.format("#%06X", (0xFFFFFF & rgb));
    }

    /**
     * Resets the UI and internal state. This allows for another image to be processed.
     */
    private void reset() {
    	
        //Clear image views
        originalImageView.setImage(null);
        greyScaleImageView.setImage(null);
        edgeImageView.setImage(null);
        gridImageView.setImage(null);

        //Reset button control states
        processBtn.setDisable(true);
        selectedFile = null;
        imageProcessor = null;

        //Reset labels
        distanceLabel.setText("Distance to exit: -");
        classificationLabel.setText("Classification: Not processed");
        classificationLabel.setStyle("");
    }

    /**
     * Displays an error alert dialog.
     * @param title alert title
     * @param message message body
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
