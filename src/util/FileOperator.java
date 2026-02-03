package util;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class FileOperator {

	public static String updateFileName(String x) {
		// depending on Windows or macOS, flip the slashes

		// Windows:
				return x;

		// macOS:
//		return x.replace('\\', '/');
	}



	public static int[][] readImage(String fileName) {
		return readImage(fileName, 1);
	}
	
	public static int[][] readImage(String fileName, int scale) {
		return readImage(fileName, scale, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public static int[][] readImage(String fileName, int scale, int minX, int minY, int maxX, int maxY) {
		int[][] result = null;
		BufferedImage src;

		try {
			src = ImageIO.read(new File(updateFileName(fileName)));
			
			int w = (Math.min(src.getWidth(), maxX) - minX)/scale;
			int h = (Math.min(src.getHeight() ,maxY) - minY)/scale;
			
			result = new int[w][h];
			
			for(int j=0;j<h;j++) {
				for(int i=0;i<w;i++) {
					if(scale == 1) {
						result[i][j] = src.getRGB(i, j);
					} else {
						Map<Integer, Integer> map = new HashMap<Integer, Integer>();
						for(int xx=0;xx<scale;xx++) {
							for(int yy=0;yy<scale;yy++) {
								int v = src.getRGB(minX + scale*i+xx, minY + scale*j+yy);
								if(map.get(v) == null) map.put(v,1);
								else map.put(v, map.get(v)+1);
							}
						}
						int max = -1;
						int argMax = 0;
						for(int v : map.keySet()) {
							if(map.get(v) > max) {
								max = map.get(v);
								argMax = v;
							}
						}
						result[i][j] = argMax;
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static int[][] readImageMix(String fileName, int scale) {
		int[][] result = null;
		BufferedImage src;

		try {
			src = ImageIO.read(new File(updateFileName(fileName)));
			result = new int[src.getWidth()/scale][src.getHeight()/scale];
			for(int j=0;j<src.getHeight()/scale;j++) {
				for(int i=0;i<src.getWidth()/scale;i++) {
					if(scale == 1) {
						result[i][j] = src.getRGB(i, j);
					} else {
						int[] rgb = new int[] {0, 0 ,0};
						for(int xx=0;xx<scale;xx++) {
							for(int yy=0;yy<scale;yy++) {
								int[] rgbx = Colors.intToRGBArray(src.getRGB(scale*i+xx, scale*j+yy));
								for(int k=0;k<rgbx.length;k++) {
									rgb[k] += rgbx[k];
								}
							}
						}
						int rgba = Colors.rgbToInt(rgb[0] / (scale*scale), rgb[1] / (scale*scale), rgb[2] / (scale*scale));
						result[i][j] = rgba;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void checkAndCreateDirectory(String fileName) throws IOException {
		Path path = Paths.get(fileName);
		Files.createDirectories(path.getParent());
	}

	public static void writeImage(int[][] b, String fileName) {
		writeImage(b, fileName, 1);
	}

	public static void writeImage(int[][] b, String fileName, int scale) {
		BufferedImage image = new BufferedImage(b.length / scale, b[0].length / scale, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < b.length / scale; x++) {
			for (int y = 0; y < b[x].length / scale; y++) {
				if(scale == 1) {
					image.setRGB(x, y, b[x][y]);
				} else {
					int[] rgb = new int[] {0, 0 ,0};
					for(int xx=0;xx<scale;xx++) {
						for(int yy=0;yy<scale;yy++) {
							int[] rgbx = Colors.intToRGBArray(b[scale*x+xx][scale*y+yy]);
							for(int k=0;k<rgbx.length;k++) {
								rgb[k] += rgbx[k];
							}
						}
					}
					int rgba = Colors.rgbToInt(rgb[0] / (scale*scale), rgb[1] / (scale*scale), rgb[2] / (scale*scale));
					image.setRGB(x, y, rgba);
				}
			}
		}

		File ImageFile = new File(updateFileName(fileName));

		try {
			checkAndCreateDirectory(fileName);
			ImageIO.write(image, "png", ImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printRegionListToFile(List<Region> regions, String fileName) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			for(int i=0;i<regions.size();i++) {
				Region region = regions.get(i);
				writer.write(region.toString());
				if(i<regions.size()-1) writer.newLine();   // Write a new line character (platform-independent)
			}
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void finalPrintPolygons(List<Region> regions, String fileName, int width, int height) {
		try {
			checkAndCreateDirectory(fileName);
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			int maxDrawOrder = -1;
			for(Region region : regions) if(region.drawOrder > maxDrawOrder) maxDrawOrder = region.drawOrder;

			boolean start = true;

			for(int d=maxDrawOrder;d>=0;d--) {
				for(int j=0;j<regions.size();j++) {
					Region region = regions.get(j);
					if(region.drawOrder == d) {
						if(start) {
							start = false;
						} else writer.write("\n");
						writer.write(""+region.colorData);
						int z = 0;
						if(region.polygon.size() > 0) {
							List<Point> vertices = region.polygon;
							while (vertices.size() > 2) {
								for (int i=vertices.size()-1;i>=0;i--) {
									int prev = (i == 0) ? vertices.size() - 1 : i - 1;
									int next = (i == vertices.size() - 1) ? 0 : i + 1;

									Point p1 = vertices.get(prev);
									Point p2 = vertices.get(i);
									Point p3 = vertices.get(next);
									List<Point> l = new ArrayList<Point>();
									l.add(p1); l.add(p2); l.add(p3);

									if(region.getBit(region.triangleDrawOrder, z)) {
										//										System.out.println(Geometry.calculatePolygonArea(l) );
										//										if(Geometry.calculatePolygonArea(l) > 0) {
										int p1x = p1.x * 4096 / width;
										int p1y = p1.y * 4096 / height;
										int p2x = p2.x * 4096 / width;
										int p2y = p2.y * 4096 / height;
										int p3x = p3.x * 4096 / width;
										int p3y = p3.y * 4096 / height;

										writer.write("\n"+p1x+","+p1y+","+p2x+","+p2y+","+p3x+","+p3y);
										//										}
										vertices.remove(i);
									}

									z++;
								}
							}
						}
					}
				}
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
