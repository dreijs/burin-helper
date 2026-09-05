package util;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import vectormaps.PolygonCreator;
import vectormaps.ElevationMapCreator.MapName;
import vectormaps.ElevationMapCreator.Trace;

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
	static final double ROUNDING_SCALE = 1000000;

	// Must match FMapLowZoom.cpp's LevelDataMagic/LevelDataVersion exactly, or the binary layout
	// this writes has diverged from what Unreal reads.
	static final int LEVEL_DATA_MAGIC = 0x4255524E; // "BURN"
	// 2 adds the cross-layer section at the end of each tile: the triangles that share ground with
	// each triangle, and how much of it later-drawn ones hide. Regions overlap wherever one encloses
	// another, and nothing before this recorded it.
	static final int LEVEL_DATA_VERSION = 2;
	
	public static void writeParameterSpecFile(String folderName,
			MapName name,
			int scale,
			double minRegionSize,
			double distortFactor,
			double maxDouglasPeuckerDist,
			double maxDouglasPeuckerSize,
			double maxRiverDPDist,
			int targetTrianglesPerGridCell,
			int w,
			int h,
			int level,
			Trace trace) {

		try {
			checkAndCreateParentDirectory(folderName+"parameters.txt");

			BufferedWriter writer = new BufferedWriter(new FileWriter(folderName+"parameters.txt"));
			writer.write("map name: "+name);
			writer.write("\nscale: "+scale);
			writer.write("\nminRegionSize: "+minRegionSize);
			writer.write("\ndistortFactor: "+distortFactor);
			writer.write("\nmaxDouglasPeuckerDist: "+maxDouglasPeuckerDist);
			writer.write("\nmaxDouglasPeuckerSize: "+maxDouglasPeuckerSize);
			writer.write("\nmaxRiverDPDist: "+maxRiverDPDist);
			writer.write("\ntargetTrianglesPerGridCell: "+targetTrianglesPerGridCell);
			writer.write("\nw: "+w);
			writer.write("\nh: "+h);
			writer.write("\nlevel: "+level);
			writer.write("\ntrace: "+trace);

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
			checkAndCreateDirectory(folderName);
			
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
	
	public static int[][] readImage(String fileName, int scale, double minX, double minY, double maxX, double maxY) {
		int[][] result = null;
		BufferedImage src;

		try {
			src = ImageIO.read(new File(updateFileName(fileName)));

			int w = ((int) Math.round(src.getWidth() * (maxX - minX)))/scale;
			int h = ((int) Math.round(src.getHeight() * (maxY - minY)))/scale;

			result = new int[w][h];

			for(int j=0;j<h;j++) {
				for(int i=0;i<w;i++) {
					if(scale == 1) {
						result[i][j] = src.getRGB(((int) Math.round(minX * src.getWidth())) + scale*i, ((int) Math.round(minY * src.getHeight())) + scale*j);
					} else {
						Map<Integer, Integer> map = new HashMap<Integer, Integer>();
						for(int xx=0;xx<scale;xx++) {
							for(int yy=0;yy<scale;yy++) {
								int v = src.getRGB(((int) Math.round(minX * src.getWidth())) + scale*i+xx, ((int) Math.round(minY * src.getHeight())) + scale*j+yy);
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

	public static void checkAndCreateDirectory(String folderName) throws IOException {
		Path path = Paths.get(folderName);
		Files.createDirectories(path);
	}
	
	public static void checkAndCreateParentDirectory(String fileName) throws IOException {
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
			checkAndCreateParentDirectory(fileName);
			ImageIO.write(image, "png", ImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void printRegionListToFile(List<Region> regions, String fileName) {
		printRegionListToFile(regions, fileName, false);
	}

	public static void printRegionListToFile(List<Region> regions, String fileName, boolean asFloat) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			for(int i=0;i<regions.size();i++) {
				Region region = regions.get(i);
				if(asFloat) writer.write(region.toFloatString());
				else writer.write(region.toString());
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

	public static String pointToString(Point p, double minX, double minY, double maxX, double maxY, int width, int height) {
		double xString = Math.round(ROUNDING_SCALE*((maxX - minX) * p.xFloat()/width - 0.5 + minX)*360)/ROUNDING_SCALE;
		double yString = Math.round(ROUNDING_SCALE*((maxY - minY) * p.yFloat()/height - 0.5 + minY)*180)/ROUNDING_SCALE;
		return xString+","+yString;
	}

	public static int checkAndAddPoint(List<String> list, Map<String,Integer> reverseMap, Point p, double minX, double minY, double maxX, double maxY, int width, int height) {
		String s = pointToString(p, minX, minY, maxX, maxY, width, height);
		if(reverseMap.containsKey(s)) {
			return reverseMap.get(s);
		}
		int idx = list.size();
		list.add(s);
		reverseMap.put(s, idx);
		return idx;
	}

	public static void finalPrintPolygons(List<Region> regions, String folder, double minX, double minY, double maxX, double maxY, int width, int height, int targetTrianglesPerGridCell) {
		System.out.println(minX+", "+minY+", "+maxX+", "+maxY+", "+width+", "+height);
		try {
			checkAndCreateDirectory(folder);
			BufferedWriter writer = new BufferedWriter(new FileWriter(folder+"Triangles.txt"));
			int maxDrawOrder = -1;
			for(Region region : regions) if(region.drawOrder > maxDrawOrder) maxDrawOrder = region.drawOrder;

			List<String> edges = new ArrayList<String>();
			List<String> points = new ArrayList<String>();
			List<List<Integer>> edgeAdjacents = new ArrayList<List<Integer>>();
			List<List<Integer>> edgeAdjacentColors = new ArrayList<List<Integer>>();
			Map<String,Integer> edgeReverseMap = new HashMap<String,Integer>();
			List<List<Integer>> pointAdjacents = new ArrayList<List<Integer>>();
			Map<String,Integer> pointReverseMap = new HashMap<String,Integer>();

			// Bounding box (in the same local pixel space as width/height) per emitted triangle,
			// index-aligned with the triangle IDs written to Triangles.txt, so a spatial grid can
			// be built once here rather than re-derived from a lookup at runtime every load.
			List<double[]> triangleBoundingBoxes = new ArrayList<double[]>();

			// The same triangles again as bare coordinates, and the region each came from. Regions
			// overlap where one encloses another -- the enclosed one is drawn on top rather than cut
			// out -- and nothing in Triangles.txt or Edges.txt says so. LayerOverlaps works it out
			// from these, so the flood can cross between layers and areas can stop double-counting.
			List<double[]> triangleGeometry = new ArrayList<double[]>();
			List<Integer> triangleRegion = new ArrayList<Integer>();

			int triangle = 0;

			// triangles
//			List<List<Point>> riverTriangles = new ArrayList<List<Point>>();

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

									List<Point> l = new ArrayList<Point>();
									l.add(p1); l.add(p2); l.add(p3);

									if(region.getBit(region.triangleDrawOrder, z1)) {
										triangleBoundingBoxes.add(new double[] {
												Math.min(p1.xFloat(), Math.min(p2.xFloat(), p3.xFloat())),
												Math.min(p1.yFloat(), Math.min(p2.yFloat(), p3.yFloat())),
												Math.max(p1.xFloat(), Math.max(p2.xFloat(), p3.xFloat())),
												Math.max(p1.yFloat(), Math.max(p2.yFloat(), p3.yFloat()))
										});
										triangleGeometry.add(new double[] {
												p1.xFloat(), p1.yFloat(), p2.xFloat(), p2.yFloat(), p3.xFloat(), p3.yFloat()
										});
										triangleRegion.add(Integer.valueOf(j));

										int p1i = checkAndAddPoint(points, pointReverseMap, p1, minX, minY, maxX, maxY, width, height);
										int p2i = checkAndAddPoint(points, pointReverseMap, p2, minX, minY, maxX, maxY, width, height);
										int p3i = checkAndAddPoint(points, pointReverseMap, p3, minX, minY, maxX, maxY, width, height);

										addPointAdjacent(pointAdjacents, p1i, triangle);
										addPointAdjacent(pointAdjacents, p2i, triangle);
										addPointAdjacent(pointAdjacents, p3i, triangle);

										IntBoolAsIntPair edge1 = checkAndAddEdge(edges, edgeReverseMap, p1i, p2i);
										IntBoolAsIntPair edge2 = checkAndAddEdge(edges, edgeReverseMap, p2i, p3i);
										IntBoolAsIntPair edge3 = checkAndAddEdge(edges, edgeReverseMap, p3i, p1i);

										if(triangle > 0) writer.write("\n");

										int oppColor1 = -1, oppColor2 = -1, oppColor3 = -1;

										if(oppRivers.size() > 0 && oppRivers.get(prev) >= 0) oppColor1 = oppRivers.get(prev);
										else if(oppRegions.get(prev) >= 0) {
											oppColor1 = -regions.get(oppRegions.get(prev)).colorData - 2;
										}

										if(oppRivers.size() > 0 && oppRivers.get(i) >= 0) oppColor2 = oppRivers.get(i);
										else if(oppRegions.get(i) >= 0) {
											oppColor2 = -regions.get(oppRegions.get(i)).colorData - 2;
										}

										if(oppRivers.size() > 0 && oppRivers.get(next) >= 0) oppColor3 = oppRivers.get(next);
										else if(oppRegions.get(next) >= 0) {
											oppColor3 = -regions.get(oppRegions.get(next)).colorData - 2;
										}
										//
										//										if(oppColor1 == 213 || oppColor2 == 213) {
										//											System.out.println(j+": "+p1+" "+p2+" "+p3+" "+oppColor1+" "+oppColor2);
										//										}

										addEdgeAdjacent(edgeAdjacents, edgeAdjacentColors, edge1.i, triangle, oppColor1);
										addEdgeAdjacent(edgeAdjacents, edgeAdjacentColors, edge2.i, triangle, oppColor2);
										if(vertices.size() > 3) addEdgeAdjacent(edgeAdjacents, edgeAdjacentColors, edge3.i, triangle, -1);
										else addEdgeAdjacent(edgeAdjacents, edgeAdjacentColors, edge3.i, triangle, oppColor3);

										//										if(triangle == 0) {
										//										int b = 5323;
										//										if(edge1.i == b || edge2.i == b || edge3.i == b) {
										//											System.out.println("region: "+region.regionIdx+", "+triangle);
										//											System.out.println(p1+" "+p2+" "+p3);
										//											System.out.println(p1i+" "+p2i+" "+p3i);
										//											System.out.println(edge1.i+" "+edge2.i+" "+edge3.i);
										//										}

										//										if(triangle == 134491 || triangle == 364463) {
//										if(j == 40149) {
//											System.out.println(triangle+": "+l+", region "+j);
//											System.out.println(edge1.i+" "+edge2.i+" "+edge3.i);
//											System.out.println(vertices);
//											System.out.println(oppRegions);
//											System.out.println(oppRivers);
//											System.out.println(oppColor1+", "+oppColor2);
//											riverTriangles.add(l);
//											PolygonCreator.visualizePolygon(l, System.getProperty("user.dir")+"\\output\\map\\polygons\\river_166_triangles_"+triangle+".png", 64);
//										}

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

			writeSpatialGrid(triangleBoundingBoxes, folder, width, height, targetTrianglesPerGridCell);
			writeLayerOverlaps(triangleGeometry, triangleRegion, triangleBoundingBoxes, folder,
					minX, minY, maxX, maxY, width, height);

			System.out.println("num triangles: "+triangle);
			System.out.println("num edges: "+edges.size());
			System.out.println("num points: "+points.size());

//			System.out.println(edgeAdjacents.get(216932));
//			System.out.println(edgeAdjacentColors.get(216932));

			// edges

//			int rrr = 166;
			List<List<Point>> riverPoints = new ArrayList<List<Point>>();
			writer = new BufferedWriter(new FileWriter(folder+"Edges.txt"));
			for(int i=0;i<edges.size();i++) {
				if(i > 0) writer.write("\n");
				writer.write(edges.get(i));

				writer.write(","+edgeAdjacents.get(i).get(0));
				if(edgeAdjacents.get(i).size() > 1) {
					List<Integer> indices = new ArrayList<Integer>();
					writer.write(","+edgeAdjacents.get(i).get(1));
					for(int j=0;j<edgeAdjacentColors.get(i).size();j++) {
						int z = edgeAdjacentColors.get(i).get(j);
						if(edgeAdjacentColors.get(i).get(j) >= 0 && !indices.contains(z)) indices.add(z);
					}
					if(indices.size() > 0) {
//						if(indices.get(0) == rrr) {
//							String[] s = edges.get(i).split(",");
//							String[] p1s = points.get(Integer.parseInt(s[0])).split(",");
//							String[] p2s = points.get(Integer.parseInt(s[1])).split(",");
//							Point p1 = new PointFloat(Double.parseDouble(p1s[0]), Double.parseDouble(p1s[1]));
//							Point p2 = new PointFloat(Double.parseDouble(p2s[0]), Double.parseDouble(p2s[1]));
//							List<Point> l =new ArrayList<Point>();
//							l.add(p1);
//							l.add(p2);
//							riverPoints.add(l);
//							System.out.println("edge "+i+" --- "+edgeAdjacents.get(i)+" --- "+edgeAdjacentColors.get(i)+", "+indices+", "+l);
//						}
						writer.write(","+indices.get(0));
					}
					if(indices.size() > 1) System.out.println("Warning: edge "+i+" has more than 1 adjacent rivers: "+edgeAdjacentColors.get(i));
				} else {
					if(edgeAdjacentColors.get(i).size() > 1) System.out.println("Warning: edge "+i+" has more than 1 adjacent colors: "+edgeAdjacentColors.get(i));
					if(edgeAdjacentColors.get(i).get(0) >= 0) {
						writer.write(",-1,"+edgeAdjacentColors.get(i).get(0));
//						if(edgeAdjacentColors.get(i).get(0) == rrr) {
//							String[] s = edges.get(i).split(",");
//							String[] p1s = points.get(Integer.parseInt(s[0])).split(",");
//							String[] p2s = points.get(Integer.parseInt(s[1])).split(",");
//							Point p1 = new PointFloat(Double.parseDouble(p1s[0]), Double.parseDouble(p1s[1]));
//							Point p2 = new PointFloat(Double.parseDouble(p2s[0]), Double.parseDouble(p2s[1]));
//							List<Point> l =new ArrayList<Point>();
//							l.add(p1);
//							l.add(p2);
//							riverPoints.add(l);
//							System.out.println("edge "+i+" -*- "+edgeAdjacents.get(i)+" -*- "+edgeAdjacentColors.get(i)+", , "+l);
//						}
					} else writer.write(","+edgeAdjacentColors.get(i).get(0));
				}

				//				if(edgeAdjacents.get(i).size() > 2) System.out.println("Warning: edge "+i+" has more than 2 adjacents: "+edgeAdjacents.get(i));
				//				if(edgeAdjacentColors.get(i).size() > 1) System.out.println("Warning: edge "+i+" has more than 1 adjacent colors: "+edgeAdjacentColors.get(i));
			}
			writer.close();

//			PolygonCreator.visualizePolygons(riverTriangles, System.getProperty("user.dir")+"\\output\\map\\polygons\\river_"+rrr+"_some_triangles.png", 64);
//			PolygonCreator.visualizePolygons(riverPoints, System.getProperty("user.dir")+"\\output\\map\\polygons\\river_"+rrr+"_from_edges.png", 64);
			// points

			writer = new BufferedWriter(new FileWriter(folder+"Points.txt"));
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

	/**
	 * Writes Layers.txt: one line per triangle, "coveredArea" followed by the indices of the
	 * triangles on other layers sharing its ground.
	 *
	 * coveredArea is how much of the triangle sits beneath later-drawn triangles, so a caller adding
	 * up terrain areas can subtract it and count each patch of ground once. It is the area of their
	 * union rather than the sum, because two triangles stacked on the same spot would otherwise
	 * appear to hide more of it than it has.
	 *
	 * It is written in SQUARE KILOMETRES rather than the tile pixels LayerOverlaps works in. The
	 * engine measures everything in kilometres, and a number that must be converted before use is
	 * a trap -- it caught the first check written against this file, which reported 296% of a
	 * tile's land as hidden. A pixel covers less ground the further from the equator it sits, so
	 * the conversion is done per triangle at its own centroid rather than once for the tile.
	 */
	private static void writeLayerOverlaps(List<double[]> geometry, List<Integer> region,
			List<double[]> boundingBoxes, String folder,
			double minX, double minY, double maxX, double maxY, int width, int height) {
		try {
			long t0 = System.currentTimeMillis();
			LayerOverlaps.Result overlaps = LayerOverlaps.compute(geometry, region, boundingBoxes);

			double degreesPerPixelX = (maxX - minX) * 360.0 / width;
			double degreesPerPixelY = (maxY - minY) * 180.0 / height;

			BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "Layers.txt"));
			int withNeighbors = 0;
			double totalCovered = 0;
			for (int i = 0; i < geometry.size(); i++) {
				if (i > 0) writer.write("\n");
				double[] g = geometry.get(i);
				double centroidY = (g[1] + g[3] + g[5]) / 3.0;
				// The mesh's y runs opposite to latitude, so negate it to get one.
				double latitude = -((maxY - minY) * centroidY / height - 0.5 + minY) * 180.0;
				double kmPerSquarePixel = (degreesPerPixelX * 111.32 * Math.cos(Math.toRadians(latitude)))
						* (degreesPerPixelY * 110.57);
				double coveredKm2 = overlaps.coveredArea[i] * kmPerSquarePixel;

				writer.write(Double.toString(coveredKm2));
				List<Integer> neighbors = overlaps.neighbors.get(i);
				if (!neighbors.isEmpty()) withNeighbors++;
				totalCovered += coveredKm2;
				for (Integer neighbor : neighbors) writer.write("," + neighbor);
			}
			writer.close();

			System.out.println("done: cross-layer overlaps, " + overlaps.pairs + " pairs over "
					+ withNeighbors + " triangles, " + Math.round(totalCovered)
					+ " km2 hidden, in " + (System.currentTimeMillis() - t0) / 1000 + " s");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Builds a uniform grid over the tile's local pixel space (0..width, 0..height, the same
	// space triangleBoundingBoxes are in) and writes, for each cell, the indices of the
	// triangles whose bounding box overlaps it. Grid resolution is derived from the actual
	// triangle count so that cells hold roughly targetTrianglesPerGridCell triangles on average,
	// and the two axes are sized proportionally to width/height so cells stay roughly square
	// even for tiles that aren't (e.g. a single zoom-level-0 tile covering the whole map, which
	// is twice as wide as it is tall). First line of Grid.txt is "gridWidth,gridHeight"; every
	// following line lists that cell's candidate triangle indices, in row-major order.
	private static void writeSpatialGrid(List<double[]> triangleBoundingBoxes, String folder, int width, int height, int targetTrianglesPerGridCell) {
		try {
			int numTriangles = triangleBoundingBoxes.size();
			int density = Math.max(1, targetTrianglesPerGridCell);

			double targetCells = Math.max(1.0, (double) numTriangles / density);
			int gridWidth = Math.max(1, (int) Math.round(Math.sqrt(targetCells * width / (double) height)));
			int gridHeight = Math.max(1, (int) Math.round(Math.sqrt(targetCells * height / (double) width)));

			List<List<Integer>> cells = new ArrayList<List<Integer>>();
			for (int i = 0; i < gridWidth * gridHeight; i++) cells.add(new ArrayList<Integer>());

			for (int t = 0; t < numTriangles; t++) {
				double[] bbox = triangleBoundingBoxes.get(t);
				int cellMinX = clampCell((int) Math.floor(bbox[0] / width * gridWidth), gridWidth);
				int cellMinY = clampCell((int) Math.floor(bbox[1] / height * gridHeight), gridHeight);
				int cellMaxX = clampCell((int) Math.floor(bbox[2] / width * gridWidth), gridWidth);
				int cellMaxY = clampCell((int) Math.floor(bbox[3] / height * gridHeight), gridHeight);

				for (int cy = cellMinY; cy <= cellMaxY; cy++) {
					for (int cx = cellMinX; cx <= cellMaxX; cx++) {
						cells.get(cy * gridWidth + cx).add(t);
					}
				}
			}

			BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "Grid.txt"));
			writer.write(gridWidth + "," + gridHeight);
			for (int c = 0; c < cells.size(); c++) {
				writer.write("\n");
				List<Integer> candidates = cells.get(c);
				for (int i = 0; i < candidates.size(); i++) {
					if (i > 0) writer.write(",");
					writer.write(String.valueOf(candidates.get(i)));
				}
			}
			writer.close();

			System.out.println("grid: "+gridWidth+"x"+gridHeight+" cells for "+numTriangles+" triangles");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int clampCell(int cell, int gridSize) {
		return Math.max(0, Math.min(gridSize - 1, cell));
	}

	private static void writeInt32LE(OutputStream out, int v) throws IOException {
		out.write(v & 0xFF);
		out.write((v >>> 8) & 0xFF);
		out.write((v >>> 16) & 0xFF);
		out.write((v >>> 24) & 0xFF);
	}

	private static void writeDoubleLE(OutputStream out, double d) throws IOException {
		long bits = Double.doubleToLongBits(d);
		for (int i = 0; i < 8; i++) {
			out.write((int) ((bits >>> (8 * i)) & 0xFF));
		}
	}

	private static List<String> readLines(String path) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line;
		while ((line = reader.readLine()) != null) lines.add(line);
		reader.close();
		return lines;
	}

	// Re-reads one tile's already-written Triangles.txt/Edges.txt/Points.txt/Grid.txt (the exact
	// files finalPrintPolygons/writeSpatialGrid just produced) and writes their content in binary,
	// resolving the text format's "terrain value carries forward until the next explicit one"
	// scheme into an explicit value per triangle so the reader doesn't need to replicate it.
	private static void writeTileBinary(OutputStream out, String tileFolder) throws IOException {
		List<String> triangleLines = readLines(tileFolder + "Triangles.txt");
		writeInt32LE(out, triangleLines.size());
		int terrainData = 0;
		for (String line : triangleLines) {
			String[] f = line.split(",");
			writeInt32LE(out, Integer.parseInt(f[0]));
			writeInt32LE(out, Integer.parseInt(f[1]));
			writeInt32LE(out, Integer.parseInt(f[2]));
			out.write(f[3].equals("1") ? 1 : 0);
			out.write(f[4].equals("1") ? 1 : 0);
			out.write(f[5].equals("1") ? 1 : 0);
			if (f.length > 6) terrainData = Integer.parseInt(f[6]);
			writeInt32LE(out, terrainData);
		}

		List<String> edgeLines = readLines(tileFolder + "Edges.txt");
		writeInt32LE(out, edgeLines.size());
		for (String line : edgeLines) {
			String[] f = line.split(",");
			writeInt32LE(out, Integer.parseInt(f[0]));
			writeInt32LE(out, Integer.parseInt(f[1]));
			writeInt32LE(out, Integer.parseInt(f[2]));
			writeInt32LE(out, f.length > 3 ? Integer.parseInt(f[3]) : -1);
			writeInt32LE(out, f.length > 4 ? Integer.parseInt(f[4]) : -1);
		}

		List<String> pointLines = readLines(tileFolder + "Points.txt");
		writeInt32LE(out, pointLines.size());
		for (String line : pointLines) {
			String[] f = line.split(",");
			writeDoubleLE(out, Double.parseDouble(f[0]));
			writeDoubleLE(out, Double.parseDouble(f[1]));
			writeInt32LE(out, f.length - 2);
			for (int i = 2; i < f.length; i++) writeInt32LE(out, Integer.parseInt(f[i]));
		}

		writeLayerBinary(out, tileFolder);

		List<String> gridLines = readLines(tileFolder + "Grid.txt");
		String[] gridHeader = gridLines.get(0).split(",");
		int gridWidth = Integer.parseInt(gridHeader[0]);
		int gridHeight = Integer.parseInt(gridHeader[1]);
		writeInt32LE(out, gridWidth);
		writeInt32LE(out, gridHeight);
		for (int c = 0; c < gridWidth * gridHeight; c++) {
			String line = c + 1 < gridLines.size() ? gridLines.get(c + 1) : "";
			if (line.isEmpty()) {
				writeInt32LE(out, 0);
			} else {
				String[] f = line.split(",");
				writeInt32LE(out, f.length);
				for (String s : f) writeInt32LE(out, Integer.parseInt(s));
			}
		}
	}

	// Consolidates every tile's Triangles/Edges/Points/Grid text files for one zoom level into a
	// single LevelData.bin, so the game can load a whole level with one file read instead of
	// scanning and parsing text across every x_y subfolder. The per-tile text files are left in
	// place for debugging. Tile order matches FMapLowZoom.cpp's read order (x outer, y inner).
	/**
	 * Appends the cross-layer section: per triangle, the area hidden beneath later-drawn triangles
	 * followed by the indices of every triangle sharing its ground.
	 *
	 * A tile generated before Layers.txt existed writes a zero count, so the reader can tell an
	 * absent section from an empty one without the file lengths disagreeing.
	 */
	private static void writeLayerBinary(OutputStream out, String tileFolder) throws IOException {
		File file = new File(tileFolder + "Layers.txt");
		if (!file.exists()) {
			writeInt32LE(out, 0);
			return;
		}
		List<String> lines = readLines(tileFolder + "Layers.txt");
		writeInt32LE(out, lines.size());
		for (String line : lines) {
			String[] f = line.split(",");
			writeDoubleLE(out, Double.parseDouble(f[0]));
			writeInt32LE(out, f.length - 1);
			for (int i = 1; i < f.length; i++) writeInt32LE(out, Integer.parseInt(f[i]));
		}
	}

	public static void writeLevelBinary(String levelFolder, int w, int h) {
		try {
			OutputStream out = new BufferedOutputStream(new FileOutputStream(levelFolder + "LevelData.bin"));

			writeInt32LE(out, LEVEL_DATA_MAGIC);
			writeInt32LE(out, LEVEL_DATA_VERSION);
			writeInt32LE(out, w);
			writeInt32LE(out, h);

			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					writeTileBinary(out, levelFolder + x + "_" + y + "\\");
				}
			}

			out.close();
			System.out.println("wrote level binary: " + levelFolder + "LevelData.bin");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
