import java.util.*;

/**
 * @author - Kallan SS, 222178219
 * This class implements the A* pathfinding algorithm to find optimal paths from 
 * parking spots to exits in a parking lot. It calculates distances, classifies 
 * parking spots based on their proximity to exits, and handles edge cases 
 * such as unavailable spots and multiple exits.
 */
public class A_Star_Classification {
	
	 public A_Star_Classification() {
		super();
	}

//	/**
//     * Finds the shortest path from a parking spot to the nearest exit
//     * @param parkingSpot - The starting parking spot node
//     * @param exits - Exit node in parking lot
//     * @return Path to the nearest exit (empty if no path found)
//     */
//    public List<Node> findPathToNearestExit(Node parkingSpot, List<Node> exits) {
//        if (parkingSpot == null || exits == null || exits.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        //Find the nearest exit by straight line distance first - h (Multiple exits?)
//        Node nearestExit = findNearestExitByHeuristic(parkingSpot, exits);
//        
//        //Now find the actual path using A* algorithm
//        return findPath(parkingSpot, nearestExit);
//    }
	 
	 /**
	  * Finds the shortest path from a parking spot to the nearest exit
	  * @param parkingSpot - The starting parking spot node
	  * @param exits - List of exit nodes in parking lot
	  * @return Path to the nearest exit (empty if no path found)
	  */
	 public List<Node> findPathToNearestExit(Node parkingSpot, List<Node> exits) {
	     if (parkingSpot == null || exits == null || exits.isEmpty()) {
	         return Collections.emptyList();
	     }

	     List<Node> shortestPath = Collections.emptyList();
	     double shortestDistance = Double.MAX_VALUE;
	     
	     for (Node exit : exits) {
	         List<Node> currentPath = findPath(parkingSpot, exit);
	         if (!currentPath.isEmpty()) {
	             double currentDistance = 0;
	             for (int i = 0; i < currentPath.size() - 1; i++) {
	                 currentDistance += currentPath.get(i).distanceTo(currentPath.get(i + 1));
	             }
	             if (currentDistance < shortestDistance) {
	                 shortestDistance = currentDistance;
	                 shortestPath = currentPath;
	             }
	         }
	     }
	     
	     return shortestPath;
	 }
    
    /**
     * Calculates the Manhattan distance heuristic between two nodes
     * (sum of absolute horizontal and vertical distances)
     * @param from - Starting node parking sport
     * @param to - Exit node 
     * @return Estimated distance between nodes
     */
    private double heuristic(Node from, Node to) {
        return Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY());
    }
    
    /**
     * Reconstructs the path from target node back to start node
     * by following the 'cameFrom' references, then reverses it
     * 
     * This method works backwards from the target node to the start node by following
     * the parent references established during the A* search. It then reverses this
     * path to provide the correct start-to-goal order
     * 
     * @param target The end node of the path (usually the exit)
     * @return Ordered list of nodes from start to target
     */
    private List<Node> reconstructPath(Node target) {
        LinkedList<Node> path = new LinkedList<>();
        Node current = target; 
        
        //Backtrack from target to start using cameFrom references
        while (current != null) {
            path.addFirst(current);  //Add to front of list to avoid reversing later
            current = current.getCameFrom(); //Get node we came from, backtracking
        }
        
        //If we found a valid path (size > 1), return it
        return path.size() > 1 ? path : Collections.emptyList();
    }

    /**
     * Finds path from start to target using A* algorithm
     * 
     * A* search algorithm implementation that:
     * 1. Uses a priority queue to always expand the most promising node
     * 2. Tracks already explored nodes to avoid revisiting them
     * 3. For each node, calculates both the cost to reach it (gScore) and the 
     *    estimated total cost to the exit (fScore = gScore + heuristic)
     * 4. Avoids occupied parking spots unless they're the starting point
     * 
     * @param start - The starting parking sport
     * @param target - The target exit we looking to reach (nearest exit)
     * @return List of nodes that lead to the exit with respect to shortest path to exit 
     */
    private List<Node> findPath(Node start, Node target) {
        resetGraphData(start);
        PriorityQueue<Node> toSearch = new PriorityQueue<>(Comparator.comparingDouble(Node::getFScore));
        Set<Node> closedSet = new HashSet<>(); //More efficient than a list
        
        start.setGScore(0);
        start.setFScore(heuristic(start, target));
        toSearch.add(start);
        
        while (!toSearch.isEmpty()) {
            Node current = toSearch.poll();
            
            if (current.equals(target)) {
                return reconstructPath(current);
            }
            
            closedSet.add(current);
            
            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getTo(); //Gets the node that the edge is connected to
                
                // Skip if neighbor is already evaluated or is an obstacle
                if (closedSet.contains(neighbor) || 
                    neighbor.getType() == Node.NodeType.PARKING_SPOT && 
                    !neighbor.equals(start) && 
                    !neighbor.isAvailable()) {
                    continue;
                }
                
                double tentativeGScore = current.getGScore() + edge.getWeight();
                
                if (tentativeGScore < neighbor.getGScore()) {
                    neighbor.setCameFrom(current);
                    neighbor.setGScore(tentativeGScore);
                    neighbor.setFScore(tentativeGScore + heuristic(neighbor, target));
                    
                    if (!toSearch.contains(neighbor)) {
                        toSearch.add(neighbor);
                    }
                }
            }
        }
        return Collections.emptyList();
    }
    
    /**
     * Resets all pathfinding data in nodes reachable from the starting node
     * 
     * Uses breadth-first search (BFS) to visit all nodes connected to the start node
     * and reset their A* specific data (gScore, fScore, cameFrom) to default values.
     * This ensures that multiple path calculations can be done without disturbance.
     * 
     * @param startNode The node where pathfinding begins (parking spot)
     */
    private void resetGraphData(Node startNode) {
        //Use BFS to traverse all connected nodes
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        
        queue.offer(startNode); //Inserts node into queue
        visited.add(startNode); //Inserts node into set
        
        while (!queue.isEmpty()) {
            Node current = queue.poll(); //Remove head of queue
            
            //Reset A* specific data
            current.setGScore(Double.POSITIVE_INFINITY); //Infinity because our distance is unknown once reset
            current.setFScore(Double.POSITIVE_INFINITY); //Infinity because our distance is unknown once reset
            current.setCameFrom(null);
            
            //Add all connected nodes to the queue
            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getTo(); /////////////////////Potential/////////////////////// 
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
    }

    /**
     * Finds the nearest exit by straight line distance - h (Multiple Exits?)
     * 
     * This method is a simple optimization that finds the exit with the shortest
     * straight-line distance to the parking spot. It's used as
     * a first approximation before calculating actual path distances.
     * 
     * @param parkingSpot - Starting node
     * @param exits - list of potential ending nodes (Multiples exits?)
     * @return the nearest exit
     */
    private Node findNearestExitByHeuristic(Node parkingSpot, List<Node> exits) {
        Node nearest = exits.get(0); //Take first exit as closest (temporarily)
        double minDistance = parkingSpot.distanceTo(nearest); //Get the distance to that exit, heuristic straight line distance
        
        for (Node exit : exits) {
            double distance = parkingSpot.distanceTo(exit); //Get the distance to that exit, heuristic straight line distance
            if (distance < minDistance) {
                minDistance = distance; //Set the distance for new nearest exit (Update new minDistance exit)
                nearest = exit; //New nearest exit
            }
        }
        
        return nearest;
    }
    
    
    //------------------------------------------For Display Purposes------------------------------------------

//    /**
//     * Calculates the actual driving distance from parking spot to exit for display purposes
//     * @param parkingSpot - The starting parking spot
//     * @param exits - List of all exits
//     * @return The actual path distance in meters, or -1 if no path
//     */
//    public double calculateExitDistance(Node parkingSpot, List<Node> exits) {
//        List<Node> path = findPathToNearestExit(parkingSpot, exits);//Find nearest exit (Series of nodes)
//        if (path.isEmpty()) return -1;
//        
//        double distance = 0;
//        for (int i = 0; i < path.size() - 1; i++) {
//            distance += path.get(i).distanceTo(path.get(i + 1)); //Adding up node distances
//        }
//        return distance;
//    }
    
    /**
     * Calculates the actual driving distance from parking spot to exit for display purposes
     * 
     * This method calculates and returns the actual driving distance to the nearest exit,
     * checking each exit individually and keeping track of which path is shortest.
     * As a fallback, if no path can be found to any exit, it returns the straight-line
     * distance to the nearest exit as an estimate.
     * 
     * @param parkingSpot - The starting parking spot
     * @param exits - List of all exits
     * @return The actual path distance in meters to the nearest exit, or Double.MAX_VALUE if no path found
     */
    public double calculateExitDistance(Node parkingSpot, List<Node> exits) {
        if (parkingSpot == null || exits == null || exits.isEmpty()) {
            System.out.println("No exits provided");
            return -1;
        }

        double minDistance = Double.MAX_VALUE;
        boolean pathFound = false;
        
        for (Node exit : exits) {
            System.out.println("Checking path to exit at (" + exit.getX() + "," + exit.getY() + ")");
            List<Node> path = findPath(parkingSpot, exit);
            
            if (!path.isEmpty()) {
                pathFound = true;
                double distance = 0;
                for (int i = 0; i < path.size() - 1; i++) {
                    distance += path.get(i).distanceTo(path.get(i + 1));
                }
                System.out.println("Path found with distance: " + distance);
                if (distance < minDistance) {
                    minDistance = distance;
                }
            } else {
                System.out.println("No path found to this exit");
            }
        }
        
        if (!pathFound) {
            System.out.println("No path found to any exit");
            // Try straight-line distance as fallback
            Node nearestExit = findNearestExitByHeuristic(parkingSpot, exits);
            double straightDistance = parkingSpot.distanceTo(nearestExit);
            System.out.println("Using straight-line distance: " + straightDistance);
            return straightDistance;
        }
        
        return minDistance;
    }

//    /**
//     * Classifies a parking spot based on its distance to the nearest exit
//     * @param parkingSpot The parking spot to classify
//     * @param exits List of all exits
//     * @return The distance classification
//     */
//    public Node.DistanceClassification classifySpot(Node parkingSpot, List<Node> exits) {
//        double distance = calculateExitDistance(parkingSpot, exits);
//        if (distance < 0) return Node.DistanceClassification.VERY_FAR;
//        
//        // Fixed thresholds for classifications based on actual distance
//        if (distance <= 20) return Node.DistanceClassification.NEAR;
//        if (distance <= 50) return Node.DistanceClassification.FAIRLY_NEAR;
//        if (distance <= 100) return Node.DistanceClassification.FAR;
//        return Node.DistanceClassification.VERY_FAR;
//    }
    
    /**
     * Classifies a parking spot based on its distance to the nearest exit
     * @param parkingSpot The parking spot to classify
     * @param exits List of all exits
     * @return The distance classification
     */
    public Node.DistanceClassification classifySpot(Node parkingSpot, List<Node> exits) {
        double distance = calculateExitDistance(parkingSpot, exits);
        if (distance < 0) return Node.DistanceClassification.VERY_FAR;
        
        // Fixed thresholds for classifications based on actual distance
        if (distance <= 20) return Node.DistanceClassification.NEAR;
        if (distance <= 50) return Node.DistanceClassification.FAIRLY_NEAR;
        if (distance <= 100) return Node.DistanceClassification.FAR;
        return Node.DistanceClassification.VERY_FAR;
    }

    /**
     * Estimates maximum possible distance to exits
     * @param referenceNode - Start node from parking spot
     * @param exits - Exit node
     * @return The distance in meters
     */
//    private double calculateMaxDistanceToExits(Node referenceNode, List<Node> exits) {
//        // Find the maximum possible X and Y dimensions of the parking lot
//        int maxX = 0, maxY = 0;
//        for (Node exit : exits) {
//            maxX = Math.max(maxX, Math.abs(exit.getX()));
//            maxY = Math.max(maxY, Math.abs(exit.getY()));
//        }
//        
//        // Calculate the diagonal distance of the parking lot
//        // This is a better estimate of the maximum possible distance
//        return Math.sqrt(maxX * maxX + maxY * maxY) * 1.5;
//    }
}