package vectormaps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Geometry;
import util.Point;
import util.Region;


class RegionResult {
	int[][] regions;
	int numRegions;

	RegionResult(int[][] regions, int numRegions) {
		this.regions = regions;
		this.numRegions = numRegions;
	}
}

public class PolygonCreator {

	RegionResult result;

	void visualizeRegion(int[][] regions, String filename) {
		int[][] regionC = new int[regions.length][];
		int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF, 0xFFBB0000, 0xFF00BB00, 0xFF0000BB, 0xFFBBBB00, 0xFFBB00BB, 0xFF00BBBB, 0xFFFF4444, 0xFF44FF44, 0xFF4444FF, 0xFFFFFF44, 0xFFFF44FF, 0xFF44FFFF, 0xFFFF8888, 0xFF88FF88, 0xFF8888FF, 0xFFFFFF88, 0xFFFF88FF, 0xFF88FFFF, 0xFFFFFFFF, 0xFFBBBBBB, 0xFF888888, 0xFF444444, 0xFF000000};
		for (int x = 0; x < regions.length; x++) {
			regionC[x] = new int[regions[x].length];
			for (int y = 0; y < regions[x].length; y++) {
				if(regions[x][y] < 0) regionC[x][y] = 0xFF000000;
				else regionC[x][y] = colors[regions[x][y] % colors.length];
			}
		}
		FileOperator.writeImage(regionC, filename);
	}

	void printToFile(List<Region> regions, String fileName) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			for(int i=0;i<regions.size();i++) {
				Region region = regions.get(i);
				writer.write("--- region "+i); 
				if(region.drawOrder >= 0) writer.write(", #"+region.drawOrder); 
				else writer.write(", ##"); 
				writer.write(", "+region.colorData);
				if(region.triangleDrawOrder.length > 0) {
					writer.write(", "); 
					for(int j=0;j<region.triangleDrawOrder.length;j++) {
						if(getBit(region.triangleDrawOrder,j)) writer.write("1");
						else writer.write("0");
					}
				}
				if(region.outerNeighbors.size() > 0) {
					writer.write(", "); 
					for(int j=0;j<region.outerNeighbors.size();j++) {
						if(j>0) writer.write(","); 
						writer.write(""+region.outerNeighbors.get(j));
					}
				}
				if(region.innerNeighbors.size() > 0) {
					writer.write(", "); 
					for(int j=0;j<region.innerNeighbors.size();j++) {
						if(j>0) writer.write(","); 
						writer.write(""+region.innerNeighbors.get(j));
					}
				}
				writer.write(" ---\n"); 
				List<Point> polygon = region.polygon;
				List<Integer> idxs = region.opposingRegions;
				for(int k=0;k<polygon.size();k++) {
					Point p = polygon.get(k);
					int idx = -2;
					if(k < idxs.size()) idx = idxs.get(k);
					writer.write("("+p.x+","+p.y+","+idx+")");
					if(k<polygon.size()-1) writer.write(", ");
				}
				if(i<regions.size()-1) writer.newLine();   // Write a new line character (platform-independent)
			}
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void addIfNew(List<Point> list, Point p) {
		for(Point pp : list) if(pp.equals(p)) return;
		list.add(p);
	}

	RegionResult findRegions(int[][] mapData) {
		// group map into regions
		int[][] regions = new int[mapData.length][];
		for (int x = 0; x < mapData.length; x++) {
			regions[x] = new int[mapData[0].length];
			for (int y = 0; y < mapData[x].length; y++) {
				regions[x][y] = -1;
			}
		}

		int cRegion = 0;
		List<Point> others = new ArrayList<Point>();
		others.add(new Point(0,0));

		int totalP = 0;

		while(cRegion == 0 || others.size() > 0) {
			System.out.println("--- Region "+cRegion+"---");
			List<Point> valids = new ArrayList<Point>();
			for(int i=others.size()-1;i>=0;i--) {
				Point p = others.get(i);
				if(regions[p.x][p.y] < 0) {
					valids.add(p);
					break;
				} 
				others.remove(i);
			}
			while(valids.size() > 0) {
				System.out.println(valids.size()+" "+(1.*totalP/(mapData.length * mapData[0].length)));
				List<Point> newValids = new ArrayList<Point>();
				for(Point p : valids) {
					regions[p.x][p.y] = cRegion; 
					totalP++;
					if(p.x > 0 && regions[p.x-1][p.y] < 0) {
						Point pp = new Point(p.x-1,p.y);
						if(mapData[p.x-1][p.y] == mapData[p.x][p.y]) addIfNew(newValids, pp);
						else addIfNew(others, pp);
					}
					if(p.x < mapData.length-1 && regions[p.x+1][p.y] < 0) {
						Point pp = new Point(p.x+1,p.y);
						if(mapData[p.x+1][p.y] == mapData[p.x][p.y]) addIfNew(newValids, pp);
						else addIfNew(others, pp);
					}
					if(p.y > 0 && regions[p.x][p.y-1] < 0) {
						Point pp = new Point(p.x,p.y-1);
						if(mapData[p.x][p.y-1] == mapData[p.x][p.y]) addIfNew(newValids, pp);
						else addIfNew(others, pp);
					}
					if(p.y < mapData[p.x].length-1 && regions[p.x][p.y+1] < 0) {
						Point pp = new Point(p.x,p.y+1);
						if(mapData[p.x][p.y+1] == mapData[p.x][p.y]) addIfNew(newValids, pp);
						else addIfNew(others, pp);
					}
				}
				valids = newValids;
			}
			cRegion++;
		}

		visualizeRegion(regions, System.getProperty("user.dir")+"\\output\\elevation_map_samples\\regions_visual.png");
		FileOperator.writeImage(regions, System.getProperty("user.dir")+"\\output\\elevation_map_samples\\regions.png");

		System.out.println((cRegion-1)+" regions");
		return new RegionResult(regions, cRegion-1);
	}



	//	void addToPolygon(Point p1, Point p2, Region region, int oppIdx) {
	//		//		for(int i=0;i<region.polygons.size();i++) {
	//		//			List<Point> polygon = region.polygons.get(i);
	//		//			List<Integer> idxs = region.opposingRegions.get(i);
	//		//
	//		//			Point p0 = polygon.get(0);
	//		//			if(p0.equals(p1)) {
	//		//				polygon.addFirst(p2);
	//		//				idxs.addFirst(oppIdx);
	//		//				return;
	//		//			}
	//		//			if(p0.equals(p2)) {
	//		//				polygon.addFirst(p1);
	//		//				idxs.addFirst(oppIdx);
	//		//				return;
	//		//			}
	//		//
	//		//			p0 = polygon.get(polygon.size() - 1);
	//		//			if(p0.equals(p1)) {
	//		//				polygon.addLast(p2);
	//		//				idxs.set(idxs.size()-1,oppIdx);
	//		//				idxs.addLast(oppIdx);
	//		//				return;
	//		//			}
	//		//			if(p0.equals(p2)) {
	//		//				polygon.addLast(p1);
	//		//				idxs.set(idxs.size()-1,oppIdx);
	//		//				idxs.addLast(oppIdx);
	//		//				return;
	//		//			}
	//		//		}
	//
	//		List<Point> list = new LinkedList<Point>();
	//		list.add(p1);
	//		list.add(p2);
	//		List<Integer> idxList = new LinkedList<Integer>();
	//		idxList.add(oppIdx);
	//		region.addPolygon(list, idxList);
	//	}

	//	void connectPolygons(List<Region> regions) {
	//		for(Region region : regions) {
	//			for(int i=region.polygons.size()-1;i>=0;i--) {
	//				if(region.regionIdx == 2989) System.out.println(region.polygons.get(i));
	//				Point fi = region.polygons.get(i).getFirst();
	//				Point li = region.polygons.get(i).getLast();
	//				for(int j=i-1;j>=0;j--) {
	//					Point fj = region.polygons.get(j).getFirst();
	//					Point lj = region.polygons.get(j).getLast();
	//
	//					if(fi.equals(fj)) {
	//						for(int k=1;k<region.polygons.get(i).size();k++) {
	//							region.polygons.get(j).addFirst(region.polygons.get(i).get(k));
	//						}
	//						for(int k=0;k<region.opposingRegions.get(i).size();k++) {
	//							region.opposingRegions.get(j).addFirst(region.opposingRegions.get(i).get(k));
	//						}
	//						region.polygons.remove(i);
	//						region.opposingRegions.remove(i);
	//						break;
	//					}
	//					if(fi.equals(lj)) {
	//						for(int k=1;k<region.polygons.get(i).size();k++) {
	//							region.polygons.get(j).addLast(region.polygons.get(i).get(k));
	//						}
	//						for(int k=0;k<region.opposingRegions.get(i).size();k++) {
	//							region.opposingRegions.get(j).addLast(region.opposingRegions.get(i).get(k));
	//						}
	//						region.polygons.remove(i);
	//						region.opposingRegions.remove(i);
	//						break;
	//					}
	//					if(li.equals(fj)) {
	//						for(int k=region.polygons.get(i).size()-2;k>=0;k--) {
	//							region.polygons.get(j).addFirst(region.polygons.get(i).get(k));
	//						}
	//						for(int k=region.opposingRegions.get(i).size()-1;k>=0;k--) {
	//							region.opposingRegions.get(j).addFirst(region.opposingRegions.get(i).get(k));
	//						}
	//						region.polygons.remove(i);
	//						region.opposingRegions.remove(i);
	//						break;
	//					}
	//					if(li.equals(lj)) {
	//						for(int k=region.polygons.get(i).size()-2;k>=0;k--) {
	//							region.polygons.get(j).addLast(region.polygons.get(i).get(k));
	//						}
	//						for(int k=region.opposingRegions.get(i).size()-1;k>=0;k--) {
	//							region.opposingRegions.get(j).addLast(region.opposingRegions.get(i).get(k));
	//						}
	//						region.polygons.remove(i);
	//						region.opposingRegions.remove(i);
	//						break;
	//					}
	//				}
	//			}
	//		}
	//	}

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

			for(int j=polygon.size()-1;j>=2;j--) {
				Point p0 = polygon.get(j);
				Point p1 = polygon.get(j-1);
				Point p2 = polygon.get(j-2);

				int i1 = idxs.get(j-1);
				int i2 = idxs.get(j-2);

				if(p0.x == p1.x && p0.x == p2.x && i1 == i2 || p0.y == p1.y && p0.y == p2.y && i1 == i2) {
					polygon.remove(j-1);
					idxs.remove(j-1);
				}
			}
		}
	}

	void filterSmallRegions(List<Region> regions, int minSize, int w, int h) {
		System.out.println("min. region size: "+minSize);

		for(int i=regions.size()-1;i>=0;i--) {
			Region region = regions.get(i);

			//			System.out.println(Geometry.calculatePolygonAreaGlobe(region.polygon, w, h)/minSize);

			if(Geometry.calculatePolygonAreaGlobe(region.polygon, w, h) <= minSize) {
				if(region.outerNeighbors.size() == 1) {
					boolean canRemove = regions.get(region.outerNeighbors.get(0)).canRemoveRegion(i);
					if(canRemove) {
						regions.get(region.outerNeighbors.get(0)).removeRegion(i, -1);
						regions.get(i).clear();
					}
				} 
				else if(region.outerNeighbors.size() == 2) {
					if(region.outerNeighbors.get(0) == -1) {
						Region region2 = regions.get(region.outerNeighbors.get(1));
						if(region2.canRemoveRegion(i)) {
							region2.removeRegion(i, region.outerNeighbors.get(0));
							regions.get(i).clear();
						}
					} else if(region.outerNeighbors.get(1) == -1) {
						Region region1 = regions.get(region.outerNeighbors.get(0));
						if(region1.canRemoveRegion(i)) {
							region1.removeRegion(i, region.outerNeighbors.get(1));
							regions.get(i).clear();
						}
					} else {
						Region region1 = regions.get(region.outerNeighbors.get(0));
						Region region2 = regions.get(region.outerNeighbors.get(1));
						if(region1.canRemoveRegion(i) && region2.canRemoveRegion(i)) {
							region1.removeRegion(i, region.outerNeighbors.get(1));
							region2.removeRegion(i, region.outerNeighbors.get(0));
							regions.get(i).clear();
						}
					}
				}
			}
		}
	}

	//	public static int[] getSortedIndices(final double[] data) {
	//		// Create an array of Integer objects representing the original indices
	//		Integer[] indices = new Integer[data.length];
	//		for (int i = 0; i < data.length; i++) {
	//			indices[i] = i;
	//		}
	//
	//		// Sort the indices array using a custom Comparator
	//		// The Comparator compares the values in the 'data' array at the given indices
	//		Arrays.sort(indices, new Comparator<Integer>() {
	//			@Override
	//			public int compare(final Integer index1, final Integer index2) {
	//				// Compare the values from the original 'data' array
	//				return Double.compare(data[index1], data[index2]);
	//			}
	//		});
	//
	//		int[] result = new int[indices.length];
	//		for(int i=0;i<result.length;i++) result[i] = indices[i];
	//
	//		return result;
	//	}

	//	void simplifyVisvalingamWhyatt(List<Region> regions, double delta, int w, int h) {
	//		for(int i=regions.size()-1;i>=0;i--) {
	//			Region region = regions.get(i);
	//			double regionSize = Geometry.calculatePolygonAreaGlobe(region.polygon, w, h);
	//			if(region.polygon.size() > 0) {
	//
	//				double[] sizes = new double[region.polygon.size()];
	//				for(int j=0;j<sizes.length;j++) {
	//					int jleft = j>0?j-1:sizes.length-1;
	//					int jright = (j+1)%sizes.length;
	//					List<Point> triangle = new ArrayList<Point>();
	//					triangle.add(region.polygon.get(jleft));
	//					triangle.add(region.polygon.get(j));
	//					triangle.add(region.polygon.get(jright));
	//					sizes[j] = Geometry.calculatePolygonAreaGlobe(triangle, w, h);
	//				}
	//				
	////				System.out.println("Region "+i);
	//
	//				int[] indices = getSortedIndices(sizes);
	//				for(int j=0;j<indices.length;j++) {
	//					int jmid = indices[j];
	//					int jleft = jmid>0?jmid-1: region.opposingRegions.size()-1;
	//					int jright = (jmid+1)%region.opposingRegions.size();
	//					// attempt remove:
	//					if(region.opposingRegions.get(jleft) == region.opposingRegions.get(jmid) && sizes[jmid] < delta && 1. * sizes[jmid] / regionSize < 0.25) {
	//						boolean intersect = false;
	//						for(int k=0;k<region.polygon.size();k++) {
	//							int kk = (k + 1) % region.polygon.size();
	//							if((kk < jleft || k > jright) && Geometry.doSegmentsIntersect(region.polygon.get(k), region.polygon.get(kk), region.polygon.get(jleft), region.polygon.get(jright))) {
	//								intersect = true;
	//								break;
	//							}
	//						}
	////						System.out.println(intersect);
	//						if(!intersect) {
	//							int idx = region.opposingRegions.get(jmid);
	//							if(idx >= 0) regions.get(idx).removeAt(region.polygon.get(jmid));
	//							region.polygon.remove(jmid);
	//							region.opposingRegions.remove(jmid);
	//							for(int k=0;k<indices.length;k++) {
	//								if(indices[k] > jmid) indices[k]--;
	//							}
	//						}
	//					}
	//				}
	////				System.out.println();
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

	void simplifyDouglasPeucker(List<Region> regions, double delta) {
		for(int i=regions.size()-1;i>=0;i--) {
			Region region = regions.get(i);

			//			if(region.regionIdx == 77483) {
			//				for(int j=0;j<region.polygon.size();j++) {
			//					if(j>0) System.out.print(",");
			//					System.out.print(region.polygon.get(j).x);
			//				}
			//				System.out.println();
			//				for(int j=0;j<region.polygon.size();j++) {
			//					if(j>0) System.out.print(",");
			//					System.out.print(region.polygon.get(j).y);
			//				}
			//				System.out.println();
			//			}

			if(region.regionIdx == 15211) {
				printShape(region.polygon);
			}

			if (region.polygon.size() > 0) {
				int prevSize = 0;
				while(prevSize != region.polygon.size()) {
					prevSize = region.polygon.size();

					List<Integer> segments = new ArrayList<Integer>();
					for(int j=0;j<region.opposingRegions.size();j++) {
						int jj = (j+1) % region.opposingRegions.size();
						if(region.opposingRegions.get(j) != region.opposingRegions.get(jj)) {
							segments.add(jj);
						}
					}

					if(region.regionIdx == 15211) System.out.println("Region "+region.regionIdx+": "+region.polygon.size()+" "+segments.size());

					if(segments.size() == 0) {
						simplifyRecursive(regions, i, 0, region.polygon.size() - 1, delta, prevSize);
					} else {
						for(int j=0;j<segments.size()-1;j++) {
							int jj = j+1;
							simplifyRecursive(regions, i, segments.get(j), segments.get(jj) - 1, delta, prevSize);
						}
					}
				}
			}
		}
	}

	private static void simplifyRecursive(List<Region> regions, int idx, int start, int end, double epsilon, int prevSize) {
		Region region = regions.get(idx);
		int n = region.polygon.size();
		if(n!= prevSize) return;

		double dMax = -1;
		int index = 0;

		for (int i = start+1; i < end; i++) {
			double distance = Geometry.perpendicularDistance(region.polygon.get(i), region.polygon.get(start), region.polygon.get(end));
			if (distance > dMax) {
				dMax = distance;
				index = i;
			}
		}

		if(dMax == -1) return;

		if(region.regionIdx == 15211) System.out.println(start+" "+end+" "+index+" "+dMax);

		boolean intersect = false;
		for (int i = end % n; i != start; i = (i+1) % n) {
			int j = (i + 1) % n;
			if(Geometry.doSegmentsIntersect(region.polygon.get(i), region.polygon.get(j), region.polygon.get(start), region.polygon.get(end))) {
				intersect = true;
				break;
			}
		}

		if(region.regionIdx == 15211) System.out.println(intersect);

		if (intersect || dMax > epsilon) {
			simplifyRecursive(regions, idx, start, index, epsilon, prevSize);
			simplifyRecursive(regions, idx, index, end, epsilon, prevSize);
		} else {
			for (int i = end-1; i > start; i--) {
				// don't make cuts that would remove too much of a region
				double totSize = Geometry.calculatePolygonArea(region.polygon);
				List<Point> triangle = new ArrayList<Point>();
				triangle.add(region.polygon.get(i-1)); triangle.add(region.polygon.get(i)); triangle.add(region.polygon.get(i+1));
				double cutSize = Geometry.calculatePolygonArea(triangle);
				if(cutSize / totSize < 0.33) {
					int j = region.opposingRegions.get(i);
					int z = 1;
					if(j >= 0) {
						z = regions.get(j).removeAt(region.polygon.get(i-1), region.polygon.get(i), region.polygon.get(i+1));
					}
					if(z > 0) {
						region.polygon.remove(i);
						region.opposingRegions.remove(i);
					}
				}
			}
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

	boolean getBit(byte[] a, long i) {
		byte b = a[(int) (i/8)];
		int p = (int) (i%8);
		return ((b >> p) & 1) == 1;
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
					printShape(polygon);
				}
			}
		}

		while (vertices.size() > 2) {

			// determine if order is clockwise or counterclockwise
			boolean clockwise = Geometry.calculatePolygonSignedArea(vertices) < 0;

			for (int i=vertices.size()-1;i>=0;i--) {

				int prev = (i == 0) ? vertices.size() - 1 : i - 1;
				int next = (i == vertices.size() - 1) ? 0 : i + 1;

				Point vPrev = vertices.get(prev);
				Point vCurr = vertices.get(i);
				Point vNext = vertices.get(next);

				List<Point> l = new ArrayList<Point>();
				l.add(vPrev); l.add(vCurr); l.add(vNext);

				// Check for convexity and if the triangle formed is an "ear"
				// (i.e., no other polygon vertices are inside the triangle)

				//				if(tracing) System.out.println(vCurr+" "+Geometry.isConvex(vPrev, vCurr, vNext, clockwise)+" ");

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

		//		System.out.println();

		byte[] result = new byte[(int) (idx/8) + 1];
		for(int i=0;i<result.length;i++) result[i] = order[i];
		return result;
	}

	void determineTriangleDrawOrders(List<Region> regions) {
		for(int i=regions.size()-1;i>=0;i--) {
			if((i % 1000) == 0) System.out.println("region "+i+"/"+regions.size());
			Region region = regions.get(i);
			if(region.polygon.size() > 0) {
				List<Point> polygon = region.polygon;
				byte[] triangleDrawOrder = determineTriangleDrawOrder(polygon, i);
				region.setTriangleDrawOrder(triangleDrawOrder);
			}
		}
	}

	void finalPrint(List<Region> regions, String fileName, int width, int height) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
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
						//						int[]argb = Colors.intToRGBArray(region.colorData);
						//						writer.write(argb[0]+","+argb[1]+","+argb[2]);
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

									if(getBit(region.triangleDrawOrder, z)) {
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

	static int[][] initMapData(String inputFileName) {
		int[][] mapData = null;

		try{
			mapData = FileOperator.readImage(inputFileName);
			System.out.println("done reading");
		} catch(Exception e){
			System.out.println(e.getMessage());
		}

		return mapData;
	}

	//	static Map<Integer,Integer> getIndexMap(int[][] mapData, String fileName) {
	//		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
	//
	//		int c = 0;
	//		for (int x = 0; x < mapData.length; x++) {
	//			for (int y = 0; y < mapData[x].length; y++) {
	//				if(!map.containsKey(mapData[x][y])) {
	//					map.put(mapData[x][y], c);
	//					c++;
	//					if(c >= 16) {
	//						System.out.println("Error: more than 16 unique colors in "+fileName);
	//						for(int i : map.keySet()) {
	//							System.out.println(Arrays.toString(Colors.intToRGBArray(i))+" "+i);
	//						}
	//					}
	//				}
	//			}
	//		}
	//
	//		return map;
	//	}

	static int[][] mergeMapData() {
		String[] inputFileNames = new String[] {
				ElevationMapCreator.ELEVATION_MAP_OUTPUT_FILENAME,
				BiomeMapCreator.BIOME_FINAL_RESCALED_MAP_FILENAME,
				SoilMapCreator.SOIL_FINAL_RESCALED_MAP_FILENAME
		};

		Map<Integer,Integer> elevationMap = new HashMap<Integer,Integer>();
		elevationMap.put(ElevationMapCreator.BLUE, 0);
		for(int i=0;i<ElevationMapCreator.LEVEL_COLORS.length;i++) elevationMap.put(ElevationMapCreator.LEVEL_COLORS[i][1],i+1);

		Map<Integer,Integer> biomeMap = new HashMap<Integer,Integer>();
		for(int i=0;i<BiomeMapCreator.BIOMES.length;i++) biomeMap.put(BiomeMapCreator.BIOMES[i],i);
		biomeMap.put(BiomeMapCreator.GLACIAL, BiomeMapCreator.BIOMES.length);

		Map<Integer,Integer> soilMap = new HashMap<Integer,Integer>();
		for(int i=0;i<SoilMapCreator.ALL_SOILS.length;i++) soilMap.put(SoilMapCreator.ALL_SOILS[i],i);

		//		for(int i : biomeMap.keySet()) {
		//			System.out.println(Arrays.toString(Colors.intToRGBArray(i))+" "+biomeMap.get(i));
		//		}

		int w = 0;
		int h = 0;

		int[][][] data = new int[inputFileNames.length][][];
		List<Map<Integer,Integer>> mappings = new ArrayList<Map<Integer,Integer>>();

		mappings.add(elevationMap);
		mappings.add(biomeMap);
		mappings.add(soilMap);

		for(int i=0;i<inputFileNames.length;i++) {
			data[i] = initMapData(inputFileNames[i]);
			if(w == 0) {
				w = data[i].length;
				h = data[i][0].length;
			} else {
				if(w != data[i].length || h != data[i][0].length) System.out.println("Error: dimension of map files inconsistent in mergeMapDataFromSources! "+inputFileNames[i]);
			}

			//			mappings.add(getIndexMap(data[i], inputFileNames[i]));
		}

		int[][] finalMap = new int[w][h];

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int v = 0;
				for(int k=0;k<data.length;k++) {
					int exp = (int) Math.pow(16,k);
					//					if(!mappings.get(k).containsKey(data[k][x][y])) System.out.println("!!! " +x+" "+y+" "+data[k][x][y]+" "+k);
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
			//			System.out.println(n+" "+polygon.get(n-2)+" "+polygon.get(n-1));
			finished = !updatePolygon(regionData,region.regionIdx,polygon,oppRegions,polygon.get(n-1),polygon.get(n-2));
			if(polygon.get(0).equals(polygon.get(polygon.size()-1))) {
				finished = true;
			}
		}

//		polygon.removeLast();
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

	List<Region> initRegions(int[][] mapData) {
		//		int iii = 16146;
		System.out.println("start: create initial polygons");

		List<Region> regions = new ArrayList<Region>();
		RegionResult regionResult = findRegions(mapData);

		result = regionResult;

		for(int i=0;i<regionResult.numRegions;i++) regions.add(new Region(i));
		boolean[] done = new boolean[regionResult.numRegions];

		//		int c = 0;

		for (int x = 0; x < mapData.length; x++) {
			for (int y = 0; y < mapData[x].length; y++) {
				Region region = regions.get(regionResult.regions[x][y]);

				if(!done[regionResult.regions[x][y]]) {
					region.setColorData(mapData[x][y]);
					processRegion(region, regionResult.regions, x, y);
					done[regionResult.regions[x][y]] = true;
				}

				if(x > 0) checkInnerNeighbor(region, regionResult.regions[x-1][y]);
				if(x < regionResult.regions.length - 1) checkInnerNeighbor(region, regionResult.regions[x+1][y]);
				if(y > 0) checkInnerNeighbor(region, regionResult.regions[x][y-1]);
				if(y < regionResult.regions[0].length - 1) checkInnerNeighbor(region, regionResult.regions[x][y+1]);

				//				if(x == 0) {
				//					addToPolygon(new Point(x, y), new Point(x, y+1), regions.get(regionResult.regions[x][y]), -1);
				//				}
				//				if(x < mapData.length - 1 && regionResult.regions[x+1][y] != regionResult.regions[x][y]) {
				//					addToPolygon(new Point(x+1, y), new Point(x+1, y+1), regions.get(regionResult.regions[x][y]), regionResult.regions[x+1][y]);
				//					addToPolygon(new Point(x+1, y), new Point(x+1, y+1), regions.get(regionResult.regions[x+1][y]), regionResult.regions[x][y]);
				//				} else if(x ==  mapData.length - 1) {
				//					addToPolygon(new Point(x+1, y), new Point(x+1, y+1), regions.get(regionResult.regions[x][y]), -1);
				//				}
				//
				//				if(y == 0) {
				//					addToPolygon(new Point(x, y), new Point(x+1, y), regions.get(regionResult.regions[x][y]), -1);
				//				} 
				//				if(y < mapData[x].length - 1 && regionResult.regions[x][y+1] != regionResult.regions[x][y]) {
				//					addToPolygon(new Point(x, y+1), new Point(x+1, y+1), regions.get(regionResult.regions[x][y]), regionResult.regions[x][y+1]);
				//					addToPolygon(new Point(x, y+1), new Point(x+1, y+1), regions.get(regionResult.regions[x][y+1]), regionResult.regions[x][y]);
				//				} else if(y ==  mapData[x].length - 1) {
				//					addToPolygon(new Point(x, y+1), new Point(x+1, y+1), regions.get(regionResult.regions[x][y]), -1);
				//				}
				//
				//				if(regions.get(iii).polygons.size() > 0 && regions.get(iii).polygons.get(0).size() != c) {
				//					System.out.println("! "+x+" "+y+" "+regions.get(iii).polygons.get(0)+" "+regions.get(iii).opposingRegions.get(0));
				//					c = regions.get(iii).polygons.get(0).size();
				//				}
			}
		}

		System.out.println("done: create initial polygons");
		printToFile(regions, System.getProperty("user.dir")+"\\output\\elevation_map_samples\\polygons_base.txt");

		return regions;
	}

	List<Region> loadConnectedPolygons() {
		List<Region> regions = new ArrayList<Region>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir")+"\\output\\elevation_map_samples\\polygons_pruned.txt"));

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

	//	void testIntegrity(List<Region> regions, int[][] mapData) {
	//		for(int i=0;i<regions.size();i++) {
	//			Region region = regions.get(i);
	//			for(int j=0;j<region.polygons.size();j++) {
	//				List<Point> polygon = region.polygons.get(j);
	//				List<Integer> opps = region.opposingRegions.get(j);
	//				for(int k=0;k<polygon.size();k++) {
	//					int kk = (k+1) % polygon.size();
	//
	//					Point p1 = polygon.get(k);
	//					Point p2 = polygon.get(kk);
	//
	//					int opp = opps.get(k);
	//
	//					boolean valid = true;
	//
	//					if(p1.x > p2.x) {
	//						for(int l=p2.x;l<p1.x;l++) {
	//							if(
	//									p1.y < result.regions[l].length-1 &&
	//									!(result.regions[l][p1.y] == region.regionIdx) && 
	//									!(result.regions[l][p1.y+1] == region.regionIdx)
	//									) {System.out.println(l+" "+p1.y+" "+i);}
	//						}
	//						if(!valid) System.out.println("fail!");
	//						valid = false;
	//					} 
	//					if(p2.x > p1.x) {
	//						if(!valid) System.out.println("fail!");
	//						valid = false;
	//					}
	//					if(p1.y > p2.y) {
	//						if(!valid) System.out.println("fail!");
	//						valid = false;
	//					} 
	//					if(p2.y > p1.y) {
	//						if(!valid) System.out.println("fail!");
	//						valid = false;
	//					}
	//				}
	//			}
	//		}
	//	}

	void processMap(int[][] mapData) {
		
		//		List<Region> regions = initRegions(mapData);
		//		System.out.println(getTotalnumPoints(regions));
		//
		//		basicPrune(regions);
		//		System.out.println("done: basic prune polygons");
		//		System.out.println(getTotalnumPoints(regions));
		//
		//		printToFile(regions, System.getProperty("user.dir")+"\\output\\elevation_map_samples\\polygons_pruned.txt");

		List<Region> regions = loadConnectedPolygons();

		mergeRegionsAndSetDrawOrder(regions);
		System.out.println("done: merged regions");
		System.out.println(getTotalnumPoints(regions));

		printToFile(regions, System.getProperty("user.dir")+"\\output\\elevation_map_samples\\polygons_ordered.txt");

		filterSmallRegions(regions, 1000, mapData.length, mapData[0].length);
		System.out.println("done: filtered small regions");
		System.out.println(getTotalnumPoints(regions));

		//		simplifyVisvalingamWhyatt(regions, 100000000000., mapData.length, mapData[0].length);
		//		System.out.println("done: simplify using Visvalingam-Whyatt");
		//		System.out.println(getTotalnumPoints(regions));

		simplifyDouglasPeucker(regions, 10);
		System.out.println("done: simplify using Douglas-Peucker");
		System.out.println(getTotalnumPoints(regions));

		printToFile(regions, System.getProperty("user.dir")+"\\output\\elevation_map_samples\\polygons_filtered.txt");

		determineTriangleDrawOrders(regions);
		System.out.println("done: determine triangle draw order");

		finalPrint(regions, System.getProperty("user.dir")+"\\output\\elevation_map_samples\\polygons_final.txt", mapData.length, mapData[0].length);

		//		mergeRegionPolygons(polygons);

		//		List<List<Triangle>> triangles = convertToTriangles(newPolygons); 
		//		System.out.println("done: convert to triangles");

	}

	public static void main(String[] args) {
		//		new PolygonCreator().processMap(initMapData(ElevationMapCreator.BASE_MAP_OUTPUT_FILENAME));
		//		new PolygonCreator().processMap(initMapData(ElevationMapCreator.ELEVATION_MAP_OUTPUT_FILENAME));
		new PolygonCreator().processMap(mergeMapData());
	}
}
