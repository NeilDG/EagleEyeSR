package neildg.com.eagleeyesr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Arrays;

import neildg.com.eagleeyesr.camera2.CameraDrawableView;
import neildg.com.eagleeyesr.camera2.CameraModule;
import neildg.com.eagleeyesr.camera2.CameraTextureView;
import neildg.com.eagleeyesr.camera2.CameraUserSettings;
import neildg.com.eagleeyesr.camera2.ICameraModuleListener;
import neildg.com.eagleeyesr.camera2.ICameraTextureViewListener;
import neildg.com.eagleeyesr.camera2.ResolutionPicker;
import neildg.com.eagleeyesr.camera2.capture.CaptureProcessor;
import neildg.com.eagleeyesr.camera2.capture.FocusProcessor;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.pipeline.ProcessingQueue;
import neildg.com.eagleeyesr.platformtools.notifications.NotificationCenter;
import neildg.com.eagleeyesr.platformtools.notifications.NotificationListener;
import neildg.com.eagleeyesr.platformtools.notifications.Notifications;
import neildg.com.eagleeyesr.platformtools.notifications.Parameters;
import neildg.com.eagleeyesr.threads.CaptureSRProcessor;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;
import neildg.com.eagleeyesr.ui.ResolutionPickerDialog;
import neildg.com.eagleeyesr.ui.views.OptionsScreen;
import neildg.com.eagleeyesr.ui.views.ProcessingQueueScreen;

public class CameraActivity extends AppCompatActivity implements ICameraTextureViewListener, ICameraModuleListener, SensorEventListener, View.OnTouchListener, NotificationListener {
    private final static String TAG = "CameraActivity";
    private final static int REQUEST_CAMERA_PERMISSION = 200;

    private String cameraId;

    private CameraModule cameraModule;
    private CameraTextureView cameraTextureView;
    private CameraDrawableView cameraDrawableView;

    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;

    private SensorManager sensorManager;
    private int sensorRotation = 0;

    private Handler backgroundTheadHandler;
    private HandlerThread backgroundThread;

    private FocusProcessor focusProcessor = new FocusProcessor(); //thread that handles auto-focus algorithm
    private CaptureProcessor captureProcessor = new CaptureProcessor(this, this.backgroundTheadHandler); //thread that handles capturing and saving of photos
    private CaptureSRProcessor srProcessor = new CaptureSRProcessor(); //thread that performs super-resolution.

    //overlay views
    private ProcessingQueueScreen processingQueueScreen;
    private OptionsScreen optionsScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        CameraUserSettings.initialize();
        this.initializeCameraModule();
        this.initializeOverlayViews();

        ProcessingQueue.initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        this.startBackgroundThread();
        this.focusProcessor.startBackgroundThread();
        this.srProcessor.startBackgroundThread();

        if (this.cameraTextureView.getTextureView().isAvailable()) {
            this.openCamera();
        }

        this.sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        this.sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        ProgressDialogHandler.initialize(this);
        NotificationCenter.getInstance().addObserver(Notifications.ON_CAPTURE_COMPLETED, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.cameraModule.closeCamera();
        this.stopBackgroundThread();
        this.captureProcessor.cleanup();
        this.focusProcessor.cleanup();

        this.sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        this.sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        this.sensorManager = null;

        ProgressDialogHandler.destroy();
        NotificationCenter.getInstance().removeObserver(Notifications.ON_CAPTURE_COMPLETED, this);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        CameraUserSettings.destroy();
        this.srProcessor.stopBackgroundThread();
        ProcessingQueue.destroy();
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

    private void initializeCameraModule() {
        this.cameraModule = new CameraModule(this);
        CameraUserSettings.getInstance().setSelectedCamera(CameraUserSettings.CameraType.BACK);
        TextureView textureView =  (TextureView) this.findViewById(R.id.camera_view);
        this.cameraTextureView = new CameraTextureView(textureView, this, this);
        this.cameraDrawableView = (CameraDrawableView) this.findViewById(R.id.camera_drawable_view);

        ImageButton takePictureBtn = (ImageButton) this.findViewById(R.id.btn_capture_image);
        takePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.this.takePicture();
            }
        });

        Button modeBtn = (Button) this.findViewById(R.id.btn_mode);
        modeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View optionsOverlayView = CameraActivity.this.findViewById(R.id.options_overlay_layout);
                optionsOverlayView.setVisibility(View.VISIBLE);
            }
        });


        final ImageButton switchCamBtn = (ImageButton) this.findViewById(R.id.btn_switch_camera);
        switchCamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //switch camera
                CameraUserSettings.getInstance().switchCamera();
                CameraActivity.this.closeCamera();
                CameraActivity.this.openCamera();
                switchCamBtn.setImageResource(CameraUserSettings.getInstance().getCameraResource(CameraUserSettings.getInstance().getCameraType()));
            }
        });

        ImageButton imagePreviewBtn = (ImageButton) this.findViewById(R.id.btn_image_preview);
        imagePreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(CameraActivity.this,ImageViewActivity.class);
                startActivity(previewIntent);
            }
        });
        imagePreviewBtn.setEnabled(false);
    }

    private void initializeOverlayViews() {
        //create container for options view
        this.optionsScreen = new OptionsScreen(this.findViewById(R.id.options_overlay_layout));
        this.optionsScreen.initialize();
        this.optionsScreen.hide();

        //create container for processing queue view
        ProgressBar processingQueueBar = (ProgressBar) this.findViewById(R.id.processing_bar);
        this.processingQueueScreen = new ProcessingQueueScreen((ViewStub)this.findViewById(R.id.processing_queue_stub), false,
                processingQueueBar, this);
        this.processingQueueScreen.initialize();

    }

    @Override
    public void onBackPressed() {
        if(this.optionsScreen != null && this.optionsScreen.isShown()) {
            this.optionsScreen.hide();
        }
        else if(this.processingQueueScreen != null && this.processingQueueScreen.isShown()) {
            this.processingQueueScreen.hide();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCameraTextureViewAvailable(CameraTextureView textureView) {
        this.openCamera();
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            this.cameraId = manager.getCameraIdList()[CameraUserSettings.getInstance().getCameraId()];
            ResolutionPicker.updateCameraSettings(this, this.cameraId);
            ResolutionPickerDialog resolutionPickerDialog = new ResolutionPickerDialog(this);
            resolutionPickerDialog.setup(ResolutionPicker.getSharedInstance().getAvailableCameraSizes());
            this.optionsScreen.setupResolutionButton(resolutionPickerDialog);
            this.cameraTextureView.updateToOptimalSize(ResolutionPicker.getSharedInstance().getAvailableCameraSizes());

            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(this.cameraId, this.cameraModule, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
        this.printCameraCharacteristics();
    }

    private void closeCamera() {
        this.cameraModule.closeCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(CameraActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /*
     * Determine if specified camera modes are available
     */
    private void printCameraCharacteristics() {
        try {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(this.cameraId);
            int hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

            if(hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
                Log.d(TAG, "Camera hardware level: INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED");
            }
            else if(hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                Log.d(TAG, "Camera hardware level: INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY");
            }
            else if(hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
                Log.d(TAG, "Camera hardware level: INFO_SUPPORTED_HARDWARE_LEVEL_3");
            }
            else if(hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) {
                Log.d(TAG, "Camera hardware level: INFO_SUPPORTED_HARDWARE_LEVEL_FULL");
            }
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraOpenedSuccess(CameraDevice cameraDevice) {
        this.createCameraPreview();
    }

    @Override
    public void onCameraDisconnected(CameraDevice camera) {

    }

    protected void createCameraPreview() {
        try {
            TextureView textureView = this.cameraTextureView.getTextureView();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            Size lastResolutionSize = ResolutionPicker.getSharedInstance().getLastAvailableSize();
            Log.d(TAG, "Last resolution size: " +lastResolutionSize.toString());
            texture.setDefaultBufferSize(lastResolutionSize.getWidth(), lastResolutionSize.getHeight());
            final Surface surface = new Surface(texture);
            final CameraDevice cameraDevice = this.cameraModule.getCameraDevice();
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (cameraDevice == null) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    CameraActivity.this.setupProcessors(cameraCaptureSession, cameraDevice, surface);
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupProcessors(CameraCaptureSession cameraCaptureSession, CameraDevice cameraDevice, Surface surface) {
        this.cameraCaptureSessions = cameraCaptureSession;
        this.focusProcessor.setup(CameraActivity.this, cameraDevice, CameraActivity.this.cameraCaptureSessions, surface); //setup focus processor
        this.focusProcessor.initiatePreview();
        this.updatePictureSettings();
    }

    public void updatePictureSettings() {
        Size lastResolutionSize = ResolutionPicker.getSharedInstance().getLastAvailableSize();
        Size thumbnailSize = ResolutionPicker.getSharedInstance().getLastAvailableThumbnailSize();
        Size swappedThumbnailSize = new Size(thumbnailSize.getHeight(), thumbnailSize.getWidth());

        this.captureProcessor.setup(this.cameraModule.getCameraDevice(), lastResolutionSize, swappedThumbnailSize, CameraUserSettings.getInstance().getCameraType());
    }

    protected void takePicture() {
        ParameterConfig.setPrefs("capture_start", System.currentTimeMillis());
        this.captureProcessor.clearSurfaces();
        this.captureProcessor.performCapture(this.sensorRotation);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float aX= event.values[0];
        float aY= event.values[1];
        //aZ= event.values[2];
        double angle = Math.atan2(aX, aY)/(Math.PI/180);

        double absAngle = Math.abs(angle);
        if(absAngle >= 0 && absAngle < 90) {
            this.sensorRotation = 90;
        }
        else if(absAngle >= 90 && absAngle < 180) {
            this.sensorRotation = 0;
        }
        else if(absAngle >= 180 && absAngle < 270) {
            this.sensorRotation = 180;
        }
        else {
            this.sensorRotation = 270;
        }

        //Log.d(TAG, "Angle: "+angle+ " Sensor rotation: " +this.sensorRotation);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "Touch event applied. X:" +event.getX()+ " Y: " +event.getY());
        int x = Math.round(event.getX());
        int y = Math.round(event.getY());
        this.cameraDrawableView.drawFocusRegion(x, y, 1000);
        this.focusProcessor.performAutoFocusOnRegion(x, y);
        return true;
    }

    @Override
    public void onNotify(String notificationString, Parameters params) {
        if(notificationString == Notifications.ON_CAPTURE_COMPLETED) {

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CameraActivity.this.createCameraPreview();

                    ImageButton imageButton = (ImageButton) CameraActivity.this.findViewById(R.id.btn_image_preview);
                    Bitmap thumbnailBmp = FileImageReader.getInstance().loadBitmapThumbnail(ProcessingQueue.getInstance().getLatestImageName(), ImageFileAttribute.FileType.JPEG, 300, 300);
                    imageButton.setImageBitmap(thumbnailBmp);
                    imageButton.setEnabled(true);
                }
            });

        }
    }
}
