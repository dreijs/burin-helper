package util;

public interface Point {

	public PointInt asIntPoint();
	
	public PointFloat asFloatPoint();
	
	public int xInt();
	
	public int yInt();
	
	public double xFloat();
	
	public double yFloat();
	
	boolean equals(Point p);
}
