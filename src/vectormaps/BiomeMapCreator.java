package vectormaps;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import util.FileOperator;
import util.MapOperator;

public class BiomeMapCreator {
	// https://upload.wikimedia.org/wikipedia/commons/e/e5/Global_soils_map_USDA.jpg
	public static final String BIOME_BASE_MAP_FILENAME = System.getProperty("user.dir")+"\\input\\Vegetation4b.png";

	public static final String BIOME_EXTENDED_MAP_FILENAME = System.getProperty("user.dir")+"\\output\\map\\biomes.png";
	public static final String BIOME_FINAL_RESCALED_MAP_FILENAME = System.getProperty("user.dir")+"\\output\\map\\biomes_rescaled.png";

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
	static final int glacial = new Color(201, 231, 255).getRGB();

	public static final int[] ALL_BIOMES = {tundra, taiga, temperate, steppe, subtropical_wet, mediterranean, monsoon, arid, xeric, dry_steppe, semiarid, grass_savanna, tree_savanna, subtropical_dry, tropical_rainforest, glacial};

	public static void main(String[] args) {
		MapOperator.fillByExtension(FileOperator.readImage(BIOME_BASE_MAP_FILENAME), ALL_BIOMES, ALL_BIOMES, 4, BIOME_EXTENDED_MAP_FILENAME);
		MapOperator.graduallyRescaleMap(FileOperator.readImage(BIOME_EXTENDED_MAP_FILENAME), ALL_BIOMES, ALL_BIOMES, BIOME_FINAL_RESCALED_MAP_FILENAME);
	}
}
