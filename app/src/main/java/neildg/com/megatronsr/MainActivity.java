package neildg.com.megatronsr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.*;

import neildg.com.megatronsr.camera.OldCameraManager;
import neildg.com.megatronsr.platformtools.utils.ApplicationCore;

public class MainActivity extends AppCompatActivity{

    private final static String TAG = "MainActivity";

    private boolean hasCamera = true;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        ApplicationCore.initialize(this);
        OldCameraManager.initialize();

        this.verifyCamera();
        this.initializeButtons();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    private void verifyCamera() {
        PackageManager packageManager = ApplicationCore.getInstance().getAppContext().getPackageManager();
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false){
            Toast.makeText(this, "This device does not have a camera.", Toast.LENGTH_SHORT)
                    .show();

            this.hasCamera = false;
        }
    }
    private void initializeButtons() {
        Button captureImageBtn = (Button) this.findViewById(R.id.capture_btn);
        captureImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.this.hasCamera) {
                    Intent imageCaptureIntent = new Intent(MainActivity.this, ImageCaptureActivity.class);
                    startActivity(imageCaptureIntent);
                }
            }
        });
    }

}
