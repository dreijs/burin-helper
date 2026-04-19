package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Region {
	public int regionIdx;
	public int colorData;
	public int drawOrder;
	public byte[] triangleDrawOrder;
	public List<Point> polygon;
	public List<Integer> opposingRegions; // opposingRegions.get(i) is the opposing region of the edge between point i and point (i + 1) % n. If negative, corresponds to the terrain data - 2 (-1 corresponds to outside the map)
	public List<Integer> opposingRivers; // 
	public List<Integer> outerNeighbors;
	public List<Integer> innerNeighbors;

	public Region(int regionIdx) {
		this.regionIdx = regionIdx;
		this.drawOrder = -1;
		this.triangleDrawOrder = new byte[] {};
		this.polygon = new ArrayList<Point>();
		this.opposingRegions = new ArrayList<Integer>();
		this.opposingRivers = new ArrayList<Integer>();
		this.outerNeighbors = new ArrayList<Integer>();
		this.innerNeighbors = new ArrayList<Integer>();
	}

	public void resetRiverData() {
		this.opposingRivers = new ArrayList<Integer>();
		for(int i=0;i<this.polygon.size();i++) {
			this.opposingRivers.add(-1);
		}
	}

	public void matchNeighbors(List<Point> newPolygon, List<Point> oldPolygon, List<Integer> newIndices, List<Integer> oldIndices, boolean trace) {
		int jStart = 0;
		int direction = 1;
		int n = oldPolygon.size();
		for(int i=0;i<newPolygon.size();i++) {
			if(trace) System.out.println("i: "+i);
			boolean found = false;
			boolean changedDirection = false;
//			for(int j = Math.floorMod(jStart + direction, n); !found && (j != jStart); j = Math.floorMod(j + direction, n)) {
			for(int j=0;j<n;j++) {
				int ii, jj;
				ii = (i+1) % newPolygon.size();
				if(direction > 0) {
					jj = (j+1) % oldPolygon.size();
				} else {
					jj = j>0?j-1:oldPolygon.size()-1;
				}
				
				if(trace) System.out.println(i+" "+j+" "+newPolygon.get(i)+" -- "+newPolygon.get(ii)+" vs "+oldPolygon.get(j)+" -- "+oldPolygon.get(jj)+" "+GeometryUtils.sharesSegmentWith(newPolygon.get(i), newPolygon.get(ii), oldPolygon.get(j), oldPolygon.get(jj), 0.001, 0.001, trace));

				if(GeometryUtils.sharesSegmentWith(newPolygon.get(i), newPolygon.get(ii), oldPolygon.get(j), oldPolygon.get(jj), 0.001, 0.001, false)) {
					if(trace) System.out.println("found!");
					if(direction > 0) {
						newIndices.set(i, oldIndices.get(j));
					} else {
						newIndices.set(i, oldIndices.get(jj));
					}
					found = true;
//					jStart = j;
				} 
//				else {
//					if(!changedDirection) {
//						direction = direction>0?-1:1;
//						changedDirection = true;
//					}
//				}
			}
//			if(trace) {
//				if(found) System.out.println("found");
//				else System.out.println("not found");
//			}
		}
	}

	public void extendBasedOnPolygon(List<Point> newPolygon, List<List<Point>> intersectingRiverData, List<Integer> riverIndices) {
//		if(regionIdx == 161) {
//			System.out.println("---"+regionIdx);
//			System.out.println(polygon);
//			System.out.println(opposingRegions);
//			System.out.println(opposingRivers);
//			System.out.println(newPolygon);
//			System.out.println(intersectingRiverData);
//			System.out.println(riverIndices);
//		}

		List<Integer> newOppRegions = new ArrayList<Integer>();
		List<Integer> newOppRivers = new ArrayList<Integer>();
		for(int i=0;i<newPolygon.size();i++) {
			newOppRegions.add(regionIdx);
			newOppRivers.add(-1);
		}

		matchNeighbors(newPolygon, polygon, newOppRegions, opposingRegions, false);
		for(int i=0;i<intersectingRiverData.size();i++) {
			List<Integer> rivers = new ArrayList<Integer>();
			for(int j=0;j<intersectingRiverData.get(i).size();j++) rivers.add(riverIndices.get(i));
			matchNeighbors(newPolygon, intersectingRiverData.get(i), newOppRivers, rivers, false);
		}

		this.polygon = newPolygon;
		this.opposingRegions = newOppRegions;
		this.opposingRivers = newOppRivers;
		
//		if(regionIdx == 161) {
//			System.out.println("---"+regionIdx);
//			System.out.println(polygon);
//			System.out.println(opposingRegions);
//			System.out.println(opposingRivers);
//		}
		
		//
		//		// the ordering of the points in the new polygon may be reversed, so check first: this is important to preserve the neighbor relation
		//		int sameOrder = 0;
		//		int start1 = -1;
		//		int start2 = -1;
		//		for(int i=0;i<polygon.size();i++) {
		//			for(int j=0;j<points.size();j++) {
		//				if(polygon.get(i).equals(points.get(j))) {
		//					int imin = i>0?i-1:polygon.size()-1;
		//					int iplus = (i+1) % polygon.size();
		//					int jmin = j>0?j-1:points.size()-1;
		//					int jplus = (j+1) % points.size();
		//					if(polygon.get(iplus).equals(points.get(jplus)) || polygon.get(imin).equals(points.get(jmin))) {
		//						sameOrder = 1;
		//					} else if(polygon.get(iplus).equals(points.get(jmin)) || polygon.get(imin).equals(points.get(jplus))) {
		//						sameOrder = -1;
		//					}
		//					if(start1 < 0) {
		//						start1 = i;
		//						start2 = j;
		//					}
		//				}
		//			}
		//		}
		//
		//		List<Point> newPolygon = new ArrayList<Point>();;
		//		List<Integer> newOppRegions = new ArrayList<Integer>();
		//		List<Integer> newOppRivers = new ArrayList<Integer>();;
		//
		//		int i = start2;
		//		int j = start1;
		//		int toGo = points.size();
		//
		//		System.out.println(sameOrder);
		//
		//		while(toGo > 0) {
		//			newPolygon.add(points.get(i));
		//
		//			if(polygon.get(j).equals(points.get(i))) {
		//				if(sameOrder > 0) {
		//					newOppRegions.add(this.opposingRegions.get(j));
		//					newOppRivers.add(-1);
		//					j = (j+1) % polygon.size();
		//				} else {
		//					int jj = j>0?j-1:polygon.size()-1;
		//					newOppRegions.add(this.opposingRegions.get(jj));
		//					newOppRivers.add(-1);
		//					j = jj;
		//				}
		//			} else {
		//				newOppRegions.add(regionIdx);
		//				//				newOppRivers.add(riverIdx);
		//			}
		//
		//			i = (i+1) % points.size();
		//			toGo--;
		//		}
		//
		//		this.polygon = newPolygon;
		//		this.opposingRegions = newOppRegions;
		//		this.opposingRivers = newOppRivers;
	}
	
	public Region splitFromPolygon(List<Point> newPolygon, List<List<Point>> intersectingRiverData, List<Integer> riverIndices, int idx) {
		Region result = new Region(idx);
		List<Integer> newOppRegions = new ArrayList<Integer>();
		List<Integer> newOppRivers = new ArrayList<Integer>();
		for(int i=0;i<newPolygon.size();i++) {
			newOppRegions.add(regionIdx);
			newOppRivers.add(-1);
		}

		matchNeighbors(newPolygon, polygon, newOppRegions, opposingRegions, false);
		for(int i=0;i<intersectingRiverData.size();i++) {
			List<Integer> rivers = new ArrayList<Integer>();
			for(int j=0;j<intersectingRiverData.get(i).size();j++) rivers.add(riverIndices.get(i));
			matchNeighbors(newPolygon, intersectingRiverData.get(i), newOppRivers, rivers, false);
		}

		result.polygon = newPolygon;
		result.opposingRegions = newOppRegions;
		result.opposingRivers = newOppRivers;
		result.colorData = this.colorData;
		result.drawOrder = this.drawOrder;
		
		return result;
	}

	public void setDrawOrder(int drawOrder) {
		this.drawOrder = drawOrder;
	}

	public int getDrawOrder() {
		return drawOrder;
	}

	public void setColorData(int colorData) {
		this.colorData = colorData;
	}

	public void setTriangleDrawOrder(byte[] triangleDrawOrder) {
		this.triangleDrawOrder = triangleDrawOrder;
	}

	public boolean canRemoveRegion(int i) {
		boolean started = false;
		int cOpp = -1;
		for(int l=0;l<opposingRegions.size();l++) {
			int j = opposingRegions.get(l);
			if(i == j) {
				if(!started) {
					started = true;
				} else if (cOpp != i) return false;
			}
			cOpp = j;
		}
		return true;
	}

	public void removeRegion(int i, int opp) {
		boolean all = true;

		for(int l=0;l<opposingRegions.size();l++) {
			int j = opposingRegions.get(l);
			if(i != j) {
				all = false;
			}
		}

		if(all) {
			this.clear();
		}

		for(int j=opposingRegions.size()-1;j>=0;j--) {
			if(opposingRegions.get(j) == i) {
				int jj = j > 0 ? j - 1:opposingRegions.size()-1;
				if(opposingRegions.get(jj) == i) {
					this.polygon.remove(j);
					this.opposingRegions.remove(j);
				} else {
					this.opposingRegions.set(j,opp);
				}
			}
		}

		for(int j=outerNeighbors.size()-1;j>=0;j--) {
			if(outerNeighbors.get(j) == 1) outerNeighbors.remove(j);
		}
		for(int j=innerNeighbors.size()-1;j>=0;j--) {
			if(innerNeighbors.get(j) == 1) innerNeighbors.remove(j);
		}
	}

	public void clear() {
		this.polygon = new ArrayList<Point>();
		this.opposingRegions = new ArrayList<Integer>();
		this.opposingRivers = new ArrayList<Integer>();
		this.outerNeighbors = new ArrayList<Integer>();
		this.innerNeighbors = new ArrayList<Integer>();
		//		System.out.println("clear region "+this.regionIdx);
	}

	public int[] getSegmentIndices(Point p1, Point p2, int oppRegion) {
		// check if p1 comes first
		int idx1 = -1;
		int idx2 = -1;
		for(int i=0;i<polygon.size();i++) {
			if(p1.equals(polygon.get(i))) {idx1 = i;}
			if(idx1 >= 0 && p2.equals(polygon.get(i))) {idx2 = i; break;}
			if(idx2 < 0 && !opposingRegions.get(i).equals(oppRegion)) idx1 = -1;
		}
		if(idx1 >= 0 && idx2 < 0) {
			for(int i=0;i<polygon.size();i++) {
				if(idx1 >= 0 && p2.equals(polygon.get(i))) {idx2 = i; break;}
				if(idx2 < 0 && !opposingRegions.get(i).equals(oppRegion)) {idx1 = -1; break;}
			}
		}

		if(idx2 >= 0) {
			return new int[] {idx1, idx2};
		}

		idx1 = -1;
		for(int i=0;i<polygon.size();i++) {
			if(p2.equals(polygon.get(i))) {idx1 = i;}
			if(idx1 >= 0 && p1.equals(polygon.get(i))) {idx2 = i; break;}
			if(idx2 < 0 && !opposingRegions.get(i).equals(oppRegion)) idx1 = -1;
		}
		if(idx1 >= 0 && idx2 < 0) {
			for(int i=0;i<polygon.size();i++) {
				if(idx1 >= 0 && p1.equals(polygon.get(i))) {idx2 = i;  break;}
				if(idx2 < 0 && !opposingRegions.get(i).equals(oppRegion)) {idx1 = -1; break;}
			}
		}

		if(idx2 >= 0) {
			return new int[] {idx1, idx2};
		}

		// segment not found: probably internal
		return null;
	}

	public int[] getSegmentIndices(Point[] points) {
		// check if p1 comes first
		int idx1 = -1;
		int idx2 = -1;
		Point p1 = points[0];
		Point p2 = points[points.length-1];
		int n = polygon.size();
		for(int i=0;i<n;i++) {
			if(points[0].equals(polygon.get(i))) {idx1 = i;}
			if(idx1 >= 0 && p2.equals(polygon.get(i))) {idx2 = i; break;}
			if(idx2 < 0 && (i-idx1 >= points.length || !polygon.get(i).equals(points[i-idx1]))) idx1 = -1;
		}
		if(idx1 >= 0 && idx2 < 0) {
			for(int i=0;i<n;i++) {
				if(idx1 >= 0 && p2.equals(polygon.get(i))) {idx2 = i; break;}
				int nn = Math.floorMod(i-idx1, n);
				if(idx2 < 0 && (nn >= points.length || !polygon.get(i).equals(points[nn]))) idx1 = -1;
			}
		}

		if(idx2 >= 0) {
			return new int[] {idx1, idx2};
		}

		idx1 = -1;
		for(int i=0;i<n;i++) {
			if(p2.equals(polygon.get(i))) {idx1 = i;}
			if(idx1 >= 0 && p1.equals(polygon.get(i))) {idx2 = i; break;}
			if(idx2 < 0 && (i-idx1 >= points.length || !polygon.get(i).equals(points[i-idx1]))) idx1 = -1;
		}
		if(idx1 >= 0 && idx2 < 0) {
			for(int i=0;i<n;i++) {
				if(idx1 >= 0 && p1.equals(polygon.get(i))) {idx2 = i;  break;}
				int nn = Math.floorMod(i-idx1, n);
				if(idx2 < 0 && (nn >= points.length || !polygon.get(i).equals(points[nn]))) idx1 = -1;
			}
		}

		if(idx2 >= 0) {
			return new int[] {idx1, idx2};
		}

		// segment not found: probably internal
		return null;
	}

	public boolean canSimplifySegment(Point p1, Point p2, int oppRegion) {
		int[] indices = getSegmentIndices(p1, p2, oppRegion);
		if(indices == null) return true;

		int n = polygon.size();
		for (int i = indices[1] % n; i != indices[0]; i = (i+1) % n) {
			int j = (i + 1) % n;
			if(GeometryUtils.doSegmentsIntersect(polygon.get(i), polygon.get(j), polygon.get(indices[0]), polygon.get(indices[1]))) {
				return false;
			}
		}
		return true;
	}

	public Point[] getSegmentPoints(int[] indices, int oppRegion) {
		if(indices == null) return new PointInt[] {};

		List<Point> points = new ArrayList<Point>();

		if(indices[1] > indices[0]) {
			for (int i = indices[1]; i != indices[0]-1;i--) {
				points.add(polygon.get(i)); 
			}
		} else {
			for (int i = polygon.size()-1; i != indices[0]-1;i--) {
				points.add(polygon.get(i)); 
			}
			for (int i = indices[1]; i >= 0;i--) {
				points.add(polygon.get(i)); 
			}
		}

		return points.toArray(new Point[0]);
	}

	public void removeSegment(int[] indices, int oppRegion) {

		if(indices == null) return;

		if(indices[1] > indices[0]) {
			for (int i = indices[1]-1; i != indices[0];i--) {
				polygon.remove(i); 
				opposingRegions.remove(i); 
			}
		} else {
			for (int i = polygon.size()-1; i != indices[0];i--) {
				polygon.remove(i); 
				opposingRegions.remove(i); 
			}
			for (int i = indices[1]-1; i >= 0;i--) {
				polygon.remove(i); 
				opposingRegions.remove(i); 
			}
		}
	}

	public int removeAt(Point p1, Point p2, Point p3) {
		int removed = 0;
		for(int i=polygon.size()-1;i>=0;i--) {
			int n = polygon.size();
			int imin = (i>0)?(i-1):(n-1);
			int imax = (i+1)%n;
			Point pmin = polygon.get(imin);
			Point pi = polygon.get(i);
			Point pmax = polygon.get(imax);
			if(pmin.equals(p1) && pi.equals(p2) && pmax.equals(p3) || pmax.equals(p1) && pi.equals(p2) && pmin.equals(p3)) {
				polygon.remove(i); 
				opposingRegions.remove(i); 
				removed++;
			}
		}
		if(removed == 1) {
			//			System.out.println(this.regionIdx+" "+p2);
			//			int a = 1/0;
		}
		return removed;
	}

	public int getTotalNumPoints() {
		return this.polygon.size();
	}

	public boolean getBit(byte[] a, long i) {
		byte b = a[(int) (i/8)];
		int p = (int) (i%8);
		return ((b >> p) & 1) == 1;
	}

	public String toString() {
		String s ="--- region "+regionIdx; 
		if(drawOrder >= 0) s += (", #"+drawOrder); 
		else s += (", ##"); 
		s += (", "+colorData);
		if(triangleDrawOrder.length > 0) {
			s += (", "); 
			for(int j=0;j<triangleDrawOrder.length;j++) {
				if(getBit(triangleDrawOrder,j)) s += ("1");
				else s += ("0");
			}
		}
		if(outerNeighbors.size() > 0) {
			s += (", "); 
			for(int j=0;j<outerNeighbors.size();j++) {
				if(j>0) s += (","); 
				s += (""+outerNeighbors.get(j));
			}
		}
		if(innerNeighbors.size() > 0) {
			s += (", "); 
			for(int j=0;j<innerNeighbors.size();j++) {
				if(j>0) s += (","); 
				s += (""+innerNeighbors.get(j));
			}
		}
		s += (" ---\n"); 
		for(int k=0;k<polygon.size();k++) {
			Point p = polygon.get(k);
			int idx = -2;
			int idx2 = -1;
			if(k < opposingRegions.size()) idx = opposingRegions.get(k);
			if(k < opposingRivers.size()) idx2 = opposingRivers.get(k);
			if(idx2 >= 0) s += ("("+p.xInt()+","+p.yInt()+","+idx+",&"+idx2+")");
			else s += ("("+p.xInt()+","+p.yInt()+","+idx+")");
			if(k<polygon.size()-1) s += (", ");
		}
		return s;
	}
}