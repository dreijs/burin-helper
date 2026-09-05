package vectormaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import util.*;
import vectormaps.ElevationMapCreator.MapName;
import vectormaps.ElevationMapCreator.Trace;


public class PolygonCreator {

	public static final String POLYGONS_ALL_FOLDER_NAME = "polygons_visual_all\\";
	public static final String POLYGONS_ERROR_FOLDER_NAME = "polygons_visual_errors\\";
	public static final String POLYGONS_NEW_CUT_FOLDER_NAME = "polygons_visual_new_cut\\";
	public static final String POLYGONS_NEW_UNCUT_FOLDER_NAME = "polygons_visual_new_uncut\\";
	public static final String POLYGONS_NEW_SPLIT_FOLDER_NAME = "polygons_visual_new_split\\";

	public static final String POLYGONS_BASE_FILENAME = "polygons_base.txt";
	public static final String POLYGONS_PRUNED_FILENAME = "polygons_pruned.txt";
	public static final String POLYGONS_ORDERED_FILENAME = "polygons_ordered.txt";
	public static final String POLYGONS_DISTORTED_FILENAME = "polygons_distorted.txt";
	public static final String POLYGONS_SIMPLIFIED_FILENAME = "polygons_simplified.txt";
	public static final String POLYGONS_FILTERED_FILENAME = "polygons_filtered.txt";

	public static final String REGIONS_FILENAME = "regions.png";
	public static final String VISUAL_REGIONS_FILENAME = "regions_visual.png";
	public static final String VISUAL_REGIONS_SMALL_REMOVED_FILENAME = "regions_visual_small_removed.png";
	public static final String VISUAL_POLYGONS_FILENAME = "region_polygons.png";
	public static final String VISUAL_POLYGONS_SCALED_FILENAME = "region_polygons_scaled.png";

	public String getPolygonFolderName(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\polygons\\";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\polygons\\";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\polygons\\";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\polygons\\";
		return "";
	}

	public String getTraceFolderName(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\polygon_traces\\";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\polygon_traces\\";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\polygon_traces\\";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\polygon_traces\\";
		return "";
	}
	
	public static boolean traceText(Trace trace) {
		if(trace.equals(Trace.TEXT)) return true;
		if(trace.equals(Trace.REGIONS_AND_TEXT)) return true;
		if(trace.equals(Trace.ALL)) return true;
		return false;
	}
	
	public static boolean traceVisualRegions(Trace trace) {
		if(trace.equals(Trace.VISUAL_REGIONS)) return true;
		if(trace.equals(Trace.REGIONS_AND_TEXT)) return true;
		if(trace.equals(Trace.ALL)) return true;
		return false;
	}
	
	public static boolean traceVisualPolygons(Trace trace) {
		if(trace.equals(Trace.VISUAL_POLYGONS)) return true;
		if(trace.equals(Trace.ALL)) return true;
		return false;
	}

	void visualizeRegion(int[][] regions, String filename) {
		visualizeRegion(regions, filename, 32);
	}

	static int[] getColorList(int stepsize) {
		int n = 256 / stepsize;

		List<Integer> colorList = new ArrayList<Integer>();
		for(int i=0;i<=n;i++) {
			for(int j=0;j<=n;j++) {
				for(int k=0;k<=n;k++) {
					colorList.add(new Color((int) Math.min(255, stepsize*i), (int) Math.min(255, stepsize*j), (int) Math.min(255, stepsize*k)).getRGB()); 
				}
			}
		}

		// shuffle for better effects (otherwise neighboring regions with similar indices have similar colors)
		Collections.shuffle(colorList);
		int[] colors = colorList.stream().filter(Objects::nonNull).mapToInt(i -> i).toArray();

		return colors;
	}

	void visualizeRegion(int[][] regions, String filename, int stepsize) {
		int[][] regionC = new int[regions.length][];
		int[] colors = getColorList(stepsize);

		//		int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF, 0xFF0000CC, 0xFF00CC00, 0xFF0000CC, 0xFF00CC00, 0xFFCC0000, 0xFFCC0000, 0xFF000099, 0xFF009900, 0xFF000099, 0xFF009900, 0xFF990000, 0xFF990000, 0xFF000066, 0xFF006600, 0xFF000066, 0xFF006600, 0xFF660000, 0xFF660000, 0xFF000033, 0xFF003300, 0xFF000033, 0xFF003300, 0xFF330000, 0xFF330000, 0xFFCCCCCC, 0xFF999999, 0xFF666666, 0xFF333333, 0xFF336699, 0xFF339966, 0xFF663399, 0xFF669933, 0xFF993366, 0xFF996633, 0xFFCC9966, 0xFFCC6699, 0xFF99CC66, 0xFF9966CC, 0xFF66CC99, 0xFF6699CC, 0xFF99CCFF, 0xFF99FFCC, 0xFFCC99FF, 0xFFCCFF99, 0xFFFF99CC, 0xFFFFCC99};
		for (int x = 0; x < regions.length; x++) {
			regionC[x] = new int[regions[x].length];
			for (int y = 0; y < regions[x].length; y++) {
				if(regions[x][y] < 0) regionC[x][y] = 0xFF000000;
				else regionC[x][y] = colors[regions[x][y] % colors.length];
			}
		}
		FileOperator.writeImage(regionC, filename);
	}

	void visualizeAllPolygons(List<Region> regions, String fileName, int w, int h, int scale) {
		try {
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					image.setRGB(x, y, 0xFFFFFFFF);
				}
			}

			Graphics2D g2d = image.createGraphics();

			for(int ii=0;ii<regions.size();ii++) {
				g2d.setColor(Color.BLACK);
				List<Point> p = regions.get(ii).polygon;
				int lastX = -1;
				int lastY = -1;
				for(int i=0;i<p.size();i++) {
					int x = (int) Math.round((p.get(i).xFloat()) * scale) + 1;
					int y = (int) Math.round((p.get(i).yFloat()) * scale) + 1;

					if(lastX >= 0) {
						g2d.drawLine(lastX, lastY, x, y);
					}

					lastX = x;
					lastY = y;
				}

				int x0 = (int) Math.round((p.get(0).xFloat()) * scale) + 1;
				int y0 = (int) Math.round((p.get(0).yFloat()) * scale) + 1;
				g2d.drawLine(lastX, lastY, x0, y0);

				g2d.setColor(Color.RED);
				for(int i=0;i<p.size();i++) {
					int x = (int) Math.round((p.get(i).xFloat()) * scale) + 1;
					int y = (int) Math.round((p.get(i).yFloat()) * scale) + 1;
					g2d.drawLine(x, y, x, y);
				}
			}				
			File ImageFile = new File(fileName);
			ImageIO.write(image, "png", ImageFile);
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}

		System.out.println("done");
	}

	public static void visualizePolygon(List<Point> p, String fileName, int scale) {
		List<List<Point>> ps = new ArrayList<List<Point>>();
		ps.add(p);
		visualizePolygons(ps, fileName, scale);
	}

	public static void visualizePolygons(List<List<Point>> ps, String fileName, int baseScale) {
		int scale = baseScale;

		Rectangle2D.Double bb = GeometryUtils.getFloatBoundingBoxFromLists(ps);

		// prevent integer overflows (width and height should roughly be below Math.sqrt(Integer.MAX_VALUE), but set to 30000 just in case)
		scale /= Math.ceil(((long) scale) * bb.width / 30000);
		scale /= Math.ceil(((long) scale) * bb.height / 30000);

		int w = (int) (Math.ceil(scale * bb.width)) + 3; // + 2 to create white margin at edge
		int h = (int) (Math.ceil(scale * bb.height)) + 3;

		//		System.out.println(w+" "+h+" "+ps);

		try {
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					image.setRGB(x, y, 0xFFFFFFFF);
				}
			}

			Graphics2D g2d = image.createGraphics();
			g2d.setColor(Color.BLACK);

			for(List<Point> p : ps) {
				int lastX = -1;
				int lastY = -1;
				for(int i=0;i<p.size();i++) {
					int x = (int) Math.round((p.get(i).xFloat() - bb.x) * scale) + 1;
					int y = (int) Math.round((p.get(i).yFloat() - bb.y) * scale) + 1;

					if(lastX >= 0) {
						g2d.drawLine(lastX, lastY, x, y);
					}

					lastX = x;
					lastY = y;
				}

				int x0 = (int) Math.round((p.get(0).xFloat() - bb.x) * scale) + 1;
				int y0 = (int) Math.round((p.get(0).yFloat() - bb.y) * scale) + 1;
				g2d.drawLine(lastX, lastY, x0, y0);
			}

			for(List<Point> p : ps) {
				g2d.setColor(Color.RED);
				for(int i=0;i<p.size();i++) {
					int x = (int) Math.round((p.get(i).xFloat() - bb.x) * scale) + 1;
					int y = (int) Math.round((p.get(i).yFloat() - bb.y) * scale) + 1;
					g2d.drawLine(x, y, x, y);
				}

				File ImageFile = new File(fileName);
				ImageIO.write(image, "png", ImageFile);
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}

		//		System.out.println("done");
	}

	void addIfNew(List<PointInt> list, PointInt p) {
		for(PointInt pp : list) if(pp.equals(p)) return;
		list.add(p);
	}

	int check(int[][] regionData, int x1, int y1, int x2, int y2) {
		int nn =  Math.min(regionData[x1][y1], regionData[x2][y2]);

		if(regionData[x1][y1] > nn) {
			regionData[x1][y1] = nn;
			return 1;
		}

		if(regionData[x2][y2] > nn) {
			regionData[x2][y2] = nn;
			return 1;
		}

		return 0;
	}

	RegionResult findRegions(int[][] mapData, int minX, int minY, int maxX, int maxY) {
		// group map into regions

		int[][] regionData = new int[mapData.length][mapData[0].length];
		for (int x = 0; x < mapData.length; x++) {
			for (int y = 0; y < mapData[x].length; y++) {
				regionData[x][y] = Integer.MAX_VALUE;
			}
		}

		regionData[0][0] = 0;
		int cRegion = 1;
		int nChanged = 1;

		while(nChanged > 0) {
			nChanged = 0;

			for (int x = minX; x < maxX; x++) {
				for (int y = minY; y < maxY; y++) {

					if(x > 0) {
						if(mapData[x-1][y] == mapData[x][y]) {
							nChanged += check(regionData, x, y, x-1, y);
						}
					}

					if(x < mapData.length-1) {
						if(mapData[x+1][y] == mapData[x][y]) {
							nChanged += check(regionData, x, y, x+1, y);
						}
					}

					if(y > 0) {
						if(mapData[x][y] == mapData[x][y-1]) {
							nChanged += check(regionData, x, y, x, y-1);
						}
					}

					if(y < mapData[x].length-1) {
						if(mapData[x][y] == mapData[x][y+1]) {
							nChanged += check(regionData, x, y, x, y+1);
						}
					}

					if(regionData[x][y] == Integer.MAX_VALUE) {
						regionData[x][y] = cRegion;
						cRegion++;
						nChanged++;
					}
				}
			}

			System.out.println("num. changed = "+nChanged);
		}

		int[] type = new int[cRegion];
		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				type[regionData[x][y]] = mapData[x][y];
			}
		}

		RegionResult result = MapOperator.cleanRegionIndices(new RegionResult(regionData, type, cRegion));
		
		return result;
	}

	int getTotalnumPoints(List<Region> regions) {
		int total = 0;
		for(Region region : regions) {
			total += region.polygon.size();
		}
		return total;
	}

	void basicPrune(List<Region> regions) {
		for(Region region : regions) {
			List<Point> polygon = region.polygon;
			List<Integer> idxs = region.opposingRegions;

			int n = polygon.size();

			for(int j=n-1;j>=0;j--) {
				n = polygon.size();
				PointInt p0 = polygon.get(Math.floorMod(j, n)).asIntPoint();
				PointInt p1 = polygon.get(Math.floorMod(j-1, n)).asIntPoint();
				PointInt p2 = polygon.get(Math.floorMod(j-2, n)).asIntPoint();

				int i1 = idxs.get(Math.floorMod(j-1, n));
				int i2 = idxs.get(Math.floorMod(j-2, n));

				if(p0.x == p1.x && p0.x == p2.x && i1 == i2 || p0.y == p1.y && p0.y == p2.y && i1 == i2) {
					polygon.remove(Math.floorMod(j-1, n));
					idxs.remove(Math.floorMod(j-1, n));
				}
			}
		}
	}

	void printShapeInt(List<Point> polygon) {
		for(int j=0;j<polygon.size();j++) {
			if(j>0) System.out.print(",");
			System.out.print(polygon.get(j).xInt());
		}
		System.out.println();
		for(int j=0;j<polygon.size();j++) {
			if(j>0) System.out.print(",");
			System.out.print(polygon.get(j).yInt());
		}
		System.out.println();
	}

	void printShapeFloat(List<Point> polygon) {
		for(int j=0;j<polygon.size();j++) {
			if(j>0) System.out.print(",");
			System.out.print(polygon.get(j).xFloat());
		}
		System.out.println();
		for(int j=0;j<polygon.size();j++) {
			if(j>0) System.out.print(",");
			System.out.print(polygon.get(j).yFloat());
		}
		System.out.println();
	}

	/**
	 * Simplifies every region's outline, leaving alone any vertex that sits on the tile boundary.
	 *
	 * Each tile is a separate run of processMap(), so the neighbouring tile simplifies its own copy
	 * of the same coastline independently. Whatever this drops on one side of a seam, the other side
	 * keeps unless it happens to make the same decision -- and it does not: on the finished level-2
	 * output only 19 of 55 vertices matched along the x = -135 seam, the rest differing by a median
	 * of 33 km and by as much as 144 km. That is what breaks the coast and river lines at tile edges,
	 * and it also leaves boundary edges unmatchable, so nothing downstream can pair a triangle with
	 * its neighbour across a tile.
	 *
	 * width and height are the tile's local pixel extent, the same ones addRandomNoise() uses to
	 * decide which points it may displace.
	 */
	void simplifyDouglasPeucker(List<Region> regions, double maxDist, double maxSize, int width, int height) {
		int nn = regions.size();
		for(int i=nn-1;i>=0;i--) {
			if((nn - 1 - i) % 1000 == 0) System.out.println((nn - 1 - i)+"/"+nn);

			Region region = regions.get(i);

			if (region.polygon.size() > 0) {
				int prevSize = 0;
				while(prevSize != region.polygon.size()) {
					prevSize = region.polygon.size();

					int n = region.opposingRegions.size();

					List<Integer> segments = new ArrayList<Integer>();
					for(int j=0;j<n;j++) {
						if(!region.opposingRegions.get(j).equals(region.opposingRegions.get((j+1)%n))) {
							segments.add(j+1);
						}
					}

					if(segments.size() == 0) {
						simplifyRecursive(regions, i, 0, n - 1, maxDist, maxSize, prevSize, width, height);
					} else {
						for(int j=0;j<segments.size();j++) {
							int jj = (j+1)%segments.size();
							simplifyRecursive(regions, i, segments.get(j)%n, segments.get(jj) - 1, maxDist, maxSize, prevSize, width, height);
						}
					}
				}
			}
		}
	}

	/**
	 * True for a point on the tile's edge, using exactly the test addRandomNoise() applies when it
	 * decides a point may not be displaced. The two must agree: a point that keeps its position but
	 * gets deleted is no more shared with the neighbouring tile than one that moves.
	 */
	private static boolean isOnTileBoundary(Point p, int width, int height) {
		int x = p.xInt();
		int y = p.yInt();
		return x <= 0 || x >= width || y <= 0 || y >= height;
	}

	private static void simplifyRecursive(List<Region> regions, int idx, int start, int end, double maxDist, double maxSize, int prevSize, int width, int height) {
		Region region = regions.get(idx);
		int n = region.polygon.size();


		if(n != prevSize) return;

		PointFloat pStart = region.polygon.get(start).asFloatPoint();
		PointFloat pEnd = region.polygon.get((end+1)%n).asFloatPoint();

		if(end >= start && end - start < 1) return;
		if(start > end && end - start + n < 1) return;

		double dMax = -1;
		int index = 0;

		List<Point> newPolygon = new ArrayList<Point>();

		boolean valid = true;
		for (int i = (start+1)%n; i != end; i=(i+1)%n) {
			double distance = GeometryUtils.perpendicularDistance(region.polygon.get(i), pStart, pEnd);
			if (distance > dMax) {
				dMax = distance;
				index = i;
			}
			newPolygon.add(region.polygon.get(i));
		}

		if(dMax == -1) return;
		if(dMax > maxDist) valid = false;

		// removeSegment() below drops the vertices strictly between pStart and pEnd, that is
		// start+1 .. end. If any of those is on the tile boundary, refuse the cut and let the
		// recursion split around it, so both tiles keep the identical vertex set along the seam.
		if(valid) {
			for(int i = (start+1)%n; ; i = (i+1)%n) {
				if(isOnTileBoundary(region.polygon.get(i), width, height)) {
					valid = false;
					break;
				}
				if(i == end) break;
			}
		}

		// check if making the cut would not lead to intersections in this polygon
		int oppRegion = region.opposingRegions.get(start);
		if(valid) {
			valid = region.canSimplifySegment(pStart, pEnd, oppRegion);
		}
		if(valid && oppRegion >= 0) {
			valid = regions.get(oppRegion).canSimplifySegment(pStart, pEnd, idx);
		}
		if(valid) {
			List<Point> allPoints = new ArrayList<Point>();
			allPoints.add(region.polygon.get(start));
			for (int i = (start+1)%n; i != end%n; i = (i+1)%n) {
				allPoints.add(region.polygon.get(i));
			}
			allPoints.add(region.polygon.get(end));

			double cutSize = GeometryUtils.polygonArea(allPoints);
			if(cutSize > maxSize) valid = false;

			// check if the cut is not too big

			double totSize = GeometryUtils.polygonArea(region.polygon);
			if(cutSize / totSize > 0.05) {
				valid = false;
			}
			if(oppRegion >= 0) {
				totSize = GeometryUtils.polygonArea(regions.get(oppRegion).polygon);				
				if(cutSize / totSize > 0.05) {
					valid = false;
				}
			}
		}

		if (!valid) {
			// maxDist then maxSize, matching the signature. These two were transposed here, so every
			// recursive step ran with the deviation tolerance and the area cap swapped -- at level 1
			// that meant 10 and 20 where 20 and 10 were intended. Only the two top-level calls in
			// simplifyDouglasPeucker() were passing them the right way round.
			simplifyRecursive(regions, idx, start, index, maxDist, maxSize, prevSize, width, height);
			simplifyRecursive(regions, idx, index, end, maxDist, maxSize, prevSize, width, height);
		} else {
			int[] indices1 = region.getSegmentIndices(pStart, pEnd, oppRegion);
			if(oppRegion >= 0 && regions.get(oppRegion).outerNeighbors.contains(idx)) {
				Point[] points = region.getSegmentPoints(indices1, oppRegion);
				int[] indices2 = regions.get(oppRegion).getSegmentIndices(points);
				if(indices2 != null) {
					region.removeSegment(indices1, oppRegion);
					regions.get(oppRegion).removeSegment(indices2, idx);
				}
			} else {
				region.removeSegment(indices1, oppRegion);
			}
		}
	}

	void addRiverData(List<Region> regions, double d, double minX, double minY, int w, int h, MapName name, String traceFolder, Trace trace) {
		Map<String,List<List<Point>>> pointMap = RiverProcessor.simplifyRiverData(d, name);
		RiverProcessor.convertRiverData(pointMap, (int) Math.round(minX * w), (int) Math.round(minY * h), w, h);

		if(traceVisualPolygons(trace)) {
			FileOperator.clearFolder(traceFolder+POLYGONS_NEW_CUT_FOLDER_NAME);
			FileOperator.clearFolder(traceFolder+POLYGONS_NEW_UNCUT_FOLDER_NAME);
			FileOperator.clearFolder(traceFolder+POLYGONS_NEW_SPLIT_FOLDER_NAME);
		}

		Map<String, Rectangle> boundingBoxes = new HashMap<String, Rectangle>();
		Map<String,Integer> riverIdxMap = new TreeMap<String,Integer>();

		for(String s : pointMap.keySet()) {
			boundingBoxes.put(s, GeometryUtils.getIntegerBoundingBoxFromLists(pointMap.get(s)));
			riverIdxMap.put(s,riverIdxMap.keySet().size());
			//			if(trace && riverIdxMap.get(s) == 166) {
			//				System.out.println("river 166: "+s+" "+pointMap.get(s).size()+" "+pointMap.get(s).get(0).size()+" "+pointMap.get(s));
			//				List<List<Point>> riverPoints = new ArrayList<List<Point>>();
			//				for(int i=0;i<pointMap.get(s).get(0).size()-1;i++) {
			//					Point p1 = pointMap.get(s).get(0).get(i);
			//					Point p2 = pointMap.get(s).get(0).get(i+1);
			//					List<Point> l = new ArrayList<Point>();
			//					l.add(p1);
			//					l.add(p2);
			//					riverPoints.add(l);
			//				}
			//				visualizePolygons(riverPoints, System.getProperty("user.dir")+"\\output\\map\\polygons\\river_2338.png", 64);
			//			}
		}

		System.out.println("adding rivers");
		int n = regions.size();
		for(int i=n-1;i>=0;i--) {
			if((n - 1 - i) % 1000 == 0) System.out.println((n - 1 - i)+"/"+n);
			Region region = regions.get(i);
			region.resetRiverData();
			Rectangle bb = GeometryUtils.getIntegerBoundingBoxFromList(region.polygon);

			List<List<Point>> intersectingRiverData = new ArrayList<List<Point>>();
			List<Integer> riverIndices = new ArrayList<Integer>(); 

			for(String s : pointMap.keySet()) {
				if(bb.intersects(boundingBoxes.get(s))) {
					List<List<Point>> riverData = pointMap.get(s);
					for(List<Point> p : riverData) {
						intersectingRiverData.add(p);
						riverIndices.add(riverIdxMap.get(s));
					}
				}
			}

			//			System.out.print("z");

			List<List<Point>> newPolygons = GeometryUtils.splitPolygon(region.polygon, intersectingRiverData, i, traceFolder, trace);

			if(newPolygons.size() > 1) {
				//				System.out.println("a");
				if(traceVisualPolygons(trace)) visualizePolygons(newPolygons, traceFolder+POLYGONS_NEW_CUT_FOLDER_NAME+"polygon_"+i+".png", 32);
				List<Region> newRegions = new ArrayList<Region>();
				for(int j=0;j<newPolygons.size();j++) {
					int cc = j > 0 ? regions.size() : i;
					if(traceVisualPolygons(trace)) visualizePolygon(newPolygons.get(j), traceFolder+POLYGONS_NEW_SPLIT_FOLDER_NAME+"polygon_"+cc+".png", 32);

					//					if(cc == 12279) {
					//						System.out.println("-*-");
					//						System.out.println(region);
					//						for(int jj=0;jj<newPolygons.size();jj++) {
					//							System.out.println(newPolygons.get(jj));
					//						}
					//						System.out.println("-*-");
					//					}

					Region newRegion = region.splitFromPolygon(newPolygons.get(j), intersectingRiverData, riverIndices, cc);
					if(j == 0) {regions.set(i, newRegion);}
					else {regions.add(newRegion);}
					newRegions.add(newRegion);

					//					if(i == 12279) {
					//						System.out.println("-*-");
					//						System.out.println(newRegion);
					//						System.out.println("-*-");
					//					}

					List<Integer> newOpps = new ArrayList<Integer>();
					for(int k=0;k<newRegion.polygon.size();k++) newOpps.add(cc);
					for(int k=0;k<j;k++) {
						Region otherRegion = newRegions.get(k);
						otherRegion.matchNeighbors(otherRegion.polygon, region.polygon, otherRegion.opposingRegions, region.opposingRegions, false);
					}
				}
			} else if(newPolygons.get(0).size() > region.polygon.size()) {
				//				System.out.println("b");
				if(traceVisualPolygons(trace)) visualizePolygon(newPolygons.get(0), traceFolder+POLYGONS_NEW_UNCUT_FOLDER_NAME+"polygon_"+i+".png", 32);
				region.extendBasedOnPolygon(newPolygons.get(0), intersectingRiverData, riverIndices);
			}
		}

		// print river indices
	}

	void mergeRegionsAndSetDrawOrder(List<Region> regions) {
		boolean finished = false;
		int drawOrder = 0;
		boolean[] done = new boolean[regions.size()];

		for(int i=0;i<done.length;i++) done[i] = false;
		while(!finished) {
			finished = true;

			for(int i=0;i<regions.size();i++) {
				if(!done[i]) {
					Region region = regions.get(i);
					boolean canRemove = true;
					for(int j=0;j<region.innerNeighbors.size();j++) {
						if(!done[region.innerNeighbors.get(j)]) canRemove = false;
					}
					if(canRemove) {
						region.setDrawOrder(drawOrder);
						done[i] = true;
						finished = false;
					}
				}
			}
			drawOrder++;
			System.out.println("draw order: "+drawOrder);
		}
	}

	void setBit(byte[] a, long i, boolean b) {
		byte bb = a[(int) (i/8)];
		int pos = (int) (i % 8);
		if(b) bb |= 1 << pos;
		else bb &= ~(1 << pos);
		a[(int) (i/8)] = bb;
	}

	byte[] extend(byte[] a) {
		byte[] b = new byte[Math.min(a.length * 2, 1073741824)];
		for(int i=0;i<a.length;i++) {
			b[i] = a[i];
		}
		return b;
	}

	byte[] determineTriangleDrawOrder(List<Point> polygon, int regId, String traceFolder, Trace trace) {		
		byte[] order = new byte[Math.min(polygon.size() * polygon.size(), 1073741824)];
		long idx = 0;
		List<Point> vertices = new ArrayList<>(polygon);

		if (vertices.size() < 3) {
			return null; // Cannot form a triangle
		}

		boolean faulty = false;

		for(int k=0;k<polygon.size();k++) {
			int kk = (k+1) % polygon.size();
			for(int k2=0;k2<polygon.size();k2++) {
				int kk2 = (k2+1) % polygon.size();	

				if(GeometryUtils.doSegmentsIntersect(polygon.get(k), polygon.get(kk), polygon.get(k2), polygon.get(kk2))) {
					System.out.println("Triangulation error! "+regId+" "+polygon.get(k)+" "+polygon.get(kk)+" "+polygon.get(k2)+" "+polygon.get(kk2));
					System.out.println(polygon);
					printShapeInt(polygon);
					faulty = true;
				}
			}
		}

		if(traceVisualPolygons(trace)) visualizePolygon(polygon, traceFolder+POLYGONS_ALL_FOLDER_NAME+"polygon_"+regId+".png", 4);
		if(traceVisualPolygons(trace) && faulty) visualizePolygon(polygon, traceFolder+POLYGONS_ERROR_FOLDER_NAME+"polygon_"+regId+".png", 32);

		boolean firstTrace = false;
		boolean nextTrace = false;

		while (vertices.size() > 2) {

			// determine if order is clockwise or counterclockwise
			boolean clockwise = GeometryUtils.calculatePolygonSignedArea(vertices) < 0;

			for (int i=vertices.size()-1;i>=0 && vertices.size() >= 0;i--) {

				int prev = (i == 0) ? vertices.size() - 1 : i - 1;
				int next = (i == vertices.size() - 1) ? 0 : i + 1;

				Point vPrev = vertices.get(prev);
				Point vCurr = vertices.get(i);
				Point vNext = vertices.get(next);

				List<Point> l = new ArrayList<Point>();
				l.add(vPrev); l.add(vCurr); l.add(vNext);

				if(firstTrace) {
					System.out.println(i+" "+l+": "+GeometryUtils.isEar(vPrev, vCurr, vNext, vertices, clockwise)+", "+GeometryUtils.calculatePolygonArea(l));
				}

				// Check for convexity and if the triangle formed is an "ear"
				// (i.e., no other polygon vertices are inside the triangle)

				if (GeometryUtils.isEar(vPrev, vCurr, vNext, vertices, clockwise) && (GeometryUtils.calculatePolygonArea(l) > GeometryUtils.EPSILON || GeometryUtils.calculatePolygonArea(vertices) <= GeometryUtils.EPSILON )) {
					//				if (GeometryUtils.isEar(vPrev, vCurr, vNext, vertices, clockwise)) {
					setBit(order, idx, true);
					vertices.remove(i);

					//					if(regId == 13123) {
					//						System.out.println("rem "+vCurr+" "+l+" "+GeometryUtils.calculatePolygonArea(l));
					//						visualizePolygon(vertices, System.getProperty("user.dir")+"\\output\\map\\polygons\\debug_polygon_"+(polygon.size() - vertices.size() - 1)+".png", 256);
					//						System.out.println(vertices);
					//					}

				} else {
					setBit(order, idx, false);
				}
				idx++;
				if(vertices.size() < 3) break;
				if(idx >= order.length*8) {
					System.out.println(regId+" "+vertices+" "+GeometryUtils.calculatePolygonArea(l));
					order = extend(order);
					printShapeInt(polygon);
					printShapeInt(vertices);
					nextTrace = true;
				}
			}
			if(nextTrace) {
				nextTrace = false;
				firstTrace = true;
			}
			else if(firstTrace) firstTrace = false;
		}

		byte[] result = new byte[(int) (idx/8) + 1];
		for(int i=0;i<result.length;i++) result[i] = order[i];
		return result;
	}

	void determineTriangleDrawOrders(List<Region> regions, String traceFolder, Trace trace) {
		if(traceVisualPolygons(trace)) {
			FileOperator.clearFolder(traceFolder+POLYGONS_ALL_FOLDER_NAME);
			FileOperator.clearFolder(traceFolder+POLYGONS_ERROR_FOLDER_NAME);
		}
		for(int i=regions.size()-1;i>=0;i--) {
			if((i % 10000) == 0) System.out.println("region "+i+"/"+regions.size());
			Region region = regions.get(i);
			if(region.polygon.size() > 0) {
				List<Point> polygon = region.polygon;
				byte[] triangleDrawOrder = determineTriangleDrawOrder(polygon, i, traceFolder, trace);
				region.setTriangleDrawOrder(triangleDrawOrder);
			}
		}
	}

	static int[][] initMapData(String inputFileName, int s) {
		int[][] mapData = null;

		try{
			mapData = FileOperator.readImage(inputFileName, s);
			System.out.println("done reading");
		} catch(Exception e){
			System.out.println(e.getMessage());
		}

		return mapData;
	}

	static int[][] mergeMapData(int s, MapName name) {
		String[] inputFileNames = new String[] {
				ElevationMapCreator.getElevationLevelsFilename(name),
				BiomeMapCreator.getRescaledBaseMapFilename(name),
				SoilMapCreator.getRescaledBaseMapFilename(name),
				FeatureMapCreator.getRawFeaturesOutputFilename(name)
		};

		Map<Integer,Integer> elevationMap = new HashMap<Integer,Integer>();
		elevationMap.put(ElevationMapCreator.BLUE, 0);
		for(int i=0;i<ElevationMapCreator.LEVEL_COLORS.length;i++) elevationMap.put(ElevationMapCreator.LEVEL_COLORS[i][1],i+1);

		Map<Integer,Integer> biomeMap = new HashMap<Integer,Integer>();
		for(int i=0;i<BiomeMapCreator.ALL_BIOMES.length;i++) biomeMap.put(BiomeMapCreator.ALL_BIOMES[i],i);

		Map<Integer,Integer> soilMap = new HashMap<Integer,Integer>();
		for(int i=0;i<SoilMapCreator.ALL_SOILS.length;i++) soilMap.put(SoilMapCreator.ALL_SOILS[i],i);

		Map<Integer,Integer> featureMap = new HashMap<Integer,Integer>();
		for(int i=0;i<FeatureMapCreator.ALL_FEATURES.length;i++) featureMap.put(FeatureMapCreator.ALL_FEATURES[i],i);

		int w = 0;
		int h = 0;

		int[][][] data = new int[inputFileNames.length][][];
		List<Map<Integer,Integer>> mappings = new ArrayList<Map<Integer,Integer>>();

		mappings.add(elevationMap);
		mappings.add(biomeMap);
		mappings.add(soilMap);
		mappings.add(featureMap);

		for(int i=0;i<inputFileNames.length;i++) {
			data[i] = initMapData(inputFileNames[i], s);
			if(w == 0) {
				w = data[i].length;
				h = data[i][0].length;
			} else {
				if(w != data[i].length || h != data[i][0].length) System.out.println("Error: dimension of map files inconsistent in mergeMapDataFromSources! "+inputFileNames[i]);
			}
		}

		int[][] finalMap = new int[w][h];

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int v = 0;
				for(int k=0;k<data.length;k++) {
					int exp = (int) Math.pow(16,k);
					if(!mappings.get(k).containsKey(data[k][x][y])) System.out.println("error in mergeMapData, invalid color!!! " +x+" "+y+" "+data[k][x][y]+" "+Arrays.toString(Colors.intToARGBArray(data[k][x][y]))+" "+k);
					v += mappings.get(k).get(data[k][x][y]) * exp;
				}
				finalMap[x][y] = v;
			}
		}

		return finalMap;
	}

	boolean updatePolygon(int[][] regionData, int regionIdx, List<Point> polygon, List<Integer> oppRegions, Point p, Point p2) {
		int x = p.xInt();
		int y = p.yInt();

		if(p.xInt() > p2.xInt()) { // from left
			if(y > 0 && regionData[x-1][y-1] == regionIdx) {
				if(x == regionData.length || regionData[x][y-1] != regionIdx) { // try up first
					polygon.add(new PointInt(x, y-1));
					if(x < regionData.length) oppRegions.add(regionData[x][y-1]);
					else oppRegions.add(-1);
					return true;
				}
				if(y == regionData[0].length || regionData[x][y] != regionIdx) { // try right
					polygon.add(new PointInt(x+1, y));
					if(y < regionData[0].length) oppRegions.add(regionData[x][y]);
					else oppRegions.add(-1);
					return true;
				} // else, must go down
				if(y < regionData[0].length) {
					polygon.add(new PointInt(x, y+1));
					oppRegions.add(regionData[x-1][y]);
					return true;
				}
			} else if(regionData[x-1][y] == regionIdx) {
				if(x == regionData.length || y < regionData[0].length && regionData[x][y] != regionIdx) { // try down first
					polygon.add(new PointInt(x, y+1));
					if(x < regionData.length) oppRegions.add(regionData[x][y]);
					else oppRegions.add(-1);
					return true;
				}
				if(y == 0 || regionData[x][y-1] != regionIdx) { // try right
					polygon.add(new PointInt(x+1, y));
					if(y > 0) oppRegions.add(regionData[x][y-1]);
					else oppRegions.add(-1);
					return true;
				} // else, must go up
				if(y > 0) {
					polygon.add(new PointInt(x, y-1));
					oppRegions.add(regionData[x-1][y-1]);
					return true;
				}
			}
		}

		if(p.xInt() < p2.xInt()) { // from right
			if(y > 0 && regionData[x][y-1] == regionIdx) {
				if(x == 0 || regionData[x-1][y-1] != regionIdx) { // try up first
					polygon.add(new PointInt(x, y-1));
					if(x > 0) oppRegions.add(regionData[x-1][y-1]);
					else oppRegions.add(-1);
					return true;
				}
				if(y == regionData[0].length || regionData[x-1][y] != regionIdx) { // try left
					polygon.add(new PointInt(x-1, y));
					if(y < regionData[0].length) oppRegions.add(regionData[x-1][y]);
					else oppRegions.add(-1);
					return true;
				} // else, must go down
				if(y < regionData[0].length) {
					polygon.add(new PointInt(x, y+1));
					oppRegions.add(regionData[x][y]);
					return true;
				}
			} else if(regionData[x][y] == regionIdx) {
				if(x == 0 || y < regionData[0].length && regionData[x-1][y] != regionIdx) { // try down first
					polygon.add(new PointInt(x, y+1));
					if(x > 0) oppRegions.add(regionData[x-1][y]);
					else oppRegions.add(-1);
					return true;
				}
				if(y == 0 || regionData[x-1][y-1] != regionIdx) { // try left
					polygon.add(new PointInt(x-1, y));
					if(y > 0) oppRegions.add(regionData[x-1][y-1]);
					else oppRegions.add(-1);
					return true;
				} // else, must go up
				if(y > 0) {
					polygon.add(new PointInt(x, y-1));
					oppRegions.add(regionData[x][y-1]);
					return true;
				}
			}
		}

		if(p.yInt() > p2.yInt()) { // from top
			if(x > 0 && regionData[x-1][y-1] == regionIdx) {
				if(y == regionData[0].length || regionData[x-1][y] != regionIdx) { // try left first
					polygon.add(new PointInt(x-1, y));
					if(y <  regionData[0].length) oppRegions.add(regionData[x-1][y]);
					else oppRegions.add(-1);
					return true;
				}
				if(x == regionData.length || y < regionData[0].length && regionData[x][y] != regionIdx) { // try down
					polygon.add(new PointInt(x, y+1));
					if(x < regionData.length) oppRegions.add(regionData[x][y]);
					else oppRegions.add(-1);
					return true;
				} // else, must go right
				if(x < regionData.length) {
					polygon.add(new PointInt(x+1, y));
					oppRegions.add(regionData[x][y-1]);
					return true;
				}
			} else if(y > 0 && regionData[x][y-1] == regionIdx) {
				if(y == regionData[0].length || x < regionData.length && regionData[x][y] != regionIdx) { // try right first
					polygon.add(new PointInt(x+1, y));
					if(y <  regionData[0].length) oppRegions.add(regionData[x][y]);
					else oppRegions.add(-1);
					return true;
				}
				if(x == 0 || regionData[x-1][y] != regionIdx) { // try down
					polygon.add(new PointInt(x, y+1));
					if(x > 0) oppRegions.add(regionData[x-1][y]);
					else oppRegions.add(-1);
					return true;
				} // else, must go left
				if(x > 0) {
					polygon.add(new PointInt(x-1, y));
					oppRegions.add(regionData[x-1][y-1]);
					return true;
				}
			}
		}

		if(p.yInt() < p2.yInt()) { // from bottom
			if(x > 0 && regionData[x-1][y] == regionIdx) {
				if(y == 0 || regionData[x-1][y-1] != regionIdx) { // try left first
					polygon.add(new PointInt(x-1, y));
					if(y > 0) oppRegions.add(regionData[x-1][y-1]);
					else oppRegions.add(-1);
					return true;
				}
				if(x == regionData.length || regionData[x][y-1] != regionIdx) { // try up
					polygon.add(new PointInt(x, y-1));
					if(x < regionData.length) oppRegions.add(regionData[x][y-1]);
					else oppRegions.add(-1);
					return true;
				} // else, must go right
				if(x < regionData.length) {
					polygon.add(new PointInt(x+1, y));
					oppRegions.add(regionData[x][y]);
					return true;
				}
			} else if(regionData[x][y] == regionIdx) {
				if(y == 0 || x < regionData.length && regionData[x][y-1] != regionIdx) { // try right first
					polygon.add(new PointInt(x+1, y));
					if(y > 0) oppRegions.add(regionData[x][y-1]);
					else oppRegions.add(-1);
					return true;
				}
				if(x == 0 || regionData[x-1][y-1] != regionIdx) { // try up
					polygon.add(new PointInt(x, y-1));
					if(x > 0) oppRegions.add(regionData[x-1][y-1]);
					else oppRegions.add(-1);
					return true;
				} // else, must go left
				if(x > 0 ) {
					polygon.add(new PointInt(x-1, y));
					oppRegions.add(regionData[x-1][y]);
					return true;
				}
			}
		}

		return false;
	}

	void processRegion(Region region, int[][] regionData, int x, int y) {
		// assumption we always search for fixed x, with y increasing
		List<Point> polygon = new ArrayList<Point>();
		List<Integer> oppRegions = new ArrayList<Integer>();
		polygon.add(new PointInt(x, y));
		polygon.add(new PointInt(x+1, y));
		if(y > 0) oppRegions.add(regionData[x][y-1]);
		else oppRegions.add(-1);

		boolean finished = false;
		while(!finished) {
			int n = polygon.size();
			finished = !updatePolygon(regionData,region.regionIdx,polygon,oppRegions,polygon.get(n-1),polygon.get(n-2));
			if(polygon.get(0).equals(polygon.get(polygon.size()-1))) {
				finished = true;
			}
		}

		polygon.remove(polygon.size()-1);

		region.polygon = polygon;
		region.opposingRegions = oppRegions;

		for(int i=0;i<oppRegions.size();i++) {
			if(i >= 0 && !region.outerNeighbors.contains(oppRegions.get(i))) region.outerNeighbors.add(oppRegions.get(i));
		}
	}

	void checkInnerNeighbor(Region region, int i) {
		if(i >= 0 && i != region.regionIdx && !region.outerNeighbors.contains(i) && !region.innerNeighbors.contains(i)) {
			region.innerNeighbors.add(i);
		}
	}

	List<Region> initRegions(int[][] mapData, int scale, double minSize, double minX, double minY, double maxX, double maxY, GlobalRegions global, SeamProfiles seams, String traceFolder, Trace trace) {
		System.out.println("start: create initial polygons");
		
		int margin = (int) Math.round(mapData.length * (maxX - minX) * 0.2);
		// margins: for accurate small region removal, we need to look outside the tile
		int minXint = (int) Math.round(minX * mapData.length);
		int minYint = (int) Math.round(minY * mapData[0].length);
		int maxXint = (int) Math.round(maxX * mapData.length);
		int maxYint = (int) Math.round(maxY * mapData[0].length);
		
		int minXintM = Math.max(0, minXint - margin);
		int minYintM = Math.max(0, minYint - margin);
		int maxXintM = Math.min(mapData.length, maxXint + margin);
		int maxYintM = Math.min(mapData[0].length, maxYint + margin);
		
		System.out.println(margin);
		System.out.println(minX+" "+minY+" "+maxX+" "+maxY);
		System.out.println(minXint+" "+minYint+" "+maxXint+" "+maxYint);
		System.out.println(minXintM+" "+minYintM+" "+maxXintM+" "+maxYintM);

		List<Region> regions = new ArrayList<Region>();
		// find contiguous regions to divide the map into polygons
		RegionResult regionResult = findRegions(mapData, minXintM, minYintM, maxXintM, maxYintM);
		
		int[][] croppedRegions = new int[maxXint - minXint][maxYint - minYint];
		int[][] croppedRecoloredRegions = new int[maxXint - minXint][maxYint - minYint];
		for(int x=0; x<croppedRegions.length; x++) {
			for(int y=0; y<croppedRegions[0].length; y++) {
				croppedRegions[x][y] = regionResult.regions[x + minXint][y + minYint];
				croppedRecoloredRegions[x][y] = regionResult.regions[x + minXint][y + minYint] + 0xFF000000;
			}
		}

		if(traceVisualRegions(trace)) visualizeRegion(croppedRegions, traceFolder+VISUAL_REGIONS_FILENAME);
		if(traceVisualRegions(trace)) FileOperator.writeImage(croppedRecoloredRegions, traceFolder+REGIONS_FILENAME);
		
		System.out.println("---");
		
		regionResult = MapOperator.removeSmallRegionsInRegionMap(regionResult, mapData, minSize, Math.max(1, 4 / scale), minX, minY, maxX, maxY, margin, global, traceFolder, trace);
		// after cropping (removing small regions uses a margin to ensure that adjacent tiles have similar borders), some 'regions' may no longer be contiguous, so we need to find regions again:
		croppedRegions = new int[maxXint - minXint][maxYint - minYint];
		for(int x=0; x<croppedRegions.length; x++) {
			for(int y=0; y<croppedRegions[0].length; y++) {
				croppedRegions[x][y] = regionResult.type[regionResult.regions[x][y]];
			}
		}
		// Small-region removal has had its say; adopt the neighbours' version of the shared edges
		// before the regions are traced, so both sides of a seam are outlining the same pixels.
		if(seams != null) seams.reconcile(croppedRegions, minXint, minYint, maxXint, maxYint);

		regionResult = findRegions(croppedRegions, 0, 0, regionResult.regions.length, regionResult.regions[0].length);
		
		if(traceVisualRegions(trace)) visualizeRegion(regionResult.regions, traceFolder+VISUAL_REGIONS_SMALL_REMOVED_FILENAME);

		for(int i=0;i<regionResult.numRegions;i++) {
			Region region = new Region(i);
			regions.add(region);
			region.setColorData(regionResult.type[i]);
		}
		boolean[] done = new boolean[regionResult.numRegions];
		
		System.out.println(regionResult.regions.length+" v "+(maxXint - minXint));
		System.out.println(regionResult.regions[0].length+" v "+(maxYint - minYint));

		for (int x = 0; x < maxXint - minXint; x++) {
			for (int y = 0; y < maxYint - minYint; y++) {
				Region region = regions.get(regionResult.regions[x][y]);

				if(!done[regionResult.regions[x][y]]) {
					processRegion(region, regionResult.regions, x, y);
					done[regionResult.regions[x][y]] = true;
				}

				if(x > 0) checkInnerNeighbor(region, regionResult.regions[x-1][y]);
				if(x < regionResult.regions.length - 1) checkInnerNeighbor(region, regionResult.regions[x+1][y]);
				if(y > 0) checkInnerNeighbor(region, regionResult.regions[x][y-1]);
				if(y < regionResult.regions[0].length - 1) checkInnerNeighbor(region, regionResult.regions[x][y+1]);
			}
		}

		System.out.println("done: create initial polygons");
		if(traceText(trace)) FileOperator.printRegionListToFile(regions, traceFolder + POLYGONS_BASE_FILENAME);

		return regions;
	}

	void addRandomNoise(List<Region> regions, long baseSeed, double v, int width, int height) {
		System.out.println("add random noise");

		Point[][] shiftedPoints = new Point[width+1][height+1];
		Random r = new Random(baseSeed);

		// initialize
		for(int i=regions.size()-1;i>=0;i--) {
			Region region = regions.get(i);
			for(Point p : region.polygon) {
				int x = p.xInt();
				int y = p.yInt();
				if(x > 0 && x < width && y > 0 && y < height) {
					shiftedPoints[x][y] = new PointFloat(p.xFloat() + v * r.nextGaussian(), p.yFloat() + v * r.nextGaussian());
				} else {
					shiftedPoints[x][y] = new PointFloat(p.xFloat(), p.yFloat());
				}
			}
		}

		// ensure that no polygons have intersecting lines
		int z = 0;
		boolean finished =  false;
		while(!finished) {
			z++;
			System.out.println("random noise step "+z);
			finished =  true;
			for(int i=regions.size()-1;i>=0;i--) {
				Region region = regions.get(i);
				List<Point> newPolygon = new ArrayList<Point>();
				for(Point p : region.polygon) {
					int x = p.xInt();
					int y = p.yInt();
					newPolygon.add(shiftedPoints[x][y]);
				}

				if(!GeometryUtils.isValidPolygon(newPolygon)) {
					for(Point p : region.polygon) {
						int x = p.xInt();
						int y = p.yInt();
						shiftedPoints[x][y] = new PointFloat(p.xFloat() + v * r.nextGaussian(), p.yFloat() + v * r.nextGaussian());
					}
					finished =  false;
				}
			}
		}

		// finalize
		for(int i=regions.size()-1;i>=0;i--) {
			Region region = regions.get(i);
			List<Point> newPolygon = new ArrayList<Point>();
			for(Point p : region.polygon) {
				int x = p.xInt();
				int y = p.yInt();
				newPolygon.add(shiftedPoints[x][y]);
			}
			region.polygon = newPolygon;
		}
	}

	List<Region> loadConnectedPolygons(String folder) {
		List<Region> regions = new ArrayList<Region>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(folder+"\\"+POLYGONS_PRUNED_FILENAME));

			String line;
			Region region = null;
			while ((line = reader.readLine()) != null) {
				String[] nameSplit = line.split("region ");
				if(nameSplit.length > 1) {
					if(region != null) regions.add(region);
					String[] idxSplit = nameSplit[1].split("\\, ");
					region = new Region(Integer.parseInt(idxSplit[0]));
					String[] colorSplit = idxSplit[2].split(" ");
					region.setColorData(Integer.parseInt(colorSplit[0]));
					if(idxSplit.length > 3) {
						String[] outerSplit = idxSplit[3].split(",");
						for(int i=0;i<outerSplit.length;i++) region.outerNeighbors.add(Integer.parseInt(outerSplit[i].replaceAll(" ", "").replaceAll("-", "")));
					}
					if(idxSplit.length > 4) {
						String[] innerSplit = idxSplit[4].split(",");
						for(int i=0;i<innerSplit.length;i++) region.innerNeighbors.add(Integer.parseInt(innerSplit[i].replaceAll(" ", "").replaceAll("-", "")));
					}
				} else {
					List<Point> points = new ArrayList<Point>();
					List<Integer> oppRegions = new ArrayList<Integer>();
					String[] coordSplit = line.split("\\, ");
					for(int i=0;i<coordSplit.length;i++) {
						String s = coordSplit[i].replace("(", "");
						s = s.replace(")", "");

						String[] intSplit = s.split("\\,");

						points.add(new PointInt(Integer.parseInt(intSplit[0]), Integer.parseInt(intSplit[1])));
						oppRegions.add(Integer.parseInt(intSplit[2]));
					}
					region.polygon = points;
					region.opposingRegions = oppRegions;
				}
			}

			regions.add(region);
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return regions;
	}

	List<Region> initAndPruneMap(int[][] mapData, int scale, double minSize, double minX, double minY, double maxX, double maxY, GlobalRegions global, SeamProfiles seams, String traceFolder, Trace trace) {
		List<Region> regions = initRegions(mapData, scale, minSize, minX, minY, maxX, maxY, global, seams, traceFolder, trace);
		System.out.println(getTotalnumPoints(regions));

		basicPrune(regions);
		System.out.println("done: basic prune polygons");
		System.out.println(getTotalnumPoints(regions));

		if(traceText(trace)) FileOperator.printRegionListToFile(regions, traceFolder+POLYGONS_PRUNED_FILENAME);

		return regions;
	}

	void processMap(int[][] mapData, String outputFolder, String traceFolder, int scale, double minRegionSize, double distortFactor, double maxDouglasPeuckerDist, double maxDouglasPeuckerSize, double maxRiverDPDist, int targetTrianglesPerGridCell, MapName name) {
		processMap(mapData, outputFolder, traceFolder, scale, minRegionSize, distortFactor, maxDouglasPeuckerDist, maxDouglasPeuckerSize, maxRiverDPDist, targetTrianglesPerGridCell, name, Trace.NONE);
	}

	void processMap(int[][] mapData, String outputFolder, String traceFolder, int scale, double minRegionSize, double distortFactor, double maxDouglasPeuckerDist, double maxDouglasPeuckerSize, double maxRiverDPDist, int targetTrianglesPerGridCell, MapName name, Trace trace) {
		processMap(mapData, outputFolder, traceFolder, 0., 0., 1., 1., scale, minRegionSize, distortFactor, maxDouglasPeuckerDist, maxDouglasPeuckerSize, maxRiverDPDist, targetTrianglesPerGridCell, null, null, name, trace);
	}

	void processMap(int[][] mapData, String outputFolder, String traceFolder, double minX, double minY, double maxX, double maxY, int scale, double minRegionSize, double distortFactor, double maxDouglasPeuckerDist, double maxDouglasPeuckerSize, double maxRiverDPDist, int targetTrianglesPerGridCell, GlobalRegions global, SeamProfiles seams, MapName name, Trace trace) {
		List<Region> regions = initAndPruneMap(mapData, scale, minRegionSize, minX, minY, maxX, maxY, global, seams, traceFolder, trace);
		int w = (int) Math.round(mapData.length * (maxX - minX));
		int h = (int) Math.round(mapData[0].length * (maxY - minY));

		//		List<Region> regions = loadConnectedPolygons(traceFolder);

		mergeRegionsAndSetDrawOrder(regions);
		System.out.println("done: merged regions");
		System.out.println(getTotalnumPoints(regions));

		if(traceText(trace)) FileOperator.printRegionListToFile(regions, traceFolder+POLYGONS_ORDERED_FILENAME);

		addRandomNoise(regions, 123456L, distortFactor * scale, w, h);
		System.out.println("done: added random noise");

		if(traceText(trace)) FileOperator.printRegionListToFile(regions, traceFolder+POLYGONS_DISTORTED_FILENAME, true);

		simplifyDouglasPeucker(regions, maxDouglasPeuckerDist, maxDouglasPeuckerSize, w, h); // lower means less smoothing
		System.out.println("done: simplify using Douglas-Peucker");
		System.out.println(getTotalnumPoints(regions));

		if(traceText(trace)) FileOperator.printRegionListToFile(regions, traceFolder+POLYGONS_SIMPLIFIED_FILENAME, true);

		addRiverData(regions, maxRiverDPDist, minX, minY, (int) Math.round(w/(maxX - minX)), (int) Math.round(h/(maxY - minY)), name, traceFolder, trace);
		System.out.println("done: added river data");
		System.out.println(getTotalnumPoints(regions));

		if(traceText(trace)) FileOperator.printRegionListToFile(regions, traceFolder+POLYGONS_FILTERED_FILENAME, true);
		if(traceVisualRegions(trace)) visualizeAllPolygons(regions, traceFolder+VISUAL_POLYGONS_FILENAME, w, h, 1);
		if(traceVisualRegions(trace)) visualizeAllPolygons(regions, traceFolder+VISUAL_POLYGONS_SCALED_FILENAME, scale * w, scale * h, scale);

		determineTriangleDrawOrders(regions, traceFolder, trace);
		System.out.println("done: determine triangle draw order");

		FileOperator.finalPrintPolygons(regions, outputFolder, minX, minY, maxX, maxY, w, h, targetTrianglesPerGridCell);
	}

	//	public void runSample(int scale, MapName name) {
	//		new PolygonCreator().processMap(mergeMapData(scale, name), OUTPUT_FOLDER_NAME+"\\Scale_"+scale,"_0_0", "", scale, 750, 0.025, 20, 10, 100000, name);
	//	}

	//	public void runZoomLevel1(MapName name) {
	//		int scale = 8;
	//		//		int w = 1;
	//		//		int h = 1;
	//		double minRegionSize = 1500;
	//		double distortFactor = 0.025;
	//		double maxDouglasPeuckerDist = 20;
	//		double maxDouglasPeuckerSize = 10;
	//		double maxRiverDPDist = 100000;
	//
	//		new PolygonCreator().processMap(mergeMapData(scale, name), OUTPUT_FOLDER_NAME+"\\Level_1\\","_0_0", "", scale, minRegionSize, distortFactor, maxDouglasPeuckerDist, maxDouglasPeuckerSize, maxRiverDPDist, name);
	//	}

	//	public void runZoomLevel2(MapName name) {
	//		int scale = 2;
	//		int w = 8;
	//		int h = 4;
	//		double minRegionSize = 400;
	//		double distortFactor = 0.025;
	//		double maxDouglasPeuckerDist = 10;
	//		double maxDouglasPeuckerSize = 5;
	//		double maxRiverDPDist = 50000;
	//
	//		for(int y=0;y<h;y++) {
	//			for(int x=0;x<w;x++) {
	//				System.out.println("--- starting section "+x+"_"+y);
	//				new PolygonCreator().processMap(mergeMapData(scale, 21600/w * x, 10800/h * y, 21600/w * (x+1), 10800/h * (y+1), name), OUTPUT_FOLDER_NAME+"\\Level_2","_"+x+"_"+y, "", 21600/scale, 10800/scale, scale, minRegionSize, distortFactor, maxDouglasPeuckerDist, maxDouglasPeuckerSize, maxRiverDPDist, name);
	//			}
	//		}
	//	}

	//	public void runZoomLevel3(MapName name) {
	//		int scale = 4;
	//		int w = 8;
	//		int h = 4;
	//		double minRegionSize = 400;
	//		double distortFactor = 0.025;
	//		double maxDouglasPeuckerDist = 10;
	//		double maxDouglasPeuckerSize = 5;
	//		double maxRiverDPDist = 50000;
	//
	//		for(int y=0;y<h;y++) {
	//			for(int x=0;x<w;x++) {
	//				System.out.println("--- starting section "+x+"_"+y);
	//				new PolygonCreator().processMap(mergeMapData(scale, 21600/w * x, 10800/h * y, 21600/w * (x+1), 10800/h * (y+1)), OUTPUT_FOLDER_NAME+"\\Level_2","_"+x+"_"+y, 21600/scale, 10800/scale, scale, minRegionSize, distortFactor, maxDouglasPeuckerDist, maxDouglasPeuckerSize, maxRiverDPDist);
	//			}
	//		}
	//	}

	//	public void runAll(MapName name) {
	//		int scale, w, h; // w number of longitude regions, h number of latitude regions 
	//		// zoom level 1
	//		scale = 8;
	//		new PolygonCreator().processMap(mergeMapData(scale), OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Triangles_0_0.txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Edges_0_0.txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Points_0_0.txt", scale);
	//
	//		// zoom level 2
	//		scale = 4;
	//		w = 8;
	//		h = 4;
	//		for(int y=0;y<h;y++) {
	//			for(int x=0;x<w;x++) {
	//				new PolygonCreator().processMap(mergeMapData(scale, 21600/w * x, 10800/h * y, 21600/w * (x+1), 10800/h * (y+1)), OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Triangles_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Edges_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Points_"+x+"_"+y+".txt", scale);
	//			}
	//		}
	//
	//		// zoom level 3
	//		scale = 2;
	//		w = 16;
	//		h = 8;
	//		for(int y=0;y<h;y++) {
	//			for(int x=0;x<w;x++) {
	//				new PolygonCreator().processMap(mergeMapData(scale, 21600/w * x, 10800/h * y, 21600/w * (x+1), 10800/h * (y+1)), OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Triangles_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Edges_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Points_"+x+"_"+y+".txt", scale);
	//			}
	//		}
	//
	//		// zoom level 4
	//		scale = 1;
	//		w = 32;
	//		h = 16;
	//		for(int y=0;y<h;y++) {
	//			for(int x=0;x<w;x++) {
	//				new PolygonCreator().processMap(mergeMapData(scale, 21600/w * x, 10800/h * y, 21600/w * (x+1), 10800/h * (y+1)), OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Triangles_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Edges_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Points_"+x+"_"+y+".txt", scale);
	//			}
	//		}
	//	}

	public void run(
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
			Trace trace
			) {
		FileOperator.writeParameterSpecFile(getPolygonFolderName(name)+"\\Level_"+level+"\\", name, scale, minRegionSize, distortFactor, maxDouglasPeuckerDist, maxDouglasPeuckerSize, maxRiverDPDist, targetTrianglesPerGridCell, w, h, level, trace);

		// Built once for the level rather than once per tile. It was identical every time anyway --
		// mergeMapData() takes no tile arguments -- and the two steps below both depend on every tile
		// working from the same map, which is the whole point of them.
		int[][] mapData = mergeMapData(scale, name);
		MapOperator.makeTileSeamsCanonical(mapData, w, h);
		GlobalRegions global = GlobalRegions.find(mapData);
		SeamProfiles seams = new SeamProfiles();

		for(int y=0;y<h;y++) {
			for(int x=0;x<w;x++) {
				String polygonFolder = getPolygonFolderName(name)+"\\Level_"+level+"\\"+x+"_"+y+"\\";
				String traceFolder = getTraceFolderName(name)+"\\Level_"+level+"\\"+x+"_"+y+"\\";
				new PolygonCreator().processMap(mapData, polygonFolder, traceFolder, 1. * x / w, 1. * y / h, 1. * (x+1) / w, 1. * (y + 1) / h, scale, minRegionSize, distortFactor, maxDouglasPeuckerDist, maxDouglasPeuckerSize, maxRiverDPDist, targetTrianglesPerGridCell, global, seams, name, trace);
			}
		}

		// Consolidate this level's per-tile text output into the single binary file the game loads.
		FileOperator.writeLevelBinary(getPolygonFolderName(name)+"\\Level_"+level+"\\", w, h);
		//		processMap(mergeMapData(scale, name), getPolygonFolderName(name)+"\\Level_1\\0_0\\", getTraceFolderName(name)+"\\Level_1\\0_0\\", scale, minRegionSize, distortFactor, maxDouglasPeuckerDist, maxDouglasPeuckerSize, maxRiverDPDist, name);
	}

	public void runAll() {
		//		int scale; 
		//		double minRegionSize, 
		//		distortFactor, 
		//		maxDouglasPeuckerDist, 
		//		maxDouglasPeuckerSize, 
		//		maxRiverDPDist; 
		//		MapName name;

		// comment out as appropriate

		// Default target average triangles per spatial-grid cell (see FileOperator.writeSpatialGrid).
		int gridDensity = 12;

		// Earth, 1CE, zoom level 1, 2
//		run(MapName.EARTH_1_CE, 4, 1500, 0.05, 20, 10, 200000, gridDensity, 1, 1, 1, Trace.VISUAL_REGIONS);

//		// zoom level 2
//		run(MapName.EARTH_1_CE, 2, 400, 0.025, 10, 5, 100000, gridDensity, 8, 4, 2, Trace.VISUAL_REGIONS);

//		// zoom level 3
//		run(MapName.EARTH_1_CE, 2, 100, 0.0125, 5, 2.5, 50000, gridDensity, 16, 8, 3, Trace.VISUAL_REGIONS);

		// zoom level 4
		run(MapName.EARTH_1_CE, 2, 25, 0.0625, 2.5, 1.25, 25000, gridDensity, 32, 16, 4, Trace.VISUAL_REGIONS);

		// zoom level 5
		run(MapName.EARTH_1_CE, 2, 5, 0.025, 1.25, 0.5, 12500, gridDensity, 64, 32, 5, Trace.VISUAL_REGIONS);

		// Earth, 16000BCE, zoom level 1
//		run(MapName.EARTH_16K_BCE, 4, 1500, 0.05, 20, 10, 200000, gridDensity, 1, 1, 1, Trace.VISUAL_REGIONS);

		// zoom level 2
//		run(MapName.EARTH_16K_BCE, 2, 400, 0.025, 10, 5, 100000, gridDensity, 8, 4, 2, Trace.VISUAL_REGIONS);

		// zoom level 3
//		run(MapName.EARTH_16K_BCE, 2, 100, 0.0125, 5, 2.5, 50000, gridDensity, 16, 8, 3, Trace.VISUAL_REGIONS);

		// Nirn (the Elder Scrolls), zoom level 1
//		run(MapName.TES_NIRN, 4, 1500, 0.05, 20, 10, 200000, gridDensity, 1, 1, 1, Trace.VISUAL_REGIONS);
//		run(MapName.TES_NIRN, 2, 400, 0.025, 10, 5, 100000, gridDensity, 8, 4, 2, Trace.VISUAL_REGIONS);
//		run(MapName.TES_NIRN, 2, 100, 0.0125, 5, 2.5, 50000, gridDensity, 16, 8, 3, Trace.VISUAL_REGIONS);

		// Final Fantasy 6 (3) Overworld, zoom level 1
		//		processMap(mergeMapData(4, MapName.FF6_OVERWORLD), getPolygonFolderName(MapName.FF6_OVERWORLD)+"\\Level_1\\0_0\\", getTraceFolderName(MapName.FF6_OVERWORLD)+"\\Level_1\\0_0\\", 8, 1500, 0.025, 20, 10, 100000, MapName.FF6_OVERWORLD, true);
	}

	public static void main(String[] args) {	
		new PolygonCreator().runAll();

		//						new PolygonCreator().runSample(8);
		//						new PolygonCreator().runSample(4);
		//								new PolygonCreator().runSample(2);
		//		new PolygonCreator().runSample(1);
		//		new PolygonCreator().runZoomLevel1(MapName.earth_1_ce);
		//		new PolygonCreator().runZoomLevel2();
	}
}
