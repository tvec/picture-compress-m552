package tony.vec;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * 
 * @author tony
 * Performs Compressions and sets them up for display
 */

public class AsyncPicView extends AsyncTask<Boolean, Integer, Bitmap[]> {

	private static final String TAG = AsyncPicView.class.getSimpleName();

	private Activity mActivity;
	private Resources res;
	
	public ProgressDialog dialog;
	
	String originalPicLocation;
	Bitmap android, compress, orig;

	
	ImageView originalImage, androidImage, compressImage;
	
	//Text info about the picture
	TextView originalInfo, androidInfo, compressInfo;

	public AsyncPicView(Activity _activity, String selection, ViewFlipper flippy) {
		this.mActivity = _activity;
		res = mActivity.getResources();

		originalImage = (ImageView) mActivity.findViewById(R.id.originalImage);
		originalInfo = (TextView) mActivity.findViewById(R.id.originalInfo);

		androidImage = (ImageView) mActivity.findViewById(R.id.androidImage);
		androidInfo = (TextView) mActivity.findViewById(R.id.androidInfo);

		compressImage = (ImageView) mActivity.findViewById(R.id.compressImage);
		compressInfo = (TextView) mActivity.findViewById(R.id.compressInfo);

		originalPicLocation = selection;

		this.dialog = new ProgressDialog(mActivity);
		this.dialog.setCancelable(true);

	}

	@Override
	protected void onPreExecute() {
		try {
			this.dialog.setMessage("Loading...");
			this.dialog.show();
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
	}

	@Override
	protected Bitmap[] doInBackground(Boolean... params) {

		int l = params.length;
		Bitmap[] results = new Bitmap[l + 1];

		RenderPicture rp = new RenderPicture(originalPicLocation,
				mActivity.getBaseContext());

		PictureResults pic = rp.getPictureResults();

		int height = pic.getHeight();
		int width = pic.getWidth();
		results[0] = pic.getBitmap();
		Log.d(TAG, "Original picture height=" + height +" and width ="+ width);
		
		//Store Bitmap colors into array
		int[] pix = new int[height * width];
		results[0].getPixels(pix, 0, width, 0, 0, width, height);
		
		
		if (params[0]) {
			String colorChoice = SettingsActivity.getPicColor(mActivity);
			int matrixChoice = SettingsActivity.getQuantMatrixSelection(mActivity);
			if (colorChoice.equals("Grayscale")) {
				results[1]= grayBitmapCompression(pix,height,width,matrixChoice);
			} else if (colorChoice.equals("RGB")) {
				results[1] = rgbBitmapCompression(pix, height, width,matrixChoice); 						
			} else if (colorChoice.equals("YUV")) {
				results[1] = yuvBitmapCompression(pix,height,width,matrixChoice);
			}
		}

		// Android Compression
		if (params[1]) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			//Compress based off the value user has inputed
			//default 99 since 100 is no compression
			
			results[0].compress(CompressFormat.JPEG,
					SettingsActivity.getBitmapVal(mActivity), out);

			byte[] res = out.toByteArray();

			results[2] = android = BitmapFactory.decodeByteArray(res, 0,
					res.length);
		}
		return results;
	}

	// Results are of bitmaps
	@Override
	protected void onPostExecute(Bitmap[] result) {

		String s1;
		try {
			//Set original image
			if (result[0] == null) {
				Log.d(TAG, "You have a problem!!");
			} else {
				originalImage.setImageBitmap(result[0]);
			}
			//Set DCT/Quant image
			if (result[1] == null) {
				s1 = "No Picture";
				compressInfo.setText(s1);
			} else {
				s1 = res.getString(R.string.loss_param_val,
						SettingsActivity.getLossParam(mActivity));
				compressImage.setImageBitmap(result[1]);
				compressInfo.setText(s1);
			}
			//Set android compress image
			if (result[2] == null) {
				s1 = "No Picture";
				androidInfo.setText(s1);
			} else {
				s1 = res.getString(R.string.bitmap_curr_val,
						SettingsActivity.getBitmapVal(mActivity));
				androidImage.setImageBitmap(result[2]);
				androidInfo.setText(s1);
			}
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
	}
	/**
	 * 
	 * @param bmArray bitmap into int[] form
	 * @param h height of bitmap
	 * @param w width of bitmap
	 * @return grayscale image w/ lossy DCT compression
	 */
	public Bitmap grayBitmapCompression(int bmArray[],int h, int w, int matrixChoice){
		
		DCT mDCT = new DCT();
		Quantization quantRun = new Quantization(mActivity, matrixChoice);
		
		//Quantization and DCT at the same time
		int[] temp = quantRun.quantizer(mDCT.forwardDCT(bmArray, h, w));
		
		//inverse DCT and return image
		return mDCT.reverseDCT(temp, h, w);
	}

	/**
	 * 
	 * @param bmArray bitmap into int[] form
	 * @param h height of bitmap
	 * @param w width of bitmap
	 * @return rgb image w/ lossy DCT compression
	 */
	public Bitmap rgbBitmapCompression(int bmArray[],int h, int w, int matrixChoice){
		DCT mDCT = new DCT();
		Quantization quantRun = new Quantization(mActivity, matrixChoice);
		
		//Apply DCT onto each r,g,b channels
		double[][][] Y = mDCT.forwardRGBDCT(bmArray, h, w);

		//Apply quantization
		int[] tempR = quantRun.quantizer(Y[0]);
		int[] tempG = quantRun.quantizer(Y[1]);
		int[] tempB = quantRun.quantizer(Y[2]);

		//Apply Inverse DCT
		int[] R = mDCT.reverseDCT3D(tempR, h, w);
		int[] G = mDCT.reverseDCT3D(tempG, h, w);
		int[] B = mDCT.reverseDCT3D(tempB, h, w);

		int[] res = new int[h * w];
		//Combine the r,g,b channels into one
		// Put values into rgb and create bitmap
		for (int i = 0; i < tempR.length; i++) {
			res[i] = 0xff000000 | (R[i] << 16) | (G[i] << 8) | B[i];
		}
		return Bitmap.createBitmap(res, w, h,
				Bitmap.Config.ARGB_8888);
	}
	
	/**
	 * 
	 * @param bmArray bitmap into int[] form
	 * @param h height of bitmap
	 * @param w width of bitmap
	 * @return yuv image w/ lossy DCT compression
	 */
	public Bitmap yuvBitmapCompression(int bmArray[],int h, int w, int matrixChoice){
		DCT mDCT = new DCT();
		Quantization quantRun = new Quantization(mActivity, matrixChoice);
		
		//Apply DCT onto each y,u,v channels
		double[][][] YUV = mDCT.forwardYUVDCT(bmArray, h, w);
		
		//Apply quantization
		int[] tempY = quantRun.quantizer(YUV[0]);
		int[] tempU = quantRun.UVquantizer(YUV[1]);
		int[] tempV = quantRun.UVquantizer(YUV[2]);

		//Apply inverse DCT
		int[] Y = mDCT.reverseDCT3D(tempY, h, w);
		int[] U = mDCT.reverseDCT3D(tempU, h, w);
		int[] V = mDCT.reverseDCT3D(tempV, h, w);

		int[] res = new int[h * w];
		
		// Convert from yuv to rgb to bitmap
		for (int i = 0; i < tempY.length; i++) {
			int R = V[i] + Y[i]; // bitwise shifting
			int B = U[i] + Y[i];
			int G = (int) ((Y[i] - .299 * R - .114 * B) / (.587));

			res[i] = 0xff000000 | (R << 16) | (G << 8) | B;
		}
		return Bitmap.createBitmap(res, w, h,
				Bitmap.Config.ARGB_8888);
	}
}
