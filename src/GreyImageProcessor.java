import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

import javax.imageio.ImageIO;

public class GreyImageProcessor {
	public static void processImage(String filePath, int i) {
		BufferedImage grey = greyScale(filePath, i);
		//BufferedImage downSampled = downSample(grey, 2); //This step has varying results
		BufferedImage binary = threshold(grey, 200, i);
	}
	
	public static BufferedImage greyScale(String filePath, int i) {
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
			
			ImageIO.write(greyImage, "png", new File("output/image_"+ i + "_greyscale.png"));
			return greyImage;
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static BufferedImage downSample(BufferedImage greyImage, int blockSize, int i) {
		try {
			BufferedImage result = new BufferedImage(greyImage.getWidth()/blockSize, greyImage.getHeight()/blockSize, BufferedImage.TYPE_BYTE_GRAY);
			
			for(int blockY = 0; blockY < greyImage.getHeight()/blockSize; blockY++) {
				for(int blockX = 0; blockX < greyImage.getWidth()/blockSize; blockX++) {
					int greySum = 0;
					int pixelCount = 0;
					for(int y = 0; y < blockSize; y++) {
						for(int x = 0; x < blockSize; x++) {
							int py = blockY * blockSize + y;
							int px = blockX * blockSize + x;
							
							if(py < greyImage.getHeight() && px < greyImage.getWidth()) {
								int greyValue = new Color(greyImage.getRGB(px, py)).getRed();
								greySum += greyValue;
								pixelCount++;
							}
						}
					}
					int average = greySum/pixelCount;
					Color avgColour = new Color(average, average, average);
					result.setRGB(blockX, blockY, avgColour.getRGB());
				}
			}
			ImageIO.write(result, "png", new File("output/image_"+ i + "_average.png"));
			return result;
			
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static BufferedImage threshold(BufferedImage downSample, int threshold, int i) {
		try {
			BufferedImage binaryImage = new BufferedImage(downSample.getWidth(), downSample.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
			
			for(int y = 0; y < downSample.getHeight(); y++) {
				for(int x = 0; x < downSample.getWidth(); x++) {
					int rgb = downSample.getRGB(x, y);
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
			
			ImageIO.write(binaryImage, "png", new File("output/image_"+ i + "_binary.png"));
			return binaryImage;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
