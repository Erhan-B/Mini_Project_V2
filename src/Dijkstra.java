/**
 * @author E Bredell 222024369
 * @version Mini project v1
 */

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

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
	
	private Node entrance;
	
	/**
	 * Parameterized constructor
	 * @param grid The 2D Node grid
	 */
	public Dijkstra(Node[][] grid, Node entrance) {
		this.grid = grid;
		map = new HashMap<Node, Double>();
		this.entrance = entrance;
	}
	
	/**
	 * Method that computes the shortest path to an available parking using Dijkstra's algorithm
	 */
	public void Compute() {
		closestDist = Double.MAX_VALUE;
		
		map.clear();
		
		if(entrance == null) {
			System.err.println("No entrance found");
			return;
		}
		
		Set<Node> visited = new HashSet<>();
		PriorityQueue<Node> queue = new PriorityQueue<Node>(Comparator.comparingDouble(node -> map.getOrDefault(node, Double.MAX_VALUE)));
		
		map.put(entrance, 0.0);
		queue.add(entrance);
		
		while(!queue.isEmpty()) {
			Node current = queue.poll();
			
			if(visited.contains(current)) {
				continue;
			}
			visited.add(current);
			
			double currentDist = map.get(current);
			 
			if(current.getType() == Node.NodeType.PARKING_SPOT && currentDist < closestDist) {
				closestParking = current;
				closestDist = currentDist;
			}
			
			//Relax the edges of the node
			try {
				for(Edge edge : current.getEdges()) {
					Node neighbor = edge.getTo();
					
					//If edge is unvisited then calculate and update distance to node
					if(visited.contains(neighbor)) {
						continue;
					}
					
					double newDist = currentDist + edge.getWeight();
					if(!map.containsKey(neighbor) || newDist < map.get(neighbor)) {
						map.put(neighbor, newDist);
						queue.add(neighbor);
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
		if(entrance != null) {
			return closestDist;
		}
		return 0.0;
	} 
}
