package tony.vec;

import java.io.File;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ViewFlipper;

/**
 * 
 * @author tony
 * Image Compression Comparison App
 * Compares compression of Discrete Cosine Transform and Quantization
 * against Android's built-in compression 
 */
public class PictureCompressActivity extends Activity {

	private static final String TAG = PictureCompressActivity.class
			.getSimpleName();

	private static final int SELECT_PICTURE = 1;

	AsyncPicView mAsyncPic;

	/*
	 * androidImage: where results of originalImage.compress is displayed
	 * compressImage: where results of DCT and quantization is displayed
	 */

	ImageView originalImage, androidImage, compressImage;
	// TextView originalTitle, androidTitle;

	ViewFlipper flipper;

	private String selectedImagePath;

	static boolean mExternalStorageAvailable = false;
	static boolean mExternalStorageWriteable = false;
	private File baseDir;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		originalImage = (ImageView) findViewById(R.id.originalImage);
		androidImage = (ImageView) findViewById(R.id.androidImage);
		compressImage = (ImageView) findViewById(R.id.compressImage);

	}

	public void clearImages() {
		originalImage.setImageResource(0);
		androidImage.setImageResource(0);
		compressImage.setImageResource(0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	public void gallerySelect() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_PICTURE);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();

				// MEDIA GALLERY
				selectedImagePath = getPath(selectedImageUri);

				mAsyncPic = new AsyncPicView(this, selectedImagePath,
						startFlipper(this, false));
				mAsyncPic.execute(SettingsActivity.getCompType(this));

			}
		}
	}

	// Add listener for viewflipper
	public ViewFlipper startFlipper(Context context, boolean firstStart) {

		setContentView(R.layout.main);
		flipper = new ViewFlipper(context);
		flipper = (ViewFlipper) findViewById(R.id.flipper);

		flipper.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewFlipper flippy = (ViewFlipper) v;
				flippy.showNext();

			}
		});
		return flipper;

	}

	// UPDATED!
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mInflater = new MenuInflater(this);
		mInflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			Log.d(TAG, "Settings Menu");
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.select_picture:
			gallerySelect();
			return true;
			/*
			 * TODO add camera case R.id.camera: Log.d(TAG,
			 * "Currently unavailable"); return true;
			 */
		}

		return super.onOptionsItemSelected(item);
	}

	public File checkSDCard(Context context) {

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {

			mExternalStorageAvailable = mExternalStorageWriteable = true;
			Log.d(TAG, "sd available");
			return baseDir = context.getExternalFilesDir(null);
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {

			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
			Log.d(TAG, "sd unavailable");

			return baseDir = context.getDir(null, 0);
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
			Log.d(TAG, "sd unavailable");

			return baseDir = context.getDir(null, 0);
		}
	}

}