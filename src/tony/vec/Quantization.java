package tony.vec;

import android.content.Context;
import android.util.Log;
/**
 * 
 * @author tony
 * Apply Quantization on matrices after DCT
 * Quantization is a form of lossy compression
 */
public class Quantization {

	@SuppressWarnings("unused")
	private static final String TAG = Quantization.class.getSimpleName();

	public int[][] quantMatrix;
	Context context;
	
	//Quantization Matrix for U and V matrices
	public final int[][] UVMatrix = { { 17, 18, 24, 47, 99, 99, 99, 99 },
			{ 18, 21, 26, 66, 99, 99, 99, 99 },
			{ 24, 26, 56, 99, 99, 99, 99, 99 },
			{ 47, 66, 99, 99, 99, 99, 99, 99 },
			{ 99, 99, 99, 99, 99, 99, 99, 99 },
			{ 99, 99, 99, 99, 99, 99, 99, 99 },
			{ 99, 99, 99, 99, 99, 99, 99, 99 },
			{ 99, 99, 99, 99, 99, 99, 99, 99 } };
	public static final int MATRIXSIZE = 8;

	int lossParam;

	public Quantization(Context context, int version) {
		this.context = context;
		lossParam = SettingsActivity.getLossParam(context);
		setQuantMatrix(lossParam, version);
	}

	
	/**
	 * Create the quantizing matrix
	 * @param version Type of Quantization Matrix.
	 * 1 is Quantization Matrix 
	 * 2 is the Jpeg Standard Quantization Matrix
	 * @param p loss parameter for quantization matrix
	 *  
	**/
	public void setQuantMatrix(int p, int version) {
		quantMatrix = new int[MATRIXSIZE][MATRIXSIZE];
		
		//Linear Quantization 
		if (version == 0) {
			Log.d(TAG, "Matrix= Linear Quantization");
			for (int i = 0; i < MATRIXSIZE; i++)
				for (int j = 0; j < MATRIXSIZE; j++) {
					quantMatrix[i][j] = MATRIXSIZE * p * (i + j + 1);
				}
		} else if (version == 1) {
			//JPEG Standard
			Log.d(TAG, "Matrix= JPEG Standard");
			int[][] temp = { { 16, 11, 10, 16, 24, 40, 51, 61 },
					{ 12, 12, 14, 19, 26, 58, 60, 55 },
					{ 14, 13, 16, 24, 40, 57, 69, 56 },
					{ 14, 17, 22, 29, 51, 87, 80, 62 },
					{ 18, 22, 37, 56, 68, 109, 103, 77 },
					{ 24, 35, 55, 64, 81, 104, 113, 92 },
					{ 49, 64, 78, 87, 103, 121, 120, 101 },
					{ 72, 92, 95, 98, 112, 100, 103, 99 } };
			if (p != 1) {
				for (int i = 0; i < MATRIXSIZE; i++)
					for (int j = 0; j < MATRIXSIZE; j++) {
						quantMatrix[i][j] = temp[i][j] * p;
					}
			}
			quantMatrix = temp;
		}
	}

	public int[][] getQuantMatrix() {
		return quantMatrix;
	}
	
	/*
	 * Combined together quantization and de-quantization steps
	 * Actual use would split these steps
	 * Main quantizer method	
	 */
	public int[] quantizer(double Y[][]) {
		int height = Y.length;
		int width = Y[0].length;
		
		int[] quantized = new int[width * height];
		int[] results = new int[width* height];

		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				int index = h * width + w;
				int i = h % MATRIXSIZE;
				int j = w % MATRIXSIZE;
				//Quantizing each indices
				quantized[index] = Math.round((float) Y[h][w]/ quantMatrix[i][j]);
				
				//De-Quantizing
				int restore = quantized[index] * quantMatrix[i][j];
				results[index] = restore;
			}
		}

		return results;
	}
	
	
	/*
	 * Combined together quantization and de-quantization steps
	 * Actual use would split these steps
	 * Quantizing Matrix for a UV Matrices
	 * U and V have less important roles in human visuals
	 * So more aggressive quantization is applied	
	 */
	public int[] UVquantizer(double Y[][]) {
		int height = Y.length;
		int width = Y[0].length;
		
		int[] quantized = new int[width * height];
		int[] results = new int[width* height];

		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				int index = h * width + w;
				int i = h % MATRIXSIZE;
				int j = w % MATRIXSIZE;
				//Quantizing each indices
				quantized[index] = Math.round((float) Y[h][w]/ UVMatrix[i][j]);
				
				//De-Quantizing Step
				int restore = quantized[index] * UVMatrix[i][j];
				results[index] = restore;
			}
		}

		return results;
	}

}
