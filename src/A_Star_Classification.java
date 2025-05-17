import java.util.*;

/**
 * A* Pathfinder specialized for parking spot to exit navigation
 */
public class A_Star_Classification {
	
	 public A_Star_Classification() {
		super();
	}

	/**
     * Finds the shortest path from a parking spot to the nearest exit
     * @param parkingSpot - The starting parking spot node
     * @param exits - Exit node in parking lot
     * @return Path to the nearest exit (empty if no path found)
     */
    public List<Node> findPathToNearestExit(Node parkingSpot, List<Node> exits) {
        if (parkingSpot == null || exits == null || exits.isEmpty()) {
            return Collections.emptyList();
        }

        //Find the nearest exit by straight line distance first - h (Multiple exits?)
        Node nearestExit = findNearestExitByHeuristic(parkingSpot, exits);
        
        //Now find the actual path using A* algorithm
        return findPath(parkingSpot, nearestExit);
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
     * @param start - The starting parking sport
     * @param target - The target exit we looking to reach (nearest exit)
     * @return List of nodes that lead to the exit with respect to shortest path to exit 
     */
    private List<Node> findPath(Node start, Node target) {
        resetGraphData(start); //Cleans node data between searches

        //Custom Comparator, using Anonymous inner class
        PriorityQueue<Node> toSearch = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node node1, Node node2) {
                if (node1.getFScore() < node2.getFScore()) {
                    return -1; //node1 comes first due to lower fScore
                } else if (node1.getFScore() > node2.getFScore()) {
                    return 1;  //node2 comes first due to lower fScore
                } else {
                    return 0;  //equal priority
                }
            }
        });
        
        //To store the nodes that have been searched
        Set<Node> closedSet = new HashSet<>();
        
        start.setGScore(0); //gScore is the distance from start node to another node
        start.setFScore(heuristic(start, target)); //fScore is the distance from start node to current node (hueristic distance to end node)
        toSearch.add(start); //Add to node to priority queue
        
        while (!toSearch.isEmpty()) {
            Node current = toSearch.poll(); //Removes head of queue
            
            //If we have reached the end node
            if (current.equals(target)) {
                return reconstructPath(current); //Go back on path
            }
            
            closedSet.add(current); //Add node to closed set to not search it again
            
            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getTo(); /////////////////////Potential/////////////////////// 
                                
                //Skip if neighbour is already evaluated or is an occupied parking spot
                //(unless it's our starting parking spot)
                if (closedSet.contains(neighbor) || 
                    (neighbor.getType() == Node.NodeType.PARKING_SPOT && 
                     !neighbor.equals(start) && 
                     !neighbor.isAvailable())) {
                    continue;
                }
                
                double tentativeGScore = current.getGScore() + edge.getWeight();
                
                if (tentativeGScore < neighbor.getGScore()) {
                    //Update node values
                    neighbor.setCameFrom(current);
                    neighbor.setGScore(tentativeGScore);
                    neighbor.setFScore(tentativeGScore + heuristic(neighbor, target));
                    
                    //Manage queue - remove and re-add for priority update
                    if (toSearch.contains(neighbor)) {
                        toSearch.remove(neighbor); //This is inefficient in Java's PriorityQueue(FIX)
                    }
                    toSearch.add(neighbor);
                }
            }
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Resets all pathfinding data in nodes reachable from the starting node
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

    /**
     * Calculates the actual driving distance from parking spot to exit for display purposes
     * @param parkingSpot - The starting parking spot
     * @param exits - List of all exits
     * @return The actual path distance in meters, or -1 if no path
     */
    public double calculateExitDistance(Node parkingSpot, List<Node> exits) {
        List<Node> path = findPathToNearestExit(parkingSpot, exits);//Find nearest exit (Series of nodes)
        if (path.isEmpty()) return -1;
        
        double distance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            distance += path.get(i).distanceTo(path.get(i + 1)); //Adding up node distances
        }
        return distance;
    }

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