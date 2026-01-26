package util;

import java.util.ArrayList;
import java.util.List;

public class Geometry {

	public static final Double EPSILON = 1e-9;

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
		double crossProduct = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x);
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
		double cp1 = (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x);
		double cp2 = (c.x - b.x) * (p.y - b.y) - (c.y - b.y) * (p.x - b.x);
		double cp3 = (a.x - c.x) * (p.y - c.y) - (a.y - c.y) * (p.x - c.x);

		// Point is inside if all cross products have the same sign (or are zero)
		return (cp1 >= -EPSILON && cp2 >= -EPSILON && cp3 >= -EPSILON) || (cp1 <= EPSILON && cp2 <= EPSILON && cp3 <= EPSILON);
	}

	public static double distance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}
	
	// Google AI
	
	public static int orientation(Point p, Point q, Point r) {
        double val = (q.y - p.y) * (r.x - q.x) -
                     (q.x - p.x) * (r.y - q.y);

        if (val > -EPSILON && val < EPSILON) return 0; // Collinear
        return (val > 0) ? 1 : 2; // Clockwise or Counterclockwise
    }
	
	public static boolean onSegment(Point p, Point q, Point r) {
        return q.x < Math.max(p.x, r.x) + EPSILON &&
               q.x > Math.min(p.x, r.x) - EPSILON &&
               q.y < Math.max(p.y, r.y) + EPSILON &&
               q.y > Math.min(p.y, r.y) - EPSILON;
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

//        // Special Cases (collinear segments)
//        // p1, q1 and p2 are collinear and p2 lies on segment p1q1
//        if (o1 == 0 && onSegment(p1, p2, q1)) return true;
//
//        // p1, q1 and q2 are collinear and q2 lies on segment p1q1
//        if (o2 == 0 && onSegment(p1, q2, q1)) return true;
//
//        // p2, q2 and p1 are collinear and p1 lies on segment p2q2
//        if (o3 == 0 && onSegment(p2, p1, q2)) return true;
//
//        // p2, q2 and q1 are collinear and q1 lies on segment p2q2
//        if (o4 == 0 && onSegment(p2, q1, q2)) return true;

        return false; // Doesn't fall in any of the above cases
    }
	
//	public static boolean doSegmentsIntersect(Point p1, Point q1, Point p2, Point q2) {
//        // Line Segment 1: p1 + t * (q1 - p1)
//        // Line Segment 2: p2 + u * (q2 - p2)
//
//        double dx1 = q1.x - p1.x;
//        double dy1 = q1.y - p1.y;
//        double dx2 = q2.x - p2.x;
//        double dy2 = q2.y - p2.y;
//
//        double denominator = dx1 * dy2 - dy1 * dx2;
//
//        // If denominator is zero, lines are parallel or collinear.
//        if (denominator == 0) {
//            // Check for collinear overlap
//            // This is a simplified check and might not cover all edge cases perfectly without orientation
//            // For a robust collinear check, you'd need to project and check for overlap on the axis.
//            // For now, assume no intersection if strictly parallel or not overlapping if collinear.
//            return false;
//        }
//
//        double t = ((p1.y - p2.y) * dx2 - (p1.x - p2.x) * dy2) / denominator;
//        double u = ((p1.y - p2.y) * dx1 - (p1.x - p2.x) * dy1) / denominator;
//
//        // Check if the intersection point lies within both segments
//        return (t > 0 && t < 1 && u > 0 && u < 1);
//    }


	public static double perpendicularDistance(Point point, Point lineStart, Point lineEnd) {
		double lineLengthSquared = Math.pow(lineEnd.x - lineStart.x, 2) + Math.pow(lineEnd.y - lineStart.y, 2);

		// Handle case of zero-length line
		if (lineLengthSquared == 0.0) {
			return Math.sqrt(Math.pow(point.x - lineStart.x, 2) + Math.pow(point.y - lineStart.y, 2));
		}

		double t = ((point.x - lineStart.x) * (lineEnd.x - lineStart.x) + (point.y - lineStart.y) * (lineEnd.y - lineStart.y)) / lineLengthSquared;
		t = Math.max(0, Math.min(1, t)); // Clamp t to [0, 1]

		double projectionX = lineStart.x + t * (lineEnd.x - lineStart.x);
		double projectionY = lineStart.y + t * (lineEnd.y - lineStart.y);

		return Math.sqrt(Math.pow(point.x - projectionX, 2) + Math.pow(point.y - projectionY, 2));
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

			sum += (p1.y + p2.y) * (p1.x - p2.x);
		}

		return 0.5 * sum;
	}
	
    public static double calculatePolygonArea(List<Point> vertices) {
        return Math.abs(calculatePolygonSignedArea(vertices));
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
				area += ConvertToRadian(p2.x - p1.x) * (2 + Math.sin(ConvertToRadian(p1.y)) + Math.sin(ConvertToRadian(p2.y)));
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
				area += ConvertToRadian(360. * (p2.x - p1.x)/w) * (2 + Math.sin(ConvertToRadian(180. * p1.y / h - 90)) + Math.sin(ConvertToRadian(180. * p2.y / h - 90)));
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
		simplified.add(points.get(end));

		simplifyRecursive(points, start, end, epsilon, simplified);

		return simplified;
	}

	private static void simplifyRecursive(List<Point> points, int start, int end, double epsilon, List<Point> simplified) {
		double dMax = 0;
		int index = 0;

		// Find the point with the maximum perpendicular distance
		for (int i = start + 1; i < end; i++) {
			double distance = Geometry.perpendicularDistance(points.get(i), points.get(start), points.get(end));
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
