import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.function.BinaryOperator;

import javax.imageio.ImageIO;

public class Main {

	public static void main(String[] args){
//		try {
			GreyImageProcessor.greyScale("data/image_1.jpg");
			GreyImageProcessor.threshold("output/parking_lot_grey.png", 200);
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
