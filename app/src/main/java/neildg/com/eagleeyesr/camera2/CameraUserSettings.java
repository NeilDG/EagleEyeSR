package neildg.com.eagleeyesr.camera2;

import neildg.com.eagleeyesr.R;

/**
 * Stores various camera user settings here
 * Created by NeilDG on 10/31/2016.
 */

public class CameraUserSettings {
    private final static String TAG = "CameraUserSettings";

    private static CameraUserSettings sharedInstance = null;
    public static CameraUserSettings getInstance() {
        return sharedInstance;
    }

    public static void initialize() {
        sharedInstance = new CameraUserSettings();
    }

    public static void destroy() {
        sharedInstance = null;
    }

    public enum CameraType{
        FRONT,
        BACK
    }

    private int resolutionIndex = 0;
    private CameraType cameraType = CameraType.BACK;

    private int backCameraRes = R.drawable.gui_almalence_settings_changecamera_back;
    private int frontCameraRes = R.drawable.gui_almalence_settings_changecamera_front;

    public void setPreferredResolution(int resolutionIndex) {
        this.resolutionIndex = resolutionIndex;
    }

    public int getPreferredResolution() {
        return this.resolutionIndex;
    }

    public void setSelectedCamera(CameraType cameraType){
        this.cameraType = cameraType;
    }

    public void switchCamera() {
        if(this.cameraType == CameraType.BACK) {
            this.cameraType = CameraType.FRONT;
        }
        else if(this.cameraType == CameraType.FRONT) {
            this.cameraType = CameraType.BACK;
        }
    }

    public CameraType getCameraType() {
        return this.cameraType;
    }

    public int getCameraId() {
        if(cameraType == CameraType.BACK) {
            return 0;
        }
        else {
            return 1;
        }
    }

    public int getCameraResource(CameraType cameraType) {
        if(cameraType == CameraType.BACK) {
            return this.frontCameraRes;
        }
        else {
            return this.backCameraRes;
        }
    }
}
