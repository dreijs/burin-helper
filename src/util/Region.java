package util;

import java.util.ArrayList;
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
}