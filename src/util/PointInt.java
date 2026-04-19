package util;

public class PointInt implements Point {
	public final int x;
	public final int y;

	public PointInt(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public PointInt asIntPoint() {
		return this;
	}
	
	public PointFloat asFloatPoint() {
		return new PointFloat(xFloat(), yFloat());
	}
	
	public int xInt() {
		return x;
	}
	
	public int yInt(){
		return y;
	}
	
	public double xFloat() {
		return 1. * x;
	}
	
	public double yFloat() {
		return 1. * y;
	}
	
	public boolean equals(Point p) {
		return x == p.xInt() && y == p.yInt(); 
	}

	public String toString() {
		return "("+x+", "+y+")";
	}
}