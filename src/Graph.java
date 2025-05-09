 // Helper class to hold the processed graph

import java.util.List;

public class Graph {
        private final List<Node> nodes;
        // Grid size for simplifying the graph (larger values create simpler graphs)
        private static final int GRID_SIZE = 10;
        public Graph(List<Node> nodes) {
            this.nodes = nodes;
        }
        
        public List<Node> getNodes() {
            return nodes;
        }
        
        public Node[][] createGrid(int height, int width) {
            Node[][] grid = new Node[height / GRID_SIZE + 1][width / GRID_SIZE + 1];
            
            for (Node node : nodes) {
                int gridX = node.getX() / GRID_SIZE;
                int gridY = node.getY() / GRID_SIZE;
                grid[gridY][gridX] = node;
            }
            
            return grid;
        }
    }