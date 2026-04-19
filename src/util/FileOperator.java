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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

class IntBoolAsIntPair {
	int i;
	int b;

	IntBoolAsIntPair(int ii, int bb) {
		i = ii;
		b = bb;
	}
}

public class FileOperator {

	static final int WIDTH = 16384;
	static final int HEIGHT = 16384;
	static final double SCALE = 1000000;

	public static String updateFileName(String x) {
		// depending on Windows or macOS, flip the slashes

		// Windows:
		return x;

		// macOS:
		//		return x.replace('\\', '/');
	}

	public static void clearFolder(String folderName) {
		Path path = Paths.get(folderName);

		// Walk the file tree and delete each file/folder
		try {
			Files.walk(path)
			.sorted(Comparator.reverseOrder()) // Delete children before parents
			.filter(p -> !p.equals(path))      // Optional: keep the root folder
			.forEach(p -> {
				try {
					Files.delete(p);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public static void addEdgeAdjacent(List<List<Integer>> adjacents, List<List<Integer>> edgeAdjacentColors, int edge, int triangle, int oppColor) {
		if(adjacents.size() <= edge) {
			for(int i=0;i<=edge - adjacents.size();i++) adjacents.add(new ArrayList<Integer>());
		}
		adjacents.get(edge).add(triangle);


		if(edgeAdjacentColors.size() <= edge) {
			for(int i=0;i<=edge - edgeAdjacentColors.size();i++) edgeAdjacentColors.add(new ArrayList<Integer>());
		}
		edgeAdjacentColors.get(edge).add(oppColor);
	}

	public static void addPointAdjacent(List<List<Integer>> adjacents, int point, int triangle) {
		if(adjacents.size() <= point) {
			for(int i=0;i<=point - adjacents.size();i++) adjacents.add(new ArrayList<Integer>());
		}
		adjacents.get(point).add(triangle);
	}

	public static IntBoolAsIntPair checkAndAddEdge(List<String> list, Map<String,Integer> reverseMap, int p1, int p2) {
		String s1 = p1+","+p2;
		if(reverseMap.containsKey(s1)) {
			return new IntBoolAsIntPair(reverseMap.get(s1), 1);
		}
		String s2 = p2+","+p1;
		if(reverseMap.containsKey(s2)) {
			return new IntBoolAsIntPair(reverseMap.get(s2), 0);
		}
		int idx = list.size();
		list.add(s1);
		reverseMap.put(s1, idx);
		return new IntBoolAsIntPair(idx, 1);
	}

	public static String pointToString(Point p, int width, int height) {
		int xString = (int) (Math.round(SCALE*p.xFloat())/SCALE * WIDTH / width);
		int yString = (int) (Math.round(SCALE*p.yFloat())/SCALE * HEIGHT / height);
		return xString+","+yString;
	}

	public static int checkAndAddPoint(List<String> list, Map<String,Integer> reverseMap, Point p, int width, int height) {
		String s = pointToString(p, width, height);
		if(reverseMap.containsKey(s)) {
			return reverseMap.get(s);
		}
		int idx = list.size();
		list.add(s);
		reverseMap.put(s, idx);
		return idx;
	}

	public static void finalPrintPolygons(List<Region> regions, String triangleFileName, String edgeFileName, String pointFileName, int width, int height) {
		try {
			checkAndCreateDirectory(triangleFileName);
			BufferedWriter writer = new BufferedWriter(new FileWriter(triangleFileName));
			int maxDrawOrder = -1;
			for(Region region : regions) if(region.drawOrder > maxDrawOrder) maxDrawOrder = region.drawOrder;

			List<String> edges = new ArrayList<String>();
			List<String> points = new ArrayList<String>();
			List<List<Integer>> edgeAdjacents = new ArrayList<List<Integer>>();
			List<List<Integer>> edgeAdjacentColors = new ArrayList<List<Integer>>();
			Map<String,Integer> edgeReverseMap = new HashMap<String,Integer>();
			List<List<Integer>> pointAdjacents = new ArrayList<List<Integer>>();
			Map<String,Integer> pointReverseMap = new HashMap<String,Integer>();

			int triangle = 0;

			// triangles

			for(int d=maxDrawOrder;d>=0;d--) {
				for(int j=0;j<regions.size();j++) {
					Region region = regions.get(j);
					if(region.drawOrder == d) {
						int z1 = 0, z2 = 0;
						if(region.polygon.size() > 0) {
							List<Point> vertices = region.polygon;
							List<Integer> oppRegions = region.opposingRegions;
							List<Integer> oppRivers = region.opposingRivers;
							while (vertices.size() > 2) {
								for (int i=vertices.size()-1;i>=0 && vertices.size() > 2;i--) {
									int prev = (i == 0) ? vertices.size() - 1 : i - 1;
									int next = (i == vertices.size() - 1) ? 0 : i + 1;

									PointFloat p1 = vertices.get(prev).asFloatPoint();
									PointFloat p2 = vertices.get(i).asFloatPoint();
									PointFloat p3 = vertices.get(next).asFloatPoint();

									List<PointFloat> l = new ArrayList<PointFloat>();
									l.add(p1); l.add(p2); l.add(p3);

									if(region.getBit(region.triangleDrawOrder, z1)) {
										int p1i = checkAndAddPoint(points, pointReverseMap, p1, width, height);
										int p2i = checkAndAddPoint(points, pointReverseMap, p2, width, height);
										int p3i = checkAndAddPoint(points, pointReverseMap, p3, width, height);

										addPointAdjacent(pointAdjacents, p1i, triangle);
										addPointAdjacent(pointAdjacents, p2i, triangle);
										addPointAdjacent(pointAdjacents, p3i, triangle);

										IntBoolAsIntPair edge1 = checkAndAddEdge(edges, edgeReverseMap, p1i, p2i);
										IntBoolAsIntPair edge2 = checkAndAddEdge(edges, edgeReverseMap, p2i, p3i);
										IntBoolAsIntPair edge3 = checkAndAddEdge(edges, edgeReverseMap, p3i, p1i);

										if(triangle > 0) writer.write("\n");

										int oppColor1 = -1, oppColor2 = -1;
										//										, oppColor3 = -1;

										if(oppRegions.get(prev) >= 0) {
											if(oppRivers.size() > 0 && oppRivers.get(prev) >= 0) oppColor1 = oppRivers.get(prev);
											else oppColor1 = -regions.get(oppRegions.get(prev)).colorData - 2;
										}
										if(oppRegions.get(i) >= 0) {
											if(oppRivers.size() > 0 && oppRivers.get(i) >= 0) oppColor2 = oppRivers.get(i);
											else oppColor2 = -regions.get(oppRegions.get(i)).colorData - 2;
										}
										//										if(oppRegions.get(next) >= 0) {
										//											if(oppRivers.size() > 0 && oppRivers.get(next) >= 0) oppColor3 = oppRivers.get(next);
										//											else oppColor3 = -regions.get(oppRegions.get(next)).colorData - 2;
										//										}
										//
										//										if(oppColor1 == 213 || oppColor2 == 213 || oppColor3 == 213) {
										//											System.out.println(j+": "+p1+" "+p2+" "+p3+" "+oppColor1+" "+oppColor2+" "+oppColor3);
										//										}

										addEdgeAdjacent(edgeAdjacents, edgeAdjacentColors, edge1.i, triangle, oppColor1);
										addEdgeAdjacent(edgeAdjacents, edgeAdjacentColors, edge2.i, triangle, oppColor2);
										addEdgeAdjacent(edgeAdjacents, edgeAdjacentColors, edge3.i, triangle, -1);

										writer.write(edge1.i+","+edge2.i+","+edge3.i+","+edge1.b+","+edge2.b+","+edge3.b);
										if(z2 == 0) writer.write(","+region.colorData);

										z2++;
										vertices.remove(i);
										oppRegions.set(prev, j);
										oppRegions.remove(i);
										if(oppRivers.size() > 0) {
											oppRivers.set(prev, -1);
											oppRivers.remove(i);
										}
										triangle++;
									}

									z1++;
								}
							}
						}
					}
				}
			}
			writer.close();

			// edges

			writer = new BufferedWriter(new FileWriter(edgeFileName));
			for(int i=0;i<edges.size();i++) {
				if(i > 0) writer.write("\n");
				writer.write(edges.get(i));

				writer.write(","+edgeAdjacents.get(i).get(0));
				if(edgeAdjacents.get(i).size() > 1) {
					writer.write(","+edgeAdjacents.get(i).get(1));
					for(int j=0;j<edgeAdjacentColors.get(i).size();j++) {
						if(edgeAdjacentColors.get(i).get(j) >= 0) {
							if(edgeAdjacentColors.get(i).get(j) == 213) System.out.println("---"+edgeAdjacentColors.get(i));
							//							writer.write(",&"+edgeAdjacentColors.get(i).get(j));
							//							break;
						}
					}
					if(edgeAdjacentColors.get(i).get(0) >= 0) {
						writer.write(","+edgeAdjacentColors.get(i).get(0));
					}
				} else {
					if(edgeAdjacentColors.get(i).get(0) >= 0) {
						writer.write(",-1,"+edgeAdjacentColors.get(i).get(0));
					} else writer.write(","+edgeAdjacentColors.get(i).get(0));
				}

				if(edgeAdjacents.get(i).size() > 2) System.out.println("Warning: edge "+i+" has more than 2 adjacents: "+edgeAdjacents.get(i));
				//				if(edgeAdjacentColors.get(i).size() > 1) System.out.println("Warning: edge "+i+" has more than 1 adjacent colors: "+edgeAdjacentColors.get(i));
			}
			writer.close();

			// points

			writer = new BufferedWriter(new FileWriter(pointFileName));
			for(int i=0;i<points.size();i++) {
				if(i > 0) writer.write("\n");
				writer.write(points.get(i));
				for(int j=0;j<pointAdjacents.get(i).size();j++) writer.write(","+pointAdjacents.get(i).get(j));
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
