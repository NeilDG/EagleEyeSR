package neildg.com.megatronsr.camera.callbacks;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.util.Log;

/**
 * Created by user on 2/8/2016.
 */

public class CaptureSession extends CameraCaptureSession.StateCallback {
    private final static String TAG = "SR_CaptureSession";
    private CameraCaptureSession session;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest previewRequest;
    private CustomCaptureCallback customCaptureCallback = new CustomCaptureCallback();

    public CaptureSession(CaptureRequest.Builder captureRequestBuilder) {
        this.captureRequestBuilder = captureRequestBuilder;
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
    }


    @Override
    public void onConfigured(CameraCaptureSession session) {
        this.session = session;
        try {
            // Auto focus should be continuous for camera preview.
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // Flash is automatically enabled when necessary.
            setAutoFlash(this.captureRequestBuilder);

            // Finally, we start displaying the camera preview.
            this.previewRequest = this.captureRequestBuilder.build();
            this.session.setRepeatingRequest(this.previewRequest,
                    this.customCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session) {
        Log.e(TAG, "Error configuring camera capture session!");
    }

    public CameraCaptureSession getSession() {
        if(this.session == null) {
            Log.e(TAG, "CameraCaptureSession is not yet existing!");
        }

        return this.session;
    }


}
