package neildg.com.megatronsr;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import neildg.com.megatronsr.camera.CameraManager;
import neildg.com.megatronsr.camera.CameraPreview;
import neildg.com.megatronsr.camera.DrawingView;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

public class ImageCaptureActivity extends AppCompatActivity {

    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_view_layout);

        Camera deviceCamera = CameraManager.getInstance().requestCamera();
        this.cameraPreview = (CameraPreview) this.findViewById(R.id.camera_surface_view);
        this.cameraPreview.assignCamera(deviceCamera);
        CameraManager.getInstance().setCameraPreview(this.cameraPreview);

        DrawingView drawingView = (DrawingView) this.findViewById(R.id.camera_drawing_view);
        this.cameraPreview.assignDrawingView(drawingView);

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
        CameraManager.getInstance().closeCamera();
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
                Camera deviceCamera = CameraManager.getInstance().swapCamera();
                ImageCaptureActivity.this.cameraPreview.updateCameraSource(deviceCamera);
            }
        });

        ImageButton captureBtn = (ImageButton) this.findViewById(R.id.btn_capture);
        captureBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                CameraManager.getInstance().capture();
            }
        });
    }

}
