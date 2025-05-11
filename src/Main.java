

public class Main {

	public static void main(String[] args){

		
			Image_Processor processor = new Image_Processor();
			processor.processImage("data/test_1.png", "data/test_meta.png", 10);
			System.out.println(processor.getGrid());
			System.out.println("Entrance:" + processor.getEntrance().getX() + " " + processor.getEntrance().getY());
			System.out.println(processor.getEntrance().getEdges());
			Dijkstra dijkstra = new Dijkstra(processor.getGrid(), processor.getEntrance());
			System.out.println(dijkstra.getClosestDist());
			System.out.println(dijkstra.getClosestParking());
			System.out.println("Final map: " + dijkstra.getMap());
		
		
    }
}

