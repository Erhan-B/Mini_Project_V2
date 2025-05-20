	/**
	 * @author E Bredell 222024369
	 * @version Mini_Project
	 */
	import java.awt.Color;
	import java.awt.Graphics2D;
	import java.awt.image.BufferedImage;
	import java.io.File;
	import java.io.FileNotFoundException;
	import java.io.IOException;
	import java.util.ArrayList;
	import java.util.Arrays;
	import java.util.List;
	
	import javax.imageio.ImageIO;

import javafx.util.Pair;
	
	
	/**
	 * Class that handles the process of taking an image and creating a node graph from it
	 * 1) The image gets scanned and the entrances, parking spots, and the exits get detected
	 * 2) The image is converted to greyscale
	 * 3) The image is passed through a method to create a binary image from the edges
	 * 4) The edge image is then passed through a method to reduce some of the noise present
	 * 5) The binary edge image is passed through the createNodes method which creates a grid of nodes
	 * 6) The grid of nodes then gets connected with edges and is ready to be used by other classes
	 */
	public class Image_Processor {
		//The grid of nodes representing the traversable image
		private Node[][] grid;
		//The entrance node of the parking lot
		private Node entrance;
		//The coordinates of the entrance (in terms of the original pixels)
		private Pair<Integer,Integer> entranceCoord;
		//The coordinates of the entrance (in terms of the scaled Node grid)
		private Pair<Integer,Integer> gridEntrance;
		//The coordinates of the detected parking spots
		private List<Pair<Integer,Integer>> parkingCoords;
		//The coordinates of the detected exits
		private List<Pair<Integer,Integer>> exitCoords;
		//The factor by which to scale the image down
		private int scale = 5;
		//The resolution of the Node grid
		private int blockSize = 10;
		//Threshold for maximum number of edge pixels before a section is no longer considered a road
		private double edgePercentage = 0.005;
		
		/**
		 * Parameterized constructor for the Image_Processor
		 */
		public Image_Processor(int scale, int blockSize, double edgePercentage) {
			grid = null;
			entrance = new Node(0,0,Node.NodeType.ENTRANCE,false);
			entranceCoord = new Pair<>(0,0);
			parkingCoords  = new ArrayList<>();
			exitCoords = new ArrayList<>();
			
			this.scale = scale;
			this.blockSize = blockSize;
			this.edgePercentage = edgePercentage;
		}
		
		/**
		 * Default constructor for the Image_Processor
		 */
		public Image_Processor() {
			grid = null;
			entrance = new Node(0,0,Node.NodeType.ENTRANCE,false);
			entranceCoord = new Pair<>(0,0);
			parkingCoords  = new ArrayList<>();
			exitCoords = new ArrayList<>();
		}
		
		/**
		 * Method that calls all the helper methods in order to process the image
		 * @param imageFilePath The path to the image that contains no metadata
		 * @param metaFilePath The path to the image that contains metadata
		 * @param i
		 */
		public void processImage(String imageFilePath, String metaFilePath, int i) {
			try {
				//Read the two image files
				BufferedImage colour = ImageIO.read(new File(imageFilePath));
				BufferedImage metaImage = ImageIO.read(new File(metaFilePath));
				
				//Scan through the images to find the entrances,parking spots, and exits and store them
				findSection(metaImage, 0, 255, 0); //Entrance (Green)
				findSection(metaImage, 0, 0, 255); //Parking spots (Blue)
				findSection(metaImage, 255, 0, 0); //Exits (Red)
				
				//Generate a greyscale image
				BufferedImage grey = greyScale(colour, i);
				
				//Detect edges on the greyscale image and create an image containing those edges
				BufferedImage edge =  edges(grey, 30, i);
				
				//Filter out extra noise in image
	//			BufferedImage filtered = medianFilter(edge, i);
				
				//Create a grid based on the detected edges
				int entranceX = entranceCoord.getKey();
				int entranceY = entranceCoord.getValue();
				grid = createNodes(edge, scale, entranceX, entranceY, blockSize, edgePercentage, parkingCoords);
				
				//Create a visual representation of the Node[][] grid
			    BufferedImage visual = visualizeGrid(grid, scale);
			    File outputfile = new File("output/grid_result_" + i + ".png");
			    ImageIO.write(visual, "png", outputfile);
			    
			} catch (FileNotFoundException fnf) {
				System.err.println("Image Processor: File not found");
				fnf.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    System.out.println("Grid visualization saved as grid_result" + i + ".png");
		}
		
//		public void processImage(String imageFilePath, String metaFilePath, int i) {
//		    try {
//		        // Check if files exist first
//		        File imageFile = new File(imageFilePath);
//		        File metaFile = new File(metaFilePath);
//		        
//		        if (!imageFile.exists()) {
//		            throw new FileNotFoundException("Image file not found: " + imageFilePath);
//		        }
//		        if (!metaFile.exists()) {
//		            throw new FileNotFoundException("Metadata file not found: " + metaFilePath);
//		        }
//
//		        // Create output directory if it doesn't exist
//		        new File("output").mkdirs();
//
//		        //Read the two image files
//		        BufferedImage colour = ImageIO.read(imageFile);
//		        BufferedImage metaImage = ImageIO.read(metaFile);
//		        
//		        //Scan through the images to find the entrances,parking spots, and exits and store them
//		        findSection(metaImage, 0, 255, 0); //Entrance (Green)
//		        findSection(metaImage, 0, 0, 255); //Parking spots (Blue)
//		        findSection(metaImage, 255, 0, 0); //Exits (Red)
//		        
//		        //Generate a greyscale image
//		        BufferedImage grey = greyScale(colour, i);
//		        
//		        //Detect edges on the greyscale image and create an image containing those edges
//		        BufferedImage edge = edges(grey, 30, i);
//		        
//		        //Create a grid based on the detected edges
//		        int scale = 5;
//		        int blockSize = 10;
//		        double edgePercentage = 0.005;
//		        int entranceX = entranceCoord.getKey();
//		        int entranceY = entranceCoord.getValue();
//		        grid = createNodes(edge, scale, entranceX, entranceY, blockSize, edgePercentage, parkingCoords);
//		        
//		        if (grid == null) {
//		            throw new Exception("Grid creation failed");
//		        }
//		        
//		        //Create a visual representation of the Node[][] grid
//		        BufferedImage visual = visualizeGrid(grid, scale);
//		        File outputfile = new File("output/grid_result_" + i + ".png");
//		        ImageIO.write(visual, "png", outputfile);
//		        
//		    } catch (FileNotFoundException fnf) {
//		        System.err.println("Image Processor: File not found: " + fnf.getMessage());
//		        throw new RuntimeException(fnf);
//		    } catch (IOException e) {
//		        System.err.println("Image Processor: IO Error: " + e.getMessage());
//		        throw new RuntimeException(e);
//		    } catch (Exception e) {
//		        System.err.println("Image Processor: Error: " + e.getMessage());
//		        throw new RuntimeException(e);
//		    }
//		    System.out.println("Grid visualization saved as grid_result" + i + ".png");
//		}
		
		/**
		 * Method to scan through the image and find blocks of set colours
		 * If a block is found, find the central coordinate of that block and store it
		 * @param image The image containing the metadata blocks
		 * @param red (0-255) The red value of the block to be found
		 * @param green (0-255) The green value of the block to be found
		 * @param blue (0-255) The blue value of the block to be found
		 */
		private void findSection(BufferedImage image, int red, int green, int blue) {
			//Get the width/height of the image
			int width = image.getWidth();
		    int height = image.getHeight();
		    //Create an array to track if section has been visited
		    boolean[][] visited = new boolean[height][width];
		    //Loop through all of the pixels in the image
		    for (int y = 0; y < height; y++) {
		        for (int x = 0; x < width; x++) {
		        	//If the pixel in that position has not been visited
		            if (!visited[y][x]) {
		            	//Get the colour of the pixel at that position
		                Color c = new Color(image.getRGB(x, y));
		                //If it matches the specified RGB value
		                if (colorClose(c, red, green, blue, 3)) {
		                    //Store the top left corner of the rectangle
		                    int maxX = x;
		                    int maxY = y;

		                    //Find the right limit of the rectangle
		                    while (maxX + 1 < width && new Color(image.getRGB(maxX + 1, y)).equals(c)) {
		                        maxX++;
		                    }
		                    //Find the bottom of the rectangle
		                    while (maxY + 1 < height && new Color(image.getRGB(x, maxY + 1)).equals(c)) {
		                        maxY++;
		                    }
		                    //Update visited
		                    for (int yy = y; yy <= maxY; yy++) {
		                        for (int xx = x; xx <= maxX; xx++) {
		                            visited[yy][xx] = true;
		                        }
		                    }

		                    //Calculate the center of the rectangle
		                    int centerX = (x + maxX) / 2;
		                    int centerY = (y + maxY) / 2;
		                    
		                    //Case where the rectangle is green (entrance)
		                    if (red == 0 && green >= 240 && blue == 0) {
		                        entranceCoord = new Pair<>(centerX, centerY);
		                        entrance = new Node(centerX, centerY, Node.NodeType.ENTRANCE, false);
		                        System.out.println("Entrance block coords: (" + centerX + "," + centerY + ")");
		                    //Case where the rectangle is blue (parking)
		                    } else if (red == 0 && green == 0 && blue >= 240) {
		                        parkingCoords.add(new Pair<>(centerX, centerY));
		                        System.out.println("Added block parking coord: (" + centerX + "," + centerY + ")");
		                    //Case where the rectangle is red (exit)
		                    } else if (red >= 240 && green == 0 && blue == 0) {
		                    	exitCoords.add(new Pair<>(centerX, centerY));
		                    	System.out.println("Added new exit coord: (" + centerX + "," + centerY + ")");
		                    }
		                }
		            }
		        }
		    }
		}
		
		
		private boolean colorClose(Color c, int r, int g, int b, int tolerance) {
		    return Math.abs(c.getRed() - r) <= tolerance &&
		           Math.abs(c.getGreen() - g) <= tolerance &&
		           Math.abs(c.getBlue() - b) <= tolerance;
		}

		/**
		 * Method to create a visual representation of the Node[][] grid
		 * Added mostly for debug purposes
		 * @param grid The Node[][] grid of Nodes 
		 * @param scale The factor of the decrease in size from the original image
		 * @return BufferedImage representation of the node grid with the following colour representations:
		 *  Black- No node present
		 *  White- Node of type ROAD
		 *  Blue- Node of type PARKING_SPOT
		 *  Red- Node of type EXIT
		 */
		public static BufferedImage visualizeGrid(Node[][] grid, int scale) {
			//Get the lenght/width of the grid and decrease it by the scale amount
		    int width = grid[0].length * scale;
		    int height = grid.length * scale;
		    
		    //For the image creation
		    BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		    Graphics2D g = output.createGraphics();
		    
		    //Loop through each pixel of the grid and check the colour at each pixel
		    for (int y = 0; y < grid.length; y++) {
		        for (int x = 0; x < grid[0].length; x++) {
		        	//Get the node at the position in the grid
		            Node node = grid[y][x];
		            Color colour;
		            
		            //Check the type of the node
		            if (node == null) {
		                colour = Color.BLACK; //Empty/invalid
		            } else if (node.getType() == Node.NodeType.ENTRANCE) {
		                colour = Color.GREEN; //Entrance
		            } else if (node.getType() == Node.NodeType.ROAD) {
		                colour = Color.WHITE; //Road
		            } else if (node.getType() == Node.NodeType.PARKING_SPOT) {
		            	colour = Color.BLUE; //Parking spot
		            } else if (node.getType() == Node.NodeType.EXIT) {
		                colour = Color.RED; //Exit
		            } else if (node.getType() == Node.NodeType.PATH) {
		            	colour = Color.ORANGE;
		            } else {
		            	colour = Color.ORANGE; //Default
		            }
		            //Create a rectangle with the appropriate size and colour
		            g.setColor(colour);
		            g.fillRect(x * scale, y * scale, scale, scale);
		        }
		    }
		    //Clean up resources
		    g.dispose();
		    return output;
		}
		
		/**
		 * Method to convert an image from RGB to greyscale
		 * @param image The colour image to be used to generate a greyscale image
		 * @param i The index of the image (used for file creation)
		 * @return BufferedImage GreyScale representation of the image
		 */
		public static BufferedImage greyScale(BufferedImage image, int i) {
			try {
				//Create new image that will be filled in with the greyscale representation
				BufferedImage greyImage = new BufferedImage(image.getWidth(), image.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
				
				//Loop through all of the pixels of the colour image
				for(int y = 0; y < image.getHeight(); y++) {
					for(int x = 0; x < image.getWidth(); x++) {
						//Get the rgb values of the pixel
						Color c = new Color(image.getRGB(x, y));
						//Calculate the brightness of the pixel
						int grey = ((c.getRed() + c.getGreen() + c.getBlue()))/3;
						//Create greyScale
						Color greyColour = new Color(grey,grey,grey);
						//Set pixel in greyScale image
						greyImage.setRGB(x, y, greyColour.getRGB());
					}
				}
				//Create new image file
				ImageIO.write(greyImage, "png", new File("output/image_"+ i + "_greyscale.png"));
				return greyImage;
			} catch(IOException ex) {
				ex.printStackTrace();
			}
			return null;
		}
		
		/**
		 * Method to reduce the pixels of the image by taking the average of pixels in a block and creating a new sampled image
		 * Accuracy of this method is questionable
		 * @param greyImage The greyScale image to be processed
		 * @param blockSize The int size of the block size to be used in the calculations
		 * @param i The int index (used for file creation)
		 * @return BufferedImage of the down sampled image
		 */
		public static BufferedImage downSample(BufferedImage greyImage, int blockSize, int i) {
			try {
				//For the image creation
				BufferedImage result = new BufferedImage(greyImage.getWidth()/blockSize, greyImage.getHeight()/blockSize, BufferedImage.TYPE_BYTE_GRAY);
				
				//Loop through the image in the specified block sizes
				for(int blockY = 0; blockY < greyImage.getHeight()/blockSize; blockY++) {
					for(int blockX = 0; blockX < greyImage.getWidth()/blockSize; blockX++) {
						//Variables used in calculations
						int greySum = 0;
						int pixelCount = 0;
						//Loop through each pixel in the block
						for(int y = 0; y < blockSize; y++) {
							for(int x = 0; x < blockSize; x++) {
								//Calculate the actual pixel coordinates
								int py = blockY * blockSize + y;
								int px = blockX * blockSize + x;
								//Check if still in valid range
								if(py < greyImage.getHeight() && px < greyImage.getWidth()) {
									//Sum all of the grey values in the block
									int greyValue = new Color(greyImage.getRGB(px, py)).getRed();
									greySum += greyValue;
									pixelCount++;
								}
							}
						}
						//Calculate the average grey value of the block
						int average = greySum/pixelCount;
						//Store the average colour to the result image
						Color avgColour = new Color(average, average, average);
						result.setRGB(blockX, blockY, avgColour.getRGB());
					}
				}
				//Create the new image file
				ImageIO.write(result, "png", new File("output/image_"+ i + "_average.png"));
				return result;
			} catch(IOException ex) {
				ex.printStackTrace();
			}
			return null;
		}
		
		/**
		 * Method to detect edges for the image
		 * Compares the colour of each pixel with the colour of the pixel to its right and below it
		 * this ensures no duplicate comparison while still calculating edges for the whole picture
		 * This method works well even for images with shadows but results in a noisy image
		 * @param greyImage The greyScale image input
		 * @param threshold The minimum colour difference for a pixel to be considered an edge
		 * @param i The int index (used for file creation)
		 * @return BufferedImage representing the edges in the greyscale image
		 */
		public static BufferedImage edges(BufferedImage greyImage, int threshold, int i) {
			try {
				//For image creation
				BufferedImage edgeImage = new BufferedImage(greyImage.getWidth(), greyImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
				
				//Loop through the pixels in the greyscale image
				for(int y = 0; y < greyImage.getHeight() -1; y++) {
					for(int x = 0; x < greyImage.getWidth() -1; x++) {
						//Store the grey value of the current pixel
						int current = new Color(greyImage.getRGB(x, y)).getRed();
						//Store the grey value of the pixel to the right
						int right = new Color(greyImage.getRGB(x+1, y)).getRed();
						//Store the grey value of the pixel under the current
						int down = new Color(greyImage.getRGB(x, y+1)).getRed();
						
						//Check if the difference is more than the threshold
						if(Math.abs(current - right) > threshold || Math.abs(current - down) > threshold) {
							edgeImage.setRGB(x, y, Color.WHITE.getRGB());
						} else {
							edgeImage.setRGB(x, y, Color.BLACK.getRGB());
						}
					}
				}
				//Create the image file
				ImageIO.write(edgeImage, "png", new File("output/image_"+ i + "_edge.png"));
				return edgeImage;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return null;
		}
		
		/**
		 * Method to convert a greyscale image to a binary image where each pixel is either black or white
		 * if the pixel value is under the threshold it gets set to black
		 * if the pixel value is at or above the threshold it gets set to white
		 * This method works well for images without shadows
		 * @param downSample The downsampled image to be processed
		 * @param threshold The rgb value to decide whether a pixel should be set to white or black
		 * @param i The int index (used for file creation)
		 * @return BufferedImage the binary (black/white) representation of the input image
		 */
		public static BufferedImage threshold(BufferedImage downSample, int threshold, int i) {
			try {
				//For image creation
				BufferedImage binaryImage = new BufferedImage(downSample.getWidth(), downSample.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
				//Loop through the pixels of the image
				for(int y = 0; y < downSample.getHeight(); y++) {
					for(int x = 0; x < downSample.getWidth(); x++) {
						//Get the grey value of the pixel in the image
						int rgb = downSample.getRGB(x, y);
						int grey = new Color(rgb).getRed();
						int binaryColour;
						//Check if it should be black/white
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
				//Create the image file
				ImageIO.write(binaryImage, "png", new File("output/image_"+ i + "_binary.png"));
				return binaryImage;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.err.println("Could not process image");
			return null;
		}
		
		/**
		 * Method to reduce some of the noise on the images created by the edges method
		 * Works by checking if the median neighbor pixel's value
		 * @param image The BufferedImage to be processed
		 * @param i The int index (used for image creation)
		 * @return BufferedImage the filtered image with less noise
		 */
		public static BufferedImage medianFilter(BufferedImage image, int i) {
			//Get the image width/height
		    int width = image.getWidth();
		    int height = image.getHeight();
		    //For image creation
		    BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		    //Loop through the image pixels
		    for (int y = 1; y < height - 1; y++) {
		        for (int x = 1; x < width - 1; x++) {
		            int[] neighbors = new int[9];
		            int index = 0;
		            //Store the neighbors of the current pixel
		            for (int dy = -1; dy <= 1; dy++) {
		                for (int dx = -1; dx <= 1; dx++) {
		                	//Get the grey value of the pixel
		                    int rgb = new Color(image.getRGB(x + dx, y + dy)).getRed();
		                    neighbors[index++] = rgb;
		                }
		            }
		            //Sort the array and get the median value
		            Arrays.sort(neighbors);
		            int median = neighbors[4]; //Middle value after sort
		            int grey = new Color(median, median, median).getRGB();
		            //Store the chosen value
		            output.setRGB(x, y, grey);
		        }
		    }
		    try {
		    	//Create the image file
				ImageIO.write(output, "png", new File("output/image_"+ i + "_filtered.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		    return output;
		}
		
		/**
		 * Method that creates nodes from an image that has been processed
		 * @param image The filtered edge image to be processed
		 * @param scale The factor by which to scale the image down
		 * 		  		1x is original, 2x is half the original etc
		 * @param entranceX The x-coordinate of the entrance
		 * @param entranceY The y-coordinate of the entrance
		 * @param blockSize The resolution of the Node grid
		 * @param edgePercentage Threshold for maximum number of edge pixels 
		 * 						 before a section is no longer considered a road
		 * @param parkingCoords The list of coordinates of the parking spots
		 * @return Node[][] The 2d grid of nodes representing the parkinglot
		 */
		public Node[][] createNodes(BufferedImage image, int scale, int entranceX, int entranceY, 
									int blockSize, double edgePercentage, List<Pair<Integer,Integer>> parkingCoords) {	
			//Get the image width/height
			int width = image.getWidth();
			int height = image.getHeight();
			//Create the scaled grid
			grid = new Node[height/scale][width/scale];
			
			//Calculate the entrance coordinates in terms of the scale
			int entranceGridX = Math.round((float) entranceX/scale);
			int entranceGridY = Math.round((float) entranceY/scale);
			gridEntrance = new Pair<>(entranceGridX,entranceGridY);
			
//			//Create buffer area around the entrance so that it doesnt get blocked by edges
//			//Loop through the block of pixels
//			for (int dy = -5; dy <= 5; dy++) {
//			    for (int dx = -5; dx <= 5; dx++) {
//			        int nx = entranceGridX + dx;
//			        int ny = entranceGridY + dy;
//			        //Set area around the entrance to ROAD
//			        if (nx >= 0 && ny >= 0 && ny < grid.length && nx < grid[0].length) {
//			            grid[ny][nx] = new Node(nx, ny, Node.NodeType.ROAD, false);
//			        }
//			        
//			    }
//			}
			
			//Create buffer area around the entrance so that it doesnt get blocked by edges
			//Loop through the block of pixels
			for (int dy = -5; dy <= 5; dy++) {
			    for (int dx = -5; dx <= 5; dx++) {
			        int nx = entranceGridX + dx;
			        int ny = entranceGridY + dy;
			        // Add additional bounds checking
			        if (nx >= 0 && ny >= 0 && ny < grid.length && nx < grid[0].length) {
			            grid[ny][nx] = new Node(nx, ny, Node.NodeType.ROAD, false);
			        }
			    }
			}
			
			for(Pair<Integer,Integer> p : exitCoords) {
				//Create buffer area around the exit
				int exitGridX = Math.round((float) p.getKey() / scale);
				int exitGridY = Math.round((float) p.getValue() / scale);
			    for (int dy = -5; dy <= 5; dy++) {
			        for (int dx = -5; dx <= 5; dx++) {
			            int nx = exitGridX + dx;
			            int ny = exitGridY + dy;
			            if (nx >= 0 && ny >= 0 && ny < grid.length && nx < grid[0].length) {
			                grid[ny][nx] = new Node(nx, ny, Node.NodeType.ROAD, false);
			            }
			        }
			    }
			}
			//Loop through the image
			for(int y = 0; y < height/scale; y++) {
				for(int x = 0; x < width/scale; x++) {
					// Make sure we don't go out of bounds
			        if (y >= grid.length || x >= grid[0].length) {
			            continue;
			        }
					int edgeCounter = 0;
					//Check the number of edge pixels in a grid
					for(int by = 0; by < blockSize; by++) {
						for(int bx = 0; bx < blockSize; bx++) {
							int pixelX = x * scale + bx;
							int pixelY = y * scale + by;
							//Check if still in bounds
							if(pixelX >= width || pixelY >= height) {
								continue;
							}
							//Store pixel grey value
							int colour = new Color(image.getRGB(pixelX, pixelY)).getRed();
							if(colour > 100) {
								edgeCounter++;
							}
						}
					}
					//check proportion of pixels that are edges
					//If less than the allowed percentage then create a road
					int totalPixels = blockSize * blockSize;
					if(((double)edgeCounter/totalPixels) < edgePercentage) {
						grid[y][x] = new Node(x,y,Node.NodeType.ROAD,false);
					}
				}
			}
			//Create and place the entrance Node
			Node entranceNode = new Node(entranceGridX, entranceGridY, Node.NodeType.ENTRANCE, false);
			grid[entranceGridY][entranceGridX] = entranceNode;
			this.entrance = entranceNode;
			System.out.println("Entrance Grid X: " + entranceGridX + ", Y: " + entranceGridY);

			//create and set the parking spots
//			for(Pair<Integer,Integer> p : parkingCoords) {
//				//Calculate the relative coordinates of each parking spot
//				int parkingGridX = Math.round((float) p.getKey()/scale);
//				int parkingGridY = Math.round((float) p.getValue()/scale);
//				grid[parkingGridY][parkingGridX] = new Node(parkingGridX,parkingGridY,Node.NodeType.PARKING_SPOT,true);
//				//Create edges for the parking spots
//				createEdges(grid);
//				System.out.println("Parking Grid X: " + parkingGridX + ", Y: " + parkingGridY);
//			}
			
			//create and set the parking spots
			for(Pair<Integer,Integer> p : parkingCoords) {
			    //Calculate the relative coordinates of each parking spot
			    int parkingGridX = Math.round((float) p.getKey()/scale);
			    int parkingGridY = Math.round((float) p.getValue()/scale);
			    // Add bounds checking
			    if (parkingGridY >= 0 && parkingGridY < grid.length && 
			        parkingGridX >= 0 && parkingGridX < grid[0].length) {
			        grid[parkingGridY][parkingGridX] = new Node(parkingGridX,parkingGridY,Node.NodeType.PARKING_SPOT,true);
			        //Create edges for the parking spots
			        createEdges(grid);
			        System.out.println("Parking Grid X: " + parkingGridX + ", Y: " + parkingGridY);
			    }
			}
			//Create and set the exits
//			for(Pair<Integer,Integer> e : exitCoords) {
//				//calculate the relative exit coords
//				int exitGridX = Math.round((float) e.getKey()/scale);
//				int exitGridY = Math.round((float) e.getValue()/scale);
//				grid[exitGridY][exitGridX] = new Node(exitGridX,exitGridY,Node.NodeType.EXIT,false);
//				//create edges for the exit spots
//				createEdges(grid);
//				System.out.println("Exit Grid X: " + exitGridX + ", Y: " + exitGridY);
//			}
			
//			for(Pair<Integer,Integer> p : exitCoords) {
//			    //Create buffer area around the exit
//			    int exitGridX = Math.round((float) p.getKey() / scale);
//			    int exitGridY = Math.round((float) p.getValue() / scale);
//			    for (int dy = -5; dy <= 5; dy++) {
//			        for (int dx = -5; dx <= 5; dx++) {
//			            int nx = exitGridX + dx;
//			            int ny = exitGridY + dy;
//			            // Add additional bounds checking
//			            if (nx >= 0 && ny >= 0 && ny < grid.length && nx < grid[0].length) {
//			                grid[ny][nx] = new Node(nx, ny, Node.NodeType.ROAD, false);
//			            }
//			        }
//			    }
//			}
			
			//Create and set the exits
			for(Pair<Integer,Integer> e : exitCoords) {
			    //calculate the relative exit coords
			    int exitGridX = Math.round((float) e.getKey()/scale);
			    int exitGridY = Math.round((float) e.getValue()/scale);
			    // Add bounds checking
			    if (exitGridY >= 0 && exitGridY < grid.length && 
			        exitGridX >= 0 && exitGridX < grid[0].length) {
			        grid[exitGridY][exitGridX] = new Node(exitGridX,exitGridY,Node.NodeType.EXIT,false);
			        //create edges for the exit spots
			        createEdges(grid);
			        System.out.println("Exit Grid X: " + exitGridX + ", Y: " + exitGridY);
			    }
			}
			return grid;
		}
		
		/**
		 * Helper method to create edges for each node in the grid
		 * @param grid the 2d Node Grid to set edges
		 */
		public static void createEdges(Node[][] grid) {
			//Loop though the grid
			for(int y = 0; y < grid.length; y++) {
				for(int x = 0; x < grid[0].length; x++) {
					//Store the Node to add edges to
					Node current = grid[y][x];
					//All of the allowed connection directions
					int[][] directions = {
							{0,1}, //Positive x
							{0,-1}, //Negative x
							{1,0}, //Positive y
							{-1,0} //Negative y
					};
					
					//Add edges in every direction where a node is present
					for(int[] dir : directions) {
						int nx = x + dir[0];
			            int ny = y + dir[1];
			            //Check if in bounds still
			            if (nx >= 0 && nx < grid[0].length && ny >= 0 && ny < grid.length) {
			            	Node neighbor = grid[ny][nx];
			            	if(neighbor == null) { continue; }
			            	if(current == null) { continue; }
			            	//Create new edge
			            	Edge edge = new Edge(current, neighbor, current.distanceTo(neighbor));
			            	current.addEdge(edge);
			            }
					}
				}
			}
			
			//For debug
//			for (int y = 0; y < grid.length; y++) {
//			    for (int x = 0; x < grid[0].length; x++) {
//			        Node node = grid[y][x];
//			        if(node != null) {
//			        if (!node.getEdges().isEmpty()) {
//			        	
//			        		 System.out.println("Node at (" + y + "," + x + ") has " + node.getEdges().size() + " neighbors.");
//			        	}
//			        }
//			    }
//			}
		}
		
//		public BufferedImage displayPath(String imagePath, List<Node> path, Color c) {
////			BufferedImage image = null;
////			try {
////				image = ImageIO.read(new File(imagePath));
////			} catch (IOException ex) {
////				ex.printStackTrace();
////			}
////			BufferedImage retImage = image;
////			for(Node n : path) {
////				image.setRGB(n.getX(), n.getY(), c.getRGB());
////			}
////			try {
////				ImageIO.write(retImage, "png", new File(imagePath));
////			} catch (IOException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
//			
//			return retImage;
//		}
		
		public void updateGrid(Node[][] grid, List<Node> path, String filePath) { 
			for(Node n : path) {
				
					grid[n.getY()][n.getX()].setType(Node.NodeType.PATH);	
			}
			BufferedImage image = visualizeGrid(grid, scale);
			try {
				ImageIO.write(image, "png", new File(filePath));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		/**
		 * Getter for the grid coordinates of the entrance
		 * @return gridEntrance A Pair<Integer,Integer> containing the entrance coordinates
		 */
		public Pair<Integer,Integer> getEntranceGrid() {
			return gridEntrance;	
		}
		
		/**
		 * Getter for the Node[][] grid
		 * @return Node[][] A grid of connected Node
		 */
		public Node[][] getGrid() {
			return grid;
		}
		
//		/**
//		 * Getter for the entrance Node
//		 * @return Node The node representing the entrance
//		 */
//		public Node getEntrance() {
//			return entrance;
//		}
		
		/**
		 * Getter for the entrance Node
		 * @return Node The node representing the entrance
		 */
		public Node getEntrance() {
		    // Add bounds checking
		    if (entrance.getY() >= 0 && entrance.getY() < grid.length && 
		        entrance.getX() >= 0 && entrance.getX() < grid[0].length) {
		        return grid[entrance.getY()][entrance.getX()];
		    }
		    return null;
		}
		
//		/**
//		 * Getter for the list of exits
//		 * @return List<Node> A list of the exit nodes
//		 */
//		public List<Node> getExitList() {
//			List<Node> nodeList = new ArrayList<Node>();
//			for(Pair<Integer,Integer> p : exitCoords) {
//				int exitGridX = Math.round((float) p.getKey() / scale);
//				int exitGridY = Math.round((float) p.getValue() / scale);
//				Node exitNode = grid[exitGridY][exitGridX];
//				nodeList.add(exitNode);
//			}
//			return nodeList;
//		}
		
		/**
		 * Getter for the list of exits
		 * @return List<Node> A list of the exit nodes
		 */
		public List<Node> getExitList() {
		    List<Node> nodeList = new ArrayList<Node>();
		    for(Pair<Integer,Integer> p : exitCoords) {
		        int exitGridX = Math.round((float) p.getKey() / scale);
		        int exitGridY = Math.round((float) p.getValue() / scale);
		        
		        // Add bounds checking
		        if (exitGridY >= 0 && exitGridY < grid.length && 
		            exitGridX >= 0 && exitGridX < grid[0].length) {
		            
		            Node exitNode = grid[exitGridY][exitGridX];
		            if (exitNode != null) {  // Additional null check
		                nodeList.add(exitNode);
		            }
		        }
		    }
		    return nodeList;
		}
		
		/**
		 * Getter for the list of nodes
		 * @return List<Node> the list of Nodes in the graph
		 */
		public List<Node> getNodes() {
			List<Node> list = new ArrayList<>();
			for(int y = 0; y < grid.length; y++) {
				for(int x = 0; x < grid[0].length; x++) {
					Node current = grid[y][x];
					if(current != null) {
						list.add(current);
					}
				}
			}
			return list;
		}
	}