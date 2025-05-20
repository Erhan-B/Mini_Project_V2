import java.util.ArrayList;
import java.util.List;

public class Node {
    private final int x, y;               //Coordinates
    
    //Node type and status
    private NodeType type;
    
    //Distance classification
    private DistanceClassification distanceClass;
    private boolean isAvailable;          //Is the spot available?
    
    private List<Edge> edges;
    
    //For pathfinding(A*)
    private Node cameFrom;
    private double gScore; //Cost from start to this node
    private double fScore; //Estimated total cost (gScore + heuristic)
    
    /**
     * Types of nodes in our parking lot graph.
     */
    public enum NodeType{
    	ENTRANCE,                //Parking lot entrance
    	EXIT,                    //Parking lot exit.
    	PARKING_SPOT,            //Individual parking spot
    	PATH,                //Junction point in driving lanes.
    	ROAD
    }
    
    /**
     * Classification of parking spots based on distance to nearest exit
     */
    public enum DistanceClassification {
        NEAR("Near", 0x4CAF50),         		//Green
        FAIRLY_NEAR("Fairly Near", 0x8BC34A), 	//Light green
        FAR("Far", 0xFFC107),           		//Amber
        VERY_FAR("Very Far", 0xF44336); 		//Red
        
        private final String label;
        private final int color; //RGB color for visualization
        
        DistanceClassification(String label, int color) {
            this.label = label;
            this.color = color;
        }
        
        public String getLabel() { return label; }
        public int getColor() { return color; }
    }
    /**
     * Parameterized constructor for the Node
     * @param x The x-coordinate for the Node
     * @param y The y-coordinate for the Node
     * @param type The NodeType to describe the type of Node
     * @param isAvailable Boolean to set if the spot is available
     */
    public Node(int x, int y, NodeType type, boolean isAvailable) {
    	this.x = x;
        this.y = y;
        this.type = type;
        this.isAvailable = isAvailable;
        this.gScore = Double.POSITIVE_INFINITY; //Since we are unsure of the gScore as yet
        this.fScore = Double.POSITIVE_INFINITY; //Since we are unsure of the fScore as yet
        
        //Default classification for now
        this.distanceClass = DistanceClassification.VERY_FAR;
        this.edges = new ArrayList<Edge>();
    }

  //Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public NodeType getType() { return type; }
    public boolean isAvailable() { return isAvailable; }
    public DistanceClassification getDistanceClass() { return distanceClass; }
    public List<Edge> getEdges() { return edges; }
    public Node getCameFrom() { return cameFrom; }
    
    public double getGScore() { return gScore; }
    public double getFScore() { return fScore; }
    
    public boolean isRoad() { return this.type == NodeType.ROAD; }
    public boolean isEntrance() { return this.type == NodeType.ENTRANCE; }
    public boolean isExit() { return this.type == NodeType.EXIT; }
    public boolean isPath() { return this.type == NodeType.PATH; }
    public boolean isParkingSpot() { return this.type == NodeType.PARKING_SPOT; }
    
    //Setters
    public void setAvailable(boolean available) { isAvailable = available; }
    public void setDistanceClass(DistanceClassification distanceClass) { this.distanceClass = distanceClass; }
    public void setCameFrom(Node cameFrom) { this.cameFrom = cameFrom; }
    
    public void setGScore(double gScore) { this.gScore = gScore; }
    public void setFScore(double fScore) { this.fScore = fScore; }
    public void setType(NodeType t) { this.type = t; }
    
    /**
     * Add edge
     * @param edge The Edge to be added
     */
    public void addEdge(Edge edge) {
    	edges.add(edge);
    }
    /**
     * Resets pathfinding values (used between pathfinding operations)
     */
    public void resetPathfindingData() {
        this.cameFrom = null;
        this.gScore = Double.POSITIVE_INFINITY;
        this.fScore = Double.POSITIVE_INFINITY;
    }

    /**
     * Calculates euclidean distance to another node
     * @param other The other Node
     * @return Distance between nodes
     */
    public double distanceTo(Node other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }
   
}