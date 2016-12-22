package neildg.com.megatronsr;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;

import org.opencv.android.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.constants.BuildMode;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.DirectoryStorage;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.platformtools.core_application.ApplicationCore;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

public class MainActivity extends AppCompatActivity{

    private final static String TAG = "MainActivity";

    private boolean hasCamera = true;

    private int PICK_IMAGE_MULTIPLE = 1;
    private String imageEncoded;
    private List<String> imagesEncodedList;

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
                Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(cameraIntent);
            }
        });

        Button pickImagesBtn = (Button) this.findViewById(R.id.select_image_btn);
        pickImagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);*/

                Intent intent = new Intent(MainActivity.this, AlbumSelectActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10);
                startActivityForResult(intent, Constants.REQUEST_CODE);
            }
        });

        RadioGroup scaleRadioGroup = (RadioGroup) this.findViewById(R.id.scale_radio_group);

        scaleRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.scale_1_btn) {
                    Toast.makeText(MainActivity.this, "No scaling applied", Toast.LENGTH_SHORT).show();
                    ParameterConfig.setScalingFactor(1);
                }
                else if(checkedId == R.id.scale_2_btn) {
                    Toast.makeText(MainActivity.this, "2x scale", Toast.LENGTH_SHORT).show();
                    ParameterConfig.setScalingFactor(2);
                }
                else if(checkedId == R.id.scale_4_btn) {
                    Toast.makeText(MainActivity.this, "4x scale", Toast.LENGTH_SHORT).show();
                    ParameterConfig.setScalingFactor(4);
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

        scaleRadioGroup.check(R.id.scale_4_btn); ParameterConfig.setScalingFactor(4);
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


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void moveToProcessingActivity() {

        /*if(BuildMode.DEVELOPMENT_BUILD == true) {
            Intent processingIntent = new Intent(MainActivity.this, ProcessingActivityDebug.class);
            this.startActivity(processingIntent);
        }
        else {
            Intent processingIntent = new Intent(MainActivity.this, ProcessingActivityRelease.class);
            this.startActivity(processingIntent);
        }*/
        Intent processingIntent = new Intent(MainActivity.this, ProcessingActivityRelease.class);
        this.startActivity(processingIntent);
    }

}
