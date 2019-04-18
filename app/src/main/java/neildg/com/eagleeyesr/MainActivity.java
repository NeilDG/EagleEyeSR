package neildg.com.eagleeyesr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;

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
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;
import neildg.com.eagleeyesr.ui.views.AboutScreen;
import neildg.com.eagleeyesr.ui.views.InfoScreen;

public class MainActivity extends AppCompatActivity{

    private final static String TAG = "MainActivity";

    private boolean hasCamera = true;

    private int REQUEST_PICTURE_EXTERNAL = 1;
    private final int PERMISSION_WRITE_EXTERNAL_STORAGE = 2;
    private final int PERMISSION_READ_EXTERNAL_STORAGE = 3;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("opencv_bridge");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully!");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private InfoScreen infoScreen;
    private AboutScreen aboutScreen;

    //private boolean readGranted = false;
    private boolean writeGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        this.getSupportActionBar().hide();

        ApplicationCore.initialize(this);
        ProgressDialogHandler.initialize(this);
        ParameterConfig.initialize(this);
        AttributeHolder.initialize(this);

        this.infoScreen = new InfoScreen(this.findViewById(R.id.overlay_intro_view));
        this.infoScreen.initialize();

        this.aboutScreen = new AboutScreen(this.findViewById(R.id.overlay_about_view));
        this.aboutScreen.initialize();
        this.aboutScreen.hide();

        this.verifyCamera();
        this.initializeButtons();
        this.requestPermission();
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ProgressDialogHandler.destroy();
        FileImageWriter.destroy();
        FileImageReader.destroy();
        super.onDestroy();
    }

    private void requestPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);

        }
        else {
            this.writeGranted = true;
        }

        /*if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ_EXTERNAL_STORAGE);
        }
        else {
            this.readGranted = true;
        }*/


        if(this.writeGranted) {
            // Permission has already been granted
            DirectoryStorage.getSharedInstance().createDirectory();
            FileImageWriter.initialize(this);
            FileImageReader.initialize(this);

            Button captureImageBtn = (Button) this.findViewById(R.id.capture_btn);
            captureImageBtn.setEnabled(true);

            Button pickImagesBtn = (Button) this.findViewById(R.id.select_image_btn);
            pickImagesBtn.setEnabled(true);

            Button grantPermissionBtn = (Button) this.findViewById(R.id.button_grant_permission);
            grantPermissionBtn.setVisibility(Button.INVISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.writeGranted = true;
                }
                break;
            }
        }

        if(this.writeGranted) {
            DirectoryStorage.getSharedInstance().createDirectory();
            FileImageWriter.initialize(this);
            FileImageReader.initialize(this);

            Button captureImageBtn = (Button) this.findViewById(R.id.capture_btn);
            captureImageBtn.setEnabled(true);

            Button pickImagesBtn = (Button) this.findViewById(R.id.select_image_btn);
            pickImagesBtn.setEnabled(true);

            Button grantPermissionBtn = (Button) this.findViewById(R.id.button_grant_permission);
            grantPermissionBtn.setVisibility(Button.INVISIBLE);
        }
        else {
            Toast.makeText(this, "Eagle-Eye needs to read and write temporary images for processing to your storage.", Toast.LENGTH_LONG)
                    .show();

            Button captureImageBtn = (Button) this.findViewById(R.id.capture_btn);
            captureImageBtn.setEnabled(false);

            Button pickImagesBtn = (Button) this.findViewById(R.id.select_image_btn);
            pickImagesBtn.setEnabled(false);

            Button grantPermissionBtn = (Button) this.findViewById(R.id.button_grant_permission);
            grantPermissionBtn.setVisibility(Button.VISIBLE);
        }
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

        Button grantPermissionBtn = (Button) this.findViewById(R.id.button_grant_permission);
        grantPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.requestPermission();
            }
        });

        /*Button captureExternalBtn = (Button) this.findViewById(R.id.capture_external_btn);
        captureExternalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_PICTURE_EXTERNAL);
                }
            }
        });*/

        Button pickImagesBtn = (Button) this.findViewById(R.id.select_image_btn);
        pickImagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            MainActivity.this.startImagePickActivity();
            }
        });

        final ImageButton infoBtn = (ImageButton) this.findViewById(R.id.about_btn);
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.aboutScreen.show();
            }
        });


        /*RadioGroup scaleRadioGroup = (RadioGroup) this.findViewById(R.id.scale_radio_group);
        scaleRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.scale_2_btn) {
                    //Toast.makeText(MainActivity.this, "2x scale", Toast.LENGTH_SHORT).show();
                    ParameterConfig.setScalingFactor(2);
                }
                else if(checkedId == R.id.scale_4_btn) {
                    //Toast.makeText(MainActivity.this, "4x scale", Toast.LENGTH_SHORT).show();
                    //ParameterConfig.setScalingFactor(4);
                }
            }
        });*/



        /*RadioGroup techniqueRadioGroup = (RadioGroup) this.findViewById(R.id.technique_radio_group);
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
        });*/

        //scaleRadioGroup.check(R.id.scale_2_btn); ParameterConfig.setScalingFactor(2);
        //RadioButton scale4Btn = (RadioButton) scaleRadioGroup.findViewById(R.id.scale_4_btn);

        //TODO: temporarily disable functionalities
        //scale4Btn.setEnabled(false);
        //captureImageBtn.setEnabled(false);

        ParameterConfig.setScalingFactor(2);
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

            if(ParameterConfig.getCurrentTechnique() == ParameterConfig.SRTechnique.MULTIPLE && imageURIList.size() >= 3) {
                Log.v("LOG_TAG", "Selected Images " + imageURIList.size());
                BitmapURIRepository.getInstance().setImageURIList(imageURIList);
                this.moveToProcessingActivity();
            }
            else if(ParameterConfig.getCurrentTechnique() == ParameterConfig.SRTechnique.MULTIPLE && imageURIList.size() < 3) {
                Toast.makeText(this, "You haven't picked enough images. Pick multiple similar images. At least 3.",
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
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

}
