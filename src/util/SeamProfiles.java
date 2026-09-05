package util;

import java.util.HashMap;
import java.util.Map;

/**
 * The terrain along each tile boundary, handed from the tile that reaches it first to the tile on
 * the other side, so that the two describe the seam identically.
 *
 * makeTileSeamsCanonical() and GlobalRegions between them make the two sides start from the same
 * pixels and agree about which regions are too small to keep, which took the level-2 seam at
 * x = -135 from 46% to 75% of its vertices matching. The rest is small-region removal itself: it
 * sweeps greedily over the tile's window and lets each decision feed the next, so an influence
 * started at the window's own edge spreads a few pixels per iteration over hundreds of iterations
 * and reaches the seam long before the sweep ends. Two windows that differ anywhere can therefore
 * differ at the seam, however far away they first disagreed.
 *
 * Rather than try to make two independent sweeps converge, the tile to the west simply publishes
 * the column it settled on and the tile to the east adopts it. Tiles are generated west to east and
 * north to south, so the tile holding a profile has always finished before the tile that needs it.
 */
public class SeamProfiles {

	private final Map<Long, int[]> vertical = new HashMap<Long, int[]>();
	private final Map<Long, int[]> horizontal = new HashMap<Long, int[]>();

	private static Long key(int x, int y) {
		return (((long) x) << 32) | (y & 0xFFFFFFFFL);
	}

	/**
	 * Replaces this tile's western column and northern row with the ones its neighbours settled on,
	 * then publishes its own eastern column and southern row for the neighbours still to come.
	 *
	 * The pixel coordinates are the key: a tile's minXint is its western neighbour's maxXint, so no
	 * tile index has to be threaded down here to find the right profile.
	 *
	 * The western column is taken before the northern row, which settles the pixel where four tiles
	 * meet. Follow it round and all four end up holding the north-western tile's south-eastern
	 * pixel: the north-east tile takes it westward, the south-west tile takes it southward, and the
	 * south-east tile arrives at the same value along either path.
	 */
	public void reconcile(int[][] tile, int minXint, int minYint, int maxXint, int maxYint) {
		int w = tile.length;
		int h = tile[0].length;

		int[] west = vertical.remove(key(minXint, minYint));
		if(west != null && west.length == h) {
			for(int y = 0; y < h; y++) tile[0][y] = west[y];
		}

		int[] north = horizontal.remove(key(minXint, minYint));
		if(north != null && north.length == w) {
			for(int x = 0; x < w; x++) tile[x][0] = north[x];
		}

		// Published after the two above, so a corner taken from the north is passed on as such.
		int[] east = new int[h];
		for(int y = 0; y < h; y++) east[y] = tile[w - 1][y];
		vertical.put(key(maxXint, minYint), east);

		int[] south = new int[w];
		for(int x = 0; x < w; x++) south[x] = tile[x][h - 1];
		horizontal.put(key(minXint, maxYint), south);

		System.out.println("done: reconciled tile seams"
				+ (west != null ? ", took " + h + " pixels from the west" : "")
				+ (north != null ? ", took " + w + " pixels from the north" : ""));
	}
}
