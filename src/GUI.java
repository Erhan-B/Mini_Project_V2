import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.geometry.*;
import javafx.animation.*;
import javafx.util.Duration;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class GUI extends Application {
    private ImageView imageView;
    private Image originalImage;
    private Image processedImage;
    private List<Node> nodes;
    private List<Node> parkingSpots;
    private List<Node> exits;
    private Node selectedEntrance;
    private Node selectedExit;
    private Path currentPath;
    private Circle entranceMarker;
    private Circle exitMarker;
    private Pane overlayPane;
    private ComboBox<String> imageSelection;
    private Button processButton;
    private Button findPathButton;
    private Button resetButton;
    private int currentImageIndex = 1;
    private Node[][] grid;
    private Dijkstra dijkstra;
    private A_Star_Classification aStar;
    private Image_Processor imageProcessor;

    private Label statusLabel;
    private String currentSelectionMode = "NONE";
    private Button selectEntranceBtn;
    private Button selectExitBtn;
    private Label classificationLabel;
    private Label distanceLabel;
    private Node targetParkingSpot;

    @Override
    public void start(Stage primaryStage) {
    	try {
        initializeComponents();
        
        BorderPane root = new BorderPane();
        
        // Top controls (image selection and processing)
        HBox topControls = createTopControls();
        
        // Center area (image with overlay)
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().addAll(imageView, overlayPane);
        
        // Bottom controls (selection and pathfinding)
        GridPane bottomControls = createBottomControls();
        
        // Right panel for classification and distance info
        VBox infoPanel = createInfoPanel();
        
        // Status bar
        statusLabel = new Label("Please process an image first");
        statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white; -fx-background-color: #333; -fx-padding: 10;");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        
        VBox mainContainer = new VBox();
        mainContainer.getChildren().addAll(topControls, imageContainer, bottomControls, statusLabel);
        
        root.setCenter(mainContainer);
        root.setRight(infoPanel);
        
        loadImage(currentImageIndex);
        
        Scene scene = new Scene(root, 1200, 850);
        primaryStage.setTitle("Parking Lot Optimizer");
        primaryStage.setScene(scene);
        primaryStage.show();
        
    	} catch (Exception e) {
            e.printStackTrace();
            showAlert("Initialization Error", "Failed to initialize application: " + e.getMessage());
        }
    }

    private VBox createInfoPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f0f0f0;");
        panel.setMinWidth(200);
        
        Label titleLabel = new Label("Parking Spot Info");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        classificationLabel = new Label("Classification: Not selected");
        classificationLabel.setStyle("-fx-font-size: 14;");
        
        distanceLabel = new Label("Distance to exit: -");
        distanceLabel.setStyle("-fx-font-size: 14;");
        
        panel.getChildren().addAll(titleLabel, classificationLabel, distanceLabel);
        return panel;
    }

    private void initializeComponents() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(900);
        imageView.setFitHeight(700);
        
        overlayPane = new Pane();
        overlayPane.setMouseTransparent(true);
        
        entranceMarker = new Circle(10, Color.GREEN);
        entranceMarker.setVisible(false);
        exitMarker = new Circle(10, Color.RED);
        exitMarker.setVisible(false);
        currentPath = new Path();
        currentPath.setStroke(Color.BLUE);
        currentPath.setStrokeWidth(3);
        
     // Initialize status label
        statusLabel = new Label("Please process an image first");
        statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white; -fx-background-color: #333; -fx-padding: 10;");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        
     // Initialize buttons here
        selectEntranceBtn = new Button("Select Entrance");
        selectExitBtn = new Button("Select Exit");
        
        overlayPane.getChildren().addAll(entranceMarker, exitMarker, currentPath);
        imageView.setOnMouseClicked(this::handleImageClick);
    }

    private HBox createTopControls() {
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);
        
        imageSelection = new ComboBox<>();
        for (int i = 0; i <= 6; i++) {
            imageSelection.getItems().add("Image " + i);
        }
        imageSelection.getSelectionModel().selectFirst();
        imageSelection.setOnAction(e -> {
            currentImageIndex = imageSelection.getSelectionModel().getSelectedIndex();
            loadImage(currentImageIndex);
        });
        
        processButton = new Button("Process Image");
        processButton.setOnAction(e -> processCurrentImage());
        
        controls.getChildren().addAll(
            new Label("Select Image:"), imageSelection,
            processButton
        );
        
        return controls;
    }

//    private GridPane createBottomControls() {
//        GridPane grid = new GridPane();
//        grid.setPadding(new Insets(10));
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setAlignment(Pos.CENTER);
//        
//        ColumnConstraints col1 = new ColumnConstraints();
//        col1.setHgrow(Priority.SOMETIMES);
//        grid.getColumnConstraints().add(col1);
//        
//        ColumnConstraints col2 = new ColumnConstraints();
//        col2.setHgrow(Priority.SOMETIMES);
//        grid.getColumnConstraints().add(col2);
//        
//        // Row 1: Selection buttons
////        selectEntranceBtn = new Button("Select Entrance");
////        selectEntranceBtn.setStyle("-fx-base: #4CAF50;");
////        selectEntranceBtn.setDisable(true);
////        selectEntranceBtn.setOnAction(e -> {
////            currentSelectionMode = "ENTRANCE";
////            statusLabel.setText("Click on the image to select ENTRANCE point");
////            selectEntranceBtn.setDisable(true);
////            selectExitBtn.setDisable(false);
////        });
////        
////        selectExitBtn = new Button("Select Exit");
////        selectExitBtn.setStyle("-fx-base: #F44336;");
////        selectExitBtn.setDisable(true);
////        selectExitBtn.setOnAction(e -> {
////            currentSelectionMode = "EXIT";
////            statusLabel.setText("Click on the image to select EXIT point");
////            selectExitBtn.setDisable(true);
////            selectEntranceBtn.setDisable(false);
////        });
//        
//        selectEntranceBtn.setOnAction(e -> {
//            currentSelectionMode = "ENTRANCE";
//            statusLabel.setText("Click on a highlighted green area to select ENTRANCE point");
//            selectEntranceBtn.setDisable(true);
//            selectExitBtn.setDisable(false);
//            highlightEntranceAreas(); // Show entrance highlights
//        });
//
//        selectExitBtn.setOnAction(e -> {
//            currentSelectionMode = "EXIT";
//            statusLabel.setText("Click on a highlighted red area to select EXIT point");
//            selectExitBtn.setDisable(true);
//            selectEntranceBtn.setDisable(false);
//            highlightExitAreas(); // Show exit highlights
//        });
//        
//        grid.add(selectEntranceBtn, 0, 0);
//        grid.add(selectExitBtn, 1, 0);
//        
//        // Row 2: Pathfinding button
//        findPathButton = new Button("Find Path to Parking Spot");
//        findPathButton.setStyle("-fx-base: #2196F3;");
//        findPathButton.setDisable(true);
//        findPathButton.setOnAction(e -> findPathToParkingSpot());
//        grid.add(findPathButton, 0, 1, 2, 1);
//        
//        // Row 3: Reset button
//        resetButton = new Button("Reset Selections");
//        resetButton.setOnAction(e -> resetSelection());
//        grid.add(resetButton, 0, 2, 2, 1);
//        
//        return grid;
//    }

    
    private GridPane createBottomControls() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.SOMETIMES);
        grid.getColumnConstraints().add(col1);
        
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.SOMETIMES);
        grid.getColumnConstraints().add(col2);
        
        // Configure buttons that were already created in initializeComponents()
        selectEntranceBtn.setStyle("-fx-base: #4CAF50;");
        selectEntranceBtn.setDisable(true);
        selectEntranceBtn.setOnAction(e -> {
            currentSelectionMode = "ENTRANCE";
            statusLabel.setText("Click on a highlighted green area to select ENTRANCE point");
            selectEntranceBtn.setDisable(true);
            selectExitBtn.setDisable(false);
            highlightEntranceAreas();
        });
        
        selectExitBtn.setStyle("-fx-base: #F44336;");
        selectExitBtn.setDisable(true);
        selectExitBtn.setOnAction(e -> {
            currentSelectionMode = "EXIT";
            statusLabel.setText("Click on a highlighted red area to select EXIT point");
            selectExitBtn.setDisable(true);
            selectEntranceBtn.setDisable(false);
            highlightExitAreas();
        });
        
        grid.add(selectEntranceBtn, 0, 0);
        grid.add(selectExitBtn, 1, 0);
        
        findPathButton = new Button("Find Path to Parking Spot");
        findPathButton.setStyle("-fx-base: #2196F3;");
        findPathButton.setDisable(true);
        findPathButton.setOnAction(e -> findPathToParkingSpot());
        grid.add(findPathButton, 0, 1, 2, 1);
        
        resetButton = new Button("Reset Selections");
        resetButton.setOnAction(e -> resetSelection());
        grid.add(resetButton, 0, 2, 2, 1);
        
        return grid;
    }
//    private void loadImage(int index) {
//        try {
//            String imagePath = "data/image_" + index + ".jpg";
//            originalImage = new Image(new File(imagePath).toURI().toString());
//            imageView.setImage(originalImage);
//            resetSelection();
//        } catch (Exception e) {
//            showAlert("Error", "Could not load image: " + e.getMessage());
//        }
//    }
    
    private void loadImage(int index) {
        try {
            String imagePath = "data/image_" + index + ".jpg";
            String metaPath = "data/image_" + index + "_meta.jpg";
            
            // Check if files exist
            if (!new File(imagePath).exists() || !new File(metaPath).exists()) {
                showAlert("Error", "Required image files not found. Please ensure both image_" + index + ".jpg and image_" + index + "_meta.jpg exist in the data directory.");
                return;
            }
            
            originalImage = new Image(new File(imagePath).toURI().toString());
            imageView.setImage(originalImage);
            resetSelection();
        } catch (Exception e) {
            showAlert("Error", "Could not load image: " + e.getMessage());
        }
    }

//    private void processCurrentImage() {
//        try {
//            // Process the image using Image_Processor
//            imageProcessor = new Image_Processor();
//            imageProcessor.processImage("data/image_" + currentImageIndex + ".jpg", 
//                                      "data/image_" + currentImageIndex + "_meta.png", 
//                                      currentImageIndex);
//            
//            // Load processed image for display
//            processedImage = new Image(new File("output/grid_result_" + currentImageIndex + ".png").toURI().toString());
//            imageView.setImage(processedImage);
//            
//            // Get the graph data - add null checks
//            grid = imageProcessor.getGrid();
//            if (grid == null) {
//                throw new Exception("Grid initialization failed");
//            }
//            
//            nodes = imageProcessor.getNodes();
//            if (nodes == null) {
//                throw new Exception("Node list initialization failed");
//            }
//            
//            parkingSpots = nodes.stream()
//                .filter(Node::isParkingSpot)
//                .collect(Collectors.toList());
//            
//            exits = imageProcessor.getExitList();
//            if (exits == null || exits.isEmpty()) {
//                throw new Exception("No exits found in the image");
//            }
//            
//            // Initialize pathfinding algorithms with proper error handling
//            if (imageProcessor.getEntrance() == null) {
//                throw new Exception("No entrance found in the image");
//            }
//            
//            dijkstra = new Dijkstra(grid, imageProcessor.getEntrance());
//            aStar = new A_Star_Classification();
//            
//            selectEntranceBtn.setDisable(false);
//            selectExitBtn.setDisable(true);
//            statusLabel.setText("Image processed. Click 'Select Entrance' to begin");
//            
//        } catch (Exception e) {
//            showAlert("Processing Error", "Could not process image: " + e.getMessage());
//            e.printStackTrace();
//            resetSelection(); // Reset UI state on failure
//        }
//    }
    
    private void processCurrentImage() {
        try {
            imageProcessor = new Image_Processor();
            imageProcessor.processImage("data/image_" + currentImageIndex + ".jpg", 
                                      "data/image_" + currentImageIndex + "_meta.jpg", 
                                      currentImageIndex);
            
            // Load processed image
            processedImage = new Image(new File("output/grid_result_" + currentImageIndex + ".png").toURI().toString());
            imageView.setImage(processedImage);
            
            // Get graph data
            grid = imageProcessor.getGrid();
            if (grid == null) throw new Exception("Grid initialization failed");
            
            nodes = imageProcessor.getNodes();
            if (nodes == null || nodes.isEmpty()) throw new Exception("No nodes found");
            
            // Debug print node positions
            System.out.println("Entrance nodes:");
            nodes.stream().filter(Node::isEntrance).forEach(n -> 
                System.out.println("  at (" + n.getX() + "," + n.getY() + ")"));
            
            System.out.println("Exit nodes:");
            nodes.stream().filter(Node::isExit).forEach(n -> 
                System.out.println("  at (" + n.getX() + "," + n.getY() + ")"));
            
            parkingSpots = nodes.stream()
                .filter(Node::isParkingSpot)
                .collect(Collectors.toList());
            
            exits = imageProcessor.getExitList();
            if (exits == null || exits.isEmpty()) throw new Exception("No exits found");
            
            dijkstra = new Dijkstra(grid, imageProcessor.getEntrance());
            aStar = new A_Star_Classification();
            
            selectEntranceBtn.setDisable(false);
            selectExitBtn.setDisable(true);
            statusLabel.setText("Image processed. Click 'Select Entrance' to begin");
            
        } catch (Exception e) {
            showAlert("Processing Error", "Could not process image: " + e.getMessage());
            e.printStackTrace();
            resetSelection();
        }
    }
    
    private void highlightEntranceAreas() {
        // Clear existing highlights
        overlayPane.getChildren().removeIf(node -> node instanceof Rectangle);
        
        if (nodes == null) return;
        
        for (Node node : nodes) {
            if (node.isEntrance()) {
                // Scale the node coordinates to view coordinates
                double x = scaleXToView(node.getX());
                double y = scaleYToView(node.getY());
                
                Rectangle highlight = new Rectangle(x - 15, y - 15, 30, 30);
                highlight.setFill(Color.GREEN.deriveColor(0, 1, 1, 0.3));
                highlight.setStroke(Color.GREEN);
                highlight.setStrokeWidth(2);
                overlayPane.getChildren().add(highlight);
            }
        }
    }

    private void highlightExitAreas() {
        // Clear existing highlights
        overlayPane.getChildren().removeIf(node -> node instanceof Rectangle);
        
        if (nodes == null) return;
        
        for (Node node : nodes) {
            if (node.isExit()) {
                // Scale the node coordinates to view coordinates
                double x = scaleXToView(node.getX());
                double y = scaleYToView(node.getY());
                
                Rectangle highlight = new Rectangle(x - 15, y - 15, 30, 30);
                highlight.setFill(Color.RED.deriveColor(0, 1, 1, 0.3));
                highlight.setStroke(Color.RED);
                highlight.setStrokeWidth(2);
                overlayPane.getChildren().add(highlight);
            }
        }
    }

//    private void handleImageClick(MouseEvent event) {
//        if (processedImage == null || currentSelectionMode.equals("NONE")) {
//            showAlert("Error", "Please select an action first");
//            return;
//        }
//        
//        double x = event.getX();
//        double y = event.getY();
//        
//        Node clickedNode = findNearestNode(x, y);
//        
//        if (clickedNode == null) {
//            showAlert("Selection Error", "No valid node found at this location");
//            return;
//        }
//        
//        if (currentSelectionMode.equals("ENTRANCE")) {
//            if (!clickedNode.isEntrance()) {
//                showAlert("Selection Error", "Please select a valid entrance point (green area)");
//                return;
//            }
//            selectedEntrance = clickedNode;
//            entranceMarker.setCenterX(x);
//            entranceMarker.setCenterY(y);
//            entranceMarker.setVisible(true);
//            statusLabel.setText("Entrance selected. Now please select EXIT point");
//            currentSelectionMode = "EXIT";
//            selectExitBtn.setDisable(false);
//        } 
//        else if (currentSelectionMode.equals("EXIT")) {
//            if (!clickedNode.isExit()) {
//                showAlert("Selection Error", "Please select a valid exit point (red area)");
//                return;
//            }
//            selectedExit = clickedNode;
//            exitMarker.setCenterX(x);
//            exitMarker.setCenterY(y);
//            exitMarker.setVisible(true);
//            statusLabel.setText("Both entrance and exit selected. Click 'Find Path' to continue");
//            currentSelectionMode = "NONE";
//            findPathButton.setDisable(false);
//        }
//    }
    
    private void handleImageClick(MouseEvent event) {
        if (processedImage == null || currentSelectionMode.equals("NONE")) {
            showAlert("Error", "Please select an action first");
            return;
        }
        
        double x = event.getX();
        double y = event.getY();
        
        System.out.println("Clicked at view coordinates: (" + x + "," + y + ")");
        
        Node clickedNode = findNearestNode(x, y);
        
        if (clickedNode == null) {
            System.out.println("No node found near click location");
            showAlert("Selection Error", "No valid node found at this location");
            return;
        }
        
        System.out.println("Selected node at: (" + clickedNode.getX() + "," + clickedNode.getY() + ")");
        
        if (currentSelectionMode.equals("ENTRANCE")) {
            if (!clickedNode.isEntrance()) {
                highlightEntranceAreas(); // Re-highlight entrances to help user
                showAlert("Selection Error", 
                    "Please click on one of the highlighted green areas to select the entrance");
                return;
            }
            selectedEntrance = clickedNode;
            entranceMarker.setCenterX(x);
            entranceMarker.setCenterY(y);
            entranceMarker.setVisible(true);
            statusLabel.setText("Entrance selected. Now please select EXIT point");
            currentSelectionMode = "EXIT";
            selectExitBtn.setDisable(false);
            highlightExitAreas(); // Now show exit highlights
        } 
        else if (currentSelectionMode.equals("EXIT")) {
            if (!clickedNode.isExit()) {
                highlightExitAreas(); // Re-highlight exits to help user
                showAlert("Selection Error", 
                    "Please click on one of the highlighted red areas to select the exit");
                return;
            }
            selectedExit = clickedNode;
            exitMarker.setCenterX(x);
            exitMarker.setCenterY(y);
            exitMarker.setVisible(true);
            statusLabel.setText("Both entrance and exit selected. Click 'Find Path' to continue");
            currentSelectionMode = "NONE";
            findPathButton.setDisable(false);
            // Clear highlights when done
            overlayPane.getChildren().removeIf(node -> node instanceof Rectangle);
        }
    }

    private Node findNearestNode(double x, double y) {
        if (nodes == null || nodes.isEmpty()) return null;
        
        // Convert view coordinates back to image coordinates
        double imageX = x * (originalImage.getWidth() / imageView.getBoundsInParent().getWidth());
        double imageY = y * (originalImage.getHeight() / imageView.getBoundsInParent().getHeight());
        
        // Find nodes of the type we're looking for
        List<Node> targetNodes = currentSelectionMode.equals("ENTRANCE") ? 
            nodes.stream().filter(Node::isEntrance).collect(Collectors.toList()) :
            currentSelectionMode.equals("EXIT") ? 
            nodes.stream().filter(Node::isExit).collect(Collectors.toList()) :
            nodes;
        
        if (targetNodes.isEmpty()) return null;
        
        // Find the closest node within a reasonable distance
        return targetNodes.stream()
            .min(Comparator.comparingDouble(node -> 
                Math.sqrt(Math.pow(node.getX() - imageX, 2) + Math.pow(node.getY() - imageY, 2))))
            .filter(node -> {
                double distance = Math.sqrt(Math.pow(node.getX() - imageX, 2) + Math.pow(node.getY() - imageY, 2));
                return distance < 30; // Max distance in image coordinates
            })
            .orElse(null);
    }

    private void findPathToParkingSpot() {
        if (selectedEntrance == null || selectedExit == null) {
            showAlert("Error", "Please select both entrance and exit points");
            return;
        }
        
        dijkstra = new Dijkstra(grid, selectedEntrance);
        dijkstra.Compute();
        targetParkingSpot = dijkstra.getClosestParking();
        
        if (targetParkingSpot == null) {
            showAlert("No Parking", "No available parking spots found");
            return;
        }
        
        // Calculate path from entrance to parking spot using A*
        List<Node> pathToParking = aStar.findPathToNearestExit(selectedEntrance, Collections.singletonList(targetParkingSpot));
        
        if (pathToParking.isEmpty()) {
            showAlert("Path Error", "Could not find a path to the parking spot");
            return;
        }
        
        // Calculate classification and distance to exit
        double distanceToExit = aStar.calculateExitDistance(targetParkingSpot, exits);
        Node.DistanceClassification classification = aStar.classifySpot(targetParkingSpot, exits);
        
        // Update info panel
        classificationLabel.setText("Classification: " + classification.getLabel());
        classificationLabel.setStyle("-fx-font-size: 14; -fx-text-fill: " + 
            String.format("#%06x", classification.getColor() & 0x00FFFFFF));
        distanceLabel.setText(String.format("Distance to exit: %.2f meters", distanceToExit));
        
        // Visualize the path
        visualizePath(pathToParking);
    }

    private void visualizePath(List<Node> path) {
        currentPath.getElements().clear();
        
        PathElement firstMove = new MoveTo(
            scaleXToView(path.get(0).getX()),
            scaleYToView(path.get(0).getY())
        );
        currentPath.getElements().add(firstMove);
        
        for (int i = 1; i < path.size(); i++) {
            PathElement line = new LineTo(
                scaleXToView(path.get(i).getX()),
                scaleYToView(path.get(i).getY())
            );
            currentPath.getElements().add(line);
        }
        
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(3));
        pathTransition.setPath(currentPath);
        
        Circle pathIndicator = new Circle(5, Color.BLUE);
        overlayPane.getChildren().add(pathIndicator);
        pathTransition.setNode(pathIndicator);
        
        // Highlight the selected parking spot when animation completes
        pathTransition.setOnFinished(e -> {
            Circle parkingMarker = new Circle(10, Color.YELLOW);
            parkingMarker.setCenterX(scaleXToView(targetParkingSpot.getX()));
            parkingMarker.setCenterY(scaleYToView(targetParkingSpot.getY()));
            overlayPane.getChildren().add(parkingMarker);
        });
        
        pathTransition.play();
    }

    private double scaleXToView(double x) {
        if (originalImage == null) return x;
        return x * (imageView.getBoundsInParent().getWidth() / originalImage.getWidth());
    }

    private double scaleYToView(double y) {
        if (originalImage == null) return y;
        return y * (imageView.getBoundsInParent().getHeight() / originalImage.getHeight());
    }

//    private void resetSelection() {
//        selectedEntrance = null;
//        selectedExit = null;
//        targetParkingSpot = null;
//        entranceMarker.setVisible(false);
//        exitMarker.setVisible(false);
//        currentPath.getElements().clear();
//        findPathButton.setDisable(true);
//        imageView.setImage(originalImage);
//        overlayPane.getChildren().removeIf(node -> node instanceof Circle && node != entranceMarker && node != exitMarker);
//        
//        classificationLabel.setText("Classification: Not selected");
//        distanceLabel.setText("Distance to exit: -");
//        
//        currentSelectionMode = "NONE";
//        if (processedImage != null) {
//            selectEntranceBtn.setDisable(false);
//            selectExitBtn.setDisable(true);
//            statusLabel.setText("Image processed. Click 'Select Entrance' to begin");
//        } else {
//            statusLabel.setText("Please process an image first");
//        }
//    }
    
    private void resetSelection() {
        selectedEntrance = null;
        selectedExit = null;
        targetParkingSpot = null;
        entranceMarker.setVisible(false);
        exitMarker.setVisible(false);
        currentPath.getElements().clear();
        findPathButton.setDisable(true);
        imageView.setImage(originalImage);
        
        // Clear all highlights and markers except entrance/exit markers
        overlayPane.getChildren().removeIf(node -> 
            (node instanceof Circle && node != entranceMarker && node != exitMarker) ||
            (node instanceof Rectangle));
        
        classificationLabel.setText("Classification: Not selected");
        distanceLabel.setText("Distance to exit: -");
        
        currentSelectionMode = "NONE";
        if (processedImage != null) {
            selectEntranceBtn.setDisable(false);
            selectExitBtn.setDisable(true);
            statusLabel.setText("Image processed. Click 'Select Entrance' to begin");
        } else {
            statusLabel.setText("Please process an image first");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}