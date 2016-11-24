package neildg.com.megatronsr.camera2.capture;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.camera2.CameraUserSettings;
import neildg.com.megatronsr.camera2.capture_requests.BasicCaptureRequest;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.io.ImageFileAttribute;

/**
 * Class that handles the capture of images with specific capture requests.
 * Created by NeilDG on 11/19/2016.
 */

public class CaptureProcessor{

    private final static String TAG = "CaptureProcessor";

    private List<Surface> outputSurfaces = new ArrayList<Surface>(); //list of output surfaces

    private CameraDevice cameraDevice;
    private Size imageResolution; //size of the image/s to save
    private Size thumbnailSize; //size of the image/s' thumbnail
    private int sensorRotation;

    private Handler backgroundTheadHandler;
    private HandlerThread backgroundThread;

    private ImageReader imageReader;

    private boolean setupCalled = false;

    private MediaActionSound soundPlayer = new MediaActionSound();

    public CaptureProcessor() {

    }

    public void setup(CameraDevice cameraDevice, Size imageResolution, Size thumbnailSize, int sensorRotation, CameraUserSettings.CameraType cameraType) {
        this.cameraDevice = cameraDevice;
        this.imageResolution = imageResolution;
        this.thumbnailSize = thumbnailSize;
        if(cameraType == CameraUserSettings.CameraType.FRONT) {
            this.sensorRotation = 270; //auto set sensor rotation for front camera
        }
        else {
            this.sensorRotation = sensorRotation;
        }

        CapturedImageSaver capturedImageSaver = new CapturedImageSaver(FileImageWriter.getInstance().getFilePath(), "test_pic", ImageFileAttribute.FileType.JPEG);
        this.imageReader = ImageReader.newInstance(this.imageResolution.getWidth(), this.imageResolution.getHeight(), ImageFormat.YUV_420_888, 10);
        this.imageReader.setOnImageAvailableListener(capturedImageSaver, this.backgroundTheadHandler);

        this.setupCalled = true;
    }

    /*
     * Performs the capture based from the capture requests created in sequence. This is performed in the background thread.
     */
    public void performCapture() {
        if(this.setupCalled == false) {
            Log.e(TAG, "Setup function was not called!");
            return;
        }

        if(this.backgroundThread == null) {
            Log.e(TAG, "Background thread is not available! Call startBackgroundThread() first!");
            return;
        }

        //capture sequence proper
        try {
            final CaptureCompletedHandler captureCompletedHandler = new CaptureCompletedHandler();
            this.addOutputSurface(this.imageReader.getSurface());
            final List<CaptureRequest> captureRequests = this.assembleCaptureRequests();
            this.cameraDevice.createCaptureSession(this.outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        CaptureProcessor.this.soundPlayer.play(MediaActionSound.SHUTTER_CLICK);
                        session.captureBurst(captureRequests, captureCompletedHandler, CaptureProcessor.this.backgroundTheadHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, this.backgroundTheadHandler);
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startBackgroundThread() {
        this.backgroundThread = new HandlerThread("Camera Background");
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


    public void addOutputSurface(Surface outputSurface) {
        this.outputSurfaces.add(outputSurface);
    }

    public void createSurfaceFromTextureView(TextureView textureView) {
        this.outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
    }

    public void clearSurfaces() {
        this.outputSurfaces.clear();
    }

    public void cleanup() {
        if(this.setupCalled) {
            this.stopBackgroundThread();
            this.outputSurfaces.clear();
            this.imageReader.close();
            this.setupCalled = false;
        }

    }

    /*
     * Function for assembling the list of capture requests for burst mode
     */
    private List<CaptureRequest> assembleCaptureRequests() throws CameraAccessException {
        List<CaptureRequest> captureRequests = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            BasicCaptureRequest basicCaptureRequest = new BasicCaptureRequest(this.cameraDevice, this.imageReader, this.sensorRotation, this.thumbnailSize);
            captureRequests.add(basicCaptureRequest.getCaptureRequest());
        }

        return captureRequests;
    }

}
