package neildg.com.megatronsr.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.*;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.Surface;

import java.lang.reflect.Array;
import java.util.ArrayList;

import neildg.com.megatronsr.ImageCaptureActivity;
import neildg.com.megatronsr.camera.callbacks.CaptureSession;
import neildg.com.megatronsr.platformtools.utils.ApplicationCore;

/**
 * New camera manager using Camera 5.0 API
 * @author NeilDG
 */
public class CameraManagerWrapper extends CameraDevice.StateCallback {
    private final static String TAG = "SR_CameraManager2";

    private static CameraManagerWrapper sharedInstance = null;

    public static CameraManagerWrapper getInstance() {
        if(sharedInstance == null) {
            Log.e(TAG, "OldCameraManager has not called initialized!");
        }
        return sharedInstance;
    }

    private CameraDevice deviceCamera;

    private CameraManager cameraManager = (CameraManager) ApplicationCore.getInstance().getMainActivity().getSystemService(Context.CAMERA_SERVICE);

    private ArrayList<Surface> cameraSurfaces = null;
    private CaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;

    private CameraManagerWrapper() {

    }

    public static void initialize(ArrayList<Surface> cameraSurfaces) {
        sharedInstance = new CameraManagerWrapper();
        sharedInstance.cameraSurfaces = cameraSurfaces;
    }
    public static void destroy() {sharedInstance = null;}

    public static boolean isCameraSupported(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /*
	 * Requests for camera access and returns its instance
	 */
    public void requestCamera() {
        try {
            this.cameraManager.openCamera("0", this, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error accessing camera: " + e.getMessage());
        }
    }

    @Override
    public void onOpened(CameraDevice camera) {
        this.deviceCamera = camera;
        try {
            this.captureRequestBuilder = this.deviceCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            this.captureRequestBuilder.addTarget(this.cameraSurfaces.get(0));

            this.captureSession = new CaptureSession(this.captureRequestBuilder);
            this.deviceCamera.createCaptureSession(this.cameraSurfaces, this.captureSession, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnected(CameraDevice camera) {
        if(this.deviceCamera == camera) {
            this.deviceCamera = null;
        }
    }

    @Override
    public void onError(CameraDevice camera, int error) {
        Log.e(TAG, "Error opening " +camera.getId()+ " error: " +error);
    }
}
