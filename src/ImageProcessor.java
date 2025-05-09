import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {
    
    // Color thresholds for identifying different elements
    private static final int PARKING_SPOT_COLOR = 0xFF0000;       // Red
    private static final int ROAD_COLOR = 0x808080;               // Gray
    private static final int ENTRANCE_COLOR = 0x00FF00;           // Green
    private static final int EXIT_COLOR = 0x0000FF;               // Blue
    private static final int JUNCTION_COLOR = 0xFFFF00;           // Yellow
    
    // Minimum size for a parking spot (in pixels)
    private static final int MIN_PARKING_SPOT_SIZE = 20;
    
    // Grid size for simplifying the graph (larger values create simpler graphs)
    private static final int GRID_SIZE = 10;
    
    public Graph processImage(BufferedImage image) {
        // Step 1: Identify all nodes in the image
        List<Node> nodes = identifyNodes(image);
        
        // Step 2: Create connections between nodes
        createEdges(nodes, image);
        
        // Step 3: Classify parking spots by distance to exits
        //classifyParkingSpots(nodes);
        
        return new Graph(nodes);
    }
    
    private List<Node> identifyNodes(BufferedImage image) {
        List<Node> nodes = new ArrayList<Node>();
        int width = image.getWidth();
        int height = image.getHeight();
        
        // First pass: identify all potential nodes
        for (int y = 0; y < height; y += GRID_SIZE) {
            for (int x = 0; x < width; x += GRID_SIZE) {
                int rgb = image.getRGB(x, y);
                
                Node.NodeType type = determineNodeType(rgb);
                if (type != null) {
                    // Check if this is a parking spot (needs area verification)
                    if (type == Node.NodeType.PARKING_SPOT) {
                        if (isValidParkingSpot(image, x, y)) {
                            nodes.add(new Node(x, y, type, true));
                        }
                    } else {
                        nodes.add(new Node(x, y, type, true));
                    }
                }
            }
        }
        
        return nodes;
    }
    
    private Node.NodeType determineNodeType(int rgb) {
        // Mask off alpha channel if present
        rgb = rgb & 0x00FFFFFF;
        
        if (rgb == PARKING_SPOT_COLOR) {
            return Node.NodeType.PARKING_SPOT;
        } else if (rgb == ROAD_COLOR) {
            return Node.NodeType.ROAD;
        } else if (rgb == ENTRANCE_COLOR) {
            return Node.NodeType.ENTRANCE;
        } else if (rgb == EXIT_COLOR) {
            return Node.NodeType.EXIT;
        } else if (rgb == JUNCTION_COLOR) {
            return Node.NodeType.JUNCTION;
        }
        return null;
    }
    
    private boolean isValidParkingSpot(BufferedImage image, int x, int y) {
        // Check if the area around (x,y) is large enough to be a parking spot
        int count = 0;
        for (int dy = -MIN_PARKING_SPOT_SIZE/2; dy <= MIN_PARKING_SPOT_SIZE/2; dy++) {
            for (int dx = -MIN_PARKING_SPOT_SIZE/2; dx <= MIN_PARKING_SPOT_SIZE/2; dx++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < image.getWidth() && ny >= 0 && ny < image.getHeight()) {
                    if ((image.getRGB(nx, ny) & 0x00FFFFFF) == PARKING_SPOT_COLOR) {
                        count++;
                        if (count >= MIN_PARKING_SPOT_SIZE) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private void createEdges(List<Node> nodes, BufferedImage image) {
        // Create a grid representation for faster lookup
        Node[][] grid = createNodeGrid(nodes, image.getWidth(), image.getHeight());
        
        // Connect nodes based on proximity and road connectivity
        for (Node node : nodes) {
            // Only connect road-like nodes (roads, junctions, entrances, exits)
            if (node.isRoad() || node.isJunction() || node.isEntrance() || node.isExit()) {
                connectNodeToNeighbors(node, grid);
            }
            
            // Connect parking spots to nearest road
            if (node.isParkingSpot()) {
                connectParkingSpotToRoad(node, grid);
            }
        }
    }
    
    private Node[][] createNodeGrid(List<Node> nodes, int width, int height) {
        Node[][] grid = new Node[height / GRID_SIZE + 1][width / GRID_SIZE + 1];
        
        for (Node node : nodes) {
            int gridX = node.getX() / GRID_SIZE;
            int gridY = node.getY() / GRID_SIZE;
            grid[gridY][gridX] = node;
        }
        
        return grid;
    }
    
    private void connectNodeToNeighbors(Node node, Node[][] grid) {
        int gridX = node.getX() / GRID_SIZE;
        int gridY = node.getY() / GRID_SIZE;
        
        // Check 8-directional neighbors
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue; // Skip self
                
                int nx = gridX + dx;
                int ny = gridY + dy;
                
                if (nx >= 0 && nx < grid[0].length && ny >= 0 && ny < grid.length) {
                    Node neighbor = grid[ny][nx];
                    if (neighbor != null && 
                        (neighbor.isRoad() || neighbor.isJunction() || 
                         neighbor.isEntrance() || neighbor.isExit())) {
                        // Create bidirectional edge
                        double distance = node.distanceTo(neighbor);
                        Edge edge1 = new Edge(node, neighbor, distance);
                        Edge edge2 = new Edge(neighbor, node, distance);
                        
                        node.addEdge(edge1);
                        neighbor.addEdge(edge2);
                    }
                }
            }
        }
    }
    
    private void connectParkingSpotToRoad(Node parkingSpot, Node[][] grid) {
        Node nearestRoad = findNearestRoad(parkingSpot, grid);
        if (nearestRoad != null) {
            double distance = parkingSpot.distanceTo(nearestRoad);
            Edge edge1 = new Edge(parkingSpot, nearestRoad, distance);
            Edge edge2 = new Edge(nearestRoad, parkingSpot, distance);
            
            parkingSpot.addEdge(edge1);
            nearestRoad.addEdge(edge2);
        }
    }
    
    private Node findNearestRoad(Node parkingSpot, Node[][] grid) {
        int gridX = parkingSpot.getX() / GRID_SIZE;
        int gridY = parkingSpot.getY() / GRID_SIZE;
        
        //Search in expanding squares around the parking spot
        for (int radius = 1; radius < Math.max(grid.length, grid[0].length); radius++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius) continue;
                    
                    int nx = gridX + dx;
                    int ny = gridY + dy;
                    
                    if (nx >= 0 && nx < grid[0].length && ny >= 0 && ny < grid.length) {
                        Node neighbor = grid[ny][nx];
                        if (neighbor != null && neighbor.isRoad()) {
                            return neighbor;
                        }
                    }
                }
            }
        }
        return null;
    }
}
    /**
    private void classifyParkingSpots(List<Node> nodes) {
        //Collect all exits
        List<Node> exits = new ArrayList<Node>();
        for (Node node : nodes) {
            if (node.isExit()) {
                exits.add(node);
            }
        }
        
        if (exits.isEmpty()) return;
        
        // Classify each parking spot
       // A_Star_Classication classifier = new A_Star_Classication();
        for (Node node : nodes) {
            if (node.isParkingSpot()) {
                Node.DistanceClassification classification = classifier.classifySpot(node, exits);
                node.setDistanceClass(classification);
            }
        }
    }
    */
   