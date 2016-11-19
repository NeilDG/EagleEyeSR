package neildg.com.megatronsr.camera2.capture_requests;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;

/**
 * A basic capture request implemented. Auto-focus moode enabled and still capture.
 * Created by NeilDG on 11/19/2016.
 */

public class BasicCaptureRequest extends ACaptureRequestWrapper {
    private final static String TAG = "BasicCaptureRequest";

    public BasicCaptureRequest(CameraDevice cameraDevice, ImageReader imageReader) throws CameraAccessException {
        super(cameraDevice, imageReader);
    }

    @Override
    protected void createCaptureRequest() throws CameraAccessException {
        CaptureRequest.Builder captureRequestBuilder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureRequestBuilder.addTarget(this.imageReader.getSurface());
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        this.captureRequest = captureRequestBuilder.build();

    }
}
