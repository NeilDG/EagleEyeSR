/**
 * 
 */
package neildg.com.megatronsr.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

import neildg.com.megatronsr.BuildConfig;
import neildg.com.megatronsr.constants.BuildMode;

/**
 * Takes care of showing the progress dialog to the user
 * @author NeilDG
 *
 */
public class ProgressDialogHandler {
	public final static String TAG = "SR_ProgressHandler";
	
	private static ProgressDialogHandler sharedInstance = null;
	public static ProgressDialogHandler getInstance() {
		if(sharedInstance == null) {
			Log.e(TAG, "Progress dialog not yet initialized!");
		}
		
		return sharedInstance;
	}
	
	private ProgressDialog progressDialog;
	private Activity activity;
	
	public ProgressDialogHandler(Activity activity) {
		this.activity = activity;
	}
	
	public static void initialize(Activity activity) {
		sharedInstance = new ProgressDialogHandler(activity);
	}
	
	public static void destroy() {
		if(sharedInstance.progressDialog != null) {
			sharedInstance.progressDialog.dismiss();
			sharedInstance.progressDialog = null;
		}
		sharedInstance = null;
	}
	
	public void showDialog(final String title, final String message) {
		if(BuildMode.DEVELOPMENT_BUILD) {
			this.activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					sharedInstance.hideDialog();
					sharedInstance.progressDialog = ProgressDialog.show(sharedInstance.activity, title, message);
					sharedInstance.progressDialog.setCancelable(false);
				}
			});
		}
	}

	/*
	 * Use this function for release-type user dialogs
	 */
	public void showUserDialog(final String title, final String message) {
		this.activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				sharedInstance.hideDialog();
				sharedInstance.progressDialog = ProgressDialog.show(sharedInstance.activity, title, message);
				sharedInstance.progressDialog.setCancelable(false);
			}
		});
	}

	/*
	 * For release-build user dialogs
	 */
	public void hideUserDialog() {
		if (this.progressDialog != null) {
			this.progressDialog.dismiss();
		}
	}
	
	public void hideDialog() {
		if(BuildMode.DEVELOPMENT_BUILD) {
			if (this.progressDialog != null) {
				this.progressDialog.dismiss();
			}
		}
	}
}
