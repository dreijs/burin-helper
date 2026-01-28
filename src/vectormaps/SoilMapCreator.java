package vectormaps;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class SoilMapCreator {
	// https://upload.wikimedia.org/wikipedia/commons/e/e5/Global_soils_map_USDA.jpg
	public static final String SOIL_BASE_MAP_FILENAME = System.getProperty("user.dir")+"\\input\\Soil4.png";
	public static final String SOIL_FINAL_MAP_FILENAME = System.getProperty("user.dir")+"\\output\\map\\soil.png";
	public static final String SOIL_FINAL_RESCALED_MAP_FILENAME = System.getProperty("user.dir")+"\\output\\map\\soil_rescaled.png";

	// soil
	public static final int alfisol = new Color(175, 206, 99).getRGB(); 
	public static final int andisol = new Color(150, 76, 141).getRGB(); 
	public static final int aridisol = new Color(255, 234, 183).getRGB(); 
	public static final int entisol = new Color(158, 207, 186).getRGB(); 
	public static final int gelisol = new Color(146, 175, 213).getRGB(); 
	public static final int histosol = new Color(132, 71, 67).getRGB(); 
	public static final int inceptisol = new Color(247, 163, 44).getRGB(); 
	public static final int mollisol = new Color(71, 156, 61).getRGB(); 
	public static final int oxisol = new Color(239, 123, 120).getRGB(); 
	public static final int spodosol = new Color(199, 156, 193).getRGB(); 
	public static final int ultisol = new Color(245, 233, 41).getRGB(); 
	public static final int vertisol = new Color(80, 90, 150).getRGB(); 
	public static final int rocky = new Color(209, 201, 194).getRGB(); 
	public static final int shiftingSand = new Color(134, 125, 118).getRGB(); 
	public static final int polarIce = new Color(198, 223, 255).getRGB(); 

	public static final int[] SPREADABLE_SOILS = {alfisol, andisol, aridisol, entisol, gelisol, histosol, inceptisol, mollisol, oxisol, spodosol, ultisol, vertisol, rocky, shiftingSand};
	public static final int[] ALL_SOILS = {alfisol, andisol, aridisol, entisol, gelisol, histosol, inceptisol, mollisol, oxisol, spodosol, ultisol, vertisol, rocky, shiftingSand, polarIce};

	final static int LEFT_OFFSET = 0;
	final static int RIGHT_OFFSET = 7;
	final static int TOP_OFFSET = 412;
	final static int BOTTOM_OFFSET = 4780 - 3134;


	public int colDist(int col1, int col2) {
		int col1r = (col1 >> 16) & 0xFF; 
		int col1g = (col1 >> 8) & 0xFF; 
		int col1b = col1 & 0xFF; 

		int col2r = (col2 >> 16) & 0xFF; 
		int col2g = (col2 >> 8) & 0xFF; 
		int col2b = col2 & 0xFF; 

		return (col1r - col2r) * (col1r - col2r) + (col1g - col2g) * (col1g - col2g) + (col1b - col2b) * (col1b - col2b);
	}

	public void createExtendedSoilMap() {
		int[][] baseData = FileOperator.readImage(SOIL_BASE_MAP_FILENAME);
		int[][] newData = new int[baseData.length][];

		int[][] nbs = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
		int changed = 1;

		for (int x = 0; x < baseData.length; x++) {
			newData[x] = new int[baseData[x].length];
			for (int y = 0; y < baseData[x].length; y++) {
				int v = baseData[x][y];
				boolean match = false;
				for(int kk=0;kk<ALL_SOILS.length;kk++) {
					if(v == ALL_SOILS[kk]) {
						newData[x][y] = v;
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
								for(int kk=0;kk<SPREADABLE_SOILS.length;kk++) {
									if(v == SPREADABLE_SOILS[kk]) matchIdx = kk;
								}
								if(matchIdx >= 0) {
									int match = SPREADABLE_SOILS[matchIdx];
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

		FileOperator.writeImage(newData, SOIL_FINAL_MAP_FILENAME);
		System.out.println("done");
	}
	
	public void rescaleSoilMap() {
		int[][] baseData = FileOperator.readImage(SOIL_FINAL_MAP_FILENAME);

		int[][] nbs = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
		int changed = 1;
		
		int wOri = baseData.length;
		int hOri = baseData[0].length;
		int wTgt = 21600;
		int hTgt = 10800;
		

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
				boolean isBiome = false;
				for(int kk=0;kk<ALL_SOILS.length;kk++) {
					if(v == ALL_SOILS[kk]) {
						isBiome = true;
					}
				}
				if(isBiome) {
					newData[x * wTgt / wOri][y * hTgt / hOri] = v;
					if(x > 0 && x <baseData.length-1 && y > 0 && y < baseData[x].length - 1) {
						if(baseData[x-1][y] == v && baseData[x+1][y] == v && baseData[x][y-1] == v && baseData[x][y+1] == v) {
							for(int xx = x * wTgt / wOri; xx < (x+1) * wTgt / wOri; xx++) {
								for(int yy = y * hTgt / hOri; yy < (y+1) * hTgt / hOri; yy++) {
									newData[xx][yy] = v;
								}
							}
						}
					}
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
								for(int kk=0;kk<ALL_SOILS.length;kk++) {
									if(v == ALL_SOILS[kk]) matchIdx = kk;
								}
								if(matchIdx >= 0) {
									int match = ALL_SOILS[matchIdx];
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

		FileOperator.writeImage(newData, SOIL_FINAL_RESCALED_MAP_FILENAME);
		System.out.println("done");
	}

	public static void main(String[] args) {
		new SoilMapCreator().createExtendedSoilMap();
		new SoilMapCreator().rescaleSoilMap();
	}
}
