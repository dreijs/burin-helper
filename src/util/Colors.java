package util;

public class Colors {

	public static int blueVal(int rgbValue) {
		return rgbValue & 0xFF;
	}

	public static int[] intToRGBArray(int argbValue) {
		int[] rgbArray = new int[3];

		// Extract Red component: Shift right by 16 bits and mask with 0xFF (255)
		rgbArray[0] = (argbValue >> 16) & 0xFF; 

		// Extract Green component: Shift right by 8 bits and mask with 0xFF
		rgbArray[1] = (argbValue >> 8) & 0xFF; 

		// Extract Blue component: Mask with 0xFF
		rgbArray[2] = argbValue & 0xFF; 

		return rgbArray;
	}
	
	public static int[] intToARGBArray(int argbValue) {
		int[] rgbArray = new int[4];

		rgbArray[0] = (argbValue >> 24) & 0xFF; 
		rgbArray[1] = (argbValue >> 16) & 0xFF; 
		rgbArray[2] = (argbValue >> 8) & 0xFF; 
		rgbArray[3] = argbValue & 0xFF; 

		return rgbArray;
	}
	
	public static int rgbToInt(int r, int g, int b) {
	    // Assuming 100% opacity (Alpha = 255)
	    return (255 << 24) | (r << 16) | (g << 8) | b;
	}
}
