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
				int scale = 5;
				int blockSize = 10;
				double edgePercentage = 0.005;
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
		
		/**
		 * Method to scan through the image and find blocks of set colours
		 * If a block is found, find the central coordinate of that block and store it
		 * @param image The image containing the metadata blocks
		 * @param red (0-255) The red value of the block to be found
		 * @param green (0-255) The green value of the block to be found
		 * @param blue (0-255) The blue value of the block to be found
		 */
		public void findSection(BufferedImage image, int red, int green, int blue) {
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
		                if (c.getRed() == red && c.getGreen() == green && c.getBlue() == blue) {
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
		                    if (red == 0 && green == 255 && blue == 0) {
		                        entranceCoord = new Pair<>(centerX, centerY);
		                        entrance = new Node(centerX, centerY, Node.NodeType.ENTRANCE, false);
		                        System.out.println("Entrance coords: (" + centerX + "," + centerY + ")");
		                    //Case where the rectangle is blue (parking)
		                    } else if (red == 0 && green == 0 && blue == 255) {
		                        parkingCoords.add(new Pair<>(centerX, centerY));
		                        System.out.println("Added parking coord: (" + centerX + "," + centerY + ")");
		                    //Case where the rectangle is red (exit)
		                    } else if (red == 255 && green == 0 && blue == 0) {
		                    	exitCoords.add(new Pair<>(centerX, centerY));
		                    	System.out.println("Added new exit coord: (" + centerX + "," + centerY + ")");
		                    }
		                }
		            }
		        }
		    }
		}
		
		/**
		 * Method to create a visual representation of the Node[][] grid
		 * Added mostly for debug purposes
		 * @param grid The Node[][] grid of Nodes 
		 * @param scale The factor of the decrease in size from the original image
		 * @return 
		 */
		public static BufferedImage visualizeGrid(Node[][] grid, int scale) {
		    int width = grid[0].length * scale;
		    int height = grid.length * scale;
	
		    BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		    Graphics2D g = output.createGraphics();
	
		    for (int y = 0; y < grid.length; y++) {
		        for (int x = 0; x < grid[0].length; x++) {
		            Node node = grid[y][x];
		            Color color;
	
		            if (node == null) {
		                color = Color.BLACK; //Empty/invalid
		            } else if (node.getType() == Node.NodeType.ENTRANCE) {
		                color = Color.GREEN; //Entrance
		            } else if (node.getType() == Node.NodeType.ROAD) {
		                color = Color.WHITE; //Road
		            } else if (node.getType() == Node.NodeType.PARKING_SPOT) {
		            	color = Color.BLUE;
		            } else {
		                color = Color.RED; //Default
		            }
	
		            g.setColor(color);
		            g.fillRect(x * scale, y * scale, scale, scale);
		        }
		    }
	
		    g.dispose();
		    return output;
		}
		
		/**
		 * Method to convert an image from RGB to greyscale
		 * @param image The colour image to be used to generate a greyscale image
		 * @param i
		 * @return
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
		 * @param greyImage
		 * @param blockSize
		 * @param i
		 * @return
		 */
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
		
		/**
		 * Method to detect edges for the image
		 * Compares the colour of each pixel with the colour of the pixel to its right and below it
		 * this ensures no duplicate comparison while still calculating edges for the whole picture
		 * This method works well even for images with shadows but results in a noisy image
		 * @param greyImage
		 * @param threshold
		 * @param i
		 * @return 
		 */
		public static BufferedImage edges(BufferedImage greyImage, int threshold, int i) {
			try {
				BufferedImage edgeImage = new BufferedImage(greyImage.getWidth(), greyImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
	
				for(int y = 0; y < greyImage.getHeight() -1; y++) {
					for(int x = 0; x < greyImage.getWidth() -1; x++) {
						int current = new Color(greyImage.getRGB(x, y)).getRed();
						int right = new Color(greyImage.getRGB(x+1, y)).getRed();
						int down = new Color(greyImage.getRGB(x, y+1)).getRed();
						
						if(Math.abs(current - right) > threshold || Math.abs(current - down) > threshold) {
							edgeImage.setRGB(x, y, Color.WHITE.getRGB());
						} else {
							edgeImage.setRGB(x, y, Color.BLACK.getRGB());
						}
					}
				}
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
		 * @param downSample
		 * @param threshold
		 * @param i
		 * @return
		 */
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
			System.err.println("Could not process image");
			return null;
		}
		
		/**
		 * Method to reduce some of the noise on the images created by the edges method
		 * 
		 * @param image
		 * @param i
		 * @return
		 */
		public static BufferedImage medianFilter(BufferedImage image, int i) {
		    int width = image.getWidth();
		    int height = image.getHeight();
		    BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
	
		    for (int y = 1; y < height - 1; y++) {
		        for (int x = 1; x < width - 1; x++) {
		            int[] neighbors = new int[9];
		            int index = 0;
	
		            for (int dy = -1; dy <= 1; dy++) {
		                for (int dx = -1; dx <= 1; dx++) {
		                    int rgb = new Color(image.getRGB(x + dx, y + dy)).getRed();
		                    neighbors[index++] = rgb;
		                }
		            }
	
		            Arrays.sort(neighbors);
		            int median = neighbors[4]; // middle value after sort
		            int grey = new Color(median, median, median).getRGB();
		            output.setRGB(x, y, grey);
		           
		        }
		    }
		    try {
				ImageIO.write(output, "png", new File("output/image_"+ i + "_filtered.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    return output;
		}
		
		/**
		 * 
		 * @param image The filtered edge image to be processed
		 * @param scale The factor by which to scale the image down
		 * 		  		1x is original, 2x is half the original etc
		 * @return
		 */
		public Node[][] createNodes(BufferedImage image, int scale, int entranceX, int entranceY, 
									int blockSize, double edgePercentage, List<Pair<Integer,Integer>> parkingCoords) {	
			int width = image.getWidth();
			int height = image.getHeight();
			int buffer = 10;
			grid = new Node[height/scale][width/scale];
			
			int entranceGridX = Math.round((float) entranceX/scale);
			int entranceGridY = Math.round((float) entranceY/scale);
			gridEntrance = new Pair<>(entranceGridX,entranceGridY);
			for (int dy = -5; dy <= 5; dy++) {
			    for (int dx = -5; dx <= 5; dx++) {
			        int nx = entranceGridX + dx;
			        int ny = entranceGridY + dy;
			        if (nx >= 0 && ny >= 0 && ny < grid.length && nx < grid[0].length) {
			            grid[ny][nx] = new Node(nx, ny, Node.NodeType.ROAD, false);
			        }
			    }
			}
			for(int y = 0; y < height/scale; y++) {
				for(int x = 0; x < width/scale; x++) {
					int edgeCounter = 0;
					
					for(int by = 0; by < blockSize; by++) {
						for(int bx = 0; bx < blockSize; bx++) {
							int pixelX = x * scale + bx;
							int pixelY = y * scale + by;
							
							if(pixelX >= width || pixelY >= height) {
								continue;
							}
							
							int colour = new Color(image.getRGB(pixelX, pixelY)).getRed();
							if(colour > 100) {
								edgeCounter++;
							}
						}
					}
					int totalPixels = blockSize * blockSize;
					if(((double)edgeCounter/totalPixels) < edgePercentage) {
						grid[y][x] = new Node(x,y,Node.NodeType.ROAD,false);
					}
				}
			}
			Node entranceNode = new Node(entranceGridX, entranceGridY, Node.NodeType.ENTRANCE, false);
			grid[entranceGridY][entranceGridX] = entranceNode;
			this.entrance = entranceNode;

			for(Pair<Integer,Integer> p : parkingCoords) {
				int parkingGridX = Math.round((float) p.getKey()/scale);
				int parkingGridY = Math.round((float) p.getValue()/scale);
				grid[parkingGridY][parkingGridX] = new Node(parkingGridX,parkingGridY,Node.NodeType.PARKING_SPOT,true);
				createEdges(grid);
				System.out.println("Parking edges: " + grid[parkingGridY][parkingGridX].getEdges().size());
				System.out.println("Parking Grid X: " + parkingGridX + ", Y: " + parkingGridY);
			}
			for(Pair<Integer,Integer> e : exitCoords) {
				int exitGridX = Math.round((float) e.getKey()/scale);
				int exitGridY = Math.round((float) e.getValue()/scale);
				grid[exitGridY][exitGridX] = new Node(exitGridX,exitGridY,Node.NodeType.EXIT,false);
				createEdges(grid);
				System.out.println("Exit Grid X: " + exitGridX + ", Y: " + exitGridY);
			}
			System.out.println("Entrance edges: " + grid[entranceGridY][entranceGridX].getEdges().size());
			return grid;
		}
		
		/**
		 * Helper method to create edges for each node in the grid
		 * @param grid
		 */
		public static void createEdges(Node[][] grid) {
			for(int y = 0; y < grid.length; y++) {
				for(int x = 0; x < grid[0].length; x++) {
					Node current = grid[y][x];
					int[][] directions = {
							{0,1}, //Positive x
							{0,-1}, //Negative x
							{1,0}, //Positive y
							{-1,0} //Negative y
					};
					
					for(int[] dir : directions) {
						int nx = x + dir[0];
			            int ny = y + dir[1];
			            if (nx >= 0 && nx < grid[0].length && ny >= 0 && ny < grid.length) {
			            	Node neighbor = grid[ny][nx];
			            	if(neighbor == null) { continue; }
			            	if(current == null) { continue; }
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
		
		/**
		 * Getter for the entrance Node
		 * @return Node The node representing the entrance
		 */
		public Node getEntrance() {
			return entrance;
		}
		
		/**
		 * Getter for the list of exits
		 * @return List<Node> A list of the exit nodes
		 */
		public List<Node> getExitList() {
			List<Node> nodeList = new ArrayList<Node>();
			for(Pair<Integer,Integer> p : exitCoords) {
				Node exitNode = grid[p.getKey()][p.getValue()];
				nodeList.add(exitNode);
			}
			return nodeList;
		}
	}
