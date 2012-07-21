package tony.vec;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * 
 * @author tony
 * DCT is for 2-D Discrete Cosine Transform
 * Instead of creating a large DCT Matrix the size of the image
 * "break" up the image into a bunch of 8x8 matrices
 * Then matrix multiply the 8x8 matrix of the image with C or CTrans
 * Go through each 8x8 block until all have been "dct'd"
 */
public class DCT {
	private static final String TAG = DCT.class.getSimpleName();
	
	private double[][] C;			// Discrete Cosine Transform
	private double[][] CTrans;		// Inverse Discrete Cosine Transform 
	public final int mSize = 8;	//Matrix size

	// Build the 8x8 DCT Matrix
	public DCT() {

		C = new double[mSize][mSize];
		CTrans = new double[mSize][mSize];

		double vector = Math.sqrt(2.0 / mSize);
		for (int i = 0; i < mSize; i++) {
			for (int j = 0; j < mSize; j++) {
				if (i == 0) {
					C[i][j] = vector * (1 / Math.sqrt(2));
					CTrans[j][i] = vector * (1 / Math.sqrt(2));
				} else {
					double val = (i * (j + .5) * Math.PI) / (mSize);
					C[i][j] = vector * Math.cos(val);
					CTrans[j][i] = vector * Math.cos(val);
				}
			}
		}
	}

	/**
	 * Used for grayscale images
	 * Regular Matrix Multiplication O(n^3)
	 * Y = C*X*C_Transpose
	 * @params X int array from a bitmap
	 */ 
	public double[][] forwardDCT(int X[], int height, int width) {
		
		double[][] Y = new double[height][width];
		//Temp results for (X*C_Transpose)
		double[][] dct1 = new double[height][width];
		
		//dct1 = X * C_Transpose
		for (int i = 0; i < height; i++) { 
			for (int j = 0; j < width; j++) { 
				for (int k = 0; k < 8; k++) { 
					int kVal = (j / 8) * 8;

					int index = (i * width) + kVal + k;

					dct1[i][j] += X[index] * CTrans[k][(j % 8)];
				}

			}
		}
		

		
		// Results = C * dct1
		for (int i = 0; i < height; i++) { // aRow
			for (int j = 0; j < width; j++) { // bColumn
				for (int k = 0; k < 8; k++) { // aColumn
					int kVal = (i / 8) * 8;
					Y[i][j] += C[(i % 8)][k] * dct1[kVal + k][j];
				}

			}
		}
		
		return Y;
	}

	/** 
	 * Inverse function to  get X back 
	 * then convert to image
	 * X = C_Transpose*Y*C
	 *  @params X int array from a bitmap
	 */ 
	public Bitmap reverseDCT(int Y[], int height, int width) {
		
		int[] X = new int[height * width];
		
		// temp = Y * C
		double[] temp = new double[height * width];
		for (int i = 0; i < height; i++) { 
			for (int j = 0; j < width; j++) { 
				int tempIndex = (i * width) + j;
				for (int k = 0; k < 8; k++) { 
					int kVal = (j / 8) * 8;
					int index = (i * width) + kVal + k;

					temp[tempIndex] += Y[index] * C[k][(j % 8)];

				}

			}
		}

		// X = C_Transpose * temp
		for (int i = 0; i < height; i++) { // aRow
			for (int j = 0; j < width; j++) { // bColumn
				for (int k = 0; k < 8; k++) { // aColumn
					int kVal = (i / 8) * 8;
					int index = (i * width) + j;
					int tempIndex = ((kVal + k) * width) + j;
					X[index] += CTrans[(i % 8)][k] * temp[tempIndex];
				}

			}
		}

		Bitmap results = Bitmap.createBitmap(X, width, height,
				Bitmap.Config.ARGB_8888);

		return results;
	}
/**
 * 
 * @param X int array from a bitmap
 * @param height of bitmap
 * @param width of bitmap
 * @return rgb array 
 */
	public double[][][] forwardRGBDCT(int X[], int height, int width) {

		int xRows = height;
		int xCols = width;

		// dct1 = X * C_Transpose
		double[][][] dct1 = new double[3][xRows][xCols];
		for (int i = 0; i < xRows; i++) { 
			for (int j = 0; j < xCols; j++) { 
				for (int k = 0; k < 8; k++) { 
					int kVal = (j / 8) * 8;

					int index = (i * xCols) + kVal + k;

					int R = (X[index] >> 16) & 0xff; 
					int G = (X[index] >> 8) & 0xff;
					int B = X[index] & 0xff;
					dct1[0][i][j] += R * CTrans[k][(j % 8)];
					dct1[1][i][j] += G * CTrans[k][(j % 8)];
					dct1[2][i][j] += B * CTrans[k][(j % 8)];

				}

			}
		}
		

		double[][][] Y = new double[3][xRows][xCols];
		// Results = C * dct1
		for (int i = 0; i < xRows; i++) { // aRow
			for (int j = 0; j < xCols; j++) { // bColumn
				for (int k = 0; k < 8; k++) { // aColumn
					int kVal = (i / 8) * 8;
					Y[0][i][j] += C[(i % 8)][k] * dct1[0][kVal + k][j];
					Y[1][i][j] += C[(i % 8)][k] * dct1[1][kVal + k][j];
					Y[2][i][j] += C[(i % 8)][k] * dct1[2][kVal + k][j];
				}

			}
		}

		return Y;

	}

	/**
 * Inverse DCT functions for 3D arrays i.e. RGB and YUV
 * @param Y
 * @param height
 * @param width
 * @return array
 */
	public int[] reverseDCT3D(int Y[], int height, int width) {

		int[] X = new int[height * width];
		
		// temp = Y * C
		double[] temp = new double[height * width];
		for (int i = 0; i < height; i++) { 
			for (int j = 0; j < width; j++) { 
				int tempIndex = (i * width) + j;
				for (int k = 0; k < 8; k++) { 
					int kVal = (j / 8) * 8;
					int index = (i * width) + kVal + k;
					temp[tempIndex] += Y[index] * C[k][(j % 8)];

				}

			}
		}



		// X = C_Transpose * temp
		for (int i = 0; i < height; i++) { // aRow
			for (int j = 0; j < width; j++) { // bColumn
				for (int k = 0; k < 8; k++) { // aColumn
					int kVal = (i / 8) * 8;
					int index = (i * width) + j;
					int tempIndex = ((kVal + k) * width) + j;
					X[index] += CTrans[(i % 8)][k] * temp[tempIndex];
				}

			}
		}

		return X;
	}

	/**
	 * y =luminance, u and v = color difference/chrominance
	 * @param X int array from a bitmap
	 * @param height
	 * @param width
	 * @return
	 */
	public double[][][] forwardYUVDCT(int X[], int height, int width) {

		int xRows = height;
		int xCols = width;

		// dct1 = X * C_Transpose
		double[][][] dct1 = new double[3][xRows][xCols];
		for (int i = 0; i < xRows; i++) { // aRow
			for (int j = 0; j < xCols; j++) { // bColumn
				for (int k = 0; k < 8; k++) { // aColumn
					int kVal = (j / 8) * 8;

					int index = (i * xCols) + kVal + k;

					int R = (X[index] >> 16) & 0xff; // bitwise shifting
					int G = (X[index] >> 8) & 0xff;
					int B = X[index] & 0xff;

					int luminance = (int) (.299 * R + .587 * G + .114 * B);
					int Y = luminance;
					int U = B - luminance;
					int V = R - luminance;

					dct1[0][i][j] += Y * CTrans[k][(j % 8)];
					dct1[1][i][j] += U * CTrans[k][(j % 8)];
					dct1[2][i][j] += V * CTrans[k][(j % 8)];

				}

			}
		}
		double[][][] yuv = new double[3][xRows][xCols];
		// Results = C * dct1
		for (int i = 0; i < xRows; i++) { // aRow
			for (int j = 0; j < xCols; j++) { // bColumn
				for (int k = 0; k < 8; k++) { // aColumn
					int kVal = (i / 8) * 8;
					yuv[0][i][j] += C[(i % 8)][k] * dct1[0][kVal + k][j];
					yuv[1][i][j] += C[(i % 8)][k] * dct1[1][kVal + k][j];
					yuv[2][i][j] += C[(i % 8)][k] * dct1[2][kVal + k][j];
				}

			}
		}

		return yuv;

	}
	
}
