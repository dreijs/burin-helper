package util;

public class Colors {

	public static int redVal(int argbValue) {
		// Extract Red component: Shift right by 16 bits and mask with 0xFF (255)
		return (argbValue >> 16) & 0xFF; 
	}
	
	public static int greenVal(int argbValue) {
		// Extract Green component: Shift right by 8 bits and mask with 0xFF
		return (argbValue >> 8) & 0xFF; 
	}
	
	public static int blueVal(int argbValue) {
		// Extract Blue component: Mask with 0xFF
		return argbValue & 0xFF; 
	}
	
	public static int alphaVal(int argbValue) {
		return (argbValue >> 24) & 0xFF; 
	}
	
	public static boolean isBlack(int argbValue) {
		return redVal(argbValue) == 0 && greenVal(argbValue) == 0 && blueVal(argbValue) == 0;
	}

	public static int[] intToRGBArray(int argbValue) {
		int[] rgbArray = new int[3];

		rgbArray[0] = redVal(argbValue); 
		rgbArray[1] = greenVal(argbValue); 
		rgbArray[2] = blueVal(argbValue); 

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
