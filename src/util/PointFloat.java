package util;

public class PointFloat implements Point {
	public final double x;
	public final double y;

	public PointFloat(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public PointInt asIntPoint() {
		return new PointInt(xInt(), yInt());
	}
	
	public PointFloat asFloatPoint() {
		return this;
	}
	
	public int xInt() {
		return (int) Math.round(x);
	}
	
	public int yInt(){
		return (int) Math.round(y);
	}
	
	public double xFloat() {
		return x;
	}
	
	public double yFloat() {
		return y;
	}

	public boolean equals(Point p) {
		return x == p.xFloat() && y == p.yFloat(); 
	}

	public String toString() {
		return "("+x+", "+y+")";
	}
}