package tony.vec;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;


/**
 * @author tony
 * RenderPicture to resize a bitmap to about 200x200
 * Adjusted to be be divisible by 8 for Matrices
 * Also, includes methods for DCT/Quantization
 */
public class RenderPicture {

	final String TAG = RenderPicture.class.getSimpleName();
	
	Bitmap gray;
	
	//holds all info of pic selection
	PictureResults PR;

	/**
	 * 
	 * @param bmPath
	 *            file location of image
	 * @param context
	 */
	public RenderPicture(String bmPath, Context context) {

		PR = new PictureResults(bmPath);
		Bitmap b = decodeSampledBitmapFromFile(bmPath);
		String picType = SettingsActivity.getPicColor(context);
		if (picType.equals("YUV")) {
			PR.setBitmap(b);
			PR.setHeight(b.getHeight());
			PR.setWidth(b.getWidth());
		} else if (picType.equals("RGB")) {
			PR.setBitmap(b);
			PR.setHeight(b.getHeight());
			PR.setWidth(b.getWidth());

		} else {
			setGrayscale(b);
		}

	}

	// http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	// From Android Dev Site; handling bitmaps
	// Hard-coded image to 200 x 200; then adjust image to be divisible by 8
	public static Bitmap decodeSampledBitmapFromFile(String pathName) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		// Calculate inSampleSize

		options.inSampleSize = calculateInSampleSize(options, 200, 200);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		
		Bitmap b = BitmapFactory.decodeFile(pathName, options);

		Bitmap scaledBitmap = Bitmap.createScaledBitmap(b,
				(int) Math.floor(b.getWidth() / 8) * 8,
				(int) Math.floor(b.getHeight() / 8) * 8, true);
		b.recycle();
		b = scaledBitmap;

		System.gc();
		return b;
	}

	// Creating grayscale details from:
	// http://stackoverflow.com/questions/3373860/convert-a-bitmap-to-grayscale-in-android
	private void setGrayscale(Bitmap scaled) {
		int h = scaled.getHeight();
		int w = scaled.getWidth();
		PR.setHeight(h);
		PR.setWidth(w);

		gray = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(gray);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);

		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);

		paint.setColorFilter(f);
		c.drawBitmap(scaled, 0, 0, paint);
		// c.

		scaled.recycle();
		PR.setBitmap(gray);
	}

	public PictureResults getPictureResults() {
		return PR;
	}


	
}
