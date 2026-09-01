package vectormaps;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import util.Colors;
import util.FileOperator;
import util.GeometryUtils;
import util.MapOperator;
import util.Point;
import util.PointInt;
import vectormaps.ElevationMapCreator.MapName;

// ZHAW icecap video: https://www.youtube.com/watch?v=C3Jwnp-Z3yE

public class FeatureMapCreator {
//	public static final String ICECAP_INPUT_FILENAME = System.getProperty("user.dir")+"\\input\\icecap1ce_filtered.png";
//
//	public static final String SAMPLES_OUTPUT_FOLDER = System.getProperty("user.dir")+"\\output\\map\\samples\\mountain_samples\\";
//	public static final String HILL_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\hill_map.png";
//	public static final String MOUNTAINS_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\mountain_map.png";
//	public static final String WETLANDS_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\wetlands_map.png";
//	public static final String WATER_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\water_map.png";
//	public static final String CLIFFS_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\cliff_map.png";
//	public static final String ICECAP_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\icecap_map.png";
//
//	public static final String FEATURES_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\features_map.png";
//	public static final String FEATURES_RAW_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\features_raw_map.png";

	public static final int NO_FEATURES = new Color(255, 255, 255).getRGB(); 

	public static final int WETLANDS_COLOR = new Color(0, 192, 128).getRGB(); 
	public static final int HILL_COLOR = new Color(255, 128, 0).getRGB(); 
	public static final int VALLEY_COLOR = new Color(255, 192, 0).getRGB(); 
	public static final int MOUNTAIN_COLOR = new Color(255, 0, 0).getRGB(); 
	public static final int CLIFF_NORTH_COLOR = new Color(192, 0, 0).getRGB(); 
	public static final int CLIFF_SOUTH_COLOR = new Color(128, 0, 0).getRGB(); 

	public static final int OCEAN_COLOR = new Color(0, 16, 96).getRGB();
	public static final int SEA_COLOR = new Color(0, 32, 128).getRGB();
	public static final int INNER_SEA_COLOR = new Color(0, 32, 192).getRGB();
	public static final int LAKE_COLOR = new Color(32, 96, 255).getRGB();
	public static final int ICECAP_COLOR = new Color(240, 248, 255).getRGB();
	public static final int ICY_WATER_COLOR = new Color(200, 224, 255).getRGB();

	public static final int[] ALL_FEATURES = {NO_FEATURES, WETLANDS_COLOR, HILL_COLOR, VALLEY_COLOR, MOUNTAIN_COLOR, CLIFF_NORTH_COLOR, CLIFF_SOUTH_COLOR, OCEAN_COLOR, SEA_COLOR, INNER_SEA_COLOR, LAKE_COLOR, ICECAP_COLOR, ICY_WATER_COLOR};

	public static String getHillMapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\hill_map.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\hill_map.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\hill_map.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\hill_map.png";
		return "";
	}

	public static String getMountainMapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\mountain_map.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\mountain_map.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\mountain_map.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\mountain_map.png";
		return "";
	}
	
	public static String getWetlandsMapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\wetlands_map.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\wetlands_map.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\wetlands_map.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\wetlands_map.png";
		return "";
	}
	
	public static String getWaterMapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\water_map.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\water_map.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\water_map.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\water_map.png";
		return "";
	}
	
	public static String getCliffMapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\cliff_map.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\cliff_map.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\cliff_map.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\cliff_map.png";
		return "";
	}
	
	public static String getIcecapFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\icecap_map.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\icecap_map.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\icecap_map.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\icecap_map.png";
		return "";
	}
	
	public static String getIcecapInputFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\input\\map\\earth\\icecap1ce_filtered.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\input\\map\\earth\\icecap18kbc_filtered.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\input\\map\\tes_nirn\\icecap.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\input\\map\\ff6_overworld\\icecap.png";
		return "";
	}
	
	public static String getFeaturesOutputFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\features_map.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\features_map.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\features_map.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\features_map.png";
		return "";
	}
	
	public static String getRawFeaturesOutputFilename(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\features_raw_map.png";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\features_raw_map.png";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\features_raw_map.png";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\features_raw_map.png";
		return "";
	}

	public String getSampleFoldername(MapName name) {
		if(name == MapName.EARTH_1_CE) return System.getProperty("user.dir")+"\\output\\map\\earth_1_ce\\samples\\";
		if(name == MapName.EARTH_16K_BCE) return System.getProperty("user.dir")+"\\output\\map\\earth_16k_bce\\samples\\\\";
		if(name == MapName.TES_NIRN) return System.getProperty("user.dir")+"\\output\\map\\tes_nirn\\samples\\\\";
		if(name == MapName.FF6_OVERWORLD) return System.getProperty("user.dir")+"\\output\\map\\ff6_overworld\\samples\\\\";
		return "";
	}
	
	public int[][] getOceanPixels(MapName name) {
		if(name == MapName.EARTH_1_CE) return new int[][] {{8200, 3600}, {10000, 7000}, {15500, 6500}, {2000, 6000}, {20500, 3500}, {13000, 2800}};
		if(name == MapName.EARTH_16K_BCE) return new int[][] {{8200, 3600}, {10000, 7000}, {15500, 6500}, {2000, 6000}, {20500, 3500}, {13000, 2800}};
		if(name == MapName.TES_NIRN) return new int[][] {{20000, 5000}, {12000, 6000}, {2500, 7500}};
		if(name == MapName.FF6_OVERWORLD) return new int[][] {{6000, 2600}, {10200, 2600}, {950, 2600}};
		return new int[][] {};
	}
	
	public int[][] getSaltWaterPixels(MapName name) {
		if(name == MapName.EARTH_1_CE) return new int[][] {{14380, 2725}, {13875, 3075}};
		if(name == MapName.EARTH_16K_BCE) return new int[][] {{14380, 2725}, {13875, 3075}};
		if(name == MapName.TES_NIRN) return new int[][] {};
		if(name == MapName.FF6_OVERWORLD) return new int[][] {};
		return new int[][] {};
	}

	public int[][] addWetlandData(int[][] baseData, int[][] elevationData, int[][] soilData, int d, int h, MapName name) {
		int[][] wetlandData = new int[baseData.length][baseData[0].length];

		for (int x = 0; x < baseData.length; x++) {
			for (int y = 0; y < baseData[x].length; y++) {
				if(soilData[x][y] == SoilMapCreator.entisol && baseData[x][y] != ElevationMapCreator.BLUE && Colors.blueVal(elevationData[x][y]) < h) {
					wetlandData[x][y] = 0xFFFFFFFF;
				} else {
					wetlandData[x][y] = ElevationMapCreator.GREEN;
				}
			}
		}

		try {
			BufferedReader reader = new BufferedReader(new FileReader(RiverProcessor.getReformattedRiverFilename(name)));

			String line;

			while ((line = reader.readLine()) != null) {
				String[] lineData = line.split(",  ");
				System.out.println(lineData.length);
				for(int i=1;i<lineData.length-1;i++) {
					// -1.40912872440386E7 -6253856.36090267 1.79637592676961E7 8782793.20125636
					String[] s1a = lineData[i].split("\\(");
					String[] s2a = s1a[1].split("\\)");
					String[] s3a = s2a[0].split(", ");
					String[] s1b = lineData[i+1].split("\\(");
					String[] s2b = s1b[1].split("\\)");
					String[] s3b = s2b[0].split(", ");
					int xa = (int) (baseData.length * (Long.parseLong(s3a[0]) + 180000000) / 360000000);
					int ya = (int) (baseData[0].length * (Long.parseLong(s3a[1]) + 90000000) / 180000000);
					int xb = (int) (baseData.length * (Long.parseLong(s3b[0]) + 180000000) / 360000000);
					int yb = (int) (baseData[0].length * (Long.parseLong(s3b[1]) + 90000000) / 180000000);

					List<Point> points = GeometryUtils.getLine(xa, baseData[0].length - ya - 1, xb, baseData[0].length - yb - 1);
					for(Point pp : points) {
						PointInt p = pp.asIntPoint();
						if(wetlandData[p.x][p.y] != ElevationMapCreator.GREEN) wetlandData[p.x][p.y] = WETLANDS_COLOR;
					}
				}
			}

			int[] wetlands = new int[] {WETLANDS_COLOR};
			int[] greenAndWetlands = new int[] {ElevationMapCreator.GREEN, WETLANDS_COLOR};

			int[][] newData = MapOperator.fillByExtension(wetlandData, greenAndWetlands, wetlands, 1, "", d);

			for (int x = 0; x < baseData.length; x++) {
				for (int y = 0; y < baseData[x].length; y++) {
					if(newData[x][y] == WETLANDS_COLOR) {
						wetlandData[x][y] = WETLANDS_COLOR;
					} else {
						wetlandData[x][y] = baseData[x][y];
					}
				}
			}

			reader.close();
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}

		System.out.println("done");

		return wetlandData;
	}

	public void createWetlandMap(int d, MapName name) {
		int[][] elevationData = FileOperator.readImage(ElevationMapCreator.getElevationInputFilename(name));
		int[][] levelData = FileOperator.readImage(ElevationMapCreator.getElevationLevelsFilename(name));
		int[][] soilData = FileOperator.readImage(SoilMapCreator.getRescaledBaseMapFilename(name));
		System.out.println("done reading");

		int[][] wetlandData = addWetlandData(levelData, elevationData, soilData, d, 36, name);

		FileOperator.writeImage(wetlandData, getWetlandsMapFilename(name));
	}

	public int[][] addIcecapData(int[][] baseData, int[][] iceInputData, int d, MapName name) {		
		int[] icecap = new int[] {ICECAP_COLOR};
		int[] blueAndIcecap = new int[] {ElevationMapCreator.BLUE, ICECAP_COLOR};
		int[][] rescaledIcecapData = MapOperator.graduallyRescaleMap(iceInputData, blueAndIcecap, blueAndIcecap, getSampleFoldername(name)+"polar_intermediate_filled_"+d+"_.png", baseData.length, baseData[0].length);
		int[][] icyWaterDataData = MapOperator.fillByExtension(rescaledIcecapData, icecap, icecap, 1, getSampleFoldername(name)+"icy_intermediate_filled_"+d+"_.png", d);
		
		System.out.println(baseData.length+" "+baseData[0].length);
		System.out.println(rescaledIcecapData.length+" "+rescaledIcecapData[0].length);
		System.out.println(icyWaterDataData.length+" "+icyWaterDataData[0].length);

		int[][] finalData = new int[rescaledIcecapData.length][rescaledIcecapData[0].length];
		for (int x = 0; x < rescaledIcecapData.length; x++) {
			for (int y = 0; y < rescaledIcecapData[x].length; y++) {
				if(rescaledIcecapData[x][y] == ICECAP_COLOR) finalData[x][y] = ICECAP_COLOR;
				else if(icyWaterDataData[x][y] == ICECAP_COLOR) finalData[x][y] = ICY_WATER_COLOR;
				else finalData[x][y] = baseData[x][y];
			}
		}

		return finalData;
	}

	public void createIcecapMap(int d, MapName name) {
		int[][] levelData = FileOperator.readImage(ElevationMapCreator.getElevationLevelsFilename(name));
		int[][] iceInputData= FileOperator.readImage(getIcecapInputFilename(name));
		for (int x = 0; x < iceInputData.length; x++) {
			for (int y = 0; y < iceInputData[x].length; y++) {
				if(Colors.isBlack(iceInputData[x][y]) && Colors.alphaVal(iceInputData[x][y]) > 0) iceInputData[x][y] = ICECAP_COLOR;
				else iceInputData[x][y] = ElevationMapCreator.BLUE;
			}
		}

		int[][] iceData = addIcecapData(levelData, iceInputData, d, name);

		FileOperator.writeImage(iceData, getIcecapFilename(name));

	}

	public int[][] addWaterData(int[][] baseData, MapName name) {
		int[] green = new int[] {ElevationMapCreator.GREEN};
		int[] ocean = new int[] {OCEAN_COLOR};
		int[] greenAndOcean = new int[] {ElevationMapCreator.GREEN, OCEAN_COLOR};

		int[][] sea1Buffer = MapOperator.fillByExtension(baseData, green, green, 1, getSampleFoldername(name)+"ocean_intermediate_"+60+"_.png", 60);
		int[][] sea2Buffer = MapOperator.fillByExtension(baseData, green, green, 1, getSampleFoldername(name)+"ocean_intermediate_"+240+"_.png", 240);

		int[][] saltWaterData = new int[baseData.length][baseData[0].length];

		for (int x = 0; x < baseData.length; x++) {
			for (int y = 0; y < baseData[x].length; y++) {
				saltWaterData[x][y] = baseData[x][y];
			}
		}

		int[][] oceanPixels = getOceanPixels(name); 
		int[][] saltWaterPixels = getSaltWaterPixels(name); 

		for(int i=0;i<oceanPixels.length;i++) {
			sea1Buffer[oceanPixels[i][0]][oceanPixels[i][1]] = OCEAN_COLOR; 
			sea2Buffer[oceanPixels[i][0]][oceanPixels[i][1]] = OCEAN_COLOR;
			saltWaterData[oceanPixels[i][0]][oceanPixels[i][1]] = OCEAN_COLOR; 
		}

		for(int i=0;i<saltWaterPixels.length;i++) {
			saltWaterData[saltWaterPixels[i][0]][saltWaterPixels[i][1]] = OCEAN_COLOR; 
		}

		int[][] sea1Data = MapOperator.fillByExtensionDepthFirst(sea1Buffer, greenAndOcean, ocean, 1, getSampleFoldername(name)+"ocean_intermediate_filled_60_.png");
		int[][] sea2Data = MapOperator.fillByExtensionDepthFirst(sea2Buffer, greenAndOcean, ocean, 1, getSampleFoldername(name)+"ocean_intermediate_filled_240_.png");

		int[][] sea1DataB = MapOperator.fillByExtension(sea1Data, ocean, ocean, 1, "", 60);
		int[][] sea2DataB = MapOperator.fillByExtension(sea2Data, ocean, ocean, 1, "", 240);
		int[][] saltwaterDataB = MapOperator.fillByExtensionDepthFirst(saltWaterData, greenAndOcean, ocean, 1, getSampleFoldername(name)+"ocean_intermediate_filled_0_.png");

		int[][] oceanData = new int[baseData.length][baseData[0].length];

		for (int x = 0; x < baseData.length; x++) {
			for (int y = 0; y < baseData[x].length; y++) {
				if(sea2DataB[x][y] == OCEAN_COLOR) {
					oceanData[x][y] = OCEAN_COLOR;
				} else if(sea1DataB[x][y] == OCEAN_COLOR) {
					oceanData[x][y] = SEA_COLOR;
				} else if(saltwaterDataB[x][y] == OCEAN_COLOR) {
					oceanData[x][y] = INNER_SEA_COLOR;
				} else if(baseData[x][y] == ElevationMapCreator.BLUE) {
					oceanData[x][y] = LAKE_COLOR;
				}
			}
		}

		return oceanData;
	}

	public void createWaterSampleMap(int w, MapName name) {
		int[][] baseData = FileOperator.readImage(ElevationMapCreator.getBaseMapFilename(name));
		int[] green = new int[] {ElevationMapCreator.GREEN};
		MapOperator.fillByExtension(baseData, green, green, 1, getSampleFoldername(name)+"ocean_intermediate_"+w+"_.png", w);
	}

	public void createWaterMap(MapName name) {
		int[][] baseData = FileOperator.readImage(ElevationMapCreator.getBaseMapFilename(name));
		System.out.println("done reading");

		int[][] waterData = addWaterData(baseData, name);

		FileOperator.writeImage(waterData, getWaterMapFilename(name));
	}

	public int[][] addHillData(int[][] baseData, int[][] elevationData, int w, int d, int m, MapName name) {
		int[][] hillData = new int[baseData.length][];

		for (int x = 0; x < baseData.length; x++) {
			hillData[x] = new int[baseData[x].length];
			for (int y = 0; y < hillData[x].length; y++) {
				hillData[x][y] = baseData[x][y];
				int minElev = 255;
				int maxElev = 0;

				boolean[][] surr = new boolean[3][3];

				for(int dx = -w; dx <= w; dx++) {
					for(int dy = -w; dy <= w; dy++) {
						int xx = x + dx;
						int yy = y + dy;
						if(Math.sqrt(dx*dx + dy*dy) <= w) {
							if(xx >= 0 && xx < hillData.length && yy >= 0 && yy < hillData[0].length) {
								int val = Colors.blueVal(elevationData[xx][yy]);
								minElev = Math.min(minElev, val);
								maxElev = Math.max(maxElev, val);

								if(elevationData[xx][yy] > elevationData[x][y]) surr[3*(dx+w)/(2*w+1)][3*(dy+w)/(2*w+1)] = true;
							}
						}
					}
				}

				int numSurr = 0;
				for(int i=0;i<3;i++) {
					for(int j=0;j<3;j++) {
						if(surr[i][j]) numSurr++;
					}
				}

				if(maxElev - minElev > d && Colors.blueVal(elevationData[x][y]) > 0 && maxElev > m) {
					if(numSurr >= 9) {
						hillData[x][y] = VALLEY_COLOR;
					} else {
						hillData[x][y] = HILL_COLOR;
					}
				}
			}
		}

		return hillData;
	}

	public void createHillMap(int w, int d, int m, MapName name) {
		int[][] elevationData = FileOperator.readImage(ElevationMapCreator.getElevationInputFilename(name));
		int[][] levelData = FileOperator.readImage(ElevationMapCreator.getElevationLevelsFilename(name));
		System.out.println("done reading");

		int[][] hillData = addHillData(levelData, elevationData, w, d, m, name);

		FileOperator.writeImage(hillData, getHillMapFilename(name));
	}

	public int[][] addMountainData(int[][] baseData, int[][] elevationData, int w, int d, int m, int slope, MapName name) {
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

	public void createMountainMap(int w, int d, int m, int slope, MapName name) {
		int[][] elevationData = FileOperator.readImage(ElevationMapCreator.getElevationInputFilename(name));
		int[][] levelData = FileOperator.readImage(ElevationMapCreator.getElevationLevelsFilename(name));
		System.out.println("done reading");

		int[][] mountainData = addMountainData(levelData, elevationData, w, d, m, slope, name);

		FileOperator.writeImage(mountainData, getMountainMapFilename(name));
	}

	public int[][] addCliffData(int[][] baseData, int[][] elevationData, double ww, int d, int dd, int m, MapName name) {
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

	public void createCliffMap(double ww, int d, int dd, int m, MapName name) {
		int[][] elevationData = FileOperator.readImage(ElevationMapCreator.getElevationInputFilename(name));
		int[][] levelData = FileOperator.readImage(ElevationMapCreator.getElevationLevelsFilename(name));
		System.out.println("done reading");

		int[][] cliffData = addCliffData(levelData, elevationData, ww, d, dd, m, name);

		FileOperator.writeImage(cliffData, getCliffMapFilename(name));
	}

	public void createCliffMapSamples(double d, int m, MapName name) {
		try{
			int[][] elevationData = FileOperator.readImage(ElevationMapCreator.getElevationInputFilename(name));
			int[][] levelData = FileOperator.readImage(ElevationMapCreator.getElevationLevelsFilename(name));
			System.out.println("done reading");

			for(int z=20;z<=28;z+=2) {
				for(int zz=Math.max(0,z-16);zz<=z;zz+=4) {
					int[][] mountainData = addCliffData(levelData, elevationData, d, z, zz, m, name);

					FileOperator.writeImage(mountainData, getFeaturesOutputFilename(name)+"elevation_mountains_"+z+"_"+zz+"_d2.png");
					System.out.println("done");
				}
			}
		} catch(Exception e){
			System.out.println(e.getMessage());
		}

	}

	public void createFeaturesMapFromIntermediates(MapName name) {
		int[][] featureData = FileOperator.readImage(ElevationMapCreator.getElevationLevelsFilename(name));
		System.out.println("done reading");

		int[][] hillData = FileOperator.readImage(getHillMapFilename(name));
		int[][] mountainData = FileOperator.readImage(getMountainMapFilename(name));
		int[][] cliffData = FileOperator.readImage(getCliffMapFilename(name));
		int[][] wetlandsData = FileOperator.readImage(getWetlandsMapFilename(name));
		int[][] waterData = FileOperator.readImage(getWaterMapFilename(name));
		int[][] icecapData = FileOperator.readImage(getIcecapFilename(name));

		for (int x = 0; x < featureData.length; x++) {
			for (int y = 0; y < featureData[x].length; y++) {
				if(waterData[x][y] == OCEAN_COLOR || waterData[x][y] == SEA_COLOR || waterData[x][y] == INNER_SEA_COLOR || waterData[x][y] == LAKE_COLOR) {
					if(icecapData[x][y] == ICECAP_COLOR) {
						featureData[x][y] = icecapData[x][y];
					} else if(icecapData[x][y] == ICY_WATER_COLOR) {
						featureData[x][y] = icecapData[x][y];
					} else {
						featureData[x][y] = waterData[x][y];
					}
				} else if(cliffData[x][y] == CLIFF_NORTH_COLOR || cliffData[x][y] == CLIFF_SOUTH_COLOR) {
					featureData[x][y] = cliffData[x][y];
				} else if(mountainData[x][y] == MOUNTAIN_COLOR) {
					featureData[x][y] = mountainData[x][y];
				} else if(hillData[x][y] == HILL_COLOR || hillData[x][y] == VALLEY_COLOR) {
					featureData[x][y] = hillData[x][y];
				} else if(wetlandsData[x][y] == WETLANDS_COLOR) {
					featureData[x][y] = wetlandsData[x][y];
				}
			}
		}

		FileOperator.writeImage(featureData, getFeaturesOutputFilename(name));

		for (int x = 0; x < featureData.length; x++) {
			for (int y = 0; y < featureData[x].length; y++) {
				boolean isFeature = false;
				for(int kk=0;kk<ALL_FEATURES.length;kk++) {
					if(featureData[x][y] == ALL_FEATURES[kk]) {
						isFeature = true;
					}
				}
				if(!isFeature) featureData[x][y] = NO_FEATURES;
			}
		}

		FileOperator.writeImage(featureData, getRawFeaturesOutputFilename(name));
	}

	public void runForParams(
			int hillScope, 
			int hillElevDiff, 
			int minHillHeight, 
			int mountainScope, 
			int mountainElevDiff, 
			int minMountainHeight, 
			int mountainSlope,
			double cliffScope, 
			int minGlobalElevDiff, 
			int minLocalElevDiff, 
			int minElev,
			int wetlandDist,
			int icyWaterDist,
			MapName name
		) {
		
		createHillMap(hillScope, hillElevDiff, minHillHeight, name);
		createMountainMap(mountainScope, mountainElevDiff, minMountainHeight, mountainSlope, name);
		createCliffMap(cliffScope, minGlobalElevDiff, minLocalElevDiff, minElev, name);
		createWetlandMap(wetlandDist, name);
		createWaterMap(name);
		createIcecapMap(icyWaterDist, name);
		
		createFeaturesMapFromIntermediates(name);
	}

	public void run(MapName name) {
		if(name == MapName.EARTH_1_CE) {
			runForParams(6, 20, 80,  	3, 32, 96, 6,	2., 24, 20, 40,		30, 160, name);
		} else if(name == MapName.EARTH_16K_BCE) {
			runForParams(6, 20, 80,  	3, 32, 96, 6,	2., 24, 20, 40,		30, 160, name);
		} else if(name == MapName.TES_NIRN) {
			runForParams(20, 8, 40,  	16, 10, 50, 6,	4., 6, 4, 40,		30, 160, name);
		} else if(name == MapName.FF6_OVERWORLD) {
			runForParams(16, 10, 40,  	10, 12, 60, 6,	4., 14, 8, 40,		30, 160, name);
		}
	}

	public static void main(String[] args) {
		new FeatureMapCreator().run(MapName.EARTH_1_CE);
//		new FeatureMapCreator().run(MapName.EARTH_16K_BCE);
//		new FeatureMapCreator().run(MapName.TES_NIRN);
//		new FeatureMapCreator().run(MapName.FF6_OVERWORLD);
	}
}
