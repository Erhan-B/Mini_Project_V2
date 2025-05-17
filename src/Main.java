

public class Main {

	public static void main(String[] args){
			Image_Processor processor = new Image_Processor();
			processor.processImage("data/test_1.png", "data/test_meta.png", 10);
//			System.out.println(processor.getGrid());
			System.out.println("Entrance:" + processor.getEntrance().getX() + " " + processor.getEntrance().getY());
//			Node[][] grid = processor.getGrid();
//			Node entranceFromGrid = grid[processor.getEntranceGrid().getValue()][processor.getEntranceGrid().getKey()];
//			Node entranceFromGetter = processor.getEntrance();

//			System.out.println("Same object? " + (entranceFromGrid == entranceFromGetter));
//			System.out.println("Grid entrance edges: " + entranceFromGrid.getEdges().size());
//			System.out.println("Getter entrance edges: " + entranceFromGetter.getEdges().size());

			Dijkstra dijkstra = new Dijkstra(processor.getGrid(), processor.getEntrance());
			dijkstra.Compute();
			System.out.println("Closest coordinate: (" + dijkstra.getClosestParking().getX() + "," + dijkstra.getClosestParking().getY() + ")"
								+ "\nDistance to closest parking: " + dijkstra.getClosestDist());
			A_Star_Classication aStar = new A_Star_Classication();
			double distance = aStar.calculateExitDistance(dijkstra.getClosestParking(), processor.getExitList());
			System.out.println("A_Star closest distance for exit: " + distance);
		
    }
}

