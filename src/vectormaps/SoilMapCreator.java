package vectormaps;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import util.FileOperator;
import util.MapOperator;

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


	public static void main(String[] args) {
		MapOperator.fillByExtension(FileOperator.readImage(SOIL_BASE_MAP_FILENAME), ALL_SOILS, SPREADABLE_SOILS, 1, SOIL_FINAL_MAP_FILENAME);
		MapOperator.graduallyRescaleMap(FileOperator.readImage(SOIL_FINAL_MAP_FILENAME), ALL_SOILS, ALL_SOILS, SOIL_FINAL_RESCALED_MAP_FILENAME);
	}
}
