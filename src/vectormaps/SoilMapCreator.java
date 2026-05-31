package vectormaps;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import util.FileOperator;
import util.MapOperator;
import vectormaps.ElevationMapCreator.MapName;

public class SoilMapCreator {
	// https://upload.wikimedia.org/wikipedia/commons/e/e5/Global_soils_map_USDA.jpg
	
//	public static final String SOIL_BASE_MAP_FILENAME = System.getProperty("user.dir")+"\\input\\Soil4.png";
//	public static final String SOIL_FINAL_MAP_FILENAME = System.getProperty("user.dir")+"\\output\\map\\soil.png";
//	public static final String SOIL_FINAL_RESCALED_MAP_FILENAME = System.getProperty("user.dir")+"\\output\\map\\soil_rescaled.png";

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
	public static final int rocky = new Color(134, 125, 118).getRGB(); 
	public static final int shiftingSand = new Color(209, 201, 194).getRGB();

//	public static final int[] SPREADABLE_SOILS = {alfisol, andisol, aridisol, entisol, gelisol, histosol, inceptisol, mollisol, oxisol, spodosol, ultisol, vertisol, rocky, shiftingSand};
	public static final int[] ALL_SOILS = {alfisol, andisol, aridisol, entisol, gelisol, histosol, inceptisol, mollisol, oxisol, spodosol, ultisol, vertisol, rocky, shiftingSand};

//	final static int LEFT_OFFSET = 0;
//	final static int RIGHT_OFFSET = 7;
//	final static int TOP_OFFSET = 412;
//	final static int BOTTOM_OFFSET = 4780 - 3134;
	
	public static String getRawBaseMapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\input\\map\\earth\\Soil4.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\input\\map\\earth\\Soil4.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\input\\map\\tes_nirn\\soil.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\input\\map\\ff6_overworld\\soil.png";
		return "";
	}
	
	public static String getExtendedBaseMapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\soil.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\soil.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\soil.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\soil.png";
		return "";
	}
	
	public static String getRescaledBaseMapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\soil.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\soil.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\soil.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\soil.png";
		return "";
	}
	
	public void run(MapName name) {
		// just to get the correct dimensions:
		int[][] elevationBaseMap = FileOperator.readImage(ElevationMapCreator.getElevationLevelsFilename(name));
		
		int[][] baseMap = FileOperator.readImage(getRawBaseMapFilename(name));
		int[][] extendedMap = MapOperator.fillByExtension(baseMap, ALL_SOILS, ALL_SOILS, 1, getExtendedBaseMapFilename(name));
		MapOperator.graduallyRescaleMap(extendedMap, ALL_SOILS, ALL_SOILS, getRescaledBaseMapFilename(name), elevationBaseMap.length, elevationBaseMap[0].length);
	}
	
	public static void main(String[] args) {
		new SoilMapCreator().run(MapName.EARTH_1_CE);
		new SoilMapCreator().run(MapName.EARTH_16K_BCE);
		new SoilMapCreator().run(MapName.TES_NIRN);
		new SoilMapCreator().run(MapName.FF6_OVERWORLD);
		
//		MapOperator.fillByExtension(FileOperator.readImage(BIOME_BASE_MAP_FILENAME), ALL_BIOMES, ALL_BIOMES, 4, BIOME_EXTENDED_MAP_FILENAME);
//		MapOperator.graduallyRescaleMap(FileOperator.readImage(BIOME_EXTENDED_MAP_FILENAME), ALL_BIOMES, ALL_BIOMES, BIOME_FINAL_RESCALED_MAP_FILENAME);
	}


//	public static void main(String[] args) {
//		MapOperator.fillByExtension(FileOperator.readImage(SOIL_BASE_MAP_FILENAME), ALL_SOILS, ALL_SOILS, 1, SOIL_FINAL_MAP_FILENAME);
//		MapOperator.graduallyRescaleMap(FileOperator.readImage(SOIL_FINAL_MAP_FILENAME), ALL_SOILS, ALL_SOILS, SOIL_FINAL_RESCALED_MAP_FILENAME);
//	}
}
