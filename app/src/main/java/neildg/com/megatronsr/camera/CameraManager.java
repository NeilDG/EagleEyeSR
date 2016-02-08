/**
 * 
 */
package neildg.com.megatronsr.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import java.util.List;

import neildg.com.megatronsr.capture.CaptureCallback;
import neildg.com.megatronsr.config.ConfigHandler;
import neildg.com.megatronsr.config.values.BaseConfig;

/**
 * Handles and controls the camera
 * @author NeilDG
 *
 */
public class CameraManager {
	private final static String TAG = "SR_CameraManager";
	
	private static CameraManager sharedInstance = null;
	
	public static CameraManager getInstance() {
		if(sharedInstance == null) {
			Log.e(TAG, "CameraManager has not called initialized!");
		}
		return sharedInstance;
	}
	
	private Camera deviceCamera;
	private CameraPreview cameraPreview;
	
	private Size defaultPreviewSize;
	private Size shutterSize;
	
	private boolean isFrontCamera = false;
	private boolean safeToTakePicture = false;
	private CaptureCallback captureCallback = new CaptureCallback();
	
	private CameraManager() {
		
	}
	
	public static void initialize() {
		sharedInstance = new CameraManager();
	}
	
	public static boolean isCameraSupported(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
	public void grantCapturePermission() {
		this.safeToTakePicture = true;
	}
	
	public void restrictCapturePermission() {
		this.safeToTakePicture = false;
	}
	
	/*
	 * Requests for camera access and returns its instance
	 */
	public Camera requestCamera() {
		if(this.deviceCamera != null) {
			return this.deviceCamera;
		}
		else {
			this.deviceCamera = Camera.open();
			this.setCameraSettings();
			/*try {
		        this.deviceCamera = Camera.open(); // attempt to get a Camera instance
		        this.setCameraSettings();
		    }
		    catch (Exception e){
		       Log.e(TAG, "Camera is not available! " +e.getMessage());
		    }*/
			
			return this.deviceCamera;
		}
	}
	
	public void setCameraPreview(CameraPreview cameraPreview) {
		this.cameraPreview = cameraPreview;
	}
	
	/**
	 * Refreshes the camera preview
	 */
	public void refreshCameraPreview() {
		if(this.deviceCamera != null) {
			this.cameraPreview.updateCameraSource(this.deviceCamera);
		}
	}
	
	/*
	 * Requests for back camera access
	 */
	public Camera requestCameraBack() {
		if(this.deviceCamera != null) {
			this.deviceCamera.release();
			this.deviceCamera = null;
		}
		
		try {
			this.deviceCamera = Camera.open(0);
			this.setCameraSettings();
		}
		catch(Exception e) {
			Log.e(TAG, "Front camera is not available!");
		}
		
		return this.deviceCamera;
	}
	
	/*
	 * Requests for front camera access
	 */
	public Camera requestCameraFront() {
		if(this.deviceCamera != null) {
			this.deviceCamera.release();
			this.deviceCamera = null;
		}
		
		try {
			this.deviceCamera = Camera.open(1);
			this.setCameraSettings();
		}
		catch(Exception e) {
			Log.e(TAG, "Front camera is not available!");
		}
		
		return this.deviceCamera;
	}
	
	/*
	 * Swaps between the back and the front camera
	 */
	public Camera swapCamera() {
		if(this.isFrontCamera) {
			this.isFrontCamera = false;
			return this.requestCameraBack();
		}
		else {
			this.isFrontCamera = true;
			return this.requestCameraFront();
		}
	}
	
	/*
	 * Close the camera being used
	 */
	public void closeCamera() {
		if(this.deviceCamera != null) {
			this.deviceCamera.stopPreview();
			this.deviceCamera.release();
			this.deviceCamera = null;
		}
	}
	
	public void setupCameraForShutter() {
		 this.deviceCamera.stopPreview();
		 Camera.Parameters parameters = this.deviceCamera.getParameters();
		 
		 //preview size should use the actual one.
		 Size pictureSize = parameters.getPictureSize();
		 
		 int largestWidth = pictureSize.width;
		 int largestHeight = pictureSize.height;
		 
		 Size bestPictureSize = this.getBestPictureSize(largestWidth, largestHeight);
		 this.shutterSize = bestPictureSize;

		 parameters.setPreviewSize(bestPictureSize.width, bestPictureSize.height);
		 this.deviceCamera.setParameters(parameters);
		 this.deviceCamera.startPreview();
	}

	public Size getShutterSize() {
		return this.shutterSize;
	}

	public Size getActualCameraSize() {
		return this.deviceCamera.getParameters().getPictureSize();
	}

	private Size getBestPictureSize(int width, int height)
	{
	        Size result=null;
	        Camera.Parameters p = this.deviceCamera.getParameters();
	        for (Size size : p.getSupportedPictureSizes()) {
	            if (size.width<=width && size.height<=height) {
	                if (result==null) {
	                    result=size;
	                } else {
	                    int resultArea=result.width*result.height;
	                    int newArea=size.width*size.height;

	                    if (newArea>resultArea) {
	                        result=size;
	                    }
	                }
	            }
	        }
	    return result;

	}

	/**
	 * Finds the best picture size based on area
	 * @param width
	 * @param height
	 * @return
	 */
	private Size getBestPictureSizedBasedOnArea(int width, int height) {
		Size result = null;
		Camera.Parameters p = this.deviceCamera.getParameters();

		List<Size> pictureSizes = p.getSupportedPictureSizes();
		int intendedArea = width * height;

		for(int i = 0; i < pictureSizes.size(); i++) {
			Size size = pictureSizes.get(i);
			int area = size.width * size.height;

			if(area <= intendedArea) {
				result = size;
				//break;
			}
		}

		return result;
	}

	public void resetSettings() {
		 this.deviceCamera.stopPreview();

		 Camera.Parameters parameters = this.deviceCamera.getParameters();
		 parameters.setPreviewSize(this.defaultPreviewSize.width, this.defaultPreviewSize.height);
		 this.deviceCamera.setParameters(parameters);

		 this.deviceCamera.startPreview();
	}

	public void setCameraSettings() {
		 Camera.Parameters parameters = this.deviceCamera.getParameters();
		 this.defaultPreviewSize = this.deviceCamera.getParameters().getPreviewSize();

		 BaseConfig baseConfig = ConfigHandler.getInstance().getCurrentConfig();
		 Size closestSize = this.getBestPictureSizedBasedOnArea(baseConfig.getCameraWidth(), baseConfig.getCameraHeight());
		 Log.d(TAG, "Setting picture size: " + closestSize.width + " X " +closestSize.height);
		 
		 parameters.setPictureSize(closestSize.width, closestSize.height);
		 this.deviceCamera.setParameters(parameters);
	}
	
	/*
	 * Captures photo using the active camera
	 */
	public void capture() {
		if(this.deviceCamera != null && this.safeToTakePicture) {
			this.safeToTakePicture = false;
			this.deviceCamera.takePicture(null, null, this.captureCallback);
		}
	}
	
}
