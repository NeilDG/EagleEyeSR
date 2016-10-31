package neildg.com.megatronsr.camera2;

import android.hardware.camera2.CameraDevice;
import android.util.Log;

/**
 * Contains the camera device reference once received by state callback from Camera 2 API
 * Created by NeilDG on 10/23/2016.
 */

public class CameraModule extends CameraDevice.StateCallback {
    private final static String TAG  = "CameraModule";
    private CameraDevice cameraDevice;

    private ICameraModuleListener moduleListener;

    public CameraModule(ICameraModuleListener moduleListener) {
        this.moduleListener = moduleListener;
    }

    public CameraDevice getCameraDevice() {
        return this.cameraDevice;
    }

    public void closeCamera() {
        if(this.cameraDevice != null) {
            this.cameraDevice.close();
        }
    }

    @Override
    public void onOpened(CameraDevice camera) {
        this.cameraDevice = camera;
        this.moduleListener.onCameraOpenedSuccess(this.cameraDevice);
    }

    @Override
    public void onDisconnected(CameraDevice camera) {
        this.cameraDevice.close();
        this.cameraDevice = null;
        this.moduleListener.onCameraDisconnected(camera);
    }

    @Override
    public void onError(CameraDevice camera, int error) {
        if(this.cameraDevice != null) {
            this.cameraDevice.close();
        }
        this.cameraDevice = null;
        this.resolveError(error);
    }

    private void resolveError(int errorCode) {
        if(errorCode == CameraDevice.StateCallback.ERROR_CAMERA_DEVICE) {
            Log.e(TAG, "Camera encountered a fatal error.");
        }
        if(errorCode == ERROR_CAMERA_DISABLED) {
            Log.e(TAG, "The camera device could not be opened due to a device policy.");
        }
        if(errorCode == ERROR_CAMERA_IN_USE) {
            Log.e(TAG, "The camera device is in use already.");
        }
        if(errorCode == ERROR_MAX_CAMERAS_IN_USE) {
            Log.e(TAG, "The camera device could not be opened because there are too many other open camera devices.");
        }
    }
}
