package util;

public class RegionResult {
	public int[][] regions;
	public int[] type;
	public int numRegions;

	public RegionResult(int[][] regions, int[] type, int numRegions) {
		this.regions = regions;
		this.type = type;
		this.numRegions = numRegions;
	}
}