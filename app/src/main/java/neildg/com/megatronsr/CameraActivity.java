package neildg.com.megatronsr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import neildg.com.megatronsr.camera2.CameraModule;
import neildg.com.megatronsr.camera2.CameraTextureView;
import neildg.com.megatronsr.camera2.ICameraModuleListener;
import neildg.com.megatronsr.camera2.ICameraTextureViewListener;
import neildg.com.megatronsr.camera2.ResolutionPicker;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.ui.ResolutionPickerDialog;

public class CameraActivity extends AppCompatActivity implements ICameraTextureViewListener, ICameraModuleListener {
    private final static String TAG = "CameraActivity";
    private final static int REQUEST_CAMERA_PERMISSION = 200;

    private String cameraId;

    private CameraModule cameraModule;
    private CameraTextureView cameraTextureView;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;

    private ResolutionPickerDialog resolutionPickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        this.initializeCameraModule();
    }

    private void initializeCameraModule() {
        this.cameraModule = new CameraModule(this);
        TextureView textureView =  (TextureView) this.findViewById(R.id.camera_view);
        this.cameraTextureView = new CameraTextureView(textureView, this);

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
               resolutionPickerDialog.show();
            }
        });
    }

    @Override
    public void onCameraTextureViewAvailable(CameraTextureView textureView) {
        this.openCamera();
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            this.cameraId = manager.getCameraIdList()[0];
            ResolutionPicker.updateCameraSettings(this, this.cameraId);
            this.resolutionPickerDialog = new ResolutionPickerDialog(this);
            this.resolutionPickerDialog.setup(ResolutionPicker.getSharedInstance().getAvailableCameraSizes());
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

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.cameraModule.getCameraDevice().close();
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
            Surface surface = new Surface(texture);
            final CameraDevice cameraDevice = this.cameraModule.getCameraDevice();
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
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

    protected void updatePreview() {
        CameraDevice cameraDevice = this.cameraModule.getCameraDevice();
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try {
            this.cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), new RepeatingRequestCallback(), mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        CameraDevice cameraDevice = this.cameraModule.getCameraDevice();
        TextureView textureView = this.cameraTextureView.getTextureView();
        if(null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {

            Size imageSize = ResolutionPicker.getSharedInstance().getLastAvailableSize();
            Log.d(TAG, "Image size to save: " +imageSize.getWidth()+ " X "+imageSize.getHeight());
            ImageReader reader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(), ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, CameraTextureView.ORIENTATIONS.get(rotation));

            final File file = new File(ImageWriter.getInstance().getFilePath()+"/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(CameraActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private class RepeatingRequestCallback extends CameraCaptureSession.CaptureCallback {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            // Orientation
            //int rotation = getWindowManager().getDefaultDisplay().getRotation();
            //Log.d(TAG, "Rotation: " +rotation);
        }
    }
}
