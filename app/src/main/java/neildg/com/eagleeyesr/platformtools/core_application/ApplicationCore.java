/**
 * 
 */
package neildg.com.eagleeyesr.platformtools.core_application;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * A utility class that contains reference to the application activity, asset manager, context
 * and other needed application data required by other classes.
 * 
 * This serves as a bridge towards those needed modules.
 * @author NeilDG
 *
 */
public class ApplicationCore {

	private final static String TAG = "ApplicationCore";
	
	private static ApplicationCore sharedInstance = null;
	
	public static ApplicationCore getInstance() {
		if(sharedInstance == null) {
			Log.e(TAG, "You must call initialize before you can use Application Core!");
		}
		
		return sharedInstance;
	}
	
	private Activity mainActivity;
	private Context  appContext;
	private AssetManager assetManager;
	
	private ApplicationCore() {
		
	}
	
	public static void initialize(Activity activity) {
		sharedInstance = new ApplicationCore();
		sharedInstance.mainActivity = activity;
		sharedInstance.appContext = activity;
		sharedInstance.assetManager = activity.getAssets();
	}
	
	public Activity getMainActivity() {
		return this.mainActivity;
	}
	
	public Context getAppContext() {
		return this.appContext;
	}
	
	public AssetManager getAssetManager() {
		return this.assetManager;
	}
}
