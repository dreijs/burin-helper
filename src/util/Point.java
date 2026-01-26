package util;

public class Point {
	public final int x;
	public final int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean equals(Point p) {
		return x == p.x && y == p.y; 
	}

	public String toString() {
		return "("+x+", "+y+")";
	}
}