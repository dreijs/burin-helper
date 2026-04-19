package vectormaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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


public class PolygonCreator {

	public static final String OUTPUT_FOLDER_NAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\";
	public static final String POLYGONS_ALL_FOLDER_NAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\polygons_visual_all\\";
	public static final String POLYGONS_ERROR_FOLDER_NAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\polygons_visual_errors\\";
	public static final String POLYGONS_NEW_CUT_FOLDER_NAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\polygons_visual_new_cut\\";
	public static final String POLYGONS_NEW_UNCUT_FOLDER_NAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\polygons_visual_new_uncut\\";
	public static final String POLYGONS_NEW_SPLIT_FOLDER_NAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\polygons_visual_new_split\\";

	public static final String POLYGONS_BASE_FILENAME = OUTPUT_FOLDER_NAME + "polygons_base.txt";
	public static final String POLYGONS_PRUNED_FILENAME = OUTPUT_FOLDER_NAME + "polygons_pruned.txt";
	public static final String POLYGONS_ORDERED_FILENAME = OUTPUT_FOLDER_NAME + "polygons_ordered.txt";
	public static final String POLYGONS_FILTERED_FILENAME = OUTPUT_FOLDER_NAME + "polygons_filtered.txt";

	public static final String REGIONS_FILENAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\regions.png";
	public static final String VISUAL_REGIONS_FILENAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\regions_visual.png";
	public static final String VISUAL_REGIONS_SMALL_REMOVED_FILENAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\regions_visual_small_removed.png";
	public static final String VISUAL_POLYGONS_FILENAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\region_polygons.png";
	public static final String VISUAL_POLYGONS_SCALED_FILENAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\region_polygons_scaled.png";

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

	void visualizePolygon(List<Point> p, String fileName, int scale) {
		List<List<Point>> ps = new ArrayList<List<Point>>();
		ps.add(p);
		visualizePolygons(ps, fileName, scale);
	}

	void visualizePolygons(List<List<Point>> ps, String fileName, int scale) {
		Rectangle2D.Double bb = GeometryUtils.getFloatBoundingBoxFromLists(ps);
		int w = (int) (Math.ceil(scale * bb.width)) + 3; // + 2 to create white margin at edge
		int h = (int) (Math.ceil(scale * bb.height)) + 3;

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

	RegionResult findRegions(int[][] mapData) {
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

			for (int x = 0; x < mapData.length; x++) {
				for (int y = 0; y < mapData[x].length; y++) {

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
		for (int x = 0; x < mapData.length; x++) {
			for (int y = 0; y < mapData[x].length; y++) {
				type[regionData[x][y]] = mapData[x][y];
			}
		}

		System.out.println("original num regions "+cRegion);

		RegionResult result = MapOperator.cleanRegionIndices(new RegionResult(regionData, type, cRegion));

		visualizeRegion(regionData, VISUAL_REGIONS_FILENAME);
		FileOperator.writeImage(regionData, REGIONS_FILENAME);

		System.out.println((result.numRegions)+" regions");
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

	void simplifyDouglasPeucker(List<Region> regions, double maxDist, double maxSize) {
		for(int i=regions.size()-1;i>=0;i--) {
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
						simplifyRecursive(regions, i, 0, n - 1, maxDist, maxSize, prevSize);
					} else {
						for(int j=0;j<segments.size();j++) {
							int jj = (j+1)%segments.size();
							simplifyRecursive(regions, i, segments.get(j)%n, segments.get(jj) - 1, maxDist, maxSize, prevSize);
						}
					}
				}
			}
		}
	}

	private static void simplifyRecursive(List<Region> regions, int idx, int start, int end, double maxDist, double maxSize, int prevSize) {
		Region region = regions.get(idx);
		int n = region.polygon.size();


		if(n != prevSize) return;

		PointInt pStart = region.polygon.get(start).asIntPoint();
		PointInt pEnd = region.polygon.get((end+1)%n).asIntPoint();

		if(end >= start && end - start < 1) return;
		if(start > end && end - start + n < 1) return;

		double dMax = -1;
		int index = 0;

		boolean valid = true;
		for (int i = (start+1)%n; i != end; i=(i+1)%n) {
			double distance = GeometryUtils.perpendicularDistance(region.polygon.get(i), pStart, pEnd);
			if (distance > dMax) {
				dMax = distance;
				index = i;
			}
		}

		if(dMax == -1) return;
		if(dMax > maxDist) valid = false;

		// check if making the cut would not lead to intersections in this polygon
		int oppRegion = region.opposingRegions.get(start);
		if(valid) {
			valid = region.canSimplifySegment(pStart, pEnd, oppRegion);
		}
		if(valid && oppRegion >= 0) {
			valid = regions.get(oppRegion).canSimplifySegment(pStart, pEnd, idx);
		}

		List<Point> allPoints = new ArrayList<Point>();
		allPoints.add(region.polygon.get(start));
		for (int i = (start+1)%n; i != end%n; i = (i+1)%n) {
			allPoints.add(region.polygon.get(i));
		}
		allPoints.add(region.polygon.get(end));

		double cutSize = GeometryUtils.polygonArea(allPoints);
		if(cutSize > maxSize) valid = false;

		// check if making the cut would not lead to intersections in this polygon
		if(valid) {
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
			simplifyRecursive(regions, idx, start, index, maxSize, maxDist, prevSize);
			simplifyRecursive(regions, idx, index, end, maxSize, maxDist, prevSize);
		} else {
			int[] indices1 = region.getSegmentIndices(pStart, pEnd, oppRegion);
			if(oppRegion >= 0) {
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

	void addRiverData(List<Region> regions, double d, int w, int h) {
		Map<String,List<List<Point>>> pointMap = RiverProcessor.simplifyRiverData(d);
		RiverProcessor.convertRiverData(pointMap, w, h);

		FileOperator.clearFolder(POLYGONS_NEW_CUT_FOLDER_NAME);
		FileOperator.clearFolder(POLYGONS_NEW_UNCUT_FOLDER_NAME);
		FileOperator.clearFolder(POLYGONS_NEW_SPLIT_FOLDER_NAME);

		Map<String, Rectangle> boundingBoxes = new HashMap<String, Rectangle>();
		Map<String,Integer> riverIdxMap = new TreeMap<String,Integer>();

		for(String s : pointMap.keySet()) {
			boundingBoxes.put(s, GeometryUtils.getIntegerBoundingBoxFromLists(pointMap.get(s)));
			riverIdxMap.put(s,riverIdxMap.keySet().size());
		}

		for(int i=regions.size()-1;i>=0;i--) {
			int c = 0;
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

			List<List<Point>> newPolygons = GeometryUtils.splitPolygon(region.polygon, intersectingRiverData, i);

			if(newPolygons.size() > 1) {
				visualizePolygons(newPolygons, POLYGONS_NEW_CUT_FOLDER_NAME+"polygon_"+i+"_"+c+".png", 32);
				List<Region> newRegions = new ArrayList<Region>();
				for(int j=0;j<newPolygons.size();j++) {
					int cc = j > 0 ? regions.size() : i;
					visualizePolygon(newPolygons.get(j), POLYGONS_NEW_SPLIT_FOLDER_NAME+"polygon_"+cc+"_"+c+".png", 32);
					
					Region newRegion = region.splitFromPolygon(newPolygons.get(j), intersectingRiverData, riverIndices, cc);
					if(j == 0) {regions.set(i, newRegion);}
					else {regions.add(newRegion);}
					newRegions.add(newRegion);
					
					List<Integer> newOpps = new ArrayList<Integer>();
					for(int k=0;k<newRegion.polygon.size();k++) newOpps.add(cc);
					for(int k=0;k<j;k++) {
						Region otherRegion = newRegions.get(k);
						otherRegion.matchNeighbors(otherRegion.polygon, region.polygon, otherRegion.opposingRegions, region.opposingRegions, false);
					}
				}
			} else if(newPolygons.get(0).size() > region.polygon.size()) {
				visualizePolygon(newPolygons.get(0), POLYGONS_NEW_UNCUT_FOLDER_NAME+"polygon_"+i+"_"+c+".png", 32);
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
			System.out.println(drawOrder);
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

	byte[] determineTriangleDrawOrder(List<Point> polygon, int regId) {		
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

		visualizePolygon(polygon, POLYGONS_ALL_FOLDER_NAME+"polygon_"+regId+".png", 4);
		if(faulty) visualizePolygon(polygon, POLYGONS_ERROR_FOLDER_NAME+"polygon_"+regId+".png", 32);

		while (vertices.size() > 2) {

			// determine if order is clockwise or counterclockwise
			boolean clockwise = GeometryUtils.calculatePolygonSignedArea(vertices) < 0;

			for (int i=vertices.size()-1;i>=0 && vertices.size() > 2;i--) {

				int prev = (i == 0) ? vertices.size() - 1 : i - 1;
				int next = (i == vertices.size() - 1) ? 0 : i + 1;

				Point vPrev = vertices.get(prev);
				Point vCurr = vertices.get(i);
				Point vNext = vertices.get(next);

				List<Point> l = new ArrayList<Point>();
				l.add(vPrev); l.add(vCurr); l.add(vNext);

				// Check for convexity and if the triangle formed is an "ear"
				// (i.e., no other polygon vertices are inside the triangle)

				if (GeometryUtils.isEar(vPrev, vCurr, vNext, vertices, clockwise)) {
					setBit(order, idx, true);
					vertices.remove(i);
				} else {
					setBit(order, idx, false);
				}
				idx++;
				if(idx >= order.length*8) {
					System.out.println(regId+" "+vertices+" "+GeometryUtils.calculatePolygonArea(l));
					order = extend(order);
					printShapeInt(polygon);
					printShapeInt(vertices);
				}
			}
		}

		byte[] result = new byte[(int) (idx/8) + 1];
		for(int i=0;i<result.length;i++) result[i] = order[i];
		return result;
	}

	void determineTriangleDrawOrders(List<Region> regions) {		
		FileOperator.clearFolder(POLYGONS_ALL_FOLDER_NAME);
		FileOperator.clearFolder(POLYGONS_ERROR_FOLDER_NAME);
		for(int i=regions.size()-1;i>=0;i--) {
			if((i % 10000) == 0) System.out.println("region "+i+"/"+regions.size());
			Region region = regions.get(i);
			if(region.polygon.size() > 0) {
				List<Point> polygon = region.polygon;
				byte[] triangleDrawOrder = determineTriangleDrawOrder(polygon, i);
				region.setTriangleDrawOrder(triangleDrawOrder);
			}
		}
	}

	static int[][] initMapData(String inputFileName, int s, int minX, int minY, int maxX, int maxY) {
		int[][] mapData = null;

		try{
			mapData = FileOperator.readImage(inputFileName, s, minX, minY, maxX, maxY);
			//			mapData = MapOperator.removeOrExpandLonePixels(mapData);
			System.out.println("done reading");
		} catch(Exception e){
			System.out.println(e.getMessage());
		}

		return mapData;
	}

	static int[][] mergeMapData(int s) {
		return mergeMapData(s, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	static int[][] mergeMapData(int s, int minX, int minY, int maxX, int maxY) {
		String[] inputFileNames = new String[] {
				ElevationMapCreator.ELEVATION_MAP_OUTPUT_FILENAME,
				BiomeMapCreator.BIOME_FINAL_RESCALED_MAP_FILENAME,
				SoilMapCreator.SOIL_FINAL_RESCALED_MAP_FILENAME,
				FeatureMapCreator.FEATURES_RAW_OUTPUT_FILENAME
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
			data[i] = initMapData(inputFileNames[i], s, minX, minY, maxX, maxY);
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
					if(!mappings.get(k).containsKey(data[k][x][y])) System.out.println("error in mergeMapData!!! " +x+" "+y+" "+data[k][x][y]+" "+Arrays.toString(Colors.intToARGBArray(data[k][x][y]))+" "+k);
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

	List<Region> initRegions(int[][] mapData, int scale, double minSize) {
		System.out.println("start: create initial polygons");

		List<Region> regions = new ArrayList<Region>();
		RegionResult regionResult = findRegions(mapData);
		regionResult = MapOperator.removeSmallRegionsInRegionMap(regionResult, mapData, minSize, Math.max(1, 4 / scale));
		visualizeRegion(regionResult.regions, VISUAL_REGIONS_SMALL_REMOVED_FILENAME);

		for(int i=0;i<regionResult.numRegions;i++) {
			Region region = new Region(i);
			regions.add(region);
			region.setColorData(regionResult.type[i]);
		}
		boolean[] done = new boolean[regionResult.numRegions];

		for (int x = 0; x < mapData.length; x++) {
			for (int y = 0; y < mapData[x].length; y++) {
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
		FileOperator.printRegionListToFile(regions, POLYGONS_BASE_FILENAME);

		return regions;
	}

	void addRandomNoise(List<Region> regions, long baseSeed, double v, int width, int height) {
		for(int i=regions.size()-1;i>=0;i--) {
			Region region = regions.get(i);
			List<Point> newPolygon = new ArrayList<Point>();

			for(Point p : region.polygon) {
				if(p.xInt() > 0 && p.xInt() < width && p.yInt() > 0 && p.yInt() < height) {
					Random r = new Random(baseSeed + p.xInt() + p.yInt());
					newPolygon.add(new PointFloat(p.xFloat() + v * r.nextGaussian(), p.yFloat() + v * r.nextGaussian()));
				} else {
					newPolygon.add(new PointFloat(p.xFloat(), p.yFloat()));

				}
			}

			region.polygon = newPolygon;
		}
	}

	List<Region> loadConnectedPolygons() {
		List<Region> regions = new ArrayList<Region>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(POLYGONS_PRUNED_FILENAME));

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

	void initAndPruneMap(int[][] mapData, int scale, double minSize) {
		List<Region> regions = initRegions(mapData, scale, minSize);
		System.out.println(getTotalnumPoints(regions));

		basicPrune(regions);
		System.out.println("done: basic prune polygons");
		System.out.println(getTotalnumPoints(regions));

		FileOperator.printRegionListToFile(regions, POLYGONS_PRUNED_FILENAME);
	}

	void processMap(int[][] mapData, String triangleFileName, String edgeFileName, String pointFileName, int scale) {
		initAndPruneMap(mapData, scale, 750);

		List<Region> regions = loadConnectedPolygons();

		mergeRegionsAndSetDrawOrder(regions);
		System.out.println("done: merged regions");
		System.out.println(getTotalnumPoints(regions));

		FileOperator.printRegionListToFile(regions, POLYGONS_ORDERED_FILENAME);

		addRandomNoise(regions, 123456L, 0.1 * 8 / scale, mapData.length, mapData[0].length);
		System.out.println("done: added random noise");

		simplifyDouglasPeucker(regions, 20, 10); // lower means less smoothing
		System.out.println("done: simplify using Douglas-Peucker");
		System.out.println(getTotalnumPoints(regions));

		addRiverData(regions, 100000, mapData.length, mapData[0].length);
		System.out.println("done: added river data");
		System.out.println(getTotalnumPoints(regions));

		FileOperator.printRegionListToFile(regions, POLYGONS_FILTERED_FILENAME);
		visualizeAllPolygons(regions, VISUAL_POLYGONS_FILENAME, mapData.length, mapData[0].length, 1);
		visualizeAllPolygons(regions, VISUAL_POLYGONS_SCALED_FILENAME, scale * mapData.length, scale * mapData[0].length, scale);

		determineTriangleDrawOrders(regions);
		System.out.println("done: determine triangle draw order");

		FileOperator.finalPrintPolygons(regions, triangleFileName, edgeFileName, pointFileName, mapData.length, mapData[0].length);
	}

	public void runSample(int scale) {
		new PolygonCreator().processMap(mergeMapData(scale), OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Triangles_0_0.txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Edges_0_0.txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Points_0_0.txt", scale);
	}

	public void runAll() {
		int scale, w, h; // w number of longitude regions, h number of latitude regions 
		// zoom level 1
		scale = 8;
		new PolygonCreator().processMap(mergeMapData(scale), OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Triangles_0_0.txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Edges_0_0.txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Points_0_0.txt", scale);

		// zoom level 2
		scale = 4;
		w = 8;
		h = 4;
		for(int y=0;y<h;y++) {
			for(int x=0;x<w;x++) {
				new PolygonCreator().processMap(mergeMapData(scale, 21600/w * x, 10800/h * y, 21600/w * (x+1), 10800/h * (y+1)), OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Triangles_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Edges_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Points_"+x+"_"+y+".txt", scale);
			}
		}

		// zoom level 3
		scale = 2;
		w = 16;
		h = 8;
		for(int y=0;y<h;y++) {
			for(int x=0;x<w;x++) {
				new PolygonCreator().processMap(mergeMapData(scale, 21600/w * x, 10800/h * y, 21600/w * (x+1), 10800/h * (y+1)), OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Triangles_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Edges_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Points_"+x+"_"+y+".txt", scale);
			}
		}

		// zoom level 4
		scale = 1;
		w = 32;
		h = 16;
		for(int y=0;y<h;y++) {
			for(int x=0;x<w;x++) {
				new PolygonCreator().processMap(mergeMapData(scale, 21600/w * x, 10800/h * y, 21600/w * (x+1), 10800/h * (y+1)), OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Triangles_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Edges_"+x+"_"+y+".txt", OUTPUT_FOLDER_NAME+"\\Scale_"+scale+"\\Points_"+x+"_"+y+".txt", scale);
			}
		}
	}

	public static void main(String[] args) {		
		new PolygonCreator().runSample(8);
		//				new PolygonCreator().runSample(4);
		//		new PolygonCreator().runSample(2);
		//				new PolygonCreator().runSample(1);
		//		new PolygonCreator().runAll();
	}
}
