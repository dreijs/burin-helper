package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapOperator {

	public static final int[][] VON_NEUMANN_NBS = {{0,1}, {1,0}, {0,-1}, {-1,0}};

	public static int colDist(int col1, int col2) {
		int col1r = (col1 >> 16) & 0xFF; 
		int col1g = (col1 >> 8) & 0xFF; 
		int col1b = col1 & 0xFF; 

		int col2r = (col2 >> 16) & 0xFF; 
		int col2g = (col2 >> 8) & 0xFF; 
		int col2b = col2 & 0xFF; 

		return (col1r - col2r) * (col1r - col2r) + (col1g - col2g) * (col1g - col2g) + (col1b - col2b) * (col1b - col2b);
	}

	static int similarity(int a, int b) {
		int result = 0;
		// water status is very important
		if(a % 16 == 0 && b % 16 == 0) return 32;
		else if(a % 16 > 0 && b % 16 > 0) result = 4 * Math.max(0, 8 - Math.abs(a-b));
		else return 1;

		int n = (int) Math.ceil(Math.log(Math.max(a, b))/Math.log(16));
		for(int i=1;i<n;i++) {
			int z = (int) Math.pow(16,i);
			if((a / z) % 16 == (b / z) % 16) result += 16;
		}

		return result;
	}

	public static RegionResult cleanRegionIndices(RegionResult oldResult) {
		int numRegions = oldResult.numRegions;

		int[][] regionData = oldResult.regions;
		int[][] result = new int[regionData.length][regionData[0].length];

		int[] idxList = new int[numRegions];
		for(int i=0;i<numRegions;i++) idxList[i] = -1;
		int cRegionIdx = 0;

		for (int x = 0; x < regionData.length; x++) {
			for (int y = 0; y < regionData[x].length; y++) {
				if(idxList[regionData[x][y]] == -1) {
					idxList[regionData[x][y]] = cRegionIdx;
					cRegionIdx++;
				}
			}
		}

		int[] type = new int[cRegionIdx];
		for(int i=0;i<numRegions;i++) {
			if(idxList[i] != -1) type[idxList[i]] = oldResult.type[i];
		}

		for (int x = 0; x < regionData.length; x++) {
			for (int y = 0; y < regionData[x].length; y++) {
				result[x][y] = idxList[regionData[x][y]];
			}
		}

		return new RegionResult(result, type, cRegionIdx);
	}

	public static double pixelSize(int x, int y, int w, int h) {
		PointInt p1 = new PointInt(x,y);
		PointInt p2 = new PointInt(x+1,y);
		PointInt p3 = new PointInt(x+1,y+1);
		PointInt p4 = new PointInt(x,y+1);
		List<Point> coords = new ArrayList<Point>();
		coords.add(p1); coords.add(p2); coords.add(p3); coords.add(p4);
		return GeometryUtils.calculatePolygonAreaGlobe(coords, w, h);
	}


	public static boolean isEssentialForCohesion(int[][] data, int x, int y) {
//		System.out.println("start");
		int w = data.length;
		int h = data[0].length;
		List<Integer> visited = new ArrayList<Integer>();
		List<Integer> newlyVisited = new ArrayList<Integer>();
		List<Integer> yetToVisit = new ArrayList<Integer>();
		int r = data[x][y];

		if(x > 0 && data[x-1][y] == r) yetToVisit.add((x-1) * w + y);
		if(x < w - 1 && data[x+1][y] == r) yetToVisit.add((x+1) * w + y);
		if(y > 0 && data[x][y-1] == r) yetToVisit.add(x * w + (y-1));
		if(y < h - 1 && data[x][y+1] == r) yetToVisit.add(x * w + (y+1));

		if(yetToVisit.size() < 2) return false;

		newlyVisited.add(yetToVisit.get(0));
		yetToVisit.remove(0);
		visited.add(x * w + y);

		while(newlyVisited.size() > 0) {
//			System.out.println(newlyVisited.size());
			List<Integer> additions = new ArrayList<Integer>();
			for(int j=newlyVisited.size()-1;j>=0;j--) {
				int xx = newlyVisited.get(j) / w;
				int yy = newlyVisited.get(j) % w;
				for(int k=0;k<VON_NEUMANN_NBS.length;k++) {
					int xxx = xx + VON_NEUMANN_NBS[k][0];
					int yyy = yy + VON_NEUMANN_NBS[k][1];

					if(xxx >= 0 && xxx < w && yyy >= 0 && yyy < h) {
						int rr = xxx * w + yyy;
						if(data[xxx][yyy] == r && !visited.contains(rr) && !additions.contains(rr)) additions.add(rr);
						if(yetToVisit.contains(rr)) yetToVisit.remove(yetToVisit.indexOf(rr));
						if(yetToVisit.size() == 0) return false;
					}
				}

				visited.add(newlyVisited.get(j));
			}

			newlyVisited = additions;
		}

		return true;
	}

	public static RegionResult removeSmallRegionsInRegionMap(RegionResult oldResult, int[][] terrainData, double threshold, int scale) {
		int numRegions = oldResult.numRegions;
		int[][] regionData = oldResult.regions;
		int[][] result = new int[regionData.length][regionData[0].length];

		int w = regionData.length;
		int h = regionData[0].length;

		double[] sizes = new double[numRegions];
		int[] terrains = new int[numRegions];

		for (int x = 0; x < regionData.length; x++) {
			for (int y = 0; y < regionData[x].length; y++) {
				sizes[regionData[x][y]] += pixelSize(x, y, w, h);

				result[x][y] = regionData[x][y];
				terrains[regionData[x][y]] = terrainData[x][y];
			}
		}

		int[][] smalls = new int[regionData.length][regionData[0].length];

		for (int x = 0; x < regionData.length; x++) {
			for (int y = 0; y < regionData[x].length; y++) {
				smalls[x][y] = 0xFFFFFFFF;
				if(sizes[regionData[x][y]] < threshold) smalls[x][y] = 0xFFFF0000;
				else if(terrains[regionData[x][y]] % 16 == 0) smalls[x][y] = 0xFF0000FF;
			}
		}

		FileOperator.writeImage(smalls, System.getProperty("user.dir")+"\\output\\map\\polygons\\small_regions.png");


		int nChanged = 1;

		int s = 4 * scale;
		int t = 64 * s * scale; // quadratic?
		int d = 3;
		
		boolean[] keep = new boolean[sizes.length];
		boolean[] remove = new boolean[sizes.length];

		while(nChanged > 0 || t > 0) {
			nChanged = 0;

			if(t > 0) {
				keep = new boolean[sizes.length];
				remove = new boolean[sizes.length];
			}

			int[][] newResult = new int[regionData.length][regionData[0].length];
			for (int x = 0; x < result.length; x++) {
				for (int y = 0; y < result[x].length; y++) {
					newResult[x][y] = result[x][y];
				}
			}

			for (int x = 0; x < result.length; x++) {
				for (int y = 0; y < result[x].length; y++) {
					if(remove[result[x][y]] || sizes[result[x][y]] < threshold && !keep[result[x][y]]) {
						if(!isEssentialForCohesion(newResult, x, y)) {
							Map<Integer,Integer> scores = new HashMap<Integer,Integer>();
							int r1 = result[x][y];
							for(int n=0;n<VON_NEUMANN_NBS.length;n++) {
								int xx = x + VON_NEUMANN_NBS[n][0];
								int yy = y + VON_NEUMANN_NBS[n][1];
								if(xx >= 0 && xx < result.length && yy >= 0 && yy < result[0].length) {
									int r2 = result[xx][yy];
									if(!remove[r2] && sizes[r2] >= sizes[r1]) {
										if(scores.containsKey(r2)) scores.put(r2, scores.get(r2) + s * similarity(terrains[r1], terrains[r2]));
										scores.put(r2, similarity(terrains[r1],terrains[r2]));
									}
								}
							}

							for(int dx=-d;dx<=d;dx++) {
								for(int dy=-d;dy<=d;dy++) {
									int xx = x + dx;
									int yy = y + dy;
									if(xx >= 0 && xx < result.length && yy >= 0 && yy < result[0].length) {
										for(int key : scores.keySet()) {
											int r2 = result[xx][yy];
											scores.put(key, scores.get(key) + s * similarity(terrains[r1], terrains[r2]) * similarity(terrains[key], terrains[r2]) / 32);
										}
									}
								}
							}

							int max = -1;
							int argmax = -1;
							for(int key : scores.keySet()) {
								if(scores.get(key) > max) {
									max = scores.get(key);
									argmax = key;
								}
							}

							if(max >= t) {
								newResult[x][y] = argmax;
								keep[argmax] = true;
								remove[result[x][y]] = true;
								sizes[argmax] += pixelSize(x, y, w, h);
								sizes[result[x][y]] -= pixelSize(x, y, w, h);
								nChanged++;
							}
						}
					}
				}
			}

			System.out.println("n. changed "+nChanged+", t = "+t);
			result = newResult;

			t = Math.max(0, t-1);
		}

		return cleanRegionIndices(new RegionResult(result, oldResult.type, numRegions));
	}

	//	public static int[][] removeOrExpandLonePixels(int[][] baseData) {
	//		int[][] newData = new int[baseData.length][baseData[0].length];
	//
	//		for (int x = 0; x < baseData.length; x++) {
	//			for (int y = 0; y < baseData[x].length; y++) {
	//				newData[x][y] = baseData[x][y];
	//			}
	//		}
	//
	//		int[][] nbs = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
	//
	//		for (int x = 0; x < baseData.length; x++) {
	//			for (int y = 0; y < baseData[x].length; y++) {
	//				if(x > 0 && x < baseData.length-1 && y > 0 && y < baseData[0].length-1) {
	//					int v = baseData[x][y];
	//					boolean changed = false;
	//					if(v != baseData[x-1][y] && v != baseData[x+1][y] && v != baseData[x][y+1] && v != baseData[x][y-1]) {
	//						if(v == baseData[x+1][y+1]) {
	//							changed = true;
	//							if(Math.random() < 0.5) {
	//								newData[x][y+1] = v;
	//							} else {
	//								newData[x+1][y] = v;
	//							}
	//						}
	//
	//						if(v == baseData[x-1][y+1]) {
	//							changed = true;
	//							if(Math.random() < 0.5) {
	//								newData[x][y+1] = v;
	//							} else {
	//								newData[x-1][y] = v;
	//							}
	//						}
	//
	//						if(v == baseData[x+1][y-1]) {
	//							changed = true;
	//							if(Math.random() < 0.5) {
	//								newData[x][y-1] = v;
	//							} else {
	//								newData[x+1][y] = v;
	//							}
	//						}
	//
	//						if(v == baseData[x-1][y-1]) {
	//							changed = true;
	//							if(Math.random() < 0.5) {
	//								newData[x][y-1] = v;
	//							} else {
	//								newData[x-1][y] = v;
	//							}
	//						}
	//					}
	//
	//					if(!changed) {
	//						Map<Integer,Integer> map = new HashMap<Integer,Integer>();
	//						for(int n=0;n<nbs.length;n++) {
	//							int xx = Math.floorMod(x + nbs[n][0], baseData.length);
	//							int yy = y + nbs[n][1];
	//							if(yy >= 0 && yy < baseData[0].length) {
	//								if(!map.keySet().contains(baseData[xx][yy])) map.put(baseData[xx][yy], 1);
	//								else map.put(baseData[xx][yy], map.get(baseData[xx][yy]) + 1);
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
	//
	//						if(max >= 3) newData[x][y] = argMax;
	//					}
	//				}
	//			}
	//		}
	//
	//		return newData;
	//	}

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
