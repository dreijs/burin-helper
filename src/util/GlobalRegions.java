package util;

/**
 * The connected regions of the whole map, found once per level, with each one's true area.
 *
 * Every stage of a tile's generation is a function of that tile's own window, which is fine because
 * the window carries a generous margin around the tile. Small-region removal was the exception. It
 * dissolves a region whose area falls under minRegionSize, and it measured that area over the
 * window, so a region reaching past the margin was measured short -- by a different amount in each
 * tile holding part of it. Two tiles sharing a seam then disagreed about which regions were small
 * enough to dissolve, and a coastline pruned away on one side but kept on the other cannot line up.
 *
 * That was the dominant cause of the breaks at tile edges. Along the level-2 seam at x = -135 the
 * raw region maps agreed on 165 of 216 transitions, and after small-region removal on 16 of 50; the
 * small/large classification itself agreed on 1 of 37, off by a median of 17 pixels. Measuring each
 * region once, over the whole map, makes the threshold test give the same answer on both sides.
 */
public class GlobalRegions {

	private final int height;

	/** Region label per pixel, flattened as x * height + y. */
	private final int[] label;

	/** Area in square kilometres, indexed by label. */
	private final double[] area;

	private GlobalRegions(int height, int[] label, double[] area) {
		this.height = height;
		this.label = label;
		this.area = area;
	}

	public int numRegions() {
		return area.length;
	}

	/** The area of the whole region containing this pixel, however far beyond the tile it reaches. */
	public double areaAt(int x, int y) {
		return area[label[x * height + y]];
	}

	/**
	 * Labels the map's four-connected runs of equal terrain and totals their areas.
	 *
	 * This is deliberately not findRegions(): that one propagates labels by repeated sweeps until
	 * nothing changes, which is affordable over a tile's window and is not over the 58 million
	 * pixels of a whole level-3 map. Union-find settles it in three passes.
	 */
	public static GlobalRegions find(int[][] mapData) {
		final int w = mapData.length;
		final int h = mapData[0].length;
		final int n = w * h;

		System.out.println("start: find global regions (" + w + "x" + h + ")");

		int[] parent = new int[n];
		for (int i = 0; i < n; i++) parent[i] = i;

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int i = x * h + y;
				int v = mapData[x][y];
				// Right and down only: every four-connected pair is still visited exactly once.
				if (x + 1 < w && mapData[x + 1][y] == v) union(parent, i, i + h);
				if (y + 1 < h && mapData[x][y + 1] == v) union(parent, i, i + 1);
			}
		}

		// Flatten every chain, then renumber the roots 0..numRegions-1 in place. A root is marked by
		// holding a negative id, which is what lets the second pass tell roots from the rest after
		// the first pass has already renumbered some of them.
		for (int i = 0; i < n; i++) parent[i] = find(parent, i);

		int numRegions = 0;
		for (int i = 0; i < n; i++) {
			if (parent[i] == i) {
				parent[i] = -1 - numRegions;
				numRegions++;
			}
		}
		for (int i = 0; i < n; i++) {
			if (parent[i] >= 0) parent[i] = parent[parent[i]];
		}
		for (int i = 0; i < n; i++) parent[i] = -1 - parent[i];

		// A cell's area on the globe depends only on its latitude, so one value per row will do --
		// pixelSize() allocates four points and a list per call, and there are n of them.
		double[] rowArea = new double[h];
		for (int y = 0; y < h; y++) rowArea[y] = MapOperator.pixelSize(0, y, w, h);

		double[] area = new double[numRegions];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) area[parent[x * h + y]] += rowArea[y];
		}

		System.out.println("done: find global regions, " + numRegions + " regions");

		return new GlobalRegions(h, parent, area);
	}

	private static int find(int[] parent, int i) {
		while (parent[i] != i) {
			parent[i] = parent[parent[i]]; // path halving
			i = parent[i];
		}
		return i;
	}

	private static void union(int[] parent, int a, int b) {
		int ra = find(parent, a);
		int rb = find(parent, b);
		if (ra == rb) return;
		// Always hang the higher root under the lower one, so the result does not depend on the
		// order the pixels were visited in.
		if (ra < rb) parent[rb] = ra;
		else parent[ra] = rb;
	}
}
