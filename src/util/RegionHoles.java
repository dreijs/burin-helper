package util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.triangulate.polygon.PolygonTriangulator;

/**
 * Turns the region list into polygons with holes, so that no two triangles cover the same ground.
 *
 * Until now a region's polygon was its outer boundary only, filled solid, and any region enclosed
 * within it was drawn on top -- finalPrintPolygons() emits by descending draw order so the enclosed
 * one gets the higher triangle index and wins. That leaves the two overlapping, which costs three
 * things: the flood fill cannot cross between them (2,143 links missing at level 1, because the
 * shared edge has a triangle on one side only), terrain areas double-count where they overlap
 * (0.90% of land, up to 4.8% for a single biome), and a domain covering the parent paints over the
 * lake sitting on it.
 *
 * Cutting the enclosed region out of its parent instead makes the two share a real edge: the
 * parent's hole ring and the child's outer ring are the same points, so checkAndAddEdge() pairs
 * them and T2 becomes a triangle index rather than the -colorData-2 fallback. All three problems
 * go at once, and the draw order stops meaning anything.
 */
public class RegionHoles {

	private static final GeometryFactory GF = new GeometryFactory();

	/** A region's shell, the rings cut out of it, and the triangles that result. */
	public static class Holed {
		public final int regionIdx;
		public final List<Integer> holeRegions = new ArrayList<Integer>();
		public List<Point[]> triangles = new ArrayList<Point[]>();
		public boolean failed = false;
		public String failure = null;
		public int shellPoints = 0;
		/** "clean", "repaired", or "shell-only" -- how the triangles below were arrived at. */
		public String route = "clean";
		public Holed(int regionIdx) { this.regionIdx = regionIdx; }
	}

	/** Closed ring for a region's polygon, or null when it is too small to be one. */
	public static LinearRing ringOf(Region region) {
		List<Point> p = region.polygon;
		if (p.size() < 3) return null;
		Coordinate[] c = new Coordinate[p.size() + 1];
		for (int i = 0; i < p.size(); i++) c[i] = new Coordinate(p.get(i).xFloat(), p.get(i).yFloat());
		c[p.size()] = c[0];
		try {
			return GF.createLinearRing(c);
		} catch (IllegalArgumentException e) {
			return null; // repeated or collapsed points
		}
	}

	private static Coordinate[] oriented(LinearRing ring, boolean wantCCW) {
		Coordinate[] c = ring.getCoordinates().clone();
		if (Orientation.isCCW(c) != wantCCW) {
			for (int i = 0; i < c.length / 2; i++) {
				Coordinate t = c[i];
				c[i] = c[c.length - 1 - i];
				c[c.length - 1 - i] = t;
			}
		}
		return c;
	}

	/**
	 * Immediate parent of each region, or -1. A region is enclosed by every region whose solid
	 * outline contains it; the immediate one is the smallest of those, which is the only one that
	 * should cut it out -- a lake inside an island inside a sea is a hole in the island, not the sea.
	 */
	public static int[] findParents(List<Region> regions) {
		int n = regions.size();
		int[] parent = new int[n];
		Polygon[] solid = new Polygon[n];
		STRtree tree = new STRtree();

		for (int i = 0; i < n; i++) {
			parent[i] = -1;
			LinearRing r = ringOf(regions.get(i));
			if (r == null) continue;
			solid[i] = GF.createPolygon(GF.createLinearRing(oriented(r, true)));
			tree.insert(solid[i].getEnvelopeInternal(), Integer.valueOf(i));
		}
		tree.build();

		for (int i = 0; i < n; i++) {
			if (solid[i] == null) continue;
			Geometry probe = solid[i].getInteriorPoint();
			@SuppressWarnings("unchecked")
			List<Integer> candidates = tree.query(solid[i].getEnvelopeInternal());
			double best = Double.MAX_VALUE;
			double own = solid[i].getArea();
			for (Integer cand : candidates) {
				int j = cand.intValue();
				if (j == i || solid[j] == null) continue;
				double a = solid[j].getArea();
				// The area test is not a tie-break, it is what keeps the relation acyclic. Every
				// region's polygon is its outer boundary filled solid, so a parent's polygon covers
				// its children's ground -- and the interior point JTS picks for the parent can land
				// inside a child, which made the child "contain" the parent and the two adopt each
				// other. That produced a hole larger than its own shell, which is why those regions
				// failed with a null coordinate inside the hole joiner and why subtracting the hole
				// left nothing behind. A genuine parent always has the larger area.
				if (a <= own) continue;
				// Envelopes overlap freely between neighbours, so the cheap point test comes first;
				// among the containers, the smallest is the immediate parent.
				//
				// Demanding that the parent cover the child's whole ring, rather than this one
				// point, was tried and is worse: 30 legitimate holes were rejected on tile 1_0 and
				// the overlap they left behind took the probe count from 12 to 60. An enclosed
				// region that touches its parent's outer boundary has that stretch simplified
				// independently on each side, so it pokes out by a hair and fails covers(). The
				// point test tolerates that; the area rule above is what keeps it honest.
				if (!solid[j].covers(probe)) continue;
				if (a < best) { best = a; parent[i] = j; }
			}
		}
		return parent;
	}

	/**
	 * Cuts every region out of its immediate parent and triangulates what is left.
	 *
	 * JTS does the hole joining (PolygonHoleJoiner) and the ear clipping. Doing it by hand would
	 * mean splicing each hole into the parent's vertex list across a bridge traversed twice, and the
	 * existing clipper is not built for that: its O(n^2) self-intersection check treats the doubled
	 * bridge as an error, and its isEar() test has no handling for a vertex that appears twice.
	 */
	public static List<Holed> build(List<Region> regions) {
		int n = regions.size();
		int[] parent = findParents(regions);

		List<Holed> out = new ArrayList<Holed>(n);
		for (int i = 0; i < n; i++) out.add(new Holed(i));
		for (int i = 0; i < n; i++) {
			if (parent[i] >= 0) out.get(parent[i]).holeRegions.add(Integer.valueOf(i));
		}

		for (int i = 0; i < n; i++) {
			Holed h = out.get(i);
			LinearRing shell = ringOf(regions.get(i));
			if (shell == null) continue;
			h.shellPoints = regions.get(i).polygon.size();

			List<LinearRing> holes = new ArrayList<LinearRing>();
			for (Integer k : h.holeRegions) {
				LinearRing r = ringOf(regions.get(k.intValue()));
				if (r != null) holes.add(GF.createLinearRing(oriented(r, false)));
			}
			// Largest first: PolygonHoleJoiner works outward, and a big hole left until last has the
			// most bridges to avoid crossing.
			holes.sort(Comparator.comparingDouble(r -> -GF.createPolygon(r).getArea()));

			LinearRing outer = GF.createLinearRing(oriented(shell, true));
			LinearRing[] inner = holes.toArray(new LinearRing[0]);

			// Three routes, in order of preference. A ring that crosses itself defeats the
			// triangulator, and the region list does contain some -- determineTriangleDrawOrder()
			// has always printed "Triangulation error!" for them and carried on regardless. Losing
			// the region is not an option: uncovered ground is a hole in the map, worse than the
			// overlap this change exists to remove.
			if (!tryTriangulate(h, GF.createPolygon(outer, inner), "clean")
					&& !tryTriangulate(h, GeometryFixer.fix(GF.createPolygon(outer, inner)), "repaired")
					&& !tryTriangulate(h, subtractHoles(outer, inner), "subtracted")
					&& !tryTriangulate(h, GeometryFixer.fix(GF.createPolygon(outer)), "shell-only")) {
				h.failed = true;
			}
		}
		return out;
	}

	/**
	 * Triangulates one geometry into the given result, returning false if it will not triangulate or
	 * comes out empty. GeometryFixer can hand back a MultiPolygon when it splits a self-touching
	 * ring, so every polygon component is taken.
	 */
	/**
	 * Cuts the holes out with a boolean difference rather than by declaring them as rings.
	 *
	 * createPolygon(shell, holes) demands well-formed input: a hole touching the shell, or two holes
	 * touching each other, makes an invalid polygon that neither triangulates nor always survives
	 * GeometryFixer. difference() has no such requirement, so this recovers the cases that would
	 * otherwise fall back to the shell alone -- and falling back means the child region keeps
	 * overlapping its parent, which is the very thing holes exist to prevent.
	 */
	private static Geometry subtractHoles(LinearRing outer, LinearRing[] inner) {
		try {
			Geometry shell = GeometryFixer.fix(GF.createPolygon(outer));
			if (inner.length == 0) return shell;
			List<Geometry> parts = new ArrayList<Geometry>();
			for (LinearRing r : inner) parts.add(GeometryFixer.fix(GF.createPolygon(r)));
			Geometry cut = UnaryUnionOp.union(parts);
			return cut == null || cut.isEmpty() ? shell : shell.difference(cut);
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static boolean tryTriangulate(Holed h, Geometry geom, String route) {
		if (geom == null || geom.isEmpty()) {
			h.failure = (h.failure == null ? "" : h.failure + " | ") + route
					+ (geom == null ? " -> not built" : " -> came back empty");
			return false;
		}
		List<Point[]> found = new ArrayList<Point[]>();
		try {
			for (int g = 0; g < geom.getNumGeometries(); g++) {
				Geometry part = geom.getGeometryN(g);
				if (!(part instanceof Polygon) || part.isEmpty()) continue;
				Geometry tris = PolygonTriangulator.triangulate(part);
				for (int t = 0; t < tris.getNumGeometries(); t++) {
					Coordinate[] c = tris.getGeometryN(t).getCoordinates();
					if (c.length < 3) continue;
					found.add(new Point[] {
							new PointFloat(c[0].x, c[0].y),
							new PointFloat(c[1].x, c[1].y),
							new PointFloat(c[2].x, c[2].y) });
				}
			}
		} catch (RuntimeException e) {
			h.failure = (h.failure == null ? "" : h.failure + " | ") + route + " -> "
					+ e.getClass().getSimpleName() + ": " + e.getMessage();
			return false;
		}
		if (found.isEmpty()) {
			h.failure = (h.failure == null ? "" : h.failure + " | ") + route + " -> produced no triangles";
			return false;
		}
		h.triangles = found;
		h.route = route;
		return true;
	}
}
