package vectormaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import util.FileOperator;
import util.Geometry;
import util.Point;

class RiverData {
	String name;
	List<Point> coords;

	int convertLatitude(double c) {
		return (int) (1000000 * c);
	}

	int convertLongitude(double c) {
		return (int) (1000000 * c);
	}

	RiverData(String name, List<Double> coords) {
		this.name = name;
		int n = coords.size()/2;
		this.coords = new ArrayList<Point>();
		for(int i=0;i<n;i++) {
			this.coords.add(new Point(convertLatitude(coords.get(i)), convertLongitude(coords.get(i + n))));
		}
	}

	void simplify(double epsilon) {
		coords = Geometry.simplify(coords, epsilon);
	}

	public String toString() {
		String result = name;
		for(int i=0;i<coords.size();i++) {
			Point coord = coords.get(i);
			result += ",  "+coord.toString();
		}
		return result;
	}
}

public class RiverProcessor {
	// https://upload.wikimedia.org/wikipedia/commons/e/e5/Global_soils_map_USDA.jpg
	public static final String RIVER_DATA_FILENAME = System.getProperty("user.dir")+"\\input\\river_data.csv";
	public static final String SIMPLIFIED_RIVER_DATA_FILENAME = System.getProperty("user.dir")+"\\output\\map\\simplified_river_data.csv";
	public static final String BASE_MAP_WITH_RIVERS_OUTPUT_FILENAME = System.getProperty("user.dir")+"\\output\\map\\rivers.png";
	
	


	public String filterRiverName(String s) {
		String s1 = s.replace("\"", "");
		String[] s2 = s1.split(" \\(");
		if(s2.length > 0) s1 = s2[0];
		return s1;
	}

	public void reformatRiverData() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(RIVER_DATA_FILENAME));
			BufferedWriter writer = new BufferedWriter(new FileWriter(SIMPLIFIED_RIVER_DATA_FILENAME));

			String line = reader.readLine(); // skip first line
			String aggLine = "";
			String cName = "";
			List<Double> coords = new ArrayList<Double>();
			int proc = 0;

			double minLat = Double.POSITIVE_INFINITY;
			double minLon = Double.POSITIVE_INFINITY;
			double maxLat = Double.NEGATIVE_INFINITY;
			double maxLon = Double.NEGATIVE_INFINITY;

			while ((line = reader.readLine()) != null) {
				String[] s1 = line.split("c\\(");
				String s1b = line;
				if(s1.length > 1) {
					String[] s2 = s1[0].split(",");
					cName = filterRiverName(s2[6]);
					s1b = s1[1];
				}

				if(s1b.length() > 0) {
					String[] s2 = s1b.split("\\)");
					if(s2.length != 1 || s1b.charAt(s1b.length()-1) == ')') {
						if(s2.length > 0) aggLine += s2[0];
						
						System.out.println(aggLine);
						
						String[] s3 = aggLine.split(", ");
						if(s3.length > 0 && aggLine.length() > 0) {
							for(int i=0;i<s3.length;i++) {
								//									System.out.println(s3[i]);
								double d = Double.parseDouble(s3[i]);
								coords.add(d);
								if(i < s3.length / 2) {
									minLat = Math.min(minLat, d);
									maxLat = Math.max(maxLat, d);
								} else {
									minLon = Math.min(minLon, d);
									maxLon = Math.max(maxLon, d);
								}
							}
							RiverData data = new RiverData(cName, coords);
//							data.simplify(1000000);
							if(proc>0) writer.write("\n");
							writer.write(data.toString());
							writer.flush();
							proc++;
							System.out.println("*"+proc);
							aggLine = "";
							coords = new ArrayList<Double>();
						}
					} else {
						aggLine += s2[0];
					}
				}
			}
			reader.close();

			System.out.println(minLat+" "+minLon+" "+maxLat+" "+maxLon);

			//            writer.write(dataToWrite);
			writer.close();
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
	}

	public void drawRivers() {
		int[][] data = FileOperator.readImage(ElevationMapCreator.BASE_MAP_FILENAME);
		int w = data.length;
		int h = data[0].length;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(SIMPLIFIED_RIVER_DATA_FILENAME));
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					image.setRGB(x, y, data[x][y]);
				}
			}

			Graphics2D g2d = image.createGraphics();
			g2d.setColor(Color.RED);

			String line;

			while ((line = reader.readLine()) != null) {
				String[] lineData = line.split(",  ");
				//				System.out.println(lineData.length);
				int lastX = -1;
				int lastY = -1;
				for(int i=1;i<lineData.length;i++) {
					String[] s1 = lineData[i].split("\\(");
					String[] s2 = s1[1].split("\\)");
					String[] s3 = s2[0].split(", ");
					int x = (int) (w * (Long.parseLong(s3[0]) + 180000000) / 360000000);
					int y = (int) (h * (Long.parseLong(s3[1]) + 90000000) / 180000000);

					if(lastX >= 0) {
						g2d.drawLine(lastX, h - lastY - 1, x, h - y - 1);
					}

					lastX = x;
					lastY = y;
				}
			}

			File ImageFile = new File(BASE_MAP_WITH_RIVERS_OUTPUT_FILENAME);
			ImageIO.write(image, "png", ImageFile);


			reader.close();
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}

		System.out.println("done");
	}

	public static void main(String[] args) {
		new RiverProcessor().reformatRiverData();
		new RiverProcessor().drawRivers();
	}
}
