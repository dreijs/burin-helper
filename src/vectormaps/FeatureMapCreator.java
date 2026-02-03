package vectormaps;

import java.awt.Color;
import util.Colors;
import util.FileOperator;

public class FeatureMapCreator {
	public static final String SAMPLES_OUTPUT_FOLDER = System.getProperty("user.dir")+"\\output\\map\\samples\\mountain_samples\\";
	public static final String HILL_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\hill_map.png";
	public static final String MOUNTAINS_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\mountain_map.png";
	public static final String CLIFFS_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\cliff_map.png";
	public static final String FEATURES_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\features_map.png";

	public static final int HILL_COLOR = new Color(255, 128, 0).getRGB(); 
	public static final int MOUNTAIN_COLOR = new Color(255, 0, 0).getRGB(); 
	public static final int CLIFF_NORTH_COLOR = new Color(192, 0, 0).getRGB(); 
	public static final int CLIFF_SOUTH_COLOR = new Color(128, 0, 0).getRGB(); 

	public int[][] addHillData(int[][] baseData, int[][] elevationData, int d, int w, int m) {
		int[][] hillData = new int[baseData.length][];

		for (int x = 0; x < baseData.length; x++) {
			hillData[x] = new int[baseData[x].length];
			for (int y = 0; y < hillData[x].length; y++) {
				hillData[x][y] = baseData[x][y];
				int minElev = 255;
				int maxElev = 0;

				for(int dx = -w; dx <= w; dx++) {
					for(int dy = -w; dy <= w; dy++) {
						int xx = x + dx;
						int yy = y + dy;
						if(Math.sqrt(dx*dx + dy*dy) <= w) {
							if(xx >= 0 && xx < hillData.length && yy >= 0 && yy < hillData[0].length) {
								int val = Colors.blueVal(elevationData[xx][yy]);
								minElev = Math.min(minElev, val);
								maxElev = Math.max(maxElev, val);
							}
						}
					}
				}

				if(maxElev - minElev > d && Colors.blueVal(elevationData[x][y]) > 0 && maxElev > m) hillData[x][y] = HILL_COLOR; 
			}
		}

		return hillData;
	}

	public void createHillMap(int d, int w, int m) {
		int[][] elevationData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_FILENAME);
		int[][] levelData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_OUTPUT_FILENAME);
		System.out.println("done reading");

		int[][] hillData = addHillData(levelData, elevationData, d, w, m);

		FileOperator.writeImage(hillData, HILL_OUTPUT_FILENAME);
	}

	public int[][] addMountainData(int[][] baseData, int[][] elevationData, int d, int w, int m, int slope) {
		int[][] mountainData = new int[baseData.length][baseData[0].length];

		for (int x = 0; x < baseData.length; x++) {
			for (int y = 0; y < baseData[x].length; y++) {
				mountainData[x][y] = baseData[x][y];
				int minElev = 255;
				int maxElev = 0;

				for(int dx = -w; dx <= w; dx++) {
					for(int dy = -w; dy <= w; dy++) {
						int xx = x + dx;
						int yy = y + dy;
						if(Math.sqrt(dx*dx + dy*dy) <= w) {
							if(xx >= 0 && xx < mountainData.length && yy >= 0 && yy < mountainData[0].length) {
								int val = Colors.blueVal(elevationData[xx][yy]);
								minElev = Math.min(minElev, val);
								maxElev = Math.max(maxElev, val);
							}
						}
					}
				}

				if(maxElev - minElev > d && Colors.blueVal(elevationData[x][y]) > 0 && maxElev > m) mountainData[x][y] = MOUNTAIN_COLOR; 
			}
		}

		int numChanged = 1;
		int[][] nbs = new int[][] {{1,0}, {0,1}, {-1,0}, {0,-1}};
		while(numChanged > 0) {
			numChanged = 0;

			for (int x = 0; x < baseData.length; x++) {
				for (int y = 0; y < baseData[x].length; y++) {
					if(mountainData[x][y] == MOUNTAIN_COLOR) {
						for(int nn=0;nn<nbs.length;nn++) {
							int xx = x + nbs[nn][0];
							int yy = y + nbs[nn][1];

							if(xx >= 0 && xx < baseData.length && yy >= 0 && yy < baseData[x].length && mountainData[xx][yy] != MOUNTAIN_COLOR) {
								int val1 = Colors.blueVal(elevationData[x][y]);
								int val2 = Colors.blueVal(elevationData[xx][yy]);
								if(Math.abs(val1 - val2) > slope && val2 > m) {
									mountainData[xx][yy] = MOUNTAIN_COLOR; 
									numChanged++;
								}

							}
						}
					}
				}
			}

			System.out.println("processing mountains: num changed = "+numChanged);
		}

		return mountainData;
	}

	public void createMountainMap(int d, int w, int m, int slope) {
		int[][] elevationData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_FILENAME);
		int[][] levelData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_OUTPUT_FILENAME);
		System.out.println("done reading");

		int[][] mountainData = addMountainData(levelData, elevationData, d, w, m, slope);

		FileOperator.writeImage(mountainData, MOUNTAINS_OUTPUT_FILENAME);
	}

	public void createFeaturesMap() {
		int[][] elevationData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_FILENAME);
		int[][] featureData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_OUTPUT_FILENAME);
		System.out.println("done reading");

		featureData = addHillData(featureData, elevationData, 20, 6, 80);
		System.out.println("added hill data");
		featureData = addMountainData(featureData, elevationData, 32, 3, 96, 6);
		System.out.println("added mountain data");
		featureData = addCliffData(featureData, elevationData, 2., 24, 20, 40);
		System.out.println("added cliff data");

		FileOperator.writeImage(featureData, FEATURES_OUTPUT_FILENAME);
	}

	public int[][] addCliffData(int[][] baseData, int[][] elevationData, double ww, int d, int dd, int m) {
		int[][] cliffData = new int[baseData.length][];
		for (int x = 0; x < baseData.length; x++) {
			cliffData[x] = new int[baseData[x].length];
			for (int y = 0; y < baseData[x].length; y++) {
				int minElv = 256;
				int maxElv = -1;
				for(int dx = -1*((int) Math.ceil(ww)); dx<= ww;dx++) {
					int xx = x + dx;
					if(xx >= 0 && xx < elevationData.length) {
						for(int dy = -((int) Math.ceil(ww)); dy<= ww;dy++) {
							if(Math.sqrt(dx*dx + dy*dy) <= ww) {
								int yy = y+dy;
								if(yy >= 0 && yy < elevationData[xx].length) {
									int val = Colors.blueVal(elevationData[xx][yy]);

									minElv = Math.min(minElv, val);
									maxElv = Math.max(maxElv, val);
								}
							}
						}
					}
				}

				int val = Colors.blueVal(elevationData[x][y]);

				if(Colors.blueVal(elevationData[x][y]) > 0 && maxElv - minElv > d && val - minElv > dd && val > m) {
					if(y > 0 && Colors.blueVal(elevationData[x][y]) > Colors.blueVal(elevationData[x][y-1])) cliffData[x][y] = CLIFF_NORTH_COLOR;
					else cliffData[x][y] = CLIFF_SOUTH_COLOR;
				}
				else cliffData[x][y] = baseData[x][y];
			}
		}

		return cliffData;
	}

	public void createCliffMapSamples(double d, int m) {
		try{
			int[][] elevationData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_FILENAME);
			int[][] levelData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_OUTPUT_FILENAME);
			System.out.println("done reading");

			for(int z=20;z<=28;z+=2) {
				for(int zz=Math.max(0,z-16);zz<=z;zz+=4) {
					int[][] mountainData = addCliffData(levelData, elevationData, d, z, zz, m);

					FileOperator.writeImage(mountainData, SAMPLES_OUTPUT_FOLDER+"elevation_mountains_"+z+"_"+zz+"_d2.png");
					System.out.println("done");
				}
			}
		} catch(Exception e){
			System.out.println(e.getMessage());
		}

	}

	public static void main(String[] args) {
		int hillScope = 8;
		int hillElevDiff = 16;
		int minHillHeight = 80;
		new FeatureMapCreator().createHillMap(hillElevDiff, hillScope, minHillHeight);

		int mountainScope = 3;
		int mountainElevDiff = 32;
		int minMountainHeight = 96;
		int mountainSlope = 6;
		new FeatureMapCreator().createMountainMap(mountainElevDiff, mountainScope, minMountainHeight, mountainSlope);

		new FeatureMapCreator().createCliffMapSamples(2., 40);

		new FeatureMapCreator().createFeaturesMap();
	}
}
