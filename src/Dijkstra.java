/**
 * @author E Bredell 222024369
 * @version Mini project v1
 */

import java.util.HashMap;

/**
 * Class that implements Dijkstra's algorithm to find the shortest path from the entrance to the an available spot
 */
public class Dijkstra {
	//Node representing the closest parking
	private Node closestParking;
	//double representing the closest calculated distance from the entrance to the node
	private double closestDist;
	//Array of nodes representing the grid
	private Node[][] grid;
	//HashMap storing the Double shortest distances from the entrance to each valid node
	private HashMap<Node, Double> map;
	
	/**
	 * Parameterized constructor
	 * @param grid The 2D Node grid
	 */
	public Dijkstra(Node[][] grid) {
		this.grid = grid;
		map = new HashMap<Node, Double>();
	}
	
	/**
	 * Method that computes the shortest path to an available parking using Dijkstra's algorithm
	 */
	public void Compute() {
		int rows = grid.length;
		int cols = grid[0].length;
		closestDist = Double.MAX_VALUE;
		
		//Array that stores the shortest distance from source to each node
		double dist[][] = new double[rows][cols];
		//Bool array that stores whether a node is checked
		boolean visited[][] = new boolean[rows][cols];
		
		//Initialize all the distances to max and all bools to false
		//Distance is set to max to represent that we dont have a path calculated
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				dist[i][j] = Double.MAX_VALUE;
				visited[i][j] = false;
			}
		}
		
		//Set distance to 0 for the root node
		dist[0][0] = 0;

		while(true) {
			//Set the distance to max value to represent that no path has been found yet
			double lowest = Double.MAX_VALUE;
			
			int u = -1;
			int v = -1;
			
			//Loop through all nodes and check if there is a new lowest distance
			for(int i = 0; i < rows; i++) {
				for(int j = 0; j < cols; j++) {
					if(!visited[i][j] && dist[i][j] < lowest) {
						lowest = dist[i][j];
						u = i;
						v = j;
					}
				}
			}
			
			//Check if still at valid index
			if(u == -1 || v == -1) {
				break;
			}
			
			visited[u][v] = true;
			
			//Relax the edges of the node
			try {
				for(Edge edge : grid[u][v].getEdges()) {
					Node neighbor = edge.getTo();
					int neighborX = neighbor.getX();
					int neighborY = neighbor.getY();
					
					//If edge is unvisited then calculate and update distance to node
					if(!visited[neighborY][neighborX]) {
						double newDist = dist[u][v] + edge.getWeight();
						//Check if new calculated distance is shorter than previously shortest
						if(newDist < dist[neighborY][neighborX]) {
							dist[neighborY][neighborX] = newDist;
							map.put(neighbor, newDist);
							if((neighbor.getType() == Node.NodeType.PARKING_SPOT) && dist[neighborY][neighborX] < closestDist) {
		                        closestParking = neighbor;
		                        closestDist = dist[neighborY][neighborX];
		                    }
						}
					}
				}
			}
			catch (NullPointerException e) {
				System.err.println("Edges are null");
				e.printStackTrace();
			}
			
		}
	}
		
//	/**
//	 * Method to get the node path to the closest available node
//	 * @return the List<Node<T>> path to the closest available
//	 */
//	public List<Node> getPathClosest() {
//		List<Node> path = new ArrayList<>();
//		Node current = closestParking;
//		while(current != null) {
//			path.add(0,current);
//			current = current.getPrev();
//		}
//		
//		System.out.print("Path to closest parking: ");
//	    for (int i = 0; i < path.size(); i++) {
//	        System.out.print(path.get(i).getData());
//	        if (i != path.size() - 1) {
//	            System.out.print(" -> ");
//	        }
//	    }
//	    System.out.println();
//	    return path;
//	}
	
	/**
	 * Getter for the HashMap of Nodes and their shortest distance from the entrance
	 * @return map a HashMap<Node,Double> of the nodes and their distance
	 */
	public HashMap<Node,Double> getMap() {
		return map;
	}
	
	/**
	 * Getter for the closest available valid parking
	 * @return closestParking the Node representing the closest available parking
	 */
	public Node getClosestParking() {
		return closestParking;
	}
	
	/**
	 * Getter for the shortest distance to the valid parking
	 * @return closestDist the shortest distance to the closest valid parking 
	 */
	public double getClosestDist() {
		return closestDist;
	} 
}
