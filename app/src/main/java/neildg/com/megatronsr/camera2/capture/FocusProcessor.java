package neildg.com.megatronsr.camera2.capture;

import android.content.Context;
import android.graphics.Point;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.MediaActionSound;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import neildg.com.megatronsr.camera2.CameraUserSettings;

/**
 * Processor for selecting AF region and locking AF.
 * Created by NeilDG on 11/27/2016.
 */

public class FocusProcessor extends CameraCaptureSession.CaptureCallback {
    private final static String TAG = "FocusProcessor";

    private Context appContext;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession captureSession;

    private MediaActionSound soundPlayer = new MediaActionSound();

    private boolean setup = false;
    private boolean autoFocusTriggered = false;
    private HandlerThread backgroundThread;
    private Handler backgroundTheadHandler;

    public FocusProcessor() {

    }

    public void setup(Context appContext, CameraDevice cameraDevice, CameraCaptureSession captureSession, Surface targetSurface) {
        try {
            this.appContext = appContext;
            this.cameraDevice = cameraDevice;
            this.captureRequestBuilder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            this.captureSession = captureSession;
            this.captureRequestBuilder.addTarget(targetSurface);
            this.setup = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void initiatePreview() {
        try {
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO); //set auto mode
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START); //trigger Auto-focus algorithm
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON); //triger auto-exposure
            this.captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, this.backgroundTheadHandler);
        } catch(CameraAccessException  e) {
            e.printStackTrace();
        } catch(IllegalStateException e) {
            Log.e(TAG, "Preview focus failed. Processor busy.");
        }

    }

    public void cleanup() {
        this.appContext = null;
        this.cameraDevice = null;
        this.setup = false;
        this.stopBackgroundThread();
    }

    public void startBackgroundThread() {
        this.backgroundThread = new HandlerThread("Camera Preview Handler");
        this.backgroundThread.start();
        this.backgroundTheadHandler = new Handler(this.backgroundThread.getLooper());
    }

    public void stopBackgroundThread() {
        if(this.backgroundThread == null) {
            return;
        }

        this.backgroundThread.quitSafely();
        try {
            this.backgroundThread.join();
            this.backgroundThread = null;
            this.backgroundTheadHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void performAutoFocusOnRegion(int x, int y) {

        if(!this.setup) {
            Log.d(TAG, "Focus processor not yet setup.");
            return;
        }

        if(this.autoFocusTriggered) {
            Log.d(TAG, "Auto-focus already initiated");
            return;
        }

        try {
            CameraManager manager = (CameraManager) this.appContext.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = manager.getCameraIdList()[CameraUserSettings.getInstance().getCameraId()];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            Log.d(TAG, "Max focus count:" + characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)+ " Max auto-exposure count: " +characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE));
            MeteringRectangle[] regionRectList = new MeteringRectangle[1];
            regionRectList[0] = new MeteringRectangle(new Point(x,y), new Size(450,450), MeteringRectangle.METERING_WEIGHT_MAX);

            this.captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO); //set auto mode
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START); //trigger Auto-focus algorithm
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON); //triger auto-exposure
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, regionRectList);
            this.captureSession.setRepeatingRequest(captureRequestBuilder.build(), this, this.backgroundTheadHandler);
            this.autoFocusTriggered = true;

        } catch(CameraAccessException e) {
            e.printStackTrace();
        } catch(IllegalStateException e) {
            Log.e(TAG, "Focus failed. Processor busy. Try again!");
        }
    }

    private void unlockAutoFocus() {
        try {
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL); //cancel auto-focus
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO); //set auto mode
            this.captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, this.backgroundTheadHandler);

        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
        catch(IllegalStateException e) {
            Log.e(TAG, "Session has been closed; further changes are illegal. Doing nothing.");
        }
        finally {
            this.autoFocusTriggered = false;
        }
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);

        if(afState == CaptureRequest.CONTROL_AF_STATE_INACTIVE) {
            //Log.d(TAG, "CONTROL_AF_STATE_INACTIVE");
        }
        else if(afState == CaptureRequest.CONTROL_AF_STATE_INACTIVE) {
            //Log.d(TAG, "CONTROL_AF_STATE_INACTIVE");
        }
        else if(afState == CaptureRequest.CONTROL_AF_STATE_PASSIVE_SCAN) {
            //Log.d(TAG, "CONTROL_AF_STATE_PASSIVE_SCAN");
        }
        else if(afState == CaptureRequest.CONTROL_AF_STATE_PASSIVE_FOCUSED) {
            //Log.d(TAG, "CONTROL_AF_STATE_PASSIVE_FOCUSED");
        }
        else if(afState == CaptureRequest.CONTROL_AF_STATE_ACTIVE_SCAN) {
            //Log.d(TAG, "CONTROL_AF_STATE_ACTIVE_SCAN");
        }
        else if(afState == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED) {
            Log.d(TAG, "CONTROL_AF_STATE_FOCUSED_LOCKED");
            this.soundPlayer.play(MediaActionSound.FOCUS_COMPLETE);
            this.unlockAutoFocus();
        }
        else if(afState == CaptureRequest.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
            //Log.d(TAG, "CONTROL_AF_STATE_FOCUSED_LOCKED");
        }
        else if(afState == CaptureRequest.CONTROL_AF_STATE_PASSIVE_UNFOCUSED) {
            //Log.d(TAG, "CONTROL_AF_STATE_FOCUSED_LOCKED");
        }
    }
}
