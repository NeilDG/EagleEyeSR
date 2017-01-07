package neildg.com.eagleeyesr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;

import net.sourceforge.opencamera.OpenCameraActivity;

import org.opencv.android.*;

import java.io.File;
import java.util.ArrayList;

import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.DirectoryStorage;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.model.AttributeHolder;
import neildg.com.eagleeyesr.platformtools.core_application.ApplicationCore;
import neildg.com.eagleeyesr.io.BitmapURIRepository;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;
import neildg.com.eagleeyesr.ui.views.InfoScreen;

public class MainActivity extends AppCompatActivity{

    private final static String TAG = "MainActivity";

    private boolean hasCamera = true;

    private int REQUEST_PICTURE_EXTERNAL = 1;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("opencv_bridge");
    }

    private native String hello();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully " +MainActivity.this.hello());
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private InfoScreen infoScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        this.getSupportActionBar().hide();

        ApplicationCore.initialize(this);
        ProgressDialogHandler.initialize(this);
        DirectoryStorage.getSharedInstance().createDirectory();
        FileImageWriter.initialize(this);
        FileImageReader.initialize(this);
        ParameterConfig.initialize(this);
        AttributeHolder.initialize(this);

        this.infoScreen = new InfoScreen(this.findViewById(R.id.overlay_intro_view));
        this.infoScreen.initialize();

        this.verifyCamera();
        this.initializeButtons();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        ProgressDialogHandler.destroy();
        FileImageWriter.destroy();
        FileImageReader.destroy();
        super.onDestroy();
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
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MainActivity.this, NewCameraActivity.class);
                startActivity(cameraIntent);
            }
        });

        Button captureExternalBtn = (Button) this.findViewById(R.id.capture_external_btn);
        captureExternalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_PICTURE_EXTERNAL);
                }
            }
        });

        Button pickImagesBtn = (Button) this.findViewById(R.id.select_image_btn);
        pickImagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            MainActivity.this.startImagePickActivity();
            }
        });

        RadioGroup scaleRadioGroup = (RadioGroup) this.findViewById(R.id.scale_radio_group);
        scaleRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.scale_1_btn) {
                    //Toast.makeText(MainActivity.this, "No scaling applied", Toast.LENGTH_SHORT).show();
                    ParameterConfig.setScalingFactor(1);
                }
                else if(checkedId == R.id.scale_2_btn) {
                    //Toast.makeText(MainActivity.this, "2x scale", Toast.LENGTH_SHORT).show();
                    ParameterConfig.setScalingFactor(2);
                }
                else if(checkedId == R.id.scale_4_btn) {
                    //Toast.makeText(MainActivity.this, "4x scale", Toast.LENGTH_SHORT).show();
                    //ParameterConfig.setScalingFactor(4);
                }
            }
        });



        RadioGroup techniqueRadioGroup = (RadioGroup) this.findViewById(R.id.technique_radio_group);
        techniqueRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.single_sr_btn) {
                    ParameterConfig.setTechnique(ParameterConfig.SRTechnique.SINGLE);
                    Toast.makeText(MainActivity.this, "Single-Image SR selected", Toast.LENGTH_SHORT).show();
                }
                else if(checkedId == R.id.multiple_sr_btn) {
                    ParameterConfig.setTechnique(ParameterConfig.SRTechnique.MULTIPLE);
                    Toast.makeText(MainActivity.this, "Multiple-Image SR selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        scaleRadioGroup.check(R.id.scale_2_btn); ParameterConfig.setScalingFactor(2);
        RadioButton scale4Btn = (RadioButton) scaleRadioGroup.findViewById(R.id.scale_4_btn);

        //TODO: temporarily disable functionalities
        scale4Btn.setEnabled(false);
        //captureImageBtn.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //The array list has the image paths of the selected images
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            ArrayList<Uri> imageURIList = new ArrayList<>();
            for(int i = 0; i < images.size(); i++) {
                imageURIList.add(Uri.fromFile(new File(images.get(i).path)));
            }

            if(ParameterConfig.getCurrentTechnique() == ParameterConfig.SRTechnique.MULTIPLE && imageURIList.size() > 1) {
                Log.v("LOG_TAG", "Selected Images " + imageURIList.size());
                BitmapURIRepository.getInstance().setImageURIList(imageURIList);
                this.moveToProcessingActivity();
            }
            else if(ParameterConfig.getCurrentTechnique() == ParameterConfig.SRTechnique.MULTIPLE && imageURIList.size() < 1) {
                Toast.makeText(this, "You haven't picked enough images. Pick multiple similar images.",
                        Toast.LENGTH_LONG).show();
            }
        }

        else if(requestCode == REQUEST_PICTURE_EXTERNAL) {
            //if user attempted to use  external camera app, just launch the pick image gallery after taking pictures.
            Log.v(TAG, "Moving to select image activity.");
            this.startImagePickActivity();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void moveToProcessingActivity() {
        Intent processingIntent = new Intent(MainActivity.this, ProcessingActivityRelease.class);
        this.startActivity(processingIntent);
    }

    private void startImagePickActivity() {
        Intent intent = new Intent(MainActivity.this, AlbumSelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 40);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

}
