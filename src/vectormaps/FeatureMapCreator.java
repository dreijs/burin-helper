package vectormaps;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import util.Colors;

public class FeatureMapCreator {

	public static final String FEATURE_MAP_FILENAME = System.getProperty("user.dir")+"\\output\\vector_output\\features.png";
	public static final String MERGED_FEATURE_MAP_FILENAME = System.getProperty("user.dir")+"\\output\\vector_output\\features_merged.png";

	public static final int HILL_COLOR = new Color(255, 255, 0).getRGB(); 

	public int[][] addHillData(int[][] oldData, int[][] elevationData, int w, int d) {
		int[][] hillData = new int[oldData.length][];

		for (int x = 0; x < oldData.length; x++) {
			hillData[x] = new int[oldData[x].length];
			for (int y = 0; y < hillData[x].length; y++) {
				hillData[x][y] = oldData[x][y];
				if(hillData[x][y] == 0) {

					int minElev = 255;
					int maxElev = 0;

					for(int dx = -w; dx <= w; dx++) {
						for(int dy = -w; dy <= w; dy++) {
							int xx = x + dx;
							int yy = y + dy;
							if(dx*dx + dy*dy <= w) {
								if(xx >= 0 && xx < hillData.length && yy >= 0 && yy < hillData[0].length) {
									int val = Colors.blueVal(elevationData[xx][yy]);
									minElev = Math.min(minElev, val);
									maxElev = Math.max(maxElev, val);
								}
							}
						}
					}

					if(maxElev - minElev > d) hillData[x][y] = HILL_COLOR; 
				}
			}
		}

		return hillData;
	}

	public void createFeaturesMap() {
		int[][] elevationData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_FILENAME);
		int[][] levelData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_OUTPUT_FILENAME);
		int[][] featureData = new int[levelData.length][];
		for (int x = 0; x < levelData.length; x++) {
			featureData[x] = new int[levelData[x].length];
			for (int y = 0; y < levelData[x].length; y++) {
				featureData[x][y] = 0;
			}
		}

		featureData = addHillData(featureData, elevationData, 3, 10);

		FileOperator.writeImage(featureData, FEATURE_MAP_FILENAME);

		for (int x = 0; x < levelData.length; x++) {
			for (int y = 0; y < levelData[x].length; y++) {
				if(featureData[x][y] == 0) featureData[x][y] = levelData[x][y];
			}
		}

		FileOperator.writeImage(featureData, MERGED_FEATURE_MAP_FILENAME);
	}

	public void createMountainMap() {
		double dd = 2;
		try{
			int[][] elevationData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_FILENAME);
			int[][] levelData = FileOperator.readImage(ElevationMapCreator.ELEVATION_MAP_OUTPUT_FILENAME);
			System.out.println("done reading");

			for(int z=20;z<=28;z+=4) {
				for(int zz=0;zz<z;zz+=1) {
					int[][] mountainData = new int[levelData.length][];
					for (int x = 0; x < levelData.length; x++) {
						mountainData[x] = new int[levelData[x].length];
						for (int y = 0; y < levelData[x].length; y++) {
							int minElv = 256;
							int maxElv = -1;
							for(int dx = -1*((int) Math.ceil(dd)); dx<= dd;dx++) {
								int xx = x + dx;
								if(xx >= 0 && xx < elevationData.length) {
									for(int dy = -((int) Math.ceil(dd)); dy<= dd;dy++) {
										if(dx*dx + dy*dy <= dd) {
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

							if(minElv > 0 && maxElv - minElv > z && val - minElv > zz && val > 40) mountainData[x][y] = 0xFFFF0000;
							else mountainData[x][y] = levelData[x][y];
						}
					}

					//					FileOperator.writeImage(mountainData, MOUNTAIN_MAP_OUTPUT_FILENAME);
					FileOperator.writeImage(mountainData, System.getProperty("user.dir")+"\\output\\elevation_map_samples\\elevation_mountains_"+z+"_"+zz+"_d2.png");
					System.out.println("done");
				}
			}
		} catch(Exception e){
			System.out.println(e.getMessage());
		}

	}

	public static void main(String[] args) {
		//		new FeatureMapCreator().createMountainMap();
		new FeatureMapCreator().createFeaturesMap();
	}
}
