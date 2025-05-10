import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.function.BinaryOperator;

import javax.imageio.ImageIO;

public class Main {

	public static void main(String[] args){
		String[] imageList = new String[6];
		//Skip one for now
		//Need to create ideal parking (drawn) for image_1.jpg
		for(int i = 2; i < 6; i++) {
			imageList[i] = "data/image_" + i +".jpg";
			System.out.println(imageList[i]);
			GreyImageProcessor.processImage(imageList[i],i);
			
		}
			
//			GreyImageProcessor.processImage("data/image_2.jpg", 2);

			
			
			
			
			
			
			//Old main
//			try {
//			File imageFile = new File("data/image_2.jpg");
//			BufferedImage image = ImageIO.read(imageFile);
//			ImageProcessor processor = new ImageProcessor();
//			Graph graph = processor.processImage(image);
//			List<Node> list = graph.getNodes();
//			System.out.println(list);
//			for(Node n : list) {
//				n.getCameFrom();
//				System.out.println(n);
//			}
//			Dijkstra dijkstra = new Dijkstra(graph.createGrid(10, 10));
//			dijkstra.Compute();
//		} catch (FileNotFoundException fnf) {
//			fnf.printStackTrace();
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
		
		
		
	}
}
