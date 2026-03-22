package vectormaps;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import util.*;


public class PolygonCreator {

	public static final String OUTPUT_FOLDER_NAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\";

	public static final String POLYGONS_BASE_FILENAME = OUTPUT_FOLDER_NAME + "polygons_base.txt";
	public static final String POLYGONS_PRUNED_FILENAME = OUTPUT_FOLDER_NAME + "polygons_pruned.txt";
	public static final String POLYGONS_ORDERED_FILENAME = OUTPUT_FOLDER_NAME + "polygons_ordered.txt";
	public static final String POLYGONS_FILTERED_FILENAME = OUTPUT_FOLDER_NAME + "polygons_filtered.txt";

	public static final String REGIONS_FILENAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\regions.png";
	public static final String VISUAL_REGIONS_FILENAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\regions_visual.png";
	public static final String VISUAL_REGIONS_SMALL_REMOVED_FILENAME = System.getProperty("user.dir")+"\\output\\map\\polygons\\regions_visual_small_removed.png";

	void visualizeRegion(int[][] regions, String filename) {
		visualizeRegion(regions, filename, 32);
	}

	void visualizeRegion(int[][] regions, String filename, int stepsize) {
		int[][] regionC = new int[regions.length][];
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

	void addIfNew(List<Point> list, Point p) {
		for(Point pp : list) if(pp.equals(p)) return;
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
				Point p0 = polygon.get(Math.floorMod(j, n));
				Point p1 = polygon.get(Math.floorMod(j-1, n));
				Point p2 = polygon.get(Math.floorMod(j-2, n));

				int i1 = idxs.get(Math.floorMod(j-1, n));
				int i2 = idxs.get(Math.floorMod(j-2, n));

				if(p0.x == p1.x && p0.x == p2.x && i1 == i2 || p0.y == p1.y && p0.y == p2.y && i1 == i2) {
					polygon.remove(Math.floorMod(j-1, n));
					idxs.remove(Math.floorMod(j-1, n));
				}
			}
		}
	}

	//	void filterSmallRegions(List<Region> regions, int minSize, int w, int h) {
	//		System.out.println("min. region size: "+minSize);
	//
	//		for(int i=regions.size()-1;i>=0;i--) {
	//			Region region = regions.get(i);
	//
	//			if(region.polygon.size() > 0 && Geometry.calculatePolygonAreaGlobe(region.polygon, w, h) <= minSize) {
	//				if(region.outerNeighbors.size() == 1) {
	//					boolean canRemove = regions.get(region.outerNeighbors.get(0)).canRemoveRegion(i);
	//					if(canRemove) {
	//						regions.get(region.outerNeighbors.get(0)).removeRegion(i, -1);
	//						regions.get(i).clear();
	//					}
	//				} 
	//				else if(region.outerNeighbors.size() == 2) {
	//					if(region.outerNeighbors.get(0) == -1) {
	//						Region region2 = regions.get(region.outerNeighbors.get(1));
	//						if(region2.canRemoveRegion(i)) {
	//							region2.removeRegion(i, region.outerNeighbors.get(0));
	//							regions.get(i).clear();
	//						}
	//					} else if(region.outerNeighbors.get(1) == -1) {
	//						Region region1 = regions.get(region.outerNeighbors.get(0));
	//						if(region1.canRemoveRegion(i)) {
	//							region1.removeRegion(i, region.outerNeighbors.get(1));
	//							regions.get(i).clear();
	//						}
	//					} else {
	//						Region region1 = regions.get(region.outerNeighbors.get(0));
	//						Region region2 = regions.get(region.outerNeighbors.get(1));
	//						if(region1.canRemoveRegion(i) && region2.canRemoveRegion(i)) {
	//							region1.removeRegion(i, region.outerNeighbors.get(1));
	//							region2.removeRegion(i, region.outerNeighbors.get(0));
	//							regions.get(i).clear();
	//						}
	//					}
	//				}
	//			}
	//		}
	//	}

	void printShape(List<Point> polygon) {
		for(int j=0;j<polygon.size();j++) {
			if(j>0) System.out.print(",");
			System.out.print(polygon.get(j).x);
		}
		System.out.println();
		for(int j=0;j<polygon.size();j++) {
			if(j>0) System.out.print(",");
			System.out.print(polygon.get(j).y);
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
//		Region region = regions.get(idx);
//		int n = region.polygon.size();
//
////		if(idx == 47790) {
////			System.out.println(region);
////			System.out.println(Geometry.isNonIntersectingPolygon(region.polygon));
////		}
//		
//		if(n != prevSize) return;
//		
////		System.out.println(region+" "+n+" "+start+" "+end);
//
//		Point pStart = region.polygon.get(start);
//		Point pEnd = region.polygon.get((end+1)%n);
//
//		if(end >= start && end - start < 1) return;
//		if(start > end && end - start + n < 1) return;
//
//		double dMax = -1;
//		int index = 0;
//
//		boolean valid = true;
//		for (int i = (start+1)%n; i != end; i=(i+1)%n) {
//			double distance = Geometry.perpendicularDistance(region.polygon.get(i), pStart, pEnd);
//			if (distance > dMax) {
//				dMax = distance;
//				index = i;
//			}
//		}
//
//		if(dMax == -1) return;
//		if(dMax > maxDist) valid = false;
//
//		List<Point> points = new ArrayList<Point>();
//		points.add(region.polygon.get(start));
//		for (int i = (start+1)%n; i != end%n; i = (i+1)%n) {
//			points.add(region.polygon.get(i));
//		}
//		points.add(region.polygon.get(end));
//		double cutSize = Geometry.calculatePolygonArea(points);
//		if(cutSize > maxSize) valid = false;
//
//		int oppRegion = region.opposingRegions.get(start);
//		// check if making the cut would not lead to intersections in this polygon
//		if(valid) {
//			valid = region.canSimplifySegment(pStart, pEnd, oppRegion);
//			if(oppRegion >= 0) {
//				valid = regions.get(oppRegion).canSimplifySegment(pStart, pEnd, idx);
//			}
//			if(valid) {
//				double totSize = Geometry.calculatePolygonArea(region.polygon);
//				if(cutSize / totSize > 0.05) {
//					valid = false;
//				}
//				if(oppRegion >= 0) {
//					cutSize = 0;
//					totSize = Geometry.calculatePolygonArea(regions.get(oppRegion).polygon);				
//					if(cutSize / totSize > 0.05) {
//						valid = false;
//					}
//				}
//			}
//		}
//
//		if (!valid) {
//			simplifyRecursive(regions, idx, start, index, maxDist, maxSize, prevSize);
//			simplifyRecursive(regions, idx, index, end, maxDist, maxSize, prevSize);
//		} else {
//			int[] indices1 = region.getSegmentIndices(pStart, pEnd, oppRegion);
//			if(oppRegion >= 0) {
//				Point[] points2 = region.getSegmentPoints(indices1, oppRegion);
//				int[] indices2 = regions.get(oppRegion).getSegmentIndices(points2);
//				if(indices2 != null) {
//					region.removeSegment(indices1, oppRegion);
//					regions.get(oppRegion).removeSegment(indices2, idx);
//				}
//			} else {
//				region.removeSegment(indices1, oppRegion);
//			}
//			
//			if(!Geometry.isNonIntersectingPolygon(region.polygon)) System.out.println("Error!!! "+idx);
//		}

		
		Region region = regions.get(idx);
		int n = region.polygon.size();
		

		if(n != prevSize) return;

		Point pStart = region.polygon.get(start);
		Point pEnd = region.polygon.get((end+1)%n);

		if(end >= start && end - start < 1) return;
		if(start > end && end - start + n < 1) return;

		//		System.out.println(start+", "+end+" "+n);

		double dMax = -1;
		int index = 0;

		boolean valid = true;
		for (int i = (start+1)%n; i != end; i=(i+1)%n) {
			double distance = Geometry.perpendicularDistance(region.polygon.get(i), pStart, pEnd);
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
		
		double cutSize = Geometry.polygonArea(allPoints);
		if(cutSize > maxSize) valid = false;
		
//		if(idx == 46973) {
//			System.out.println(region);
//			System.out.println(Geometry.isNonIntersectingPolygon(region.polygon));
//			System.out.println(cutSize);
//			System.out.println(Geometry.polygonArea(region.polygon));
//			if(oppRegion >= 0) System.out.println(Geometry.polygonArea(regions.get(oppRegion).polygon));
//		}
		
		// check if making the cut would not lead to intersections in this polygon
		if(valid) {
			double totSize = Geometry.polygonArea(region.polygon);
			if(cutSize / totSize > 0.05) {
				valid = false;
			}
			if(oppRegion >= 0) {
				totSize = Geometry.polygonArea(regions.get(oppRegion).polygon);				
				if(cutSize / totSize > 0.05) {
					valid = false;
				}
			}
		}

		// don't make cuts that would remove too much of a region
//		if(valid) {
//			double totSize = Geometry.calculatePolygonArea(region.polygon);
//			double cutSize = 0;
//			for (int i = (start+1)%n; i != end; i = (i+1)%n) {
//				List<Point> triangle = new ArrayList<Point>();
//				triangle.add(region.polygon.get(i>0?i-1:n-1)); triangle.add(region.polygon.get(i)); triangle.add(region.polygon.get((i+1)%n));
//				cutSize += Geometry.calculatePolygonArea(triangle);
//			}				
//			if(cutSize / totSize > 0.05) {
//				valid = false;
//			}
//			if(oppRegion >= 0) {
//				//				System.out.println(idx+" "+oppRegion+" "+pStart+" "+pEnd);
//				//				System.out.println(regions.get(oppRegion));
//				cutSize = 0;
//				if(regions.get(oppRegion).polygon.size() < 3) System.out.println(regions.get(oppRegion).polygon);
//				totSize = Geometry.calculatePolygonArea(regions.get(oppRegion).polygon);
//				for (int i = (start+1)%n; i != end; i = (i+1)%n) {
//					List<Point> triangle = new ArrayList<Point>();
//					triangle.add(region.polygon.get(i>0?i-1:n-1)); triangle.add(region.polygon.get(i)); triangle.add(region.polygon.get((i+1)%n));
//					cutSize += Geometry.calculatePolygonArea(triangle);
//				}					
//				if(cutSize / totSize > 0.05) {
//					valid = false;
//				}
//			}
//		}

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
				
//				if(oppRegion == 46973) {
//					System.out.println("-*-");
//					System.out.println(region);
//					System.out.println(regions.get(oppRegion));
//					System.out.println(Geometry.isNonIntersectingPolygon(region.polygon));
//					System.out.println(cutSize);
//					System.out.println(Geometry.polygonArea(region.polygon));
//					if(oppRegion >= 0) System.out.println(Geometry.polygonArea(regions.get(oppRegion).polygon));
//					System.out.println(allPoints);
//				}
			} else {
				region.removeSegment(indices1, oppRegion);
			}
			
//			if(idx == 46973) {
//				System.out.println("---");
//				System.out.println(region);
//				System.out.println(regions.get(oppRegion));
//				System.out.println(Geometry.isNonIntersectingPolygon(region.polygon));
//				System.out.println(cutSize);
//				System.out.println(Geometry.polygonArea(region.polygon));
//				if(oppRegion >= 0) System.out.println(Geometry.polygonArea(regions.get(oppRegion).polygon));
//				System.out.println(allPoints);
//			}
			
//			if(!Geometry.isNonIntersectingPolygon(region.polygon)) System.out.println("Error!!! "+idx);
		}
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

		for(int k=0;k<polygon.size();k++) {
			int kk = (k+1) % polygon.size();
			for(int k2=0;k2<polygon.size();k2++) {
				int kk2 = (k2+1) % polygon.size();	

				if(Geometry.doSegmentsIntersect(polygon.get(k), polygon.get(kk), polygon.get(k2), polygon.get(kk2))) {
					System.out.println("AAA! "+regId);
					System.out.println(polygon);
					printShape(polygon);
				}
			}
		}

		while (vertices.size() > 2) {

			// determine if order is clockwise or counterclockwise
			boolean clockwise = Geometry.calculatePolygonSignedArea(vertices) < 0;

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

				if (Geometry.isEar(vPrev, vCurr, vNext, vertices, clockwise)) {
					setBit(order, idx, true);
					vertices.remove(i);
				} else {
					setBit(order, idx, false);
				}
				idx++;
				if(idx >= order.length*8) {
					System.out.println(regId+" "+vertices+" "+Geometry.calculatePolygonArea(l));
					order = extend(order);
					printShape(polygon);
					printShape(vertices);
				}
			}
		}

		byte[] result = new byte[(int) (idx/8) + 1];
		for(int i=0;i<result.length;i++) result[i] = order[i];
		return result;
	}

	void determineTriangleDrawOrders(List<Region> regions) {
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
		int x = p.x;
		int y = p.y;

		if(p.x > p2.x) { // from left
			if(y > 0 && regionData[x-1][y-1] == regionIdx) {
				if(x == regionData.length || regionData[x][y-1] != regionIdx) { // try up first
					polygon.add(new Point(x, y-1));
					if(x < regionData.length) oppRegions.add(regionData[x][y-1]);
					else oppRegions.add(-1);
					return true;
				}
				if(y == regionData[0].length || regionData[x][y] != regionIdx) { // try right
					polygon.add(new Point(x+1, y));
					if(y < regionData[0].length) oppRegions.add(regionData[x][y]);
					else oppRegions.add(-1);
					return true;
				} // else, must go down
				if(y < regionData[0].length) {
					polygon.add(new Point(x, y+1));
					oppRegions.add(regionData[x-1][y]);
					return true;
				}
			} else if(regionData[x-1][y] == regionIdx) {
				if(x == regionData.length || y < regionData[0].length && regionData[x][y] != regionIdx) { // try down first
					polygon.add(new Point(x, y+1));
					if(x < regionData.length) oppRegions.add(regionData[x][y]);
					else oppRegions.add(-1);
					return true;
				}
				if(y == 0 || regionData[x][y-1] != regionIdx) { // try right
					polygon.add(new Point(x+1, y));
					if(y > 0) oppRegions.add(regionData[x][y-1]);
					else oppRegions.add(-1);
					return true;
				} // else, must go up
				if(y > 0) {
					polygon.add(new Point(x, y-1));
					oppRegions.add(regionData[x-1][y-1]);
					return true;
				}
			}
		}

		if(p.x < p2.x) { // from right
			if(y > 0 && regionData[x][y-1] == regionIdx) {
				if(x == 0 || regionData[x-1][y-1] != regionIdx) { // try up first
					polygon.add(new Point(x, y-1));
					if(x > 0) oppRegions.add(regionData[x-1][y-1]);
					else oppRegions.add(-1);
					return true;
				}
				if(y == regionData[0].length || regionData[x-1][y] != regionIdx) { // try left
					polygon.add(new Point(x-1, y));
					if(y < regionData[0].length) oppRegions.add(regionData[x-1][y]);
					else oppRegions.add(-1);
					return true;
				} // else, must go down
				if(y < regionData[0].length) {
					polygon.add(new Point(x, y+1));
					oppRegions.add(regionData[x][y]);
					return true;
				}
			} else if(regionData[x][y] == regionIdx) {
				if(x == 0 || y < regionData[0].length && regionData[x-1][y] != regionIdx) { // try down first
					polygon.add(new Point(x, y+1));
					if(x > 0) oppRegions.add(regionData[x-1][y]);
					else oppRegions.add(-1);
					return true;
				}
				if(y == 0 || regionData[x-1][y-1] != regionIdx) { // try left
					polygon.add(new Point(x-1, y));
					if(y > 0) oppRegions.add(regionData[x-1][y-1]);
					else oppRegions.add(-1);
					return true;
				} // else, must go up
				if(y > 0) {
					polygon.add(new Point(x, y-1));
					oppRegions.add(regionData[x][y-1]);
					return true;
				}
			}
		}

		if(p.y > p2.y) { // from top
			if(x > 0 && regionData[x-1][y-1] == regionIdx) {
				if(y == regionData[0].length || regionData[x-1][y] != regionIdx) { // try left first
					polygon.add(new Point(x-1, y));
					if(y <  regionData[0].length) oppRegions.add(regionData[x-1][y]);
					else oppRegions.add(-1);
					return true;
				}
				if(x == regionData.length || y < regionData[0].length && regionData[x][y] != regionIdx) { // try down
					polygon.add(new Point(x, y+1));
					if(x < regionData.length) oppRegions.add(regionData[x][y]);
					else oppRegions.add(-1);
					return true;
				} // else, must go right
				if(x < regionData.length) {
					polygon.add(new Point(x+1, y));
					oppRegions.add(regionData[x][y-1]);
					return true;
				}
			} else if(y > 0 && regionData[x][y-1] == regionIdx) {
				if(y == regionData[0].length || x < regionData.length && regionData[x][y] != regionIdx) { // try right first
					polygon.add(new Point(x+1, y));
					if(y <  regionData[0].length) oppRegions.add(regionData[x][y]);
					else oppRegions.add(-1);
					return true;
				}
				if(x == 0 || regionData[x-1][y] != regionIdx) { // try down
					polygon.add(new Point(x, y+1));
					if(x > 0) oppRegions.add(regionData[x-1][y]);
					else oppRegions.add(-1);
					return true;
				} // else, must go left
				if(x > 0) {
					polygon.add(new Point(x-1, y));
					oppRegions.add(regionData[x-1][y-1]);
					return true;
				}
			}
		}

		if(p.y < p2.y) { // from bottom
			if(x > 0 && regionData[x-1][y] == regionIdx) {
				if(y == 0 || regionData[x-1][y-1] != regionIdx) { // try left first
					polygon.add(new Point(x-1, y));
					if(y > 0) oppRegions.add(regionData[x-1][y-1]);
					else oppRegions.add(-1);
					return true;
				}
				if(x == regionData.length || regionData[x][y-1] != regionIdx) { // try up
					polygon.add(new Point(x, y-1));
					if(x < regionData.length) oppRegions.add(regionData[x][y-1]);
					else oppRegions.add(-1);
					return true;
				} // else, must go right
				if(x < regionData.length) {
					polygon.add(new Point(x+1, y));
					oppRegions.add(regionData[x][y]);
					return true;
				}
			} else if(regionData[x][y] == regionIdx) {
				if(y == 0 || x < regionData.length && regionData[x][y-1] != regionIdx) { // try right first
					polygon.add(new Point(x+1, y));
					if(y > 0) oppRegions.add(regionData[x][y-1]);
					else oppRegions.add(-1);
					return true;
				}
				if(x == 0 || regionData[x-1][y-1] != regionIdx) { // try up
					polygon.add(new Point(x, y-1));
					if(x > 0) oppRegions.add(regionData[x-1][y-1]);
					else oppRegions.add(-1);
					return true;
				} // else, must go left
				if(x > 0 ) {
					polygon.add(new Point(x-1, y));
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
		polygon.add(new Point(x, y));
		polygon.add(new Point(x+1, y));
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

						points.add(new Point(Integer.parseInt(intSplit[0]), Integer.parseInt(intSplit[1])));
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

		simplifyDouglasPeucker(regions, 20, 10); // lower means less smoothing
		System.out.println("done: simplify using Douglas-Peucker");
		System.out.println(getTotalnumPoints(regions));

		FileOperator.printRegionListToFile(regions, POLYGONS_FILTERED_FILENAME);

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
//				new PolygonCreator().runSample(2);
//				new PolygonCreator().runSample(1);
		//		new PolygonCreator().runAll();
	}
}
