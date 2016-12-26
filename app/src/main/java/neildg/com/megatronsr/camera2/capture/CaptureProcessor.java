package neildg.com.megatronsr.camera2.capture;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.CameraActivity;
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

    private CameraActivity cameraActivity;

    private Handler backgroundTheadHandler;

    private ImageReader imageReader;

    private boolean setupCalled = false;

    private CaptureCompletedHandler captureCompletedHandler = new CaptureCompletedHandler();

    public CaptureProcessor(CameraActivity cameraActivity, Handler backgroundTheadHandler) {
        this.cameraActivity = cameraActivity;
        this.backgroundTheadHandler = backgroundTheadHandler;
    }

    public void setup(CameraDevice cameraDevice, Size imageResolution, Size thumbnailSize, CameraUserSettings.CameraType cameraType) {
        this.cameraDevice = cameraDevice;
        this.imageResolution = imageResolution;
        this.thumbnailSize = thumbnailSize;
        /*if(cameraType == CameraUserSettings.CameraType.FRONT) {
            this.sensorRotation = 270; //auto set sensor rotation for front camera
        }
        else {
            this.sensorRotation = sensorRotation;
        }*/

        CapturedImageSaver capturedImageSaver = new CapturedImageSaver(FileImageWriter.getInstance().getFilePath(), ImageFileAttribute.FileType.JPEG);
        this.imageReader = ImageReader.newInstance(this.imageResolution.getWidth(), this.imageResolution.getHeight(), ImageFormat.JPEG, 1);
        this.imageReader.setOnImageAvailableListener(capturedImageSaver, this.backgroundTheadHandler);
        this.setupCalled = true;

    }

    /*
     * Performs the capture based from the capture requests created in sequence. This is performed in the background thread.
     */
    public void performCapture(int sensorRotation) {
        if(this.setupCalled == false) {
            Log.e(TAG, "Setup function was not called!");
            return;
        }

        //capture sequence proper
        try {
            this.addOutputSurface(this.imageReader.getSurface());
            final BasicCaptureRequest basicCaptureRequest = new BasicCaptureRequest(this.cameraDevice, this.imageReader, sensorRotation, this.thumbnailSize);
            this.cameraDevice.createCaptureSession(this.outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(basicCaptureRequest.getCaptureRequest(), captureCompletedHandler, CaptureProcessor.this.backgroundTheadHandler);
                        //List<CaptureRequest> captureRequests = CaptureProcessor.this.assembleCaptureRequests();
                        //session.captureBurst(captureRequests, captureCompletedHandler, CaptureProcessor.this.backgroundTheadHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    catch(IllegalStateException e) {
                        Log.e(TAG, "Capture failed. Processor busy. Try again!");
                        Toast.makeText(CaptureProcessor.this.cameraActivity, "Capture failed. Processor busy. Try again!", Toast.LENGTH_SHORT).show();
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

    /*public void startBackgroundThread() {
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
    }*/


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
            //this.stopBackgroundThread();
            this.outputSurfaces.clear();
            this.imageReader.close();
            this.setupCalled = false;
        }

    }

    /*
     * Function for assembling the list of capture requests for burst mode
     */
    private List<CaptureRequest> assembleCaptureRequests(int sensorRotation) throws CameraAccessException {
        List<CaptureRequest> captureRequests = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            BasicCaptureRequest basicCaptureRequest = new BasicCaptureRequest(this.cameraDevice, this.imageReader, sensorRotation, this.thumbnailSize);
            captureRequests.add(basicCaptureRequest.getCaptureRequest());
        }

        return captureRequests;
    }
}
