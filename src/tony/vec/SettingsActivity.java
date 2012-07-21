package tony.vec;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;
/**
 * 
 * @author tony
 *
 */
public class SettingsActivity extends PreferenceActivity {

	public final String TAG = SettingsActivity.class.getSimpleName();

	//Loss Value used during Quantization
	private final static String quant_loss_val = "loss_param";
	
	//Value of Compress value used by Bitmap.compress
	private final static String bitmap_val = "bitmap_val";
	
	//Used for selection of Matrix type
	private final static String quant_matrix = "quant_matrix";
		
	//Compression types that were checked
	private static Boolean[] checks = new Boolean[2];

	Context context;
	static int count;

	//Set Up Edit Text Values and set up listeners for values being changed
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference);
		context = this;
		
		
		final EditTextPreference bitmap_text = (EditTextPreference) findPreference(bitmap_val);
		String bmValPost = getString(R.string.bitmap_curr_val,
				getBitmapVal(this));
		bitmap_text.setSummary(bmValPost);
		
		
		//Save bitmap compression value
		bitmap_text.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				//Save new Value
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(bitmap_val, newValue.toString());
				editor.commit();

				//Change value on screen
				String bmValPost = getString(R.string.bitmap_curr_val,
						Integer.parseInt(newValue.toString()));
				preference.setSummary(bmValPost);

				return true;
			}
		});

		
		final ListPreference lp = (ListPreference) findPreference(quant_loss_val);
		String lParamPost = getString(R.string.loss_param_val,
				getLossParam(this));

		lp.setSummary(lParamPost);
		//Save loss parameter for quantization
		lp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				//Save new Value
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(quant_loss_val, newValue.toString());
				editor.commit();
				
				//Change value on screen
				String lParamPost = getString(R.string.loss_param_val,
						Integer.parseInt(newValue.toString()));
				preference.setSummary(lParamPost);

				return true;
			}
		});


		final CheckBoxPreference dctPref = (CheckBoxPreference) getPreferenceManager()
				.findPreference("select_dct");
		dctPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if (newValue.toString().equals("true")) {
					checks[0] = true;
					
				} else {
					checks[0] = false;
				}

				printMessage();
				return true;

			}
		});

		final CheckBoxPreference androidPref = (CheckBoxPreference) getPreferenceManager()
				.findPreference("select_android");
		androidPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (newValue.toString().equals("true")) {
							checks[1] = true;
						} else {
							checks[1] = false;
						}

						printMessage();
						return true;

					}
				});
		
		//Color Selection Setup
		final ListPreference colorPref = (ListPreference) getPreferenceManager()
				.findPreference("colors");
		colorPref.setSummary("Currently:"+getPicColor(this));
		
		colorPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				colorPref.setSummary("Currently:"+newValue.toString());
				return true;
			}});
		
		//Quantization Matrix Selection Setup
	}

	//Go back to PictureCompressActivity
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public int boolCounter() {
		count = 0;
		for (boolean b : checks) {
			// if true: increment counter;
			if (b)
				count++;
		}
		System.out.println(count);
		return count;
	}

	public void printMessage() {

		if (boolCounter() > 1)
			Toast.makeText(
					getBaseContext(),
					"Selecting multiple compression types will take longer to generate",
					Toast.LENGTH_LONG).show();

	}

	@Override
	public void onStart() {
		getCompType(this);
		super.onStart();
	}

	/**
	 * 
	 * @param context
	 * @return check box selections
	 */
	public static Boolean[] getCompType(Context context) {
		count = 0;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		checks[0] = prefs.getBoolean("select_dct", false);
		if (checks[0])
			count++;
		checks[1] = prefs.getBoolean("select_android", false);
		if (checks[1])
			count++;

		return checks;
	}

	/**
	 * 
	 * @param context
	 * @return Loss Parameter
	 */
	public static int getLossParam(Context context) {
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String val = prefs.getString("loss_param", "1");

		int param = Integer.parseInt(val);
		return param;

	}

	/**
	 * 
	 * @param context
	 * @return Color types (Grayscale/RGB/YUV)
	 */
	public static String getPicColor(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getString("colors", "Grayscale");
	}

	/**
	 * 
	 * @param context
	 * @return bitmap compress value, for bitmap.compress 
	 */
	public static int getBitmapVal(Context context) {
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		String val = prefs.getString(bitmap_val, "99");

		int param = Integer.parseInt(val);
		return param;
	}

	/**
	 * 
	 * @param context
	 * @return quantization matrix type that was selected
	 */
	public static int getQuantMatrixSelection(Context context){
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		String val = prefs.getString(quant_matrix, "0");

		int param = Integer.parseInt(val);		
		return param;
		
	}
}