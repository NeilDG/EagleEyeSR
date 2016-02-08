package neildg.com.megatronsr;

import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

import neildg.com.megatronsr.camera.CameraManagerWrapper;
import neildg.com.megatronsr.camera.OldCameraManager;
import neildg.com.megatronsr.camera.CameraPreview;
import neildg.com.megatronsr.camera.DrawingView;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

public class ImageCaptureActivity extends AppCompatActivity {

    private final static String TAG = "SR_ImageCapture";
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_view_layout);

        SurfaceView testCameraView = (SurfaceView) this.findViewById(R.id.camera_surface_view);
        ArrayList<Surface> surfaces = new ArrayList<Surface>();
        surfaces.add(testCameraView.getHolder().getSurface());

        CameraManagerWrapper.initialize(surfaces);
        CameraManagerWrapper.getInstance().requestCamera();
        this.initializeButtons();

    }

    @Override
    protected void onResume() {
        super.onResume();
        ProgressDialogHandler.initialize(this);
        ImageWriter.initialize(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        OldCameraManager.getInstance().closeCamera();
        ProgressDialogHandler.destroy();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ImageWriter.destroy();
    }

    private void initializeButtons() {
        ImageButton rotateCameraBtn = (ImageButton) this.findViewById(R.id.btn_rotate);
        rotateCameraBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Camera deviceCamera = OldCameraManager.getInstance().swapCamera();
                ImageCaptureActivity.this.cameraPreview.updateCameraSource(deviceCamera);
            }
        });

        ImageButton captureBtn = (ImageButton) this.findViewById(R.id.btn_capture);
        captureBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                OldCameraManager.getInstance().capture();
            }
        });
    }

}
