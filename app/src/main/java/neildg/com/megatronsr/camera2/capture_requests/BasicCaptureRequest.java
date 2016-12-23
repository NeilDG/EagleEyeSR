package neildg.com.megatronsr.camera2.capture_requests;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;

/**
 * A basic capture request implemented. Auto-focus moode enabled and still capture.
 * Created by NeilDG on 11/19/2016.
 */

public class BasicCaptureRequest extends ACaptureRequestWrapper {
    private final static String TAG = "BasicCaptureRequest";

    private int jpegOrientation;
    private Size thumbnailSize;

    public BasicCaptureRequest(CameraDevice cameraDevice, ImageReader imageReader, int jpegOrientation, Size thumbnailSize) throws CameraAccessException {
        super(cameraDevice, imageReader);
        this.jpegOrientation = jpegOrientation;
        this.thumbnailSize = thumbnailSize;

        this.createCaptureRequest();
    }

    @Override
    protected void createCaptureRequest() throws CameraAccessException {
        CaptureRequest.Builder captureRequestBuilder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureRequestBuilder.addTarget(this.imageReader.getSurface());
        captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, this.jpegOrientation);
        captureRequestBuilder.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, this.thumbnailSize);

        Log.d(TAG, "Sensor rotation: " +this.jpegOrientation+ " Thumbnail size: " +this.thumbnailSize.toString());
        this.captureRequest = captureRequestBuilder.build();

    }
}
