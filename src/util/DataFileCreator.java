package util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DataFileCreator {
	
	private String INPUT_DIR = "C:/Users/Admin/Documents/Unreal Projects/VectorMapTest/Content/Data/";
	
	public String reformat(String s) {
		String result = "";
		for(int i=0;i<s.length();i++) {
			if(s.charAt(i) == ' ' || s.charAt(i) == '-' || s.charAt(i) == '/') {
				if(i > 0) result += "_";
			}
			else if(s.charAt(i) == ',' || s.charAt(i) == '\'') result += "";
			else result += Character.toUpperCase(s.charAt(i));
		}
		result = Normalizer.normalize(result, Normalizer.Form.NFD);
		result = result.replaceAll("\\p{M}", "");
		return result;
	}
	
	public int numSpaces(String s) {
		int cc = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c == ' ' || c == '\t') {
				cc++;
			}
	    }
		return cc;
	}
	
	public void createDataFile(String s1, String s2, String s3) {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(INPUT_DIR+s1+".txt"));
			String sCurrentLine;
			List<String> values = new ArrayList<String>(); 

			while ((sCurrentLine = br.readLine()) != null) {
				String[] split0 = sCurrentLine.split("<"+s2+">");
				if(split0.length > 1) {
					String[] split1 = split0[1].split("</"+s2+">");
					values.add(reformat(split1[0]));
				}
			}

			for(int i=0;i<values.size();i++) {
				System.out.println("static const int "+values.get(i)+";");
			}
			System.out.println();
			for(int i=0;i<values.size();i++) {
				System.out.println("const int "+s3+"::"+values.get(i)+" = "+i+";");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createDataFiles() {
		createDataFile("TerrainData", "name", "UTerrain");
	}
	
	public static void main(String[] a) {
		new DataFileCreator().createDataFiles();
	}
}
