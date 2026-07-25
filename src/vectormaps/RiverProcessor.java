package vectormaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import util.FileOperator;
import util.GeometryUtils;
import util.Point;
import util.PointFloat;
import util.PointInt;
import vectormaps.ElevationMapCreator.MapName;

class RiverData {
	String name;
	List<Point> coords;

	int convertLatitude(double c) {
		return (int) (1000000 * c);
	}

	int convertLongitude(double c) {
		return (int) (1000000 * c);
	}

	RiverData(String name, List<Double> coords) {
		this.name = name;
		int n = coords.size()/2;
		this.coords = new ArrayList<Point>();
		for(int i=0;i<n;i++) {
			this.coords.add(new PointInt(convertLatitude(coords.get(i)), convertLongitude(coords.get(i + n))));
		}
	}

	void simplify(double epsilon) {
		coords = GeometryUtils.simplify(coords, epsilon);
	}

	public String toString() {
		String result = name;
		for(int i=0;i<coords.size();i++) {
			Point coord = coords.get(i);
			result += ",  "+coord.toString();
		}
		return result;
	}
}

public class RiverProcessor {
//	public static final String RIVER_DATA_FILENAME = System.getProperty("user.dir")+"\\input\\river_data.csv";
//	public static final String REFORMATTED_RIVER_DATA_FILENAME = System.getProperty("user.dir")+"\\output\\map\\reformatted_river_data.csv";
//	public static final String SIMPLIFIED_RIVER_DATA_FILENAME = System.getProperty("user.dir")+"\\output\\map\\simplified_river_data.csv";
//	public static final String BASE_MAP_WITH_RIVERS_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\rivers.png";
//	public static final String BASE_MAP_WITH_SIMPLIFIED_RIVERS_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\rivers_simplified.png";
//	public static final String BASE_MAP_WITH_SIMPLIFIED_SEGMENTED_RIVERS_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\rivers_simplified_segmented.png";
	
//	public static String REFORMATTED_RIVER_DATA_FILENAME = "reformatted_river_data.csv";
//	public static String SIMPLIFIED_RIVER_DATA_FILENAME = "simplified_river_data.csv";
//	public static String BASE_MAP_WITH_RIVERS_OUTPUT_FILENAME = "rivers.csv";
//	public static String BASE_MAP_WITH_SIMPLIFIED_RIVERS_OUTPUT_FILENAME = "rivers_simplified.csv";
//	public static String BASE_MAP_WITH_SIMPLIFIED_SEGMENTED_RIVERS_OUTPUT_FILENAME = "rivers_simplified_segmented.csv";

	public static String getRiverInputFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\input\\map\\earth\\river_data.csv";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\input\\map\\earth\\river_data.csv";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\input\\map\\tes_nirn\\river_data.csv";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\input\\map\\ff6_overworld\\river_data.csv";
		return "";
	}
	
	public static String getReformattedRiverFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\reformatted_river_data.csv";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\reformatted_river_data.csv";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\reformatted_river_data.csv";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\reformatted_river_data.csv";
		return "";
	}
	
	public static String getSimplifiedRiverFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\simplified_river_data.csv";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\simplified_river_data.csv";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\simplified_river_data.csv";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\simplified_river_data.csv";
		return "";
	}
	
	public String filterRiverName(String s) {
		String s1 = s.replace("\"", "");
		String[] s2 = s1.split(" \\(");
		if(s2.length > 0) s1 = s2[0];
		return s1;
	}

	public void reformatRiverData(MapName name) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(getRiverInputFilename(name)));
			BufferedWriter writer = new BufferedWriter(new FileWriter(getReformattedRiverFilename(name)));

			String line = reader.readLine(); // skip first line
			String aggLine = "";
			String cName = "";
			List<Double> coords = new ArrayList<Double>();
			int proc = 0;

			double minLat = Double.POSITIVE_INFINITY;
			double minLon = Double.POSITIVE_INFINITY;
			double maxLat = Double.NEGATIVE_INFINITY;
			double maxLon = Double.NEGATIVE_INFINITY;

			while ((line = reader.readLine()) != null) {
				String[] s1 = line.replace(", Central America", "").split("c\\(");
				String s1b = line;
				if(s1.length > 1) {
					String[] s2 = s1[0].split(",");
					cName = filterRiverName(s2[6]);
					s1b = s1[1];
				}

				if(s1b.length() > 0) {
					String[] s2 = s1b.split("\\)");
					if(s2.length != 1 || s1b.charAt(s1b.length()-1) == ')') {
						if(s2.length > 0) aggLine += s2[0];

						System.out.println(aggLine);

						String[] s3 = aggLine.split(", ");
						if(s3.length > 0 && aggLine.length() > 0) {
							for(int i=0;i<s3.length;i++) {
								//									System.out.println(s3[i]);
								double d = Double.parseDouble(s3[i]);
								coords.add(d);
								if(i < s3.length / 2) {
									minLat = Math.min(minLat, d);
									maxLat = Math.max(maxLat, d);
								} else {
									minLon = Math.min(minLon, d);
									maxLon = Math.max(maxLon, d);
								}
							}
							RiverData data = new RiverData(cName, coords);
							if(proc>0) writer.write("\n");
							writer.write(data.toString());
							writer.flush();
							proc++;
							System.out.println("*"+proc);
							aggLine = "";
							coords = new ArrayList<Double>();
						}
					} else {
						aggLine += s2[0];
					}
				}
			}
			reader.close();

			System.out.println(minLat+" "+minLon+" "+maxLat+" "+maxLon);

			writer.close();
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
	}

	public static List<List<Point>> segmentate(List<List<Point>> l) {
		boolean removed = true;
		while(removed) {
			removed = removeOneSegment(l);
		}

		boolean split = true;
		while(split) {
			split = splitOneSegment(l);
		}
		return l;
	}

	public static boolean removeOneSegment(List<List<Point>> l) {
		//		System.out.println(l);
		int n = l.size();
		for(int i=0;i<n;i++) {
			for(int j=0;j<i;j++) {
				//				System.out.println(i+" "+j);
				if(i != j) {
					List<Point> ilist = l.get(i);
					List<Point> jlist = l.get(j);
					int isize = ilist.size();
					int jsize = jlist.size();
					List<Point> p = new ArrayList<Point>();
					if(ilist.get(0).equals(jlist.get(0))) {
						for(int k=isize-1;k>=0;k--) p.add(ilist.get(k));
						for(int k=1;k<jsize;k++) p.add(jlist.get(k));
						l.remove(i);
						l.remove(j);
						l.add(p);
						return true;
					}
					if(ilist.get(0).equals(jlist.get(jsize-1))) {
						for(int k=isize-1;k>=0;k--) p.add(ilist.get(k));
						for(int k=jsize-2;k>=0;k--) p.add(jlist.get(k));
						l.remove(i);
						l.remove(j);
						l.add(p);
						return true;
					}
					if(ilist.get(isize-1).equals(jlist.get(0))) {
						for(int k=0;k<isize;k++) p.add(ilist.get(k));
						for(int k=1;k<jsize;k++) p.add(jlist.get(k));
						l.remove(i);
						l.remove(j);
						l.add(p);
						return true;
					}
					if(ilist.get(isize-1).equals(jlist.get(jsize-1))) {
						for(int k=0;k<isize;k++) p.add(ilist.get(k));
						for(int k=jsize-2;k>=0;k--) p.add(jlist.get(k));
						l.remove(i);
						l.remove(j);
						l.add(p);
						return true;
					}
				}
			}
		}

		return false;
	}

	public static boolean splitOneSegment(List<List<Point>> l) {
		int n = l.size();
		for(int i=0;i<n;i++) {
			for(int j=0;j<i;j++) {
				if(i != j) {
					List<Point> ilist = l.get(i);
					List<Point> jlist = l.get(j);
					int isize = ilist.size();
					int jsize = jlist.size();
					for(int ii=1;ii<isize-1;ii++) {
						if(ilist.get(ii).equals(jlist.get(0)) || ilist.get(ii).equals(jlist.get(jsize-1))) {
							List<Point> p1 = new ArrayList<Point>();
							List<Point> p2 = new ArrayList<Point>();
							for(int k=0;k<=ii;k++) p1.add(ilist.get(k));
							for(int k=ii;k<isize;k++) p2.add(ilist.get(k));
							l.remove(i);
							l.add(p1);
							l.add(p2);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static Map<String,List<List<Point>>> simplifyRiverData(double d, MapName name) {
		// first merge the segments
		Map<String,List<List<Point>>> pointMap = readRiverDataFromFile(getReformattedRiverFilename(name));
		for(String s : pointMap.keySet()) {
			List<List<Point>> pointLists = pointMap.get(s);
			pointMap.put(s, segmentate(pointLists));
		}

		// simplify the resulting rivers
		for(String s : pointMap.keySet()) {
			List<List<Point>> points = pointMap.get(s);
			List<List<Point>> simplifiedPoints = GeometryUtils.simplifyJts(points, d);
			boolean valid = true;
			// check that simplication did not cause any intersections:
			for(String ss : pointMap.keySet()) {
				if(!s.equals(ss)) {
					List<List<Point>> pointLists = pointMap.get(ss);

					for(List<Point> line1 : pointLists) {
						for(List<Point> line2 : simplifiedPoints) {
							if(GeometryUtils.doBoundingBoxesIntersect(line1, line2)) {
								if(!GeometryUtils.areNonIntersectingLines(line1, line2)) valid = false;
							}
						}
					}
				}
			}

			if(valid) {
				pointMap.put(s, GeometryUtils.simplifyJts(points, d));
			} else System.out.println("***");
//				System.out.println("+");
//			} else System.out.println("-");
			//			System.out.println(pointMap.get(s));
		}

		return pointMap;
	}

	public void simplifyAndWriteRiverData(double d, MapName name) {
		Map<String,List<List<Point>>> pointMap = simplifyRiverData(d, name);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(getSimplifiedRiverFilename(name)));

			int proc = 0;
			for(String s : pointMap.keySet()) {
				List<List<Point>> points = pointMap.get(s);
				for(int i=0;i<points.size();i++) {
					if(proc>0) writer.write("\n");
					writer.write(s);
					for(int j=0;j<points.get(i).size();j++) {
						writer.write(",  "+points.get(i).get(j).asIntPoint());
					}
					proc++;
				}
			}

			writer.close();
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
	}

	public void drawRivers(String riverDataFileName, String outputFileName, boolean colorSegments) {
		int[][] data = FileOperator.readImage("");
		int w = data.length;
		int h = data[0].length;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(riverDataFileName));
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					image.setRGB(x, y, data[x][y]);
				}
			}

			Graphics2D g2d = image.createGraphics();
			g2d.setColor(Color.RED);

			String line;
			int c = 0;
			int[] colors = PolygonCreator.getColorList(32);

			while ((line = reader.readLine()) != null) {
				String[] lineData = line.split(",  ");
				if(colorSegments) g2d.setColor(new Color(colors[c % colors.length]));
				int lastX = -1;
				int lastY = -1;
				for(int i=1;i<lineData.length;i++) {
					String[] s1 = lineData[i].split("\\(");
					String[] s2 = s1[1].split("\\)");
					String[] s3 = s2[0].split(", ");
					int x = (int) (w * (Long.parseLong(s3[0]) + 180000000) / 360000000);
					int y = (int) (h * (Long.parseLong(s3[1]) + 90000000) / 180000000);

					if(lastX >= 0) {
						g2d.drawLine(lastX, h - lastY - 1, x, h - y - 1);
					}

					lastX = x;
					lastY = y;
				}

				c++;
			}

			File ImageFile = new File(outputFileName);
			ImageIO.write(image, "png", ImageFile);

			reader.close();
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}

		System.out.println("done");
	}

	static Map<String,List<List<Point>>> readRiverDataFromFile(String filename) {
		Map<String,List<List<Point>>> pointMap = new TreeMap<String,List<List<Point>>>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			String line;
			String cName = "";
			List<List<Point>> pointLists = new ArrayList<List<Point>>();

			while ((line = reader.readLine()) != null) {
				String[] lineData = line.split(",  ");
				if(!lineData[0].equals(cName)) {
					if(!cName.equals("")) {
						pointMap.put(cName, pointLists);
						pointLists = new ArrayList<List<Point>>();
					}
				}
				List<Point> points= new ArrayList<Point>();
				for(int i=1;i<lineData.length;i++) {
					String[] s1 = lineData[i].split("\\(");
					String[] s2 = s1[1].split("\\)");
					String[] s3 = s2[0].split(", ");

					points.add(new PointFloat(Double.parseDouble(s3[0]), Double.parseDouble(s3[1])));
				}
				pointLists.add(points);
				cName = lineData[0];
			}

			pointMap.put(cName, pointLists);
			pointLists = new ArrayList<List<Point>>();

			reader.close();
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}

		return pointMap;
	}

	static void convertRiverData(Map<String,List<List<Point>>> pointMap, int minX, int minY, int w, int h) {
		for(String s : pointMap.keySet()) {
			for(int i=0;i<pointMap.get(s).size();i++) {
				for(int j=0;j<pointMap.get(s).get(i).size();j++) {	
					Point p = pointMap.get(s).get(i).get(j);
					double x = w * (p.xFloat() + 180000000) / 360000000 - minX;
					double y = h * (1 - (p.yFloat() + 90000000) / 180000000) - minY;
					Point p2 = new PointFloat(x, y);
					pointMap.get(s).get(i).set(j, p2);
				}
			}
		}
	}

	public void test1() {
		List<Point> polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(10,0));
		polygon.add(new PointInt(10,10));
		polygon.add(new PointInt(0,10));

		List<Point> line = new ArrayList<Point>();
		line.add(new PointInt(-5,10));
		line.add(new PointInt(5,0));
		line.add(new PointInt(10,5));

		List<List<Point>> lines = new ArrayList<List<Point>>();
		lines.add(line);
		List<List<Point>> newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
	}

	public void test2() {
		List<Point> polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(2,0));
		polygon.add(new PointInt(2,2));
		polygon.add(new PointInt(0,2));

		List<Point> line = new ArrayList<Point>();
		line.add(new PointFloat(-1,2));
		line.add(new PointFloat(0.5,0.5));
		line.add(new PointFloat(1,1));

		List<List<Point>> lines = new ArrayList<List<Point>>();
		lines.add(line);
		List<List<Point>> newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
	}

	public void test3() {
		List<Point> polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(2,0));
		polygon.add(new PointInt(2,2));
		polygon.add(new PointInt(0,2));

		List<Point> line = new ArrayList<Point>();
		line.add(new PointFloat(0.5,0.5));
		line.add(new PointFloat(-1,2));
		line.add(new PointFloat(-1,3));
		line.add(new PointFloat(3,3));
		line.add(new PointFloat(3,2));
		line.add(new PointFloat(1.5,0.5));

		List<List<Point>> lines = new ArrayList<List<Point>>();
		lines.add(line);
		List<List<Point>> newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
	}

	public void test4() {
		List<List<Point>> lines = new ArrayList<List<Point>>();

		List<Point> line1 = new ArrayList<Point>();
		line1.add(new PointFloat(0.5,0.5));
		line1.add(new PointFloat(-1,2));
		line1.add(new PointFloat(-1,3));
		line1.add(new PointFloat(3,3));
		line1.add(new PointFloat(3,2));
		line1.add(new PointFloat(1.5,0.5));
		lines.add(line1);

		List<Point> line2 = new ArrayList<Point>();
		line2.add(new PointFloat(-1,2));
		line2.add(new PointFloat(-2,3));
		line2.add(new PointFloat(-3,4));
		line2.add(new PointFloat(-3,5));
		line2.add(new PointFloat(-3,5));
		lines.add(line2);

		List<Point> line3 = new ArrayList<Point>();
		line3.add(new PointFloat(-1,2));
		line3.add(new PointFloat(-2,3));
		line3.add(new PointFloat(-3,4));
		line3.add(new PointFloat(-3,5));
		line3.add(new PointFloat(-3,5));
		lines.add(line3);

		List<List<Point>> aaa = GeometryUtils.simplifyJts(lines, 0.1);
		System.out.println(aaa);
	}

	public void test5() {
		List<Point> polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(10,0));
		polygon.add(new PointInt(10,10));
		polygon.add(new PointInt(0,10));

		List<Point> line1 = new ArrayList<Point>();
		line1.add(new PointInt(-5,9));
		line1.add(new PointInt(15,1));

		List<Point> line2 = new ArrayList<Point>();
		line2.add(new PointInt(-5,10));
		line2.add(new PointInt(15,2));

		List<List<Point>> lines = new ArrayList<List<Point>>();
		lines.add(line1);
		lines.add(line2);
		List<List<Point>> newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
	}

	public void test6() {
		List<Point> polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(10,0));
		polygon.add(new PointInt(10,10));
		polygon.add(new PointInt(0,10));

		List<Point> line1 = new ArrayList<Point>();
		line1.add(new PointInt(-5,9));
		line1.add(new PointInt(15,1));

		List<Point> line2 = new ArrayList<Point>();
		line2.add(new PointInt(-5,10));
		line2.add(new PointInt(15,2));

		List<Point> line3 = new ArrayList<Point>();
		line3.add(new PointInt(-1,1));
		line3.add(new PointInt(2,1));

		List<Point> line4 = new ArrayList<Point>();
		line4.add(new PointInt(9,9));
		line4.add(new PointInt(12,9));

		List<List<Point>> lines = new ArrayList<List<Point>>();
		lines.add(line1);
		lines.add(line2);
		lines.add(line3);
		lines.add(line4);
		List<List<Point>> newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
	}

	public void test7() {
		List<Point> polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(0,10));
		polygon.add(new PointInt(10,10));
		polygon.add(new PointInt(10,1));
		polygon.add(new PointInt(9,1));
		polygon.add(new PointInt(9,2));
		polygon.add(new PointInt(8,2));
		polygon.add(new PointInt(8,1));
		polygon.add(new PointInt(9,1));
		polygon.add(new PointInt(9,0));
		polygon.add(new PointInt(0,0));

		List<Point> line1 = new ArrayList<Point>();
		line1.add(new PointInt(-5,8));
		line1.add(new PointInt(15,8));

		List<List<Point>> lines = new ArrayList<List<Point>>();
		lines.add(line1);

		List<List<Point>> newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
	}

	public void test8() {
		List<Point> polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(0,10));
		polygon.add(new PointInt(10,10));
		polygon.add(new PointInt(10,1));
		polygon.add(new PointInt(9,1));
		polygon.add(new PointInt(9,2));
		polygon.add(new PointInt(8,2));
		polygon.add(new PointInt(8,3));
		polygon.add(new PointInt(7,3));
		polygon.add(new PointInt(7,2));
		polygon.add(new PointInt(8,2));
		polygon.add(new PointInt(8,1));
		polygon.add(new PointInt(9,1));
		polygon.add(new PointInt(9,0));
		polygon.add(new PointInt(0,0));

		List<Point> line1 = new ArrayList<Point>();
		line1.add(new PointInt(-5,8));
		line1.add(new PointInt(15,8));

		List<List<Point>> lines = new ArrayList<List<Point>>();
		lines.add(line1);

		List<List<Point>> newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
		System.out.println("expected output:");
		System.out.println("[[(0.0, 0.0), (0.0, 8.0), (10.0, 8.0), (10.0, 1.0), (9.0, 1.0), (9.0, 2.0), (8.0, 2.0), (8.0, 3.0), (7.0, 3.0), (7.0, 2.0), (8.0, 2.0), (8.0, 1.0), (9.0, 1.0), (9.0, 0.0)], [(0.0, 8.0), (0.0, 10.0), (10.0, 10.0), (10.0, 8.0)]]");
	}

	public boolean check(List<Point> polygon1, double[][] polygon2) {
		if(polygon1.size() != polygon2.length || polygon2[0].length != 2) return false;

		for(int i=0;i<polygon1.size();i++) {
			if(!polygon1.get(i).equals(new PointFloat(polygon2[i][0], polygon2[i][1]))) return false;
		}

		return true;
	}

	public void test9() {
		List<Boolean> results = new ArrayList<Boolean>();

		List<Point> polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(0,10));
		polygon.add(new PointInt(10,10));
		polygon.add(new PointInt(10,0));
		polygon.add(new PointInt(0,0));

		List<Point> line1 = new ArrayList<Point>();
		line1.add(new PointInt(-2,4));
		line1.add(new PointInt(2,6));

		List<Point> line2 = new ArrayList<Point>();
		line2.add(new PointInt(-2,6));
		line2.add(new PointInt(2,4));

		List<List<Point>> lines = new ArrayList<List<Point>>();
		lines.add(line1);
		lines.add(line2);

		List<List<Point>> newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
		results.add(check(newPolygons.get(0), new double[][] {{0, 0}, {0, 5}, {2, 4}, {0, 5}, {2, 6}, {0, 5}, {0, 10}, {10, 10}, {10, 0}}));

		System.out.println("1---");

		polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(10,0));
		polygon.add(new PointInt(10,10));
		polygon.add(new PointInt(0,10));
		polygon.add(new PointInt(0,0));

		line1 = new ArrayList<Point>();
		line1.add(new PointInt(-2,4));
		line1.add(new PointInt(2,6));

		line2 = new ArrayList<Point>();
		line2.add(new PointInt(-2,6));
		line2.add(new PointInt(2,4));

		lines = new ArrayList<List<Point>>();
		lines.add(line1);
		lines.add(line2);

		newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
		results.add(check(newPolygons.get(0), new double[][] {{0, 0}, {0, 5}, {2, 4}, {0, 5}, {2, 6}, {0, 5}, {0, 10}, {10, 10}, {10, 0}}));

		System.out.println("2---");

		polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(10,0));
		polygon.add(new PointInt(10,10));
		polygon.add(new PointInt(0,10));
		polygon.add(new PointInt(0,0));

		line1 = new ArrayList<Point>();
		line1.add(new PointInt(8,4));
		line1.add(new PointInt(12,6));

		line2 = new ArrayList<Point>();
		line2.add(new PointInt(8,6));
		line2.add(new PointInt(12,4));

		lines = new ArrayList<List<Point>>();
		lines.add(line1);
		lines.add(line2);

		newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
		results.add(check(newPolygons.get(0), new double[][] {{10, 5}, {8, 6}, {10, 5}, {8, 4}, {10, 5}, {10, 0}, {0, 0}, {0, 10}, {10, 10}}));

		System.out.println("3---");

		polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(10,0));
		polygon.add(new PointInt(10,10));
		polygon.add(new PointInt(0,10));
		polygon.add(new PointInt(0,0));

		line1 = new ArrayList<Point>();
		line1.add(new PointInt(8,4));
		line1.add(new PointInt(12,6));

		line2 = new ArrayList<Point>();
		line2.add(new PointInt(8,6));
		line2.add(new PointInt(12,4));

		lines = new ArrayList<List<Point>>();
		lines.add(line1);
		lines.add(line2);

		newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
		results.add(check(newPolygons.get(0), new double[][] {{10, 5}, {8, 6}, {10, 5}, {8, 4}, {10, 5}, {10, 0}, {0, 0}, {0, 10}, {10, 10}}));

		System.out.println("4---");

		polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(0,10));
		polygon.add(new PointInt(-10,10));
		polygon.add(new PointInt(-10,0));
		polygon.add(new PointInt(0,0));

		line1 = new ArrayList<Point>();
		line1.add(new PointInt(-8,6));
		line1.add(new PointInt(-12,4));

		line2 = new ArrayList<Point>();
		line2.add(new PointInt(-8,4));
		line2.add(new PointInt(-12,6));

		lines = new ArrayList<List<Point>>();
		lines.add(line1);
		lines.add(line2);

		newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
		results.add(check(newPolygons.get(0), new double[][] {{0, 0}, {-10, 0}, {-10, 5}, {-8, 4}, {-10, 5}, {-8, 6}, {-10, 5}, {-10, 10}, {0, 10}}));

		System.out.println("5---");

		polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(-10,0));
		polygon.add(new PointInt(-10,10));
		polygon.add(new PointInt(0,10));
		polygon.add(new PointInt(0,0));

		line1 = new ArrayList<Point>();
		line1.add(new PointInt(-8,6));
		line1.add(new PointInt(-12,4));

		line2 = new ArrayList<Point>();
		line2.add(new PointInt(-8,4));
		line2.add(new PointInt(-12,6));

		lines = new ArrayList<List<Point>>();
		lines.add(line1);
		lines.add(line2);

		newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
		results.add(check(newPolygons.get(0), new double[][] {{-10, 5}, {-8, 4}, {-10, 5}, {-8, 6}, {-10, 5}, {-10, 10}, {0, 10}, {0, 0}, {-10, 0}}));

		System.out.println("6---");

		polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(0,10));
		polygon.add(new PointInt(-10,10));
		polygon.add(new PointInt(-10,0));
		polygon.add(new PointInt(0,0));

		line1 = new ArrayList<Point>();
		line1.add(new PointInt(-6,12));
		line1.add(new PointInt(-4,8));

		line2 = new ArrayList<Point>();
		line2.add(new PointInt(-6,8));
		line2.add(new PointInt(-4,12));

		lines = new ArrayList<List<Point>>();
		lines.add(line1);
		lines.add(line2);

		newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
		results.add(check(newPolygons.get(0), new double[][] {{-5, 10}, {-6, 8}, {-5, 10}, {-4, 8}, {-5, 10}, {0, 10}, {0, 0}, {-10, 0}, {-10, 10}}));

		System.out.println("7---");

		polygon = new ArrayList<Point>();
		polygon.add(new PointInt(0,0));
		polygon.add(new PointInt(-10,0));
		polygon.add(new PointInt(-10,10));
		polygon.add(new PointInt(0,10));
		polygon.add(new PointInt(0,0));

		line1 = new ArrayList<Point>();
		line1.add(new PointInt(-6,12));
		line1.add(new PointInt(-4,8));

		line2 = new ArrayList<Point>();
		line2.add(new PointInt(-6,8));
		line2.add(new PointInt(-4,12));

		lines = new ArrayList<List<Point>>();
		lines.add(line1);
		lines.add(line2);

		newPolygons = GeometryUtils.splitPolygon(polygon, lines);
		System.out.println(newPolygons);
		results.add(check(newPolygons.get(0), new double[][] {{0, 0}, {-10, 0}, {-10, 10}, {-5, 10}, {-6, 8}, {-5, 10}, {-4, 8}, {-5, 10}, {0, 10}}));

		System.out.println(results);
	}
	
	public void reformatAll() {
		reformatRiverData(MapName.EARTH_1_CE);
		reformatRiverData(MapName.EARTH_16K_BCE);
		reformatRiverData(MapName.TES_NIRN);
		reformatRiverData(MapName.FF6_OVERWORLD);
	}

	public static void main(String[] args) {
		new RiverProcessor().reformatAll();
//				new RiverProcessor().reformatRiverData();
		//		new RiverProcessor().simplifyAndWriteRiverData(100000);
		//		new RiverProcessor().drawRivers(REFORMATTED_RIVER_DATA_FILENAME, BASE_MAP_WITH_RIVERS_OUTPUT_FILENAME);
		//		new RiverProcessor().drawRivers(SIMPLIFIED_RIVER_DATA_FILENAME, BASE_MAP_WITH_SIMPLIFIED_RIVERS_OUTPUT_FILENAME, false);
		//		new RiverProcessor().drawRivers(SIMPLIFIED_RIVER_DATA_FILENAME, BASE_MAP_WITH_SIMPLIFIED_SEGMENTED_RIVERS_OUTPUT_FILENAME, true);

		//		new RiverProcessor().test1();
		//		new RiverProcessor().test2();
		//		new RiverProcessor().test3();
		//		new RiverProcessor().test4();
		//		new RiverProcessor().test5();
		//		new RiverProcessor().test6();
		//		new RiverProcessor().test7();
		//		new RiverProcessor().test8();
//		new RiverProcessor().test9();

		//		System.out.println(GeometryUtils.sharesSegmentWith(new PointInt(0,0), new PointInt(2,0), new PointInt(0,0), new PointInt(1,0)));
		//		System.out.println(GeometryUtils.sharesSegmentWith(new PointInt(0,0), new PointInt(1,0), new PointInt(0,0), new PointInt(2,0)));
		//		System.out.println(GeometryUtils.sharesSegmentWith(new PointInt(0,0), new PointInt(1,0), new PointInt(1,0), new PointInt(2,0)));
		//		System.out.println(GeometryUtils.sharesSegmentWith(new PointInt(0,0), new PointInt(1,0), new PointInt(2,0), new PointInt(3,0)));
		//		System.out.println(GeometryUtils.sharesSegmentWith(new PointInt(0,0), new PointInt(1,0), new PointInt(-1,0), new PointInt(2,0)));
		//		System.out.println(GeometryUtils.sharesSegmentWith(new PointInt(-1,0), new PointInt(2,0), new PointInt(0,0), new PointInt(1,0)));
		//		
		//		System.out.println(GeometryUtils.sharesSegmentWith(new PointInt(0,0), new PointInt(1,0), new PointInt(2,1), new PointInt(3,1)));
		//		System.out.println(GeometryUtils.sharesSegmentWith(new PointInt(0,0), new PointInt(1,0), new PointInt(0,0), new PointInt(1,1)));
		//		System.out.println(GeometryUtils.sharesSegmentWith(new PointInt(0,0), new PointInt(1,0), new PointInt(-1,0), new PointInt(0,1)));

		//		System.out.println(GeometryUtils.sharesSegmentWith(new PointFloat(130.98181315800002, 172.10139991300002), new PointFloat(136.97886383200003, 171.88664246500002), new PointFloat(136.97886383241146, 171.886642465297), new PointFloat(130.98181315791462, 172.1013999130309)));
	}
}
