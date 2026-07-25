package vectormaps;
import util.Colors;
import util.FileOperator;

public class ElevationMapCreator {

	// idea: https://commons.wikimedia.org/wiki/File:World_elevation_map.png
	// source: https://visibleearth.nasa.gov/images/73934/topography
	// https://visibleearth.nasa.gov/images/73963/bathymetry
	
	// https://www.nexusmods.com/skyrim/mods/52675
	// https://www.reddit.com/r/skyrim/comments/18ye5o/map_of_nirn_is_there_ever_any_interaction_between/

//	public static final String ELEVATION_MAP_FILENAME = System.getProperty("user.dir")+"\\input\\gebco_08_rev_elev_21600x10800.png";
//	public static final String BATHYMETRY_MAP_FILENAME = System.getProperty("user.dir")+"\\input\\gebco_08_rev_bath_21600x10800.png";
//	public static final String BASE_MAP_FILENAME = System.getProperty("user.dir")+"\\input\\eo_base_2020_clean_geo_modified.png";
//	
//	public static final String SAMPLES_OUTPUT_FOLDER = System.getProperty("user.dir")+"\\output\\map\\samples\\elevation_samples\\";
//	public static final String BASE_MAP_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\base_map.png";
//	public static final String ELEVATION_MAP_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\elevation_levels.png";

	public static final int BLUE = 0xFF106090;
	public static final int GREEN = 0xFF709070;

	public static final int SCALE = 1;

	public enum MapName {
	    EARTH_1_CE, EARTH_16K_BCE, TES_NIRN, FF6_OVERWORLD
	}
	
	public enum Trace {
	    NONE, VISUAL_REGIONS, VISUAL_POLYGONS, TEXT, REGIONS_AND_TEXT, ALL
	}
	
	public static String getElevationInputFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\input\\map\\earth\\gebco_08_rev_elev_21600x10800.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\input\\map\\earth\\gebco_08_rev_elev_21600x10800.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\input\\map\\tes_nirn\\TamrielHeightmap_Full_Combined.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\input\\map\\ff6_overworld\\heightmaps_20260518_153317_2.png";
		return "";
	}
	
	public static String getBathymetryInputFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\input\\map\\earth\\gebco_08_rev_bath_21600x10800.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\input\\map\\earth\\gebco_08_rev_bath_21600x10800.png";
		if(name == MapName.TES_NIRN) return "";
		if(name == MapName.FF6_OVERWORLD) return "";
		return "";
	}
	
	public static String getRawBaseMapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\input\\map\\earth\\eo_base_2020_clean_geo_modified.png";
		if(name == MapName.EARTH_16K_BCE) return "";
		if(name == MapName.TES_NIRN) return "";
		if(name == MapName.FF6_OVERWORLD) return "";
		return "";
	}
	
	public static String getBaseMapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\base_map.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\base_map.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\base_map.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\base_map.png";
		return "";
	}
	
	public static String getElevationLevelsFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\elevation_levels.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\elevation_levels.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\elevation_levels.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\elevation_levels.png";
		return "";
	}
	
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
			int[][] elevationData = FileOperator.readImage(getElevationInputFilename(MapName.EARTH_1_CE));
			int[][] bathymetryData = FileOperator.readImage(getBathymetryInputFilename(MapName.EARTH_1_CE));
			System.out.println("done reading");

			for(int threshold=-5;threshold<=5;threshold ++) {
				createBaseMapFromThreshold(bathymetryData, elevationData, threshold, System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\samples\\elevation_samples\\threshold_"+threshold+".png");
			}

			System.out.println("done");
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public int[][] createBaseMapFromThreshold(int[][] elevationData, int[][] bathymetryData, int threshold, String outputFileName) {
		try{
			int[][] filteredData = new int[elevationData.length][];
			for (int x = 0; x < elevationData.length; x++) {
				filteredData[x] = new int[elevationData[x].length];
				for (int y = 0; y < elevationData[x].length; y++) {
					int val = 255;
					if(bathymetryData != null) val = Colors.blueVal(bathymetryData[x][y]);
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

			FileOperator.writeImage(filteredData, outputFileName, SCALE);

			System.out.println("done: create base map "+outputFileName);
			
			return filteredData;
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		return null;
	}

	public int[][] createBaseMap(int[][] baseData, String outputFileName) {
		try{
			System.out.println("done reading");

			int[][] filteredData = new int[baseData.length][];
			for (int x = 0; x < baseData.length; x++) {
				filteredData[x] = new int[baseData[x].length];
				for (int y = 0; y < baseData[x].length; y++) {
					int[] rgb = Colors.intToRGBArray(baseData[x][y]);

					if((y > 1840 && y < 9000 || 1.*rgb[2]/rgb[1] > 1.03 || rgb[0] < 208) && rgb[2] > rgb[1] + 2 && rgb[2] > rgb[0] + 2 && rgb[2] <= 240 && rgb[2] > 200 && (rgb[2] > 225 || 1.*rgb[2]/rgb[1] > 1.02) && (rgb[2] > 220 || 1.*rgb[2]/rgb[1] > 1.025) && (rgb[2] > 215 || 1.*rgb[2]/rgb[1] > 1.03)) filteredData[x][y] = BLUE;
					else filteredData[x][y] = GREEN;
				}
			}

			FileOperator.writeImage(filteredData, outputFileName, SCALE);

			System.out.println("done: create base map "+outputFileName);
			
			return filteredData;
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		return null;
	}

	public void createElevationMap(int[][] elevationData, int[][] baseData, int offset, String outputFileName) {
		try{
			System.out.println("done reading");

			int[][] levelData = new int[baseData.length][];
			for (int x = 0; x < baseData.length; x++) {
				levelData[x] = new int[baseData[x].length];
				for (int y = 0; y < baseData[x].length; y++) {
					levelData[x][y] = LEVEL_COLORS[LEVEL_COLORS.length-1][1];
					if(baseData[x][y] == BLUE) {
						levelData[x][y] = BLUE;
					} else {
						int val = Colors.blueVal(elevationData[x][y]) + offset;
						for(int i=0;i<LEVEL_COLORS.length;i++) {
							if(val < LEVEL_COLORS[i][0]) {levelData[x][y] = LEVEL_COLORS[i][1]; break;}
						}
					}
				}
			}

			FileOperator.writeImage(levelData, outputFileName);
			System.out.println("done: create elevation level map "+outputFileName);
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	public void run(MapName name) {
		if(name == MapName.EARTH_1_CE) {
			int[][] rawBaseData = FileOperator.readImage(getRawBaseMapFilename(name));
			int[][] elevationData = FileOperator.readImage(getElevationInputFilename(name));
			int[][] baseData = new ElevationMapCreator().createBaseMap(rawBaseData, getBaseMapFilename(name));
			new ElevationMapCreator().createElevationMap(elevationData, baseData, 0, getElevationLevelsFilename(name));
		} else if(name == MapName.EARTH_16K_BCE) {
			int[][] elevationData = FileOperator.readImage(getElevationInputFilename(name));
			int[][] bathymetryData = FileOperator.readImage(getBathymetryInputFilename(name));
			int[][] baseData = new ElevationMapCreator().createBaseMapFromThreshold(elevationData, bathymetryData, -3, getBaseMapFilename(name));
			new ElevationMapCreator().createElevationMap(elevationData, baseData, 3, getElevationLevelsFilename(name));
		} else if(name == MapName.TES_NIRN) {
			int[][] elevationData = FileOperator.readImage(getElevationInputFilename(name));
			int[][] baseData = new ElevationMapCreator().createBaseMapFromThreshold(elevationData, null, 12, getBaseMapFilename(name));
			new ElevationMapCreator().createElevationMap(elevationData, baseData, 0, getElevationLevelsFilename(name));
		} else if(name == MapName.FF6_OVERWORLD) {
			int[][] elevationData = FileOperator.readImage(getElevationInputFilename(name));
			int[][] baseData = new ElevationMapCreator().createBaseMapFromThreshold(elevationData, null, 10, getBaseMapFilename(name));
			new ElevationMapCreator().createElevationMap(elevationData, baseData, 0, getElevationLevelsFilename(name));
		}
	}

	public static void main(String[] args) {
		new ElevationMapCreator().run(MapName.EARTH_1_CE);
		new ElevationMapCreator().run(MapName.EARTH_16K_BCE);
		new ElevationMapCreator().run(MapName.TES_NIRN);
		new ElevationMapCreator().run(MapName.FF6_OVERWORLD);
		
//		new ElevationMapCreator().createBaseMap();
//		new ElevationMapCreator().createElevationMap();
//		new ElevationMapCreator().createSampleMaps();
	}

}
