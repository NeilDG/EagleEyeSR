/**
 * 
 */
package neildg.com.eagleeyesr.ui.progress_dialog;

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

	private IProgressImplementor progressImplementor;

	private Activity activity;
	
	public ProgressDialogHandler(Activity activity) {
		this.activity = activity;
	}

	public void setDefaultProgressImplementor() {
		this.setProgressImplementor(new ProcessingDialog(this.activity));
		Log.i(TAG, "Set a default progress implementor.");
	}

	public void setProgressImplementor(IProgressImplementor progressImplementor) {

		float progress = 0.0f;
		String text = null;
		if(this.progressImplementor != null) {
			progress = this.progressImplementor.getProgress();
			text = this.progressImplementor.getMessage();
		}

		this.progressImplementor = progressImplementor;
		this.progressImplementor.setup("", text);
		this.progressImplementor.updateProgress(progress);
	}
	
	public static void initialize(Activity activity) {
		sharedInstance = new ProgressDialogHandler(activity);
	}
	
	public static void destroy() {
		if(sharedInstance != null && sharedInstance.progressImplementor != null) {
			sharedInstance.progressImplementor.hide();
			sharedInstance.progressImplementor = null;
		}

		sharedInstance = null;
	}

	/*
	 * Shows a custom processing dialog created via layout res
	 */
	public void showProcessDialog(final String title, final String message, final float progress) {
		this.activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				sharedInstance.progressImplementor.setup(title, message);
				sharedInstance.progressImplementor.updateProgress(progress);
				sharedInstance.progressImplementor.show();


			}
		});
	}

	public void showProcessDialog(final String title, final String message) {
		this.activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				//sharedInstance.hideDialog();
				sharedInstance.progressImplementor.setup(title, message);
				sharedInstance.progressImplementor.show();

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
				if(sharedInstance.progressImplementor.isShown()) {
					sharedInstance.progressImplementor.updateProgress(progress);
				}
			}
		});
	}

	public float getProgress() {
		return sharedInstance.progressImplementor.getProgress();
	}

	/*
	 * Hides the custom process dialog
	 */
	public void hideProcessDialog() {
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (sharedInstance.progressImplementor != null) {
					sharedInstance.progressImplementor.hide();
				}
			}
		});
	}
}
