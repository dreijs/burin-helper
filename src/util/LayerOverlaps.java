package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;

/**
 * Which triangles cover the same ground as which others, and how much of each is covered.
 *
 * A region's polygon is its outer boundary filled solid, and a region enclosed within it is drawn on
 * top rather than cut out. The two therefore overlap, and the mesh records nothing about it: their
 * shared boundary has a triangle on one side only, so FEdgeDataEntry::T2 goes negative and the two
 * layers are invisible to each other. That costs the flood fill its links -- 71,594 km2 of Canada
 * severed from its own mainland at level 1 -- and it makes terrain areas double-count, by 0.90% of
 * land overall and up to 4.8% for a single biome.
 *
 * Cutting the enclosed region out instead was tried and abandoned: these rings deliberately contain
 * zero-area spikes, because addRiverData() threads a river polyline through the outline and a
 * zero-width river is the same line walked out and back. The ear clipper emits each spike as a
 * degenerate triangle whose edges carry the river flag, which is how rivers exist in the mesh at
 * all. A triangulator that insists on valid polygons discards them, and roughly 10% of every river
 * went with them.
 *
 * So the layers stay, and what was missing is recorded instead. For each triangle: the triangles on
 * other layers it shares ground with, and the area of it hidden beneath later-drawn ones.
 */
public class LayerOverlaps {

	private static final GeometryFactory GF = new GeometryFactory();

	/** Ignore slivers below this share of a triangle's own area: shared edges, not shared ground. */
	private static final double MIN_OVERLAP_FRACTION = 1e-9;

	public static class Result {
		/** Per triangle, the triangles on other layers covering the same ground. */
		public final List<List<Integer>> neighbors;
		/** Per triangle, how much of its area later-drawn triangles hide, in the tile's own units. */
		public final double[] coveredArea;
		public int pairs = 0;

		Result(int n) {
			neighbors = new ArrayList<List<Integer>>(n);
			for (int i = 0; i < n; i++) neighbors.add(new ArrayList<Integer>());
			coveredArea = new double[n];
		}
	}

	/**
	 * @param geom   per triangle, six doubles: x1, y1, x2, y2, x3, y3
	 * @param region per triangle, the region that emitted it
	 * @param bbox   per triangle, minX, minY, maxX, maxY -- the boxes finalPrintPolygons already built
	 */
	public static Result compute(List<double[]> geom, List<Integer> region, List<double[]> bbox) {
		final int n = geom.size();
		Result out = new Result(n);
		if (n == 0) return out;

		// A grid sized so cells hold a handful of triangles each. Only pairs sharing a cell are
		// considered, and the vast majority of those turn out to be neighbours within one region,
		// which the region test below discards before any geometry is built.
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (double[] b : bbox) {
			minX = Math.min(minX, b[0]); minY = Math.min(minY, b[1]);
			maxX = Math.max(maxX, b[2]); maxY = Math.max(maxY, b[3]);
		}
		int cells = Math.max(1, (int) Math.round(Math.sqrt(n / 4.0)));
		double cellW = Math.max(1e-9, (maxX - minX) / cells);
		double cellH = Math.max(1e-9, (maxY - minY) / cells);

		Map<Long, List<Integer>> grid = new HashMap<Long, List<Integer>>();
		for (int i = 0; i < n; i++) {
			double[] b = bbox.get(i);
			int gx0 = (int) ((b[0] - minX) / cellW), gx1 = (int) ((b[2] - minX) / cellW);
			int gy0 = (int) ((b[1] - minY) / cellH), gy1 = (int) ((b[3] - minY) / cellH);
			for (int gx = gx0; gx <= gx1; gx++) {
				for (int gy = gy0; gy <= gy1; gy++) {
					grid.computeIfAbsent((((long) gx) << 32) ^ (gy & 0xFFFFFFFFL),
							k -> new ArrayList<Integer>()).add(Integer.valueOf(i));
				}
			}
		}

		Polygon[] poly = new Polygon[n];
		double[] area = new double[n];

		// Everything a triangle is hidden under, collected so the covered area can be the union of
		// them rather than the sum -- two triangles on top of one another would otherwise be counted
		// twice and could hide more of a triangle than the triangle has.
		List<List<Geometry>> hiddenBy = new ArrayList<List<Geometry>>(n);
		for (int i = 0; i < n; i++) hiddenBy.add(null);

		boolean[] seenPair = null; // not needed: the cell loop below visits each ordered pair once
		for (List<Integer> cell : grid.values()) {
			for (int a = 0; a < cell.size(); a++) {
				for (int b = a + 1; b < cell.size(); b++) {
					int i = cell.get(a).intValue(), j = cell.get(b).intValue();
					if (i > j) { int t = i; i = j; j = t; }
					if (region.get(i).intValue() == region.get(j).intValue()) {
						continue; // same layer: neighbours share edges, not ground
					}
					double[] bi = bbox.get(i), bj = bbox.get(j);
					if (bi[2] < bj[0] || bj[2] < bi[0] || bi[3] < bj[1] || bj[3] < bi[1]) {
						continue;
					}
					if (out.neighbors.get(i).contains(Integer.valueOf(j))) {
						continue; // already settled from another cell
					}

					if (poly[i] == null) { poly[i] = triangle(geom.get(i)); area[i] = poly[i].getArea(); }
					if (poly[j] == null) { poly[j] = triangle(geom.get(j)); area[j] = poly[j].getArea(); }
					if (area[i] <= 0 || area[j] <= 0) {
						continue; // a river spike covers no ground
					}

					Geometry shared;
					try {
						shared = poly[i].intersection(poly[j]);
					} catch (RuntimeException e) {
						continue;
					}
					double sharedArea = shared.getArea();
					if (sharedArea <= MIN_OVERLAP_FRACTION * Math.min(area[i], area[j])) {
						continue; // touching along an edge, not overlapping
					}

					out.neighbors.get(i).add(Integer.valueOf(j));
					out.neighbors.get(j).add(Integer.valueOf(i));
					out.pairs++;

					// j was emitted later, so it is drawn over i. Draw order is the only thing that
					// decides which of two overlapping triangles is visible, and finalPrintPolygons
					// emits by descending region draw order, so the higher index is the later one.
					if (hiddenBy.get(i) == null) hiddenBy.set(i, new ArrayList<Geometry>());
					hiddenBy.get(i).add(shared);
				}
			}
		}

		for (int i = 0; i < n; i++) {
			List<Geometry> parts = hiddenBy.get(i);
			if (parts == null) continue;
			try {
				Geometry union = parts.size() == 1 ? parts.get(0) : UnaryUnionOp.union(parts);
				out.coveredArea[i] = Math.min(union == null ? 0 : union.getArea(), area[i]);
			} catch (RuntimeException e) {
				double sum = 0;
				for (Geometry g : parts) sum += g.getArea();
				out.coveredArea[i] = Math.min(sum, area[i]); // union failed, so cap the sum instead
			}
		}
		return out;
	}

	private static Polygon triangle(double[] g) {
		return GF.createPolygon(new Coordinate[] {
				new Coordinate(g[0], g[1]), new Coordinate(g[2], g[3]),
				new Coordinate(g[4], g[5]), new Coordinate(g[0], g[1]) });
	}
}
