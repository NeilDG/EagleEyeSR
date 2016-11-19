package neildg.com.megatronsr.camera2.capture;

import android.graphics.ImageFormat;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.camera2.CameraUserSettings;

/**
 * Class that handles the capture of images with specific capture requests.
 * Created by NeilDG on 11/19/2016.
 */

public class CaptureProcessor {

    private final static String TAG = "CaptureProcessor";

    private List<Surface> outputSurfaces = new ArrayList<Surface>(); //list of output surfaces
    private List<CaptureRequest> captureRequests = new ArrayList<>(); //list of capture requests to be made.

    private Size imageResolution; //size of the image/s to save
    private Size thumbnailSize; //size of the image/s' thumbnail
    private int sensorRotation;

    private Handler backgroundTheadHandler;
    private HandlerThread backgroundThread;

    private ImageReader imageReader;

    private boolean setupCalled = false;

    public CaptureProcessor() {

    }

    public void setup(Size imageResolution, Size thumbnailSize, int sensorRotation, CameraUserSettings.CameraType cameraType) {
        this.imageResolution = imageResolution;
        this.thumbnailSize = thumbnailSize;
        if(cameraType == CameraUserSettings.CameraType.FRONT) {
            this.sensorRotation = 270; //auto set sensor rotation for front camera
        }
        else {
            this.sensorRotation = sensorRotation;
        }

        this.imageReader = ImageReader.newInstance(this.imageResolution.getWidth(), this.imageResolution.getHeight(), ImageFormat.JPEG, 1);

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

        this.startBackgroundThread();
        //perform capture request sequence here.

        this.addOutputSurface(this.imageReader.getSurface());
    }

    private void startBackgroundThread() {
        this.backgroundThread = new HandlerThread("Camera Background");
        this.backgroundThread.start();
        this.backgroundTheadHandler = new Handler(this.backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
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
        outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
    }

    public void cleanup() {
        this.outputSurfaces.clear();
        this.setupCalled = false;
    }

}
