import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Main {

	public static void main(String[] args){
//			Image_Processor processor = new Image_Processor();
//			processor.processImage("data/image_0.jpg", "data/image_0_meta.jpg", 0);
			Image_Processor processor2 = new Image_Processor(5, 3, 0.05);
			processor2.processImage("data/image_1.jpg", "data/image_1_meta.jpg", 1);
//			System.out.println(processor.getGrid());
//			System.out.println("Entrance:" + processor.getEntrance().getX() + " " + processor.getEntrance().getY());
			

//			Dijkstra dijkstra = new Dijkstra(processor.getGrid(), processor.getEntrance());
//			dijkstra.Compute();
//			System.out.println("Closest coordinate: (" + dijkstra.getClosestParking().getX() + "," + dijkstra.getClosestParking().getY() + ")"
//								+ "\nDistance to closest parking: " + dijkstra.getClosestDist());
			A_Star_Classification aStar = new A_Star_Classification();
//			double distance = aStar.calculateExitDistance(dijkstra.getClosestParking(), processor.getExitList());
//			System.out.println("A_Star closest distance for exit: " + distance);
//			processor.updateGrid(processor.getGrid(), dijkstra.getPathClosest(), "output/image_0_path.png");
			
			Dijkstra dijkstra2 = new Dijkstra(processor2.getGrid(), processor2.getEntrance());
			dijkstra2.Compute();
			
			processor2.updateGrid(processor2.getGrid(), dijkstra2.getPathClosest(), "output/image_1_path.png");
//			    System.out.print("Path: ");
//			    for (int i = 0; i < dijkstra.getPathClosest().size(); i++) {
//			        Node n = dijkstra.getPathClosest().get(i);
//			        System.out.print("(" + n.getX() + "," + n.getY() + ")");
//			        if (i != dijkstra.getPathClosest().size() - 1) {
//			            System.out.print(" -> ");
//			        }
//			    }
//			    System.out.println();
			
			Image_Processor processor3 = new Image_Processor(5, 3, 0.05);
			processor3.processImage("data/image_0.jpg", "data/image_0_meta.jpg", 1);
			Dijkstra dijkstra3 = new Dijkstra(processor3.getGrid(), processor3.getEntrance());
			dijkstra3.Compute();
			processor3.updateGrid(processor3.getGrid(), dijkstra3.getPathClosest(), "output/image_0_path.png");
			
    }
}
