package neildg.com.megatronsr.camera2.capture;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.util.Log;
import android.widget.Toast;

import neildg.com.megatronsr.CameraActivity;
import neildg.com.megatronsr.platformtools.utils.notifications.NotificationCenter;
import neildg.com.megatronsr.platformtools.utils.notifications.Notifications;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Handles cleanup of capture
 * Created by NeilDG on 11/19/2016.
 */

public class CaptureCompletedHandler extends CameraCaptureSession.CaptureCallback {
    private final static String TAG = "CaptureCompletedHandler";

    private float progressIncrement = 0.0f;

    @Override
    public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
        super.onCaptureProgressed(session, request, partialResult);

        this.progressIncrement = ProgressDialogHandler.getInstance().getProgress() + 3.0f;
        ProgressDialogHandler.getInstance().updateProgress(this.progressIncrement);
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        Log.d(TAG, "Capture completed!");
        NotificationCenter.getInstance().postNotification(Notifications.ON_CAPTURE_COMPLETED);
    }
}
