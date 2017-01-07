/**
 * 
 */
package neildg.com.eagleeyesr.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

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
	private ProcessingDialog processingDialog;
	private Activity activity;
	
	public ProgressDialogHandler(Activity activity) {
		this.activity = activity;
		this.processingDialog = new ProcessingDialog(this.activity);
	}
	
	public static void initialize(Activity activity) {
		sharedInstance = new ProgressDialogHandler(activity);
	}
	
	public static void destroy() {
		if(sharedInstance != null && sharedInstance.progressDialog != null) {
			sharedInstance.progressDialog.dismiss();
			sharedInstance.progressDialog = null;
		}

		if(sharedInstance != null && sharedInstance.processingDialog != null) {
			sharedInstance.processingDialog.dismiss();
			sharedInstance.processingDialog = null;
		}

		sharedInstance = null;
	}
	
	/*public void showDialog(final String title, final String message) {
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
	}*/

	/*
	 * Use this function for release-type user dialogs
	 */
	public void showUserDialog(final String title, final String message) {
		this.activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				//sharedInstance.hideDialog();
				sharedInstance.progressDialog = ProgressDialog.show(sharedInstance.activity, title, message);
				sharedInstance.progressDialog.setCancelable(false);
			}
		});
	}

	/*
	 * Shows a custom processing dialog created via layout res
	 */
	public void showProcessDialog(final String title, final String message, final float progress) {
		this.activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				//sharedInstance.hideDialog();
				sharedInstance.processingDialog.setup(title, message);
				sharedInstance.processingDialog.updateProgress(progress);
				sharedInstance.processingDialog.show();
				sharedInstance.processingDialog.setCancelable(false);

			}
		});
	}

	public void showProcessDialog(final String title, final String message) {
		this.activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				//sharedInstance.hideDialog();
				sharedInstance.processingDialog.setup(title, message);
				sharedInstance.processingDialog.show();
				sharedInstance.processingDialog.setCancelable(false);

			}
		});
	}

	/*
	 * Updates the progress of the process dialog, should it be shown.
	 */
	public void updateProgress(final float progress) {
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(sharedInstance.processingDialog.isShowing()) {
					sharedInstance.processingDialog.updateProgress(progress);
				}
			}
		});
	}

	public float getProgress() {
		return sharedInstance.processingDialog.getProgress();
	}

	/*
	 * For release-build user dialogs
	 */
	public void hideUserDialog() {
		if (this.progressDialog != null) {
			this.progressDialog.dismiss();
		}
	}

	/*
	 * Hides the custom process dialog
	 */
	public void hideProcessDialog() {
		if(this.processingDialog != null) {
			this.processingDialog.dismiss();
		}
	}
	
	/*public void hideDialog() {
		if(BuildMode.DEVELOPMENT_BUILD) {
			if (this.progressDialog != null) {
				this.progressDialog.dismiss();
			}
		}
	}*/
}
