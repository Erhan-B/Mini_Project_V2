import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.animation.PathTransition;
import javafx.animation.FillTransition;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.util.Duration;
import javafx.scene.shape.Circle;
import java.util.*;

public class GUI extends Application {
    private static final int SPOT_WIDTH = 60;
    private static final int SPOT_HEIGHT = 30;
    private static final int LANE_WIDTH = 80;
    private static final int VEHICLE_SIZE = 15;
    private static final int PARKING_BAY_WIDTH = 400;
    private static final int MAIN_LANE_WIDTH = 40;
    
    private Pane parkingLotPane;
    private List<ParkingSpot> parkingSpots;
    private Rectangle exit;
    private Circle car;
    
    private enum Distance {
        CLOSE(Color.LIGHTGREEN, "Close to Entrance"),
        MEDIUM(Color.LIGHTYELLOW, "Medium Distance"),
        FAR(Color.LIGHTPINK, "Far from Entrance");
        
        private final Color color;
        private final String label;
        
        Distance(Color color, String label) {
            this.color = color;
            this.label = label;
        }
    }
    
    private static class ParkingSpot {
        Rectangle rect;
        int row;
        int col;
        boolean isOccupied;
        Distance distance;
        
        ParkingSpot(Rectangle rect, int row, int col, Distance distance) {
            this.rect = rect;
            this.row = row;
            this.col = col;
            this.isOccupied = false;
            this.distance = distance;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        parkingLotPane = new Pane();
        parkingLotPane.setStyle("-fx-background-color: #333333;");
        parkingSpots = new ArrayList<>();
        
        createParkingLot();
        
        HBox legend = createLegend();
        
        HBox controls = new HBox(10);
        Button findPathButton = new Button("Find Parking Spot");
        Button resetButton = new Button("Reset Parking Lot");
        
        findPathButton.setOnAction(e -> simulatePathfinding());
        resetButton.setOnAction(e -> resetParkingLot());
        
        controls.getChildren().addAll(findPathButton, resetButton);
        root.getChildren().addAll(parkingLotPane, legend, controls);
        
        Scene scene = new Scene(root, 1100, 850);
        primaryStage.setTitle("Parking Lot Optimizer - Top Down View");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private HBox createLegend() {
        HBox legend = new HBox(20);
        legend.setPadding(new Insets(10));
        legend.setStyle("-fx-background-color: #444444; -fx-padding: 10;");
        
        for (Distance distance : Distance.values()) {
            HBox item = new HBox(10);
            Rectangle rect = new Rectangle(25, 25, distance.color);
            rect.setStroke(Color.BLACK);
            Label label = new Label(distance.label);
            label.setTextFill(Color.WHITE);
            item.getChildren().addAll(rect, label);
            legend.getChildren().add(item);
        }
        
        HBox occupiedItem = new HBox(10);
        Rectangle occupiedRect = new Rectangle(25, 25, Color.RED);
        occupiedRect.setStroke(Color.BLACK);
        Label occupiedLabel = new Label("Occupied");
        occupiedLabel.setTextFill(Color.WHITE);
        occupiedItem.getChildren().addAll(occupiedRect, occupiedLabel);
        legend.getChildren().add(occupiedItem);
        
        return legend;
    }
    
    private void createParkingLot() {
        // Clear previous elements
        parkingLotPane.getChildren().clear();
        parkingSpots.clear();
        
        // Create entrance (top center)
        Rectangle entrance = new Rectangle(530, 50, 100, 30);
        entrance.setFill(Color.GREEN);
        entrance.setStroke(Color.BLACK);
        Text entranceText = new Text(540, 70, "ENTRANCE");
        entranceText.setFill(Color.WHITE);
        
        // Create exit (bottom center)
        exit = new Rectangle(530, 770, 100, 30);
        exit.setFill(Color.RED);
        exit.setStroke(Color.BLACK);
        Text exitText = new Text(555, 790, "EXIT");
        exitText.setFill(Color.WHITE);
        
        // Create car
        car = new Circle(580, 65, VEHICLE_SIZE, Color.BLUE);
        car.setStroke(Color.BLACK);
        
        parkingLotPane.getChildren().addAll(entrance, entranceText, exit, exitText, car);
        
        // Create parking bays and lanes
        createParkingBays();
    }
    
    private void createParkingBays() {
        // Main vertical lane (center)
        Rectangle centerLane = new Rectangle(580, 150, MAIN_LANE_WIDTH, 620);
        centerLane.setFill(Color.GRAY);
        centerLane.setStroke(Color.WHITE);
        
        // Left and right parking bays
        for (int row = 0; row < 8; row++) {
            double y = 150 + (row * (SPOT_HEIGHT + LANE_WIDTH));
            
            // Left side parking bay
            createParkingBay(100, y, row, false);
            
            // Right side parking bay
            createParkingBay(580 + MAIN_LANE_WIDTH, y, row, true);
            
            // Horizontal driving lane
            Rectangle lane = new Rectangle(100, y + SPOT_HEIGHT, PARKING_BAY_WIDTH * 2 + MAIN_LANE_WIDTH, LANE_WIDTH);
            lane.setFill(Color.GRAY);
            lane.setStroke(Color.WHITE);
            parkingLotPane.getChildren().add(lane);
        }
        
        parkingLotPane.getChildren().add(centerLane);
    }
    
    private void createParkingBay(double startX, double startY, int row, boolean rightSide) {
        Distance distance;
        if (row < 2) distance = Distance.CLOSE;
        else if (row < 5) distance = Distance.MEDIUM;
        else distance = Distance.FAR;
        
        for (int col = 0; col < 6; col++) {
            double x = rightSide ? startX + col * (SPOT_WIDTH + 10) : startX + (5 - col) * (SPOT_WIDTH + 10);
            Rectangle spot = new Rectangle(x, startY, SPOT_WIDTH, SPOT_HEIGHT);
            spot.setFill(distance.color);
            spot.setStroke(Color.BLACK);
            
            ParkingSpot parkingSpot = new ParkingSpot(spot, row, col, distance);
            parkingSpots.add(parkingSpot);
            
            Text spotNumber = new Text(x + 5, startY + 20, String.format("%d", parkingSpots.size()));
            spotNumber.setFill(Color.BLACK);
            
            spot.setOnMouseClicked(e -> toggleSpotOccupancy(parkingSpot));
            
            parkingLotPane.getChildren().addAll(spot, spotNumber);
        }
    }
    
    private void toggleSpotOccupancy(ParkingSpot spot) {
        spot.isOccupied = !spot.isOccupied;
        FillTransition ft = new FillTransition(
            Duration.millis(300),
            spot.rect,
            spot.isOccupied ? spot.distance.color : Color.RED,
            spot.isOccupied ? Color.RED : spot.distance.color
        );
        ft.play();
    }
    
    private void simulatePathfinding() {
        ParkingSpot targetSpot = findNearestAvailableSpot();
        
        if (targetSpot != null) {
            car.toFront();
            
            Path path = new Path();
            path.getElements().add(new MoveTo(580, 65)); // Start at entrance
            
            // Move down main lane to target row
            double laneY = targetSpot.rect.getY() + SPOT_HEIGHT + LANE_WIDTH/2;
            path.getElements().add(new LineTo(580, laneY));
            
            // Move horizontally to parking bay
            double turnX = targetSpot.col < 6 ? 480 : 680; // Left or right bay
            path.getElements().add(new LineTo(turnX, laneY));
            
            // Move into parking spot
            path.getElements().add(new LineTo(
                targetSpot.rect.getX() + SPOT_WIDTH/2,
                targetSpot.rect.getY() + SPOT_HEIGHT/2
            ));
            
            PathTransition pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.seconds(3));
            pathTransition.setPath(path);
            pathTransition.setNode(car);
            
            pathTransition.setOnFinished(e -> {
                car.toFront();
                FillTransition ft = new FillTransition(
                    Duration.millis(500),
                    targetSpot.rect,
                    targetSpot.distance.color,
                    Color.GREEN
                );
                ft.setCycleCount(6);
                ft.setAutoReverse(true);
                ft.play();
            });
            
            pathTransition.play();
        }
    }
    
    private ParkingSpot findNearestAvailableSpot() {
        return parkingSpots.stream()
            .filter(spot -> !spot.isOccupied)
            .min(Comparator.comparingInt(spot -> spot.row))
            .orElse(null);
    }
    
    private void resetParkingLot() {
        createParkingLot();
    }

    public static void main(String[] args) {
        launch(args);
    }
}