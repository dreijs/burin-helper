package util;

import java.util.HashMap;
import java.util.Map;

public class MapOperator {
	
	public static int colDist(int col1, int col2) {
		int col1r = (col1 >> 16) & 0xFF; 
		int col1g = (col1 >> 8) & 0xFF; 
		int col1b = col1 & 0xFF; 

		int col2r = (col2 >> 16) & 0xFF; 
		int col2g = (col2 >> 8) & 0xFF; 
		int col2b = col2 & 0xFF; 

		return (col1r - col2r) * (col1r - col2r) + (col1g - col2g) * (col1g - col2g) + (col1b - col2b) * (col1b - col2b);
	}
	
	public static int[][] removeOrExpandLonePixels(int[][] baseData) {
		int[][] newData = new int[baseData.length][baseData[0].length];
		
		for (int x = 0; x < baseData.length; x++) {
			for (int y = 0; y < baseData[x].length; y++) {
				newData[x][y] = baseData[x][y];
			}
		}
		
		int[][] nbs = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
		
		for (int x = 0; x < baseData.length; x++) {
			for (int y = 0; y < baseData[x].length; y++) {
				if(x > 0 && x < baseData.length-1 && y > 0 && y < baseData[0].length-1) {
					int v = baseData[x][y];
					boolean changed = false;
					if(v != baseData[x-1][y] && v != baseData[x+1][y] && v != baseData[x][y+1] && v != baseData[x][y-1]) {
						if(v == baseData[x+1][y+1]) {
							changed = true;
							if(Math.random() < 0.5) {
								newData[x][y+1] = v;
							} else {
								newData[x+1][y] = v;
							}
						}
						
						if(v == baseData[x-1][y+1]) {
							changed = true;
							if(Math.random() < 0.5) {
								newData[x][y+1] = v;
							} else {
								newData[x-1][y] = v;
							}
						}
						
						if(v == baseData[x+1][y-1]) {
							changed = true;
							if(Math.random() < 0.5) {
								newData[x][y-1] = v;
							} else {
								newData[x+1][y] = v;
							}
						}
						
						if(v == baseData[x-1][y-1]) {
							changed = true;
							if(Math.random() < 0.5) {
								newData[x][y-1] = v;
							} else {
								newData[x-1][y] = v;
							}
						}
					}
					
					if(!changed) {
						Map<Integer,Integer> map = new HashMap<Integer,Integer>();
						for(int n=0;n<nbs.length;n++) {
							int xx = Math.floorMod(x + nbs[n][0], baseData.length);
							int yy = y + nbs[n][1];
							if(yy >= 0 && yy < baseData[0].length) {
								if(!map.keySet().contains(baseData[xx][yy])) map.put(baseData[xx][yy], 1);
								else map.put(baseData[xx][yy], map.get(baseData[xx][yy]) + 1);
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
						
						if(max >= 3) newData[x][y] = argMax;
					}
				}
			}
		}
		
		return newData;
	}
	
	public static int[][] fillByExtension(int[][] baseData, int[] terrains, int[] spreadableTerrains, int d, String outputFileName) {
		return fillByExtension(baseData, terrains, spreadableTerrains, d, outputFileName, Integer.MAX_VALUE);
	}
	
	public static int[][] fillByExtension(int[][] baseData, int[] terrains, int[] spreadableTerrains, int d, String outputFileName, int maxIteration) {
		int[][] newData = new int[baseData.length][baseData[0].length];

		int[][] nbs = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
		int changed = 1;

		for (int x = 0; x < baseData.length; x++) {
			for (int y = 0; y < baseData[x].length; y++) {
				int v = baseData[x][y];
				boolean isSupportedTerrain = false;
				for(int kk=0;kk<terrains.length;kk++) {
					if(colDist(v, terrains[kk]) < d) {
						newData[x][y] = terrains[kk];
						isSupportedTerrain = true;
					}
				}
				if(!isSupportedTerrain) newData[x][y] = 0xFFFFFFFF;
			}
		}
		
		int c = 0;

		while(changed > 0 && c < maxIteration) {
			changed = 0;
			int[][] tempNewData = new int[baseData.length][];
			for (int x = 0; x < newData.length; x++) {
				tempNewData[x] = new int[baseData[x].length];
				for (int y = 0; y < newData[x].length; y++) {
					if(newData[x][y] == 0xFFFFFFFF) {
						Map<Integer,Integer> map = new HashMap<Integer,Integer>();
						for(int n=0;n<nbs.length;n++) {
							int xx = Math.floorMod(x + nbs[n][0], newData.length);
							int yy = y + nbs[n][1];
							if(yy >= 0 && yy < newData[0].length) {
								int v = newData[xx][yy];
								int matchIdx = -1;
								for(int kk=0;kk<spreadableTerrains.length;kk++) {
									if(v == spreadableTerrains[kk]) matchIdx = kk;
								}
								if(matchIdx >= 0) {
									int match = spreadableTerrains[matchIdx];
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
								for(int key : map.keySet()) {
									if(map.get(key) == max) {
//										if(Math.random() <= 1./(nMax - ii)) {
											tempNewData[x][y] = key;
											changed++;
//											break;
//										}
									}
								}
							}
						} else tempNewData[x][y] = 0xFFFFFFFF;
					} else {
						tempNewData[x][y] = newData[x][y];
					}
				}
			}
			
			c++;

			newData = tempNewData;
			System.out.println(changed);
		}

		if(outputFileName != "") FileOperator.writeImage(newData, outputFileName);
		System.out.println("done");
		
		return newData;
	}
	
	public static int[][] fillByExtensionDepthFirst(int[][] baseData, int[] terrains, int[] spreadableTerrains, int d, String outputFileName) {
		int[][] newData = new int[baseData.length][];

		int[][] nbs = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
		int changed = 1;

		for (int x = 0; x < baseData.length; x++) {
			newData[x] = new int[baseData[x].length];
			for (int y = 0; y < baseData[x].length; y++) {
				int v = baseData[x][y];
				boolean isSupportedTerrain = false;
				for(int kk=0;kk<terrains.length;kk++) {
					if(colDist(v, terrains[kk]) < d) {
						newData[x][y] = terrains[kk];
						isSupportedTerrain = true;
					}
				}
				if(!isSupportedTerrain) newData[x][y] = 0xFFFFFFFF;
			}
		}
		
		boolean reverse = false;

		while(changed > 0) {
			changed = 0;
			for (int xxx = 0; xxx < newData.length; xxx++) {
				for (int yyy = 0; yyy < newData[xxx].length; yyy++) {
					int x = xxx;
					int y = yyy;
					if(reverse) {
						x = newData.length - 1 - xxx;
						y = newData[xxx].length - 1 - yyy;
					}
					if(newData[x][y] == 0xFFFFFFFF) {
						Map<Integer,Integer> map = new HashMap<Integer,Integer>();
						for(int n=0;n<nbs.length;n++) {
							int xx = Math.floorMod(x + nbs[n][0], newData.length);
							int yy = y + nbs[n][1];
							if(yy >= 0 && yy < newData[0].length) {
								int v = newData[xx][yy];
								int matchIdx = -1;
								for(int kk=0;kk<spreadableTerrains.length;kk++) {
									if(v == spreadableTerrains[kk]) matchIdx = kk;
								}
								if(matchIdx >= 0) {
									int match = spreadableTerrains[matchIdx];
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
									newData[x][y] = argMax;
									changed++;
								} else newData[x][y] = 0xFFFFFFFF;
							} else {
								int ii=0;
								for(int key : map.keySet()) {
									if(map.get(key) == max) {
										if(Math.random() <= 1./(nMax - ii)) {
											newData[x][y] = key;
											changed++;
											break;
										}
										ii++;
									}
								}
							}
						} else newData[x][y] = 0xFFFFFFFF;
					} else {
						newData[x][y] = newData[x][y];
					}
				}
			}
			
			reverse = !reverse;

			System.out.println(changed);
		}

		if(outputFileName != "") FileOperator.writeImage(newData, outputFileName);
		System.out.println("done");
		
		return newData;
	}
	
	public static int[][] rescaleMap(int[][] baseData, int[] terrains, int[] spreadableTerrains, String outputFileName, int wTgt, int hTgt) {
		int[][] nbs = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
		int changed = 1;
		
		int wOri = baseData.length;
		int hOri = baseData[0].length;

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
				boolean isSupportedTerrain = false;
				for(int kk=0;kk<terrains.length;kk++) {
					if(v == terrains[kk]) {
						isSupportedTerrain = true;
					}
				}
				if(isSupportedTerrain) {
					newData[x * wTgt / wOri][y * hTgt / hOri] = v;
					if(x > 0 && x <baseData.length-1 && y > 0 && y < baseData[x].length - 1) {
//						newData[x * wTgt / wOri+1][y * hTgt / hOri+1] = v;
//						newData[x * wTgt / wOri-1][y * hTgt / hOri-1] = v;
//						newData[x * wTgt / wOri][y * hTgt / hOri+1] = v;
//						newData[x * wTgt / wOri][y * hTgt / hOri-1] = v;
//						newData[x * wTgt / wOri+1][y * hTgt / hOri+1] = v;
//						newData[x * wTgt / wOri+1][y * hTgt / hOri-1] = v;
//						newData[x * wTgt / wOri-1][y * hTgt / hOri+1] = v;
//						newData[x * wTgt / wOri-1][y * hTgt / hOri-1] = v;
//						if(baseData[x-1][y] == v && baseData[x+1][y] == v && baseData[x][y-1] == v && baseData[x][y+1] == v) {
//							for(int xx = x * wTgt / wOri; xx < (x+1) * wTgt / wOri; xx++) {
//								for(int yy = y * hTgt / hOri; yy < (y+1) * hTgt / hOri; yy++) {
//									newData[xx][yy] = v;
//								}
//							}
//						}
					}
				}
			}
		}

		while(changed > 0) {
			changed = 0;
			int[][] tempNewData = new int[newData.length][];
			int maxC = 0;
			
			// determine max number of colored neighbors
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
								for(int kk=0;kk<spreadableTerrains.length;kk++) {
									if(v == spreadableTerrains[kk]) matchIdx = kk;
								}
								if(matchIdx >= 0) {
									int match = spreadableTerrains[matchIdx];
									if(!map.keySet().contains(match)) map.put(match, 1);
									else map.put(match, map.get(match) + 1);
								}
							}
						}

						int max = 0;
						for(int key : map.keySet()) {
							if(map.get(key) > max) {
								max = map.get(key);
							}
						}
						if(max > 0) {
							maxC = Math.max(maxC, max);
						}
					}
				}
			}
			
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
								for(int kk=0;kk<spreadableTerrains.length;kk++) {
									if(v == spreadableTerrains[kk]) matchIdx = kk;
								}
								if(matchIdx >= 0) {
									int match = spreadableTerrains[matchIdx];
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
						if(max > 0 && max >= maxC) {
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

		if(outputFileName != "") FileOperator.writeImage(newData, outputFileName);
		System.out.println("done");
		
		return newData;
	}
	
	public static int[][] graduallyRescaleMap(int[][] baseData, int[] terrains, int[] spreadableTerrains, String outputFileName) {
		return graduallyRescaleMap(baseData, terrains, spreadableTerrains, outputFileName, 21600, 10800);
	}
	
	public static int[][] graduallyRescaleMap(int[][] baseData, int[] terrains, int[] spreadableTerrains, String outputFileName, int wTgt, int hTgt) {
		int[][] newData = new int[baseData.length][baseData[0].length];
		for (int x = 0; x < newData.length; x++) {
			for (int y = 0; y < newData[x].length; y++) {
				newData[x][y] = baseData[x][y];
			}
		}
		
		for(int i = 6; i * baseData.length < wTgt && i * baseData[0].length < hTgt; i+=6) {
			newData = rescaleMap(newData, terrains, spreadableTerrains, outputFileName, i * baseData.length, i * baseData[0].length);
		}
		
		return rescaleMap(newData, terrains, spreadableTerrains, outputFileName);
	}
	
	public static int[][] rescaleMap(int[][] baseData, int[] terrains, int[] spreadableTerrains, String outputFileName) {
		return rescaleMap(baseData, terrains, spreadableTerrains, outputFileName, 21600, 10800);
	}
	
//	public static int[][] rescaleMap(int[][] baseData, int[] terrains, int[] spreadableTerrains, String outputFileName, int wTgt, int hTgt) {
//		int[][] nbs = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
//		int changed = 1;
//		
//		int wOri = baseData.length;
//		int hOri = baseData[0].length;
//
//		int[][] newData = new int[wTgt][];
//		for (int x = 0; x < newData.length; x++) {
//			newData[x] = new int[hTgt];
//			for (int y = 0; y < newData[x].length; y++) {
//				newData[x][y] = 0xFFFFFFFF;
//			}
//		}
//		
//		for (int x = 0; x < baseData.length; x++) {
//			for (int y = 0; y < baseData[x].length; y++) {
//				int v = baseData[x][y];
//				boolean isSupportedTerrain = false;
//				for(int kk=0;kk<terrains.length;kk++) {
//					if(v == terrains[kk]) {
//						isSupportedTerrain = true;
//					}
//				}
//				if(isSupportedTerrain) {
//					newData[x * wTgt / wOri][y * hTgt / hOri] = v;
//					if(x > 0 && x <baseData.length-1 && y > 0 && y < baseData[x].length - 1) {
//						if(baseData[x-1][y] == v && baseData[x+1][y] == v && baseData[x][y-1] == v && baseData[x][y+1] == v
//							&& baseData[x-1][y-1] == v && baseData[x+1][y+1] == v && baseData[x+1][y-1] == v && baseData[x-1][y+1] == v) {
//							for(int xx = x * wTgt / wOri; xx < (x+1) * wTgt / wOri; xx++) {
//								for(int yy = y * hTgt / hOri; yy < (y+1) * hTgt / hOri; yy++) {
//									newData[xx][yy] = v;
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
//		while(changed > 0) {
//			changed = 0;
//			int[][] tempNewData = new int[newData.length][];
//
//			for (int x = 0; x < newData.length; x++) {
//				tempNewData[x] = new int[newData[x].length];
//				for (int y = 0; y < newData[x].length; y++) {
//					if(newData[x][y] == 0xFFFFFFFF) {
//						Map<Integer,Integer> map = new HashMap<Integer,Integer>();
//						for(int n=0;n<nbs.length;n++) {
//							int xx = x + nbs[n][0];
//							int yy = y + nbs[n][1];
//							if(xx >= 0 && xx < newData.length && yy >= 0 && yy < newData[0].length) {
//								int v = newData[xx][yy];
//								int matchIdx = -1;
//								for(int kk=0;kk<spreadableTerrains.length;kk++) {
//									if(v == spreadableTerrains[kk]) matchIdx = kk;
//								}
//								if(matchIdx >= 0) {
//									int match = spreadableTerrains[matchIdx];
//									if(!map.keySet().contains(match)) map.put(match, 1);
//									else map.put(match, map.get(match) + 1);
//								}
//							}
//						}
//
//						int max = 0;
//						int argMax = 0;
//						for(int key : map.keySet()) {
//							if(map.get(key) > max) {
//								max = map.get(key);
//								argMax = key;
//							}
//						}
//						if(Math.random() < 1. * max / 4) {
//							tempNewData[x][y] = argMax;
//							changed++;
//						} else tempNewData[x][y] = 0xFFFFFFFF;
//					} else {
//						tempNewData[x][y] = newData[x][y];
//					}
//				}
//			}
//
//			newData = tempNewData;
//			System.out.println(changed);
//		}
//
//		if(outputFileName != "") FileOperator.writeImage(newData, outputFileName);
//		System.out.println("done");
//		
//		return newData;
//	}

}
