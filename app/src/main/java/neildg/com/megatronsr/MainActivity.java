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

import org.opencv.android.*;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.constants.BuildMode;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.platformtools.utils.ApplicationCore;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

public class MainActivity extends AppCompatActivity{

    private final static String TAG = "MainActivity";

    private boolean hasCamera = true;

    private int PICK_IMAGE_MULTIPLE = 1;
    private String imageEncoded;
    private List<String> imagesEncodedList;

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

        this.getSupportActionBar().hide();

        ApplicationCore.initialize(this);
        ProgressDialogHandler.initialize(this);
        ImageWriter.initialize(this);
        ImageReader.initialize(this);

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
        captureImageBtn.setEnabled(false); //TODO:disable capture image btn

        Button pickImagesBtn = (Button) this.findViewById(R.id.select_image_btn);
        pickImagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
            }
        });



        RadioGroup scaleRadioGroup = (RadioGroup) this.findViewById(R.id.scale_radio_group);
        ParameterConfig.setScalingFactor(4);
        scaleRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.scale_1_btn) {
                    Toast.makeText(MainActivity.this, "Scale 1", Toast.LENGTH_SHORT).show();
                    ParameterConfig.setScalingFactor(1);
                }
                else if(checkedId == R.id.scale_2_btn) {
                    Toast.makeText(MainActivity.this, "Scale 2", Toast.LENGTH_SHORT).show();
                    ParameterConfig.setScalingFactor(2);
                }
                else if(checkedId == R.id.scale_4_btn) {
                    Toast.makeText(MainActivity.this, "Scale 4", Toast.LENGTH_SHORT).show();
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();
                if(data.getData()!=null){

                    Uri mImageUri=data.getData();

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);
                    cursor.close();

                    if(ParameterConfig.getCurrentTechnique() == ParameterConfig.SRTechnique.MULTIPLE) {
                        Toast.makeText(this, "Technique needs multiple images. Tap and hold to pick multiple images.",
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        mArrayUri.add(mImageUri);
                        BitmapURIRepository.getInstance().setImageURIList(mArrayUri);
                        this.moveToProcessingActivity();
                    }


                }else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            // Get the cursor
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();

                        }

                        if(ParameterConfig.getCurrentTechnique() == ParameterConfig.SRTechnique.MULTIPLE) {
                            Log.v("LOG_TAG", "Selected Images " + mArrayUri.size());
                            BitmapURIRepository.getInstance().setImageURIList(mArrayUri);
                            this.moveToProcessingActivity();
                        }
                        else {
                            Toast.makeText(this, "Technique only requires a single image. Click an image to select.",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void moveToProcessingActivity() {

        if(BuildMode.DEVELOPMENT_BUILD == true) {
            Intent processingIntent = new Intent(MainActivity.this, ProcessingActivityDebug.class);
            this.startActivity(processingIntent);
        }
        else {
            Intent processingIntent = new Intent(MainActivity.this, ProcessingActivityRelease.class);
            this.startActivity(processingIntent);
        }

    }

}
