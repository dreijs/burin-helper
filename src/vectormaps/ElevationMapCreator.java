package vectormaps;
import util.Colors;
import util.FileOperator;

public class ElevationMapCreator {

	// idea: https://commons.wikimedia.org/wiki/File:World_elevation_map.png
	// source: https://visibleearth.nasa.gov/images/73934/topography
	// https://visibleearth.nasa.gov/images/73963/bathymetry

	public static final String ELEVATION_MAP_FILENAME = System.getProperty("user.dir")+"\\input\\gebco_08_rev_elev_21600x10800.png";
	public static final String BATHYMETRY_MAP_FILENAME = System.getProperty("user.dir")+"\\input\\gebco_08_rev_bath_21600x10800.png";
	public static final String BASE_MAP_FILENAME = System.getProperty("user.dir")+"\\input\\eo_base_2020_clean_geo_modified.png";
	
	public static final String SAMPLES_OUTPUT_FOLDER = System.getProperty("user.dir")+"\\output\\map\\samples\\elevation_samples\\";
	public static final String BASE_MAP_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\base_map.png";
	public static final String ELEVATION_MAP_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\elevation_levels.png";

	public static final int BLUE = 0xFF106090;
	public static final int GREEN = 0xFF709070;

	public static final int SCALE = 1;
	
	public static final int[][] LEVEL_COLORS = new int[][] {
		{20, 0xFF008800},
		{40, 0xFF00BB00},
		{60, 0xFF00EE00},
		{80, 0xFF88FF00},
		{100, 0xFFFFFF00},
		{120, 0xFFDDBB22},
		{140, 0xFFBB7744},
		{160, 0xFFAA6633},
		{180, 0xFF995522},
		{200, 0xFF884411},
		{220, 0xFF773300},
		{240, 0xFF662200},
		{256, 0xFF551100}
	};

//	public static final int[][] LEVEL_COLORS = new int[][] {
//		{16, 0xFF007700},
//		{32, 0xFF009200},
//		{48, 0xFF00AA00},
//		{64, 0xFF11CC00},
//		{80, 0xFF33EE00},
//		{96, 0xFF66FF00},
//		{112, 0xFF99FF00},
//		{128, 0xFFFFFF00},
//		{144, 0xFFFFDD11},
//		{160, 0xFFDDBB22},
//		{176, 0xFFBB7744},
//		{192, 0xFF995522},
//		{208, 0xFF884411},
//		{224, 0xFF773300},
//		{240, 0xFF662200},
//		{256, 0xFF551100}
//	};

	public void createSampleMaps() {

		try{
			int[][] elevationData = FileOperator.readImage(ELEVATION_MAP_FILENAME);
			int[][] bathymetryData = FileOperator.readImage(BATHYMETRY_MAP_FILENAME);
			System.out.println("done reading");

			for(int threshold=-5;threshold<=5;threshold ++) {
				int[][] filteredData = new int[elevationData.length][];
				for (int x = 0; x < elevationData.length; x++) {
					filteredData[x] = new int[elevationData[x].length];
					for (int y = 0; y < elevationData[x].length; y++) {
						int val = Colors.blueVal(bathymetryData[x][y]);
						if(val == 255) {
							val = Colors.blueVal(elevationData[x][y]);
							if(val < threshold) filteredData[x][y] = BLUE;
							else filteredData[x][y] = GREEN;

						} else {
							if(val < 255 + threshold) filteredData[x][y] = filteredData[x][y] = BLUE;
							else filteredData[x][y] = GREEN;
						}
					}
				}

				FileOperator.writeImage(filteredData, SAMPLES_OUTPUT_FOLDER+"threshold_"+threshold+".png");

				System.out.println("threshold "+threshold+" finished");
			}

			System.out.println("done");
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	public void createBaseMap() {

		try{
			int[][] baseData = FileOperator.readImage(BASE_MAP_FILENAME);
			System.out.println("done reading");

			int[][] filteredData = new int[baseData.length][];
			for (int x = 0; x < baseData.length; x++) {
				filteredData[x] = new int[baseData[x].length];
				for (int y = 0; y < baseData[x].length; y++) {
					int[] rgb = Colors.intToRGBArray(baseData[x][y]);

					if((y > 1840 && y < 9000 || 1.*rgb[2]/rgb[1] > 1.03 || rgb[0] < 208) && rgb[2] > rgb[1] + 2 && rgb[2] > rgb[0] + 2 && rgb[2] <= 240 && rgb[2] > 200) filteredData[x][y] = BLUE;
					else filteredData[x][y] = GREEN;
				}
			}

			FileOperator.writeImage(filteredData, BASE_MAP_OUTPUT_FILENAME, SCALE);

			System.out.println("done");
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	public void createElevationMap() {
		try{
			int[][] elevationData = FileOperator.readImage(ELEVATION_MAP_FILENAME, SCALE);
			int[][] baseData = FileOperator.readImage(BASE_MAP_OUTPUT_FILENAME);
			System.out.println("done reading");

			int[][] levelData = new int[baseData.length][];
			for (int x = 0; x < baseData.length; x++) {
				levelData[x] = new int[baseData[x].length];
				for (int y = 0; y < baseData[x].length; y++) {
					if(baseData[x][y] == BLUE) {
						levelData[x][y] = BLUE;
					} else {
						int val = Colors.blueVal(elevationData[x][y]);
						for(int i=0;i<LEVEL_COLORS.length;i++) {
							if(val < LEVEL_COLORS[i][0]) {levelData[x][y] = LEVEL_COLORS[i][1]; break;}
						}
					}
				}
			}

			FileOperator.writeImage(levelData, ELEVATION_MAP_OUTPUT_FILENAME);
			System.out.println("done");
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}



	public static void main(String[] args) {
		new ElevationMapCreator().createBaseMap();
		new ElevationMapCreator().createElevationMap();
		new ElevationMapCreator().createSampleMaps();
	}

}
