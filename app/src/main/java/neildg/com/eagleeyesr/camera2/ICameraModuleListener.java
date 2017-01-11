package neildg.com.eagleeyesr.camera2;

import android.hardware.camera2.CameraDevice;

/**
 * Created by NeilDG on 10/23/2016.
 */

public interface ICameraModuleListener {
    void onCameraOpenedSuccess(CameraDevice cameraDevice);
    void onCameraDisconnected(CameraDevice camera);
}
