package neildg.com.eagleeyesr.camera2.capture_requests;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;

/**
 * A capture request wrapper convenience class that contains the CaptureRequest created from the builder.
 * By default, all capture requests have at least one target, which is the image reader instance
 * Created by NeilDG on 11/19/2016.
 */

public abstract class ACaptureRequestWrapper {
    private final static String TAG = "ACaptureRequestWrapper";

    protected CameraDevice cameraDevice;
    protected ImageReader imageReader;
    protected CaptureRequest captureRequest;

    public ACaptureRequestWrapper(CameraDevice cameraDevice, ImageReader imageReader) throws CameraAccessException {
        this.cameraDevice = cameraDevice;
        this.imageReader = imageReader;
    }

    public CaptureRequest getCaptureRequest() {
        return this.captureRequest;
    }

    protected abstract void createCaptureRequest() throws CameraAccessException; //implemented by custom capture request wrappers
}
