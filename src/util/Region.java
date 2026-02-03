package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Region {
	public int regionIdx;
	public int colorData;
	public int drawOrder;
	public byte[] triangleDrawOrder;
	public List<Point> polygon;
	public List<Integer> opposingRegions; // opposingRegionss.get(i) is the opposing region of the edge between point i and point (i + 1) % n
	public List<Integer> outerNeighbors;
	public List<Integer> innerNeighbors;

	public Region(int regionIdx) {
		this.regionIdx = regionIdx;
		this.drawOrder = -1;
		this.triangleDrawOrder = new byte[] {};
		this.polygon = new ArrayList<Point>();
		this.opposingRegions = new ArrayList<Integer>();
		this.outerNeighbors = new ArrayList<Integer>();
		this.innerNeighbors = new ArrayList<Integer>();
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
//		int r1 = 57219;
//		int r2 = 57217;
//		boolean print = (regionIdx == r1 || regionIdx == r2);
//		if(print) System.out.println("*+*+* "+regionIdx+" "+Arrays.toString(points));
		
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
		
//		if(print) System.out.println("final"+idx1+" "+idx2);

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
			if(Geometry.doSegmentsIntersect(polygon.get(i), polygon.get(j), polygon.get(indices[0]), polygon.get(indices[1]))) {
				return false;
			}
		}
		return true;
	}
	
	public Point[] getSegmentPoints(int[] indices, int oppRegion) {
		if(indices == null) return new Point[] {};
		
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
//		int r1 = 57219;
//		int r2 = 57217;
//		boolean print = (regionIdx == r1 && oppRegion == r2) || (regionIdx == r2 && oppRegion == r1);
//		
//		if(print) System.out.println(this);
		
		if(indices == null) return;
		
//		if(print) System.out.println(regionIdx+": "+indices[0]+" to "+indices[1] +" ("+oppRegion+")");

		if(indices[1] > indices[0]) {
			for (int i = indices[1]-1; i != indices[0];i--) {
//				if(print) System.out.println(regionIdx+" !remove "+polygon.get(i)+" "+opposingRegions.get(i));
				polygon.remove(i); 
				opposingRegions.remove(i); 
			}
		} else {
			for (int i = polygon.size()-1; i != indices[0];i--) {
//				if(print) System.out.println(regionIdx+" *remove "+polygon.get(i)+" "+opposingRegions.get(i));
				polygon.remove(i); 
				opposingRegions.remove(i); 
			}
			for (int i = indices[1]-1; i >= 0;i--) {
//				if(print) System.out.println(regionIdx+" *remove "+polygon.get(i)+" "+opposingRegions.get(i));
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
			if(k < opposingRegions.size()) idx = opposingRegions.get(k);
			s += ("("+p.x+","+p.y+","+idx+")");
			if(k<polygon.size()-1) s += (", ");
		}
		return s;
	}
}