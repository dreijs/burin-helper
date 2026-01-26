package vectormaps;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class BiomeMapCreator {
	// https://upload.wikimedia.org/wikipedia/commons/e/e5/Global_soils_map_USDA.jpg
	public static final String BIOME_BASE_MAP_FILENAME = System.getProperty("user.dir")+"\\input\\Vegetation4b.png";
	public static final String BIOME_EXTENDED_MAP_FILENAME = System.getProperty("user.dir")+"\\output\\vector_output\\biomes_final_extended.png";
	public static final String BIOME_FINAL_RESCALED_MAP_FILENAME = System.getProperty("user.dir")+"\\output\\vector_output\\biomes_final_rescaled.png";

	// biome
	static final int tundra = new Color(140, 204, 189).getRGB();
	static final int taiga = new Color(0, 87, 78).getRGB();
	static final int temperate = new Color(146, 216, 71).getRGB();
	static final int steppe = new Color(245, 231, 89).getRGB();
	static final int subtropical_wet = new Color(6, 104, 6).getRGB();
	static final int mediterranean = new Color(124, 96, 134).getRGB();
	static final int monsoon = new Color(89, 129, 89).getRGB();
	static final int arid = new Color(129, 66, 41).getRGB();
	static final int xeric = new Color(170, 95, 61).getRGB();
	static final int dry_steppe = new Color(136, 111, 51).getRGB();
	static final int semiarid = new Color(214, 169, 114).getRGB();
	static final int grass_savanna = new Color(193, 189, 62).getRGB();
	static final int tree_savanna = new Color(155, 149, 14).getRGB();
	static final int subtropical_dry = new Color(96, 122, 34).getRGB();
	static final int tropical_rainforest = new Color(0, 70, 0).getRGB();

	public static final int GLACIAL = new Color(178, 178, 178).getRGB();

	public static final int[] BIOMES = {tundra, taiga, temperate, steppe, subtropical_wet, mediterranean, monsoon, arid, xeric, dry_steppe, semiarid, grass_savanna, tree_savanna, subtropical_dry, tropical_rainforest};


	public int colDist(int col1, int col2) {
		int col1r = (col1 >> 16) & 0xFF; 
		int col1g = (col1 >> 8) & 0xFF; 
		int col1b = col1 & 0xFF; 

		int col2r = (col2 >> 16) & 0xFF; 
		int col2g = (col2 >> 8) & 0xFF; 
		int col2b = col2 & 0xFF; 

		return (col1r - col2r) * (col1r - col2r) + (col1g - col2g) * (col1g - col2g) + (col1b - col2b) * (col1b - col2b);
	}

	public void createExtendedBiomeMap() {
		int[][] baseData = FileOperator.readImage(BIOME_BASE_MAP_FILENAME);
		int[][] newData = new int[baseData.length][];

		int[][] nbs = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
		int changed = 1;

		int d = 20;

		for (int x = 0; x < baseData.length; x++) {
			newData[x] = new int[baseData[x].length];
			for (int y = 0; y < baseData[x].length; y++) {
				int v = baseData[x][y];
				boolean match = false;
				for(int kk=0;kk<BIOMES.length;kk++) {
					if(colDist(v, BIOMES[kk]) < d) {
						newData[x][y] = BIOMES[kk];
						match = true;
					}
				}
				if(!match) newData[x][y] = 0xFFFFFFFF;
			}
		}

		while(changed > 0) {
			changed = 0;
			int[][] tempNewData = new int[baseData.length][];
			for (int x = 0; x < newData.length; x++) {
				tempNewData[x] = new int[baseData[x].length];
				for (int y = 0; y < newData[x].length; y++) {
					if(newData[x][y] == 0xFFFFFFFF) {
						Map<Integer,Integer> map = new HashMap<Integer,Integer>();
						for(int n=0;n<nbs.length;n++) {
							int xx = x + nbs[n][0];
							int yy = y + nbs[n][1];
							if(xx >= 0 && xx < newData.length && yy >= 0 && yy < newData[0].length) {
								int v = newData[xx][yy];
								int matchIdx = -1;
								for(int kk=0;kk<BIOMES.length;kk++) {
									if(v == BIOMES[kk]) matchIdx = kk;
								}
								if(matchIdx >= 0) {
									int match = BIOMES[matchIdx];
									if(!map.keySet().contains(match)) map.put(match, 1);
									else map.put(match, map.get(match) + 1);
								}
							}
						}

						int max = 0;
						int argMax = 0;
						for(int key : map.keySet()) {
							if(map.get(key) > max) {
								max = map.get(key);
								argMax = key;
							}
						}
						if(max > 0) {
							tempNewData[x][y] = argMax;
							changed++;
						} else tempNewData[x][y] = 0xFFFFFFFF;
					} else {
						tempNewData[x][y] = newData[x][y];
					}
				}
			}

			newData = tempNewData;
			System.out.println(changed);
		}

		FileOperator.writeImage(newData, BIOME_EXTENDED_MAP_FILENAME);
		System.out.println("done");
	}

	public void rescaleBiomeMap() {
		int[][] baseData = FileOperator.readImage(BIOME_BASE_MAP_FILENAME);

		int[][] nbs = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
		int changed = 1;

		int wOri = baseData.length;
		int hOri = baseData[0].length;
		int wTgt = 2160;
		int hTgt = 1080;
//				int wTgt = 2680;
//				int hTgt = 1340;

		int[][] newData = new int[wTgt][];
		for (int x = 0; x < newData.length; x++) {
			newData[x] = new int[hTgt];
			for (int y = 0; y < newData[x].length; y++) {
				newData[x][y] = 0xFFFFFFFF;
			}
		}

		for (int x = 0; x < baseData.length; x++) {
			for (int y = 0; y < baseData[x].length; y++) {
				int v = baseData[x][y];
				for(int kk=0;kk<BIOMES.length;kk++) {
					if(v == BIOMES[kk]) {
						newData[x * wTgt / wOri][y * hTgt / hOri] = v;
					}
//					if(v == glacial) newData[x * wTgt / wOri][y * hTgt / hOri] = v;
				}
			}
		}

		while(changed > 0) {
			changed = 0;
			int[][] tempNewData = new int[newData.length][];
			for (int x = 0; x < newData.length; x++) {
				tempNewData[x] = new int[newData[x].length];
				for (int y = 0; y < newData[x].length; y++) {
					if(newData[x][y] == 0xFFFFFFFF) {
						Map<Integer,Integer> map = new HashMap<Integer,Integer>();
						for(int n=0;n<nbs.length;n++) {
							int xx = x + nbs[n][0];
							int yy = y + nbs[n][1];
							if(xx >= 0 && xx < newData.length && yy >= 0 && yy < newData[0].length) {
								int v = newData[xx][yy];
								int matchIdx = -1;
								for(int kk=0;kk<BIOMES.length;kk++) {
									if(v == BIOMES[kk]) matchIdx = kk;
								}
								if(matchIdx >= 0) {
									int match = BIOMES[matchIdx];
									if(!map.keySet().contains(match)) map.put(match, 1);
									else map.put(match, map.get(match) + 1);
								}
							}
						}

						int max = 0;
						int argMax = 0;
						int nMax = 0;
						for(int key : map.keySet()) {
							if(map.get(key) > max) {
								max = map.get(key);
								argMax = key;
								nMax = 1;
							} else if(map.get(key) == max) {
								nMax++;
							}
						}
						if(max > 0) {
							if(nMax == 1) {
								if(max >= 4 || Math.random() <= (1./(4-max))) {
									tempNewData[x][y] = argMax;
									changed++;
								} else tempNewData[x][y] = 0xFFFFFFFF;
							} else {
								int ii=0;
								for(int key : map.keySet()) {
									if(map.get(key) == max) {
										if(Math.random() <= 1./(nMax - ii)) {
											tempNewData[x][y] = key;
											changed++;
											break;
										}
										ii++;
									}
								}
							}
						} else tempNewData[x][y] = 0xFFFFFFFF;
					} else {
						tempNewData[x][y] = newData[x][y];
					}
				}
			}

			newData = tempNewData;
			System.out.println(changed);
		}

		FileOperator.writeImage(newData, BIOME_FINAL_RESCALED_MAP_FILENAME);
		System.out.println("done");
	}

	public static void main(String[] args) {
		new BiomeMapCreator().createExtendedBiomeMap();
		new BiomeMapCreator().rescaleBiomeMap();
	}
}
