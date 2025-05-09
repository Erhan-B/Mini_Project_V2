//Edge class
public class Edge {
    private final Node from;
    private final Node to;
    private double weight;
    /**
     * 
     * @param from
     * @param to
     * @param weight
     */
	public Edge(Node from, Node to, double weight) {
		this.from = from;
		this.to = to;
		this.weight = weight;
	}
	
	//getters and setters.
	public Node getFrom() {
		return this.from;
	}
	
	public Node getTo() {
		return this.to;
	}
	
	public double getWeight() {
		return this.weight;
	}
	/**
	 * Set the weight of the edge.(Distance between 2 nodes.)
	 * @param weight
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	@Override
	public String toString() {
		return String.format("Edge(%s -> %s, weight=%d)", from, to, weight);
	}

}