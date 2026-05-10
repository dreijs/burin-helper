package util;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.noding.snapround.GeometryNoder;
import org.locationtech.jts.operation.overlay.snap.GeometrySnapper;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import util.GeometryUtils;
import vectormaps.PolygonCreator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

//import net.sf.geographiclib.Geodesic;
//import net.sf.geographiclib.PolygonArea;
//import net.sf.geographiclib.PolygonResult;



class LineStringAndAngle {
	LineString c;
	double angle;
	
	LineStringAndAngle(LineString c, double angle) {
		this.c = c;
		this.angle = angle;
	}
}

public class GeometryUtils {

	public static final Double EPSILON = 1e-9;

	public static boolean isNonIntersectingPolygon(List<Point> points) {
		int n = points.size();
		for(int i=0;i<n;i++) {
			int ii = (i+1)%n;
			for(int j=0;j<n;j++) {
				int jj = (j+1)%n;

				if(doSegmentsIntersect(points.get(i), points.get(ii), points.get(j), points.get(jj))) return false;
			}
		}
		return true;
	}

	public static List<Point> getLine(int x0, int y0, int x1, int y1) {
		List<Point> line = new ArrayList<>();
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		int sx = x0 < x1 ? 1 : -1; // Step direction for x
		int sy = y0 < y1 ? 1 : -1; // Step direction for y
		int err = dx - dy; // Initial decision parameter (error)
		int e2; // Used for decision making within the loop

		while (true) {
			line.add(new PointInt(x0, y0)); // Plot the current pixel

			if (x0 == x1 && y0 == y1) {
				break; // Reached the end point, exit loop
			}

			e2 = 2 * err;
			if (e2 > -dy) {
				err -= dy;
				x0 += sx; // Move in x direction
			}
			if (e2 < dx) {
				err += dx;
				y0 += sy; // Move in y direction
			}
		}
		return line;
	}

	public static boolean isEar(Point a, Point b, Point c, List<Point> vertices, boolean clockwise) {
		// 1. Check if the vertex b is convex
		if (!isConvex(a, b, c, clockwise)) {
			return false;
		}

		// 2. Check if any other vertex of the polygon lies inside the triangle (a, b, c)
		for (Point p : vertices) {
			// Skip the triangle's own vertices
			if (p.equals(a) || p.equals(b) || p.equals(c)) {
				continue;
			}

			if (isInsideTriangle(a, b, c, p)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if the angle at pCurrent is convex.
	 * This can be determined by the orientation of the triangle (pPrev, pCurrent, pNext).
	 * For a counter-clockwise polygon, a convex vertex will result in a counter-clockwise orientation.
	 */
	public static boolean isConvex(Point a, Point b, Point c, boolean clockwise) {
		// Cross product of vectors AB and BC.
		double crossProduct = (b.xFloat() - a.xFloat()) * (c.yFloat() - b.yFloat()) - (b.yFloat() - a.yFloat()) * (c.xFloat() - b.xFloat());
		// For counter-clockwise polygon, cross product should be positive for a convex vertex.
		if(clockwise) return crossProduct <= EPSILON;
		return crossProduct >= -EPSILON;
	}

	/**
	 * Checks if a point 'p' is inside the triangle formed by 't1', 't2', 't3'.
	 * This can be done using barycentric coordinates or by checking the orientation of sub-triangles.
	 */
	public static boolean isInsideTriangle(Point a, Point b, Point c, Point p) {
		// Calculate the cross products for the three sub-triangles (PAB, PBC, PCA)
		double cp1 = (b.xFloat() - a.xFloat()) * (p.yFloat() - a.yFloat()) - (b.yFloat() - a.yFloat()) * (p.xFloat() - a.xFloat());
		double cp2 = (c.xFloat() - b.xFloat()) * (p.yFloat() - b.yFloat()) - (c.yFloat() - b.yFloat()) * (p.xFloat() - b.xFloat());
		double cp3 = (a.xFloat() - c.xFloat()) * (p.yFloat() - c.yFloat()) - (a.yFloat() - c.yFloat()) * (p.xFloat() - c.xFloat());

		// Point is inside if all cross products have the same sign (or are zero)
		return (cp1 >= -EPSILON && cp2 >= -EPSILON && cp3 >= -EPSILON) || (cp1 <= EPSILON && cp2 <= EPSILON && cp3 <= EPSILON);
	}

	public static double distance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.xFloat() - p2.xFloat(), 2) + Math.pow(p1.yFloat() - p2.yFloat(), 2));
	}

	// Google AI

	public static boolean sharesSegmentWith(Point p1, Point q1, Point p2, Point q2, boolean trace) {
		return sharesSegmentWith(p1, q1, p2, q2, EPSILON, EPSILON, trace);
	}

	public static boolean sharesSegmentWith(Point p1, Point q1, Point p2, Point q2, double threshold1, double threshold2, boolean trace) {
		// check if they are parallel
		double o1 = orientation(p2,p1,q2,threshold1,threshold1);
		double o2 = orientation(p2,q1,q2,threshold1,threshold1);
		if(o1 < 0 || o1 > 0 || o2 < 0 || o2 > 0) return false;
		// check if they are not strictly left or right of each other
		double f1 = getProjectionFactor(p1,p2,q2);
		double f2 = getProjectionFactor(q1,p2,q2);
		if(trace) System.out.println(f1+" "+f2);
		if((f1 < -threshold2 && f2 < -threshold2) || (f1 > 1+threshold2 && f2 > 1+threshold2)) return false;
		return true;
	}

	public static int orientation(Point p, Point q, Point r) {
		return orientation(p, q, r, EPSILON, EPSILON);
	}

	// orientation of q with respect to p and r
	public static int orientation(Point p, Point q, Point r, double threshold1, double threshold2) {
		if(distance(p,q) < threshold1 || distance(q,r) < threshold1) return 0; 
		double val = (q.yFloat() - p.yFloat()) * (r.xFloat() - q.xFloat()) -
				(q.xFloat() - p.xFloat()) * (r.yFloat() - q.yFloat());

		if (val > -threshold2 && val < threshold2) return 0; // Collinear
		return (val > 0) ? 1 : 2; // Clockwise or Counterclockwise
	}

	// 0.0 if is p at q, 1.0 if p is at r, and 0.5 if it exactly at the midpoint
	public static double getProjectionFactor(Point p, Point q, Point r) {
		double dx = r.xFloat() - q.xFloat();
		double dy = r.yFloat() - q.yFloat();
		double magSq = dx * dx + dy * dy;

		// Handle degenerate case where q and r are the same point
		if (magSq == 0) {
			return 0.0;
		}

		// Dot product of vector qp and vector qr
		return ((p.xFloat() - q.xFloat()) * dx + (p.yFloat() - q.yFloat()) * dy) / magSq;
	}

	public static boolean doSegmentsIntersect(Point p1, Point q1, Point p2, Point q2) {
		if(p1.equals(p2) || p1.equals(q2) || q1.equals(p2) || q1.equals(q2)) return false;
		// Find the four orientations needed for the general case
		int o1 = orientation(p1, q1, p2);
		int o2 = orientation(p1, q1, q2);
		int o3 = orientation(p2, q2, p1);
		int o4 = orientation(p2, q2, q1);

		// General case: segments intersect if orientations are different
		if (o1 != o2 && o3 != o4) {
			return true;
		}

		return false; // Doesn't fall in any of the above cases
	}

	public static double perpendicularDistance(Point point, Point lineStart, Point lineEnd) {
		double lineLengthSquared = Math.pow(lineEnd.xFloat() - lineStart.xFloat(), 2) + Math.pow(lineEnd.yFloat() - lineStart.yFloat(), 2);

		// Handle case of zero-length line
		if (lineLengthSquared == 0.0) {
			return Math.sqrt(Math.pow(point.xFloat() - lineStart.xFloat(), 2) + Math.pow(point.yFloat() - lineStart.yFloat(), 2));
		}

		double t = ((point.xFloat() - lineStart.xFloat()) * (lineEnd.xFloat() - lineStart.xFloat()) + (point.yFloat() - lineStart.yFloat()) * (lineEnd.yFloat() - lineStart.yFloat())) / lineLengthSquared;
		t = Math.max(0, Math.min(1, t)); // Clamp t to [0, 1]

		double projectionX = lineStart.xFloat() + t * (lineEnd.xFloat() - lineStart.xFloat());
		double projectionY = lineStart.yFloat() + t * (lineEnd.yFloat() - lineStart.yFloat());

		return Math.sqrt(Math.pow(point.xFloat() - projectionX, 2) + Math.pow(point.yFloat() - projectionY, 2));
	}

	public static double calculatePolygonSignedArea(List<Point> vertices) {
		if (vertices == null || vertices.size() < 3) {
			throw new IllegalArgumentException("A polygon must have at least 3 vertices.");
		}

		double sum = 0;
		int n = vertices.size();

		for (int i = 0; i < n; i++) {
			Point p1 = vertices.get(i);
			Point p2 = vertices.get((i + 1) % n); // Connects last vertex to first

			sum += (p1.yFloat() + p2.yFloat()) * (p1.xFloat() - p2.xFloat());
		}

		return 0.5 * sum;
	}

	public static double calculatePolygonArea(List<Point> vertices) {
		return Math.abs(calculatePolygonSignedArea(vertices));
	}

	public static double polygonArea(List<Point> vertices) {
		GeneralPath path = new GeneralPath();
		if (vertices.size() < 3) return 0.0;

		path.moveTo(vertices.get(0).xFloat(), vertices.get(0).yFloat());
		for (int i = 1; i < vertices.size(); i++) {
			path.lineTo(vertices.get(i).xFloat(), vertices.get(i).yFloat());
		}
		path.closePath();

		// Area constructor handles decomposition internally
		Area area = new Area(path);
		PathIterator iterator = new FlatteningPathIterator(area.getPathIterator(null), 0.01);

		return getAreaFromIterator(iterator);
	}

	// Calculates area using the shoelace formula on decomposed segments
	private static double getAreaFromIterator(PathIterator i) {
		double area = 0.0;
		double[] coords = new double[6];
		double startX = 0, startY = 0, currentX = 0, currentY = 0;

		while (!i.isDone()) {
			int segType = i.currentSegment(coords);
			if (segType == PathIterator.SEG_MOVETO) {
				startX = coords[0]; startY = coords[1];
				currentX = startX; currentY = startY;
			} else if (segType == PathIterator.SEG_LINETO) {
				area += currentX * coords[1] - coords[0] * currentY;
				currentX = coords[0]; currentY = coords[1];
			} else if (segType == PathIterator.SEG_CLOSE) {
				area += currentX * startY - startX * currentY;
			}
			i.next();
		}
		return Math.abs(area / 2.0);
	}

	public static Rectangle getIntegerBoundingBoxFromList(List<Point> l) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;

		for(int j=0;j<l.size();j++) {
			Point p = l.get(j);

			minX = Math.min(p.xInt(), minX);
			minY = Math.min(p.yInt(), minY);
			maxX = Math.max(p.xInt(), maxX);
			maxY = Math.max(p.yInt(), maxY);
		}

		return new Rectangle(minX, minY, maxX-minX, maxY-minY);
	}

	public static Rectangle getIntegerBoundingBoxFromLists(List<List<Point>> lists) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;

		for(int i=0;i<lists.size();i++) {
			List<Point> l = lists.get(i);
			for(int j=0;j<l.size();j++) {
				Point p = l.get(j);

				minX = Math.min(p.xInt(), minX);
				minY = Math.min(p.yInt(), minY);
				maxX = Math.max(p.xInt(), maxX);
				maxY = Math.max(p.yInt(), maxY);
			}
		}

		return new Rectangle(minX, minY, maxX-minX, maxY-minY);
	}

	public static Rectangle2D.Double getFloatBoundingBoxFromList(List<Point> l) {
		double minX = Integer.MAX_VALUE;
		double minY = Integer.MAX_VALUE;
		double maxX = Integer.MIN_VALUE;
		double maxY = Integer.MIN_VALUE;

		for(int j=0;j<l.size();j++) {
			Point p = l.get(j);

			minX = Math.min(p.xFloat(), minX);
			minY = Math.min(p.yFloat(), minY);
			maxX = Math.max(p.xFloat(), maxX);
			maxY = Math.max(p.yFloat(), maxY);
		}

		return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
	}

	public static Rectangle2D.Double getFloatBoundingBoxFromLists(List<List<Point>> ls) {
		double minX = Integer.MAX_VALUE;
		double minY = Integer.MAX_VALUE;
		double maxX = Integer.MIN_VALUE;
		double maxY = Integer.MIN_VALUE;

		for(List<Point> l : ls) {
			for(int j=0;j<l.size();j++) {
				Point p = l.get(j);

				minX = Math.min(p.xFloat(), minX);
				minY = Math.min(p.yFloat(), minY);
				maxX = Math.max(p.xFloat(), maxX);
				maxY = Math.max(p.yFloat(), maxY);
			}
		}

		return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
	}

	public static List<List<Point>> simplifyJts(List<List<Point>> points, double distanceTolerance) {
		GeometryFactory gf = new GeometryFactory();

		// polygon
		LineString[] lines = new LineString[points.size()];
		for(int i=0;i<points.size();i++) {
			List<Coordinate> polyCoordList = new ArrayList<Coordinate>();
			for(int j=0;j<points.get(i).size();j++) {
				Point p = points.get(i).get(j);
				polyCoordList.add(new Coordinate(p.xFloat(), p.yFloat()));
			}
			Coordinate[] lineCoords = polyCoordList.toArray(new Coordinate[0]);
			lines[i] = gf.createLineString(lineCoords);
		}
		MultiLineString lineStrings= gf.createMultiLineString(lines);

		// use DouglasPeuckerSimplifier to simplify the polygon
		DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(lineStrings);
		simplifier.setDistanceTolerance(distanceTolerance);

		// Optional: ensure valid topology, though it adds processing time
		simplifier.setEnsureValid(true);

		Geometry simplifiedGeometry = simplifier.getResultGeometry();
		GeometrySnapper.snapToSelf(simplifiedGeometry, distanceTolerance, true);

		//		System.out.println("Simplified polygon points (Tolerance: " + distanceTolerance + "): " + simplifiedGeometry.getNumPoints());
		//		System.out.println("Simplified Geometry WKT: " + simplifiedGeometry.toText());

		List<List<Point>> result = new ArrayList<List<Point>>();

		if (simplifiedGeometry instanceof MultiLineString) {
			MultiLineString multilines = (MultiLineString) simplifiedGeometry;
			for (int i=0;i<multilines.getNumGeometries();i++) {
				LineString line = (LineString) multilines.getGeometryN(i);
				List<Coordinate> coords = List.of(line.getCoordinates());
				List<Point> newPoints = new ArrayList<Point>();
				for(int j=0;j<coords.size();j++) {
					Coordinate c = coords.get(j);
					newPoints.add(new PointFloat(c.x, c.y));
				}
				result.add(newPoints);
			}
		} else if(simplifiedGeometry instanceof LineString) {
			LineString line = (LineString) simplifiedGeometry;
			List<Coordinate> coords = List.of(line.getCoordinates());
			List<Point> newPoints = new ArrayList<Point>();
			for(int j=0;j<coords.size();j++) {
				Coordinate c = coords.get(j);
				newPoints.add(new PointFloat(c.x, c.y));
			}
			result.add(newPoints);
		}
		return result;
	}

	// from Google AI:
	//	public static List<List<Point>> splitPolygon(List<Point> polygonPoints, List<Point> lines) {
	//		ArrayList<List<Point>> result = new ArrayList<List<Point>>();
	//		GeometryFactory gf = new GeometryFactory();
	//
	//		// polygon
	//		List<Coordinate> polyCoordList = new ArrayList<Coordinate>();
	//		for(int i=0;i<polygonPoints.size();i++) {
	//			Point p = polygonPoints.get(i);
	//			polyCoordList.add(new Coordinate(p.xFloat(), p.yFloat()));
	//		}
	//		if(!polygonPoints.get(0).equals(polygonPoints.get(polygonPoints.size()-1))) {
	//			polyCoordList.add(new Coordinate(polygonPoints.get(0).xFloat(), polygonPoints.get(0).yFloat()));
	//		}
	//		Coordinate[] polyCoords = polyCoordList.toArray(new Coordinate[0]);
	//
	//		Polygon polygon = gf.createPolygon(polyCoords);
	//		if (!polygon.isValid()) {
	//			polygon = (Polygon) org.locationtech.jts.geom.util.GeometryFixer.fix(polygon);
	//		}
	//
	//		List<Coordinate> coords = new ArrayList<Coordinate>();
	//		for(int i=0;i<lines.size();i++) {
	//			Point p = lines.get(i);
	//			coords.add(new Coordinate(p.xFloat(), p.yFloat()));
	//		}
	//
	//		Coordinate[] lineCoords = coords.toArray(new Coordinate[0]); 
	//		LineString cuttingLine = gf.createLineString(lineCoords);
	//		if (!cuttingLine.isValid()) {
	//			cuttingLine = (LineString) org.locationtech.jts.geom.util.GeometryFixer.fix(cuttingLine);
	//		}
	//
	//		Geometry cuttingLineInsidePolygonGeometry = polygon.intersection(cuttingLine);
	//
	//		if (!cuttingLineInsidePolygonGeometry.isEmpty()) {
	//			if (!cuttingLineInsidePolygonGeometry.isValid()) {
	//				cuttingLineInsidePolygonGeometry = org.locationtech.jts.geom.util.GeometryFixer.fix(cuttingLineInsidePolygonGeometry);
	//			}
	//
	//			// Create a geometry consisting of the polygon boundary + the cutting line
	//			Geometry boundaryAndLine = polygon.getBoundary().union(cuttingLineInsidePolygonGeometry);
	//
	//			if (!boundaryAndLine.isValid()) {
	//				boundaryAndLine = org.locationtech.jts.geom.util.GeometryFixer.fix(boundaryAndLine);
	//			}
	//
	//			//			System.out.println(boundaryAndLine);
	//
	//			// Use Polygonizer to reconstruct polygons from lines
	//			Polygonizer polygonizer = new Polygonizer();
	//			polygonizer.add(boundaryAndLine);
	//
	//			Collection<Polygon> polygons = polygonizer.getPolygons();
	//			Collection<LineString> dangles = polygonizer.getDangles(); 
	//
	//
	//			for (Polygon poly : polygons) {
	//				coords = List.of(poly.getCoordinates());
	//				List<Point> points = new ArrayList<Point>();
	//				for(int i=0;i<coords.size()-1;i++) { // do not include the last point because the region polygon do not include the first point twice
	//					Coordinate c = coords.get(i);
	//					points.add(new PointFloat(c.x, c.y));
	//
	//					for(LineString dangle : dangles) {
	//						Coordinate cc = dangle.getCoordinateN(0);
	//						if(cc.x == c.x && cc.y == c.y) {
	//							for(int j=1;j<dangle.getNumPoints();j++) {
	//								Coordinate cc2 = dangle.getCoordinateN(j);
	//								points.add(new PointFloat(cc2.x, cc2.y));
	//							}
	//							for(int j=1;j<dangle.getNumPoints();j++) {
	//								Coordinate cc2 = dangle.getCoordinateN(dangle.getNumPoints() - j - 1);
	//								points.add(new PointFloat(cc2.x, cc2.y));
	//							}
	//						}
	//
	//						cc = dangle.getCoordinateN(dangle.getNumPoints()-1);
	//						if(cc.x == c.x && cc.y == c.y) {
	//							for(int j=1;j<dangle.getNumPoints();j++) {
	//								Coordinate cc2 = dangle.getCoordinateN(dangle.getNumPoints() - j - 1);
	//								points.add(new PointFloat(cc2.x, cc2.y));
	//							}
	//							for(int j=1;j<dangle.getNumPoints();j++) {
	//								Coordinate cc2 = dangle.getCoordinateN(j);
	//								points.add(new PointFloat(cc2.x, cc2.y));
	//							}
	//						}
	//					}
	//				}
	//
	//
	//
	//				result.add(points);
	//			}
	//
	//			return result;
	//		}
	//
	//		result.add(polygonPoints);
	//		return result;
	//	}

	static Coordinate[] polygonPointListToCoordinates(List<Point> polygonPoints) {
		List<Coordinate> polyCoordList = new ArrayList<Coordinate>();
		for(int i=0;i<polygonPoints.size();i++) {
			Point p = polygonPoints.get(i);
			polyCoordList.add(new Coordinate(p.xFloat(), p.yFloat()));
		}
		if(!polygonPoints.get(0).equals(polygonPoints.get(polygonPoints.size()-1))) {
			polyCoordList.add(new Coordinate(polygonPoints.get(0).xFloat(), polygonPoints.get(0).yFloat()));
		}
		Coordinate[] polyCoords = polyCoordList.toArray(new Coordinate[0]);
		return polyCoords;
	}

	static Coordinate[] linePointListToCoordinates(List<Point> polygonPoints) {
		List<Coordinate> polyCoordList = new ArrayList<Coordinate>();
		for(int i=0;i<polygonPoints.size();i++) {
			Point p = polygonPoints.get(i);
			polyCoordList.add(new Coordinate(p.xFloat(), p.yFloat()));
		}
		Coordinate[] polyCoords = polyCoordList.toArray(new Coordinate[0]);
		return polyCoords;
	}
	
	static List<Point> polygonToPointList(Polygon polygon) {
		List<Point> pointList = new ArrayList<Point>();
		LinearRing ring = polygon.getExteriorRing();
		for(int i=0;i<ring.getNumPoints();i++) {
			Coordinate c = ring.getCoordinateN(i);
			pointList.add(new PointFloat(c.x, c.y));
		}
		
		return pointList;
	}

	public static List<List<Point>> splitPolygon(List<Point> polygonPoints, List<List<Point>> lines) {
		return splitPolygon(polygonPoints, lines, 0);
	}
	
	public static boolean isValidPolygon(List<Point> polygonPoints) {
		GeometryFactory gf = new GeometryFactory();
		Coordinate[] polyCoords = polygonPointListToCoordinates(polygonPoints);
		Polygon polygon = gf.createPolygon(polyCoords);
		Geometry geo = org.locationtech.jts.geom.util.GeometryFixer.fix(polygon);
		
		if(geo instanceof MultiPolygon) return false;
		if(geo instanceof Polygon) return true;
		return false;
	}

	public static List<List<Point>> splitPolygon(List<Point> polygonPoints, List<List<Point>> lines, int idx) {
		ArrayList<List<Point>> result = new ArrayList<List<Point>>();
		GeometryFactory gf = new GeometryFactory();

		// polygon
		Coordinate[] polyCoords = polygonPointListToCoordinates(polygonPoints);

		Polygon polygon = gf.createPolygon(polyCoords);
		if (!polygon.isValid()) {
			Geometry geo = org.locationtech.jts.geom.util.GeometryFixer.fix(polygon);
			if(geo instanceof MultiPolygon) {
				System.out.println(geo);
				System.out.println(idx);
				PolygonCreator.visualizePolygon(polygonPoints, System.getProperty("user.dir")+"\\output\\map\\polygons\\debug_multipolygon.png", 32);
			}
			polygon = (Polygon) org.locationtech.jts.geom.util.GeometryFixer.fix(polygon);
		}

		LineString[] crossLines = new LineString[lines.size()];
		for(int i=0;i<lines.size();i++) {
			Coordinate[] lineCoords = linePointListToCoordinates(lines.get(i));
			LineString cuttingLine = gf.createLineString(lineCoords);
			if (!cuttingLine.isValid()) {
				cuttingLine = (LineString) org.locationtech.jts.geom.util.GeometryFixer.fix(cuttingLine);
			}

			crossLines[i] = cuttingLine;
		}

		MultiLineString aaa = new MultiLineString(crossLines, gf);
		Geometry cuttingLineInsidePolygonGeometry = polygon.intersection(aaa);

		if (!cuttingLineInsidePolygonGeometry.isEmpty()) {
			if (!cuttingLineInsidePolygonGeometry.isValid()) {
				cuttingLineInsidePolygonGeometry = org.locationtech.jts.geom.util.GeometryFixer.fix(cuttingLineInsidePolygonGeometry);
			}

			// Create a geometry consisting of the polygon boundary + the cutting line
			//			System.out.println(cuttingLineInsidePolygonGeometry);
			Geometry boundaryAndLine = polygon.getBoundary().union(cuttingLineInsidePolygonGeometry);

			if (!boundaryAndLine.isValid()) {
				boundaryAndLine = org.locationtech.jts.geom.util.GeometryFixer.fix(boundaryAndLine);
			}

			PrecisionModel pm = new PrecisionModel(1000000.);
			GeometryNoder noder = new GeometryNoder(pm);
			List<Geometry> boundaryAndLineCollection = new ArrayList<Geometry>();
			boundaryAndLineCollection.add(boundaryAndLine);
			Collection<GeometryUtils> nodedLines = noder.node(boundaryAndLineCollection);

			// Use Polygonizer to reconstruct polygons from lines
			Polygonizer polygonizer = new Polygonizer();
			polygonizer.add(nodedLines);

			Collection<Polygon> polygons = polygonizer.getPolygons();
			Collection<LineString> dangles = polygonizer.getDangles(); 

//			int bbb = 28895;
//			if(idx == bbb) {
//				System.out.println("polygon "+bbb);
//				int ii=0;
//				List<List<Point>> allPolygons = new ArrayList<List<Point>>();
//				for (Polygon poly : polygons) {
//					System.out.println(poly);
//					List<Point> polyPoints = polygonToPointList(poly);
//					PolygonCreator.visualizePolygon(polyPoints, System.getProperty("user.dir")+"\\output\\map\\polygons\\debug_bigpolygon_"+bbb+"_"+ii+".png", 32);
//					ii++;
//					allPolygons.add(polyPoints);
//				}
//				System.out.println("dangles: "+dangles);
//				System.out.println("noded lines: "+nodedLines);
//				PolygonCreator.visualizePolygons(allPolygons, System.getProperty("user.dir")+"\\output\\map\\polygons\\debug_bigpolygon_"+bbb+"_all.png", 32);
//				PolygonCreator.visualizePolygon(polygonPoints, System.getProperty("user.dir")+"\\output\\map\\polygons\\debug_bigpolygon_"+bbb+"_all_pre.png", 32);
//			}

			//			for (Polygon poly : polygons) {
			//				System.out.println(poly);
			//			}

			for (Polygon poly : polygons) {
				boolean covered = false;
				for(Polygon poly2 : polygons) {
					if(!poly2.equals(poly)) {
						Polygon p1 = gf.createPolygon(poly.getExteriorRing(), null);
						Polygon p2 = gf.createPolygon(poly2.getExteriorRing(), null);
						if(p2.contains(p1)) covered = true;
					}
				}

				if(!covered) {
					List<Coordinate> coords = new ArrayList<>(List.of(poly.getExteriorRing().getCoordinates()));
					List<List<Coordinate>> innerRings = new ArrayList<List<Coordinate>>();
					for(int i=0;i<poly.getNumInteriorRing();i++) {
						innerRings.add(List.of(poly.getInteriorRingN(i).getCoordinates()));
					}
					while(innerRings.size() > 0) {
						for(int i = innerRings.size() - 1;i>=0;i--) {
							List<Coordinate> ring = innerRings.get(i);
							int eqJ = -1;
							int eqK = -1;
							for(int j=0;j<coords.size();j++) {
								for(int k=0;k<ring.size();k++) {
									if(coords.get(j).equals2D(ring.get(k))) {
										eqJ = j;
										eqK = k;
									}
								}
							}
							if(eqK >= 0) {
								for(int k=0;k<ring.size()-1;k++) {
									int kk = (eqK + k) % (ring.size()-1);
									coords.add(eqJ+k, ring.get(kk));
								}
								innerRings.remove(i);
							}

						}
					}

					List<LineString> filteredDangles = new ArrayList<>();
					for(LineString baseDangle : dangles) {
						Geometry dangleGeometry = baseDangle.intersection(poly);
						if(dangleGeometry.getNumPoints() > 1) {
							filteredDangles.add((LineString) dangleGeometry);
						}
					}

					List<Point> points = new ArrayList<Point>();

					// process dangles, make sure they are added in the correct order
					Map<Integer,List<LineString>> dangleMap = new HashMap<Integer,List<LineString>>();
					for(int i=0;i<coords.size()-1;i++) {
						Coordinate c = coords.get(i);
						for(LineString dangle : filteredDangles) {
							Coordinate cc = dangle.getCoordinateN(0);
							if(cc.x == c.x && cc.y == c.y) {
								if(!dangleMap.keySet().contains(i)) dangleMap.put(i, new ArrayList<LineString>());
								dangleMap.get(i).add(dangle);
							}
							cc = dangle.getCoordinateN(dangle.getNumPoints()-1);
							if(cc.x == c.x && cc.y == c.y) {
								if(!dangleMap.keySet().contains(i)) dangleMap.put(i, new ArrayList<LineString>());
								dangleMap.get(i).add(dangle.reverse());
							}
						}
					}
					
//					if(idx == bbb) System.out.println(coords);

					for(int i=0;i<coords.size()-1;i++) { // do not include the last point because the region polygons do not include the first point twice
						Coordinate c = coords.get(i);
						points.add(new PointFloat(c.x, c.y));
						if(dangleMap.keySet().contains(i)) {
							int iprev = i>0 ? i-1: coords.size()-2;
							Coordinate cprev = coords.get(iprev);
							double angleprev = Math.atan2(cprev.y - c.y, cprev.x - c.x) + Math.PI;
							if(angleprev > 2 * Math.PI - EPSILON) angleprev = 0;
//							if(idx == bbb) System.out.println(angleprev+" "+cprev+" to "+c+" ");
							
							List<LineStringAndAngle> list = new ArrayList<LineStringAndAngle>();
							
							for(LineString dangle : dangleMap.get(i)) {
								double angle = Math.atan2(dangle.getCoordinateN(1).y - c.y, dangle.getCoordinateN(1).x - c.x) + Math.PI;
//								if(idx == bbb) System.out.println(angle+" "+Math.abs(angle - angleprev));
								list.add(new LineStringAndAngle(dangle, Math.abs(angle - angleprev)));
							}
							
//							if(list.get(0).angle > angleprev && list.get(0).angle < anglenext) {
//							if(anglenext < angleprev) {
								list.sort((e1, e2) -> Double.compare(e1.angle, e2.angle));
//							} else list.sort((e1, e2) -> Double.compare(e1.angle, e2.angle));
							
							for(int ii=0;ii<list.size();ii++) {
								LineString dangle = list.get(ii).c;
								
								for(int j=1;j<dangle.getNumPoints();j++) {
									Coordinate cc2 = dangle.getCoordinateN(j);
									points.add(new PointFloat(cc2.x, cc2.y));
								}
								for(int j=1;j<dangle.getNumPoints();j++) {
									Coordinate cc2 = dangle.getCoordinateN(dangle.getNumPoints() - j - 1);
									points.add(new PointFloat(cc2.x, cc2.y));
								}
							}
						}
					}

					result.add(points);
				}
			}

			if(result.size() == 0) {
				System.out.println("AAA "+idx);
				System.out.println(polygons);
				System.out.println(dangles);
				System.out.println(nodedLines);

				result.add(polygonPoints);
			}

//			if(idx == bbb) {
//				for(int zz = 0; zz < result.size();zz++) {
//					PolygonCreator.visualizePolygon(result.get(zz), System.getProperty("user.dir")+"\\output\\map\\polygons\\debug_polygon_"+zz+".png", 32);
//				}
//			}

			return result;
		}

		result.add(polygonPoints);
		return result;
	}

	// https://stackoverflow.com/questions/2861272/polygon-area-calculation-using-latitude-and-longitude-generated-from-cartesian-s
	// output: square kilometers
	public static double ConvertToRadian(double input)
	{
		return input * Math.PI / 180;
	}

	public static double calculatePolygonAreaGlobe(List<Point> coordinates)
	{
		double area = 0;
		int n = coordinates.size();

		if (n > 2)
		{
			for (var i = 0; i < n - 1; i++)
			{
				Point p1 = coordinates.get(i);
				Point p2 = coordinates.get((i + 1) % n); // Connects last vertex to first
				area += ConvertToRadian(p2.xFloat() - p1.xFloat()) * (2 + Math.sin(ConvertToRadian(p1.yFloat())) + Math.sin(ConvertToRadian(p2.yFloat())));
			}

			area = area * 6378.137 * 6378.137 / 2;
		}

		return Math.abs(area);
	}
	public static double calculatePolygonAreaGlobe(List<Point> coordinates, int w, int h)
	{
		double area = 0;
		int n = coordinates.size();

		if (n > 2)
		{
			for (var i = 0; i < n - 1; i++)
			{
				Point p1 = coordinates.get(i);
				Point p2 = coordinates.get((i + 1) % n); // Connects last vertex to first
				area += ConvertToRadian(360. * (p2.xFloat() - p1.xFloat())/w) * (2 + Math.sin(ConvertToRadian(180. * p1.yFloat() / h - 90)) + Math.sin(ConvertToRadian(180. * p2.yFloat() / h - 90)));
			}

			area = area * 6378.137 * 6378.137 / 2;
		}

		return Math.abs(area);
	}

	// from Google AI:
	public static List<Point> simplify(List<Point> points, double epsilon) {
		if (points.size() <= 2) {
			return new ArrayList<>(points);
		}

		int start = 0;
		int end = points.size() - 1;
		List<Point> simplified = new ArrayList<>();
		simplified.add(points.get(start));
		simplifyRecursive(points, start, end, epsilon, simplified);
		simplified.add(points.get(end));

		return simplified;
	}

	private static void simplifyRecursive(List<Point> points, int start, int end, double epsilon, List<Point> simplified) {
		double dMax = 0;
		int index = 0;

		// Find the point with the maximum perpendicular distance
		for (int i = start + 1; i < end; i++) {
			double distance = GeometryUtils.perpendicularDistance(points.get(i), points.get(start), points.get(end));
			if (distance > dMax) {
				dMax = distance;
				index = i;
			}
		}

		// If max distance is greater than epsilon, recursively simplify
		if (dMax > epsilon) {
			simplifyRecursive(points, start, index, epsilon, simplified);
			simplified.add(points.get(index));
			simplifyRecursive(points, index, end, epsilon, simplified);
		}
	}
}
