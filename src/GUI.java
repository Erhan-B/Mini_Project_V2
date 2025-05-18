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
    private Dijkstra dijkstra;
    private A_Star_Classification aStar;

    private Label statusLabel;
    private String currentSelectionMode = "NONE";
    private Button selectEntranceBtn;
    private Button selectExitBtn;

    @Override
    public void start(Stage primaryStage) {
        initializeComponents();
        
        BorderPane root = new BorderPane();
        
        // Top controls (image selection and processing)
        HBox topControls = createTopControls();
        
        // Center area (image with overlay)
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().addAll(imageView, overlayPane);
        
        // Bottom controls (selection and pathfinding)
        GridPane bottomControls = createBottomControls();
        
        // Status bar
        statusLabel = new Label("Please process an image first");
        statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white; -fx-background-color: #333; -fx-padding: 10;");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        
        VBox mainContainer = new VBox();
        mainContainer.getChildren().addAll(topControls, imageContainer, bottomControls, statusLabel);
        
        root.setCenter(mainContainer);
        
        loadImage(currentImageIndex);
        
        Scene scene = new Scene(root, 1000, 850);
        primaryStage.setTitle("Parking Lot Optimizer");
        primaryStage.setScene(scene);
        primaryStage.show();
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
        
        overlayPane.getChildren().addAll(entranceMarker, exitMarker, currentPath);
        imageView.setOnMouseClicked(this::handleImageClick);
    }

    private HBox createTopControls() {
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);
        
        imageSelection = new ComboBox<>();
        for (int i = 1; i <= 6; i++) {
            imageSelection.getItems().add("Image " + i);
        }
        imageSelection.getSelectionModel().selectFirst();
        imageSelection.setOnAction(e -> {
            currentImageIndex = imageSelection.getSelectionModel().getSelectedIndex() + 1;
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

    private GridPane createBottomControls() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        
        // Selection buttons column
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.SOMETIMES);
        grid.getColumnConstraints().add(col1);
        
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.SOMETIMES);
        grid.getColumnConstraints().add(col2);
        
        // Row 1: Selection buttons
        selectEntranceBtn = new Button("Select Entrance");
        selectEntranceBtn.setStyle("-fx-base: #4CAF50;");
        selectEntranceBtn.setDisable(true);
        selectEntranceBtn.setOnAction(e -> {
            currentSelectionMode = "ENTRANCE";
            statusLabel.setText("Click on the image to select ENTRANCE point");
            selectEntranceBtn.setDisable(true);
            selectExitBtn.setDisable(false);
        });
        
        selectExitBtn = new Button("Select Exit");
        selectExitBtn.setStyle("-fx-base: #F44336;");
        selectExitBtn.setDisable(true);
        selectExitBtn.setOnAction(e -> {
            currentSelectionMode = "EXIT";
            statusLabel.setText("Click on the image to select EXIT point");
            selectExitBtn.setDisable(true);
            selectEntranceBtn.setDisable(false);
        });
        
        grid.add(selectEntranceBtn, 0, 0);
        grid.add(selectExitBtn, 1, 0);
        
        // Row 2: Pathfinding button
        findPathButton = new Button("Find Path to Parking Spot");
        findPathButton.setStyle("-fx-base: #2196F3;");
        findPathButton.setDisable(true);
        findPathButton.setOnAction(e -> findPathToParkingSpot());
        grid.add(findPathButton, 0, 1, 2, 1);
        
        // Row 3: Reset button
        resetButton = new Button("Reset Selections");
        resetButton.setOnAction(e -> resetSelection());
        grid.add(resetButton, 0, 2, 2, 1);
        
        return grid;
    }

    private void loadImage(int index) {
        try {
            String imagePath = "data/image_" + index + ".jpg";
            originalImage = new Image(new File(imagePath).toURI().toString());
            imageView.setImage(originalImage);
            resetSelection();
        } catch (Exception e) {
            showAlert("Error", "Could not load image: " + e.getMessage());
        }
    }

    private void processCurrentImage() {
        try {
            Image_Processor.processImage("data/image_" + currentImageIndex + ".jpg", currentImageIndex);
            
            processedImage = new Image(new File("output/image_" + currentImageIndex + "_filtered.png").toURI().toString());
            imageView.setImage(processedImage);
            
            Image_Processor imageProcessor = new Image_Processor();
            BufferedImage bufferedImage = ImageIO.read(new File("data/image_" + currentImageIndex + ".jpg"));
            graph = imageProcessor.processImage(bufferedImage);
            nodes = graph.getNodes();
            
            parkingSpots = nodes.stream()
                .filter(Node::isParkingSpot)
                .collect(Collectors.toList());
            
            dijkstra = new Dijkstra(graph.createGrid(bufferedImage.getHeight(), bufferedImage.getWidth()));
            aStar = new A_Star_Classification();
            
            selectEntranceBtn.setDisable(false);
            selectExitBtn.setDisable(true);
            statusLabel.setText("Image processed. Click 'Select Entrance' to begin");
            
        } catch (Exception e) {
            showAlert("Processing Error", "Could not process image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleImageClick(MouseEvent event) {
        if (processedImage == null || currentSelectionMode.equals("NONE")) {
            showAlert("Error", "Please select an action first");
            return;
        }
        
        double x = event.getX();
        double y = event.getY();
        
        Node clickedNode = findNearestNode(x, y);
        
        if (clickedNode == null) {
            showAlert("Selection Error", "No valid node found at this location");
            return;
        }
        
        if (currentSelectionMode.equals("ENTRANCE")) {
            if (!clickedNode.isEntrance()) {
                showAlert("Selection Error", "Please select a valid entrance point (green area)");
                return;
            }
            selectedEntrance = clickedNode;
            entranceMarker.setCenterX(x);
            entranceMarker.setCenterY(y);
            entranceMarker.setVisible(true);
            statusLabel.setText("Entrance selected. Now please select EXIT point");
            currentSelectionMode = "EXIT";
            selectExitBtn.setDisable(false);
        } 
        else if (currentSelectionMode.equals("EXIT")) {
            if (!clickedNode.isExit()) {
                showAlert("Selection Error", "Please select a valid exit point (red area)");
                return;
            }
            selectedExit = clickedNode;
            exitMarker.setCenterX(x);
            exitMarker.setCenterY(y);
            exitMarker.setVisible(true);
            statusLabel.setText("Both entrance and exit selected. Click 'Find Path' to continue");
            currentSelectionMode = "NONE";
            findPathButton.setDisable(false);
        }
    }

    private Node findNearestNode(double x, double y) {
        if (nodes == null || nodes.isEmpty()) return null;
        
        double scaleX = originalImage.getWidth() / imageView.getBoundsInParent().getWidth();
        double scaleY = originalImage.getHeight() / imageView.getBoundsInParent().getHeight();
        
        double scaledX = x * scaleX;
        double scaledY = y * scaleY;
        
        return nodes.stream()
            .min(Comparator.comparingDouble(node -> 
                Math.sqrt(Math.pow(node.getX() - scaledX, 2) + Math.pow(node.getY() - scaledY, 2)))
            )
            .orElse(null);
    }

    private void findPathToParkingSpot() {
        if (selectedEntrance == null || selectedExit == null) {
            showAlert("Error", "Please select both entrance and exit points");
            return;
        }
        
        dijkstra.Compute();
        Node targetParkingSpot = dijkstra.getClosestParking();
        
        if (targetParkingSpot == null) {
            showAlert("No Parking", "No available parking spots found");
            return;
        }
        
        List<Node> path = aStar.findPathToNearestExit(selectedEntrance, Collections.singletonList(targetParkingSpot));
        
        if (path.isEmpty()) {
            showAlert("Path Error", "Could not find a path to the parking spot");
            return;
        }
        
        visualizePath(path);
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
        pathTransition.play();
    }

    private double scaleXToView(double x) {
        return x * (imageView.getBoundsInParent().getWidth() / originalImage.getWidth());
    }

    private double scaleYToView(double y) {
        return y * (imageView.getBoundsInParent().getHeight() / originalImage.getHeight());
    }

    private void resetSelection() {
        selectedEntrance = null;
        selectedExit = null;
        entranceMarker.setVisible(false);
        exitMarker.setVisible(false);
        currentPath.getElements().clear();
        findPathButton.setDisable(true);
        
        if (processedImage != null) {
            selectEntranceBtn.setDisable(false);
            selectExitBtn.setDisable(true);
            statusLabel.setText("Selections reset. Click 'Select Entrance' to begin");
            currentSelectionMode = "NONE";
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