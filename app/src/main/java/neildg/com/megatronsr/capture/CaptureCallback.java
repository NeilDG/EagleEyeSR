/**
 * 
 */
package neildg.com.megatronsr.capture;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;

import neildg.com.megatronsr.camera.OldCameraManager;
import neildg.com.megatronsr.platformtools.utils.notifications.NotificationCenter;
import neildg.com.megatronsr.platformtools.utils.notifications.Notifications;
import neildg.com.megatronsr.platformtools.utils.notifications.Parameters;


/**
 * Implements the picture callback called by the camera upon capture of image
 * @author NeilDG
 *
 */
public class CaptureCallback implements PictureCallback {
	private final static String TAG = "SR_CaptureCallback";
	
	public final static String CAPTURED_IMAGE_DATA_KEY = "CAPTURED_IMAGE_DATA_KEY";
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d(TAG, "Picture taken!");
		
		Parameters params = new Parameters();
		params.putExtra(CAPTURED_IMAGE_DATA_KEY, data);
		
		ImageSequencesHolder.getInstance().setOriginalImageData(data);
		NotificationCenter.getInstance().postNotification(Notifications.ON_CREATE_THUMBNAIL, params);
		
		//update camera source to refresh preview
		OldCameraManager.getInstance().refreshCameraPreview();
		
		ShutterCallbackHandler shutterHandler = new ShutterCallbackHandler();
		shutterHandler.start();
	}
	
	

}
