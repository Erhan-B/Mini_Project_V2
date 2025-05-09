import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class GreyImageProcessor {
	
	
	public static void greyScale(String filePath) {
		try {
			//Read image to be processed
			BufferedImage colourImage = ImageIO.read(new File(filePath));
			//Create new image that will be filled in with the greyscale representation
			BufferedImage greyImage = new BufferedImage(colourImage.getWidth(), colourImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
			
			//Loop through all of the pixels of the colour image
			for(int y = 0; y < colourImage.getHeight(); y++) {
				for(int x = 0; x < colourImage.getWidth(); x++) {
					//Get the rgb values of the pixel
					Color c = new Color(colourImage.getRGB(x, y));
					//Calculate the brightness of the pixel
					int grey = ((c.getRed() + c.getGreen() + c.getBlue()))/3;
					//Create greyScale
					Color greyColour = new Color(grey,grey,grey);
					//Set pixel in greyScale image
					greyImage.setRGB(x, y, greyColour.getRGB());
				}
			}
			
			ImageIO.write(greyImage, "png", new File("output/parking_lot_grey.png"));
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void threshold(String filePath, int threshold) {
		try {
			BufferedImage greyImage = ImageIO.read(new File(filePath));
			BufferedImage binaryImage = new BufferedImage(greyImage.getWidth(), greyImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
			
			for(int y = 0; y < greyImage.getHeight(); y++) {
				for(int x = 0; x < greyImage.getWidth(); x++) {
					int rgb = greyImage.getRGB(x, y);
					int grey = new Color(rgb).getRed();
					int binaryColour;
					if(grey >= threshold) {
						binaryColour = 255;
					}
					else {
						binaryColour = 0;
					}
					Color c = new Color(binaryColour,binaryColour,binaryColour);
					binaryImage.setRGB(x, y, c.getRGB());
					
					
				}
			}
			
			ImageIO.write(binaryImage, "png", new File("output/parking_lot_binary.png"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
