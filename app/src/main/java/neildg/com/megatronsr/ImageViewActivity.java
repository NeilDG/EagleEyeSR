package neildg.com.megatronsr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.ImageFileAttribute;

public class ImageViewActivity extends AppCompatActivity {
    private final static String TAG = "ImageViewActivity";

    private enum ImageViewType {
        NEAREST,
        LINEAR,
        CUBIC,
        SUPER_RES
    }

    private SubsamplingScaleImageView nearestView;
    private SubsamplingScaleImageView linearView;
    private SubsamplingScaleImageView cubicView;
    private SubsamplingScaleImageView srView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.loadImageView();
        this.setupRadioButtons();
        this.setImageViewType(ImageViewType.CUBIC);
    }

    private void loadImageView() {

        this.nearestView = (SubsamplingScaleImageView) this.findViewById(R.id.nearest_image_view);
        String imageSource = FileImageReader.getInstance().getDecodedFilePath(FilenameConstants.HR_NEAREST, ImageFileAttribute.FileType.JPEG);
        this.nearestView.setImage(ImageSource.uri(imageSource));
        this.nearestView.setRotation(180.0f);

        this.linearView = (SubsamplingScaleImageView) this.findViewById(R.id.linear_image_view);
        imageSource = FileImageReader.getInstance().getDecodedFilePath(FilenameConstants.HR_LINEAR, ImageFileAttribute.FileType.JPEG);
        this.linearView.setImage(ImageSource.uri(imageSource));
        this.linearView.setRotation(180.0f);

        this.cubicView = (SubsamplingScaleImageView) this.findViewById(R.id.cubic_image_view);
        imageSource = FileImageReader.getInstance().getDecodedFilePath(FilenameConstants.HR_CUBIC, ImageFileAttribute.FileType.JPEG);
        this.cubicView.setImage(ImageSource.uri(imageSource));
        this.cubicView.setRotation(180.0f);

        this.srView = (SubsamplingScaleImageView) this.findViewById(R.id.sr_image_view);
        this.srView.setVisibility(View.GONE); //temporary
    }

    private void setupRadioButtons() {
        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.image_view_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.nearest_radio_btn) {
                    ImageViewActivity.this.setImageViewType(ImageViewType.NEAREST);
                }
                else if(checkedId == R.id.linear_radio_btn) {
                    ImageViewActivity.this.setImageViewType(ImageViewType.LINEAR);
                }
                else if(checkedId == R.id.cubic_radio_btn) {
                    ImageViewActivity.this.setImageViewType(ImageViewType.CUBIC);
                }
                else if(checkedId == R.id.sr_radio_btn) {
                    //TODO;
                }
            }
        });

        RadioButton nearestBtn = (RadioButton) radioGroup.findViewById(R.id.nearest_radio_btn);
        RadioButton linearBtn = (RadioButton) radioGroup.findViewById(R.id.linear_radio_btn);
        RadioButton cubicBtn = (RadioButton) radioGroup.findViewById(R.id.cubic_radio_btn);

        if(FileImageReader.getInstance().doesImageExists(FilenameConstants.HR_NEAREST, ImageFileAttribute.FileType.JPEG)) {
            nearestBtn.setEnabled(true);
        }
        else {
            nearestBtn.setEnabled(false);
        }

        if(FileImageReader.getInstance().doesImageExists(FilenameConstants.HR_LINEAR, ImageFileAttribute.FileType.JPEG)) {
            linearBtn.setEnabled(true);
        }
        else {
            linearBtn.setEnabled(false);
        }

        if(FileImageReader.getInstance().doesImageExists(FilenameConstants.HR_CUBIC, ImageFileAttribute.FileType.JPEG)) {
            cubicBtn.setEnabled(true);
        }
        else {
            cubicBtn.setEnabled(false);
        }

    }

    private void setImageViewType(ImageViewType imageViewType) {
        if(imageViewType == ImageViewType.NEAREST) {
            this.nearestView.setVisibility(View.VISIBLE);
            this.linearView.setVisibility(View.INVISIBLE);
            this.cubicView.setVisibility(View.INVISIBLE);
        }
        else if(imageViewType == ImageViewType.LINEAR) {
            this.nearestView.setVisibility(View.INVISIBLE);
            this.linearView.setVisibility(View.VISIBLE);
            this.cubicView.setVisibility(View.INVISIBLE);
        }
        else if(imageViewType == ImageViewType.CUBIC){
            this.nearestView.setVisibility(View.INVISIBLE);
            this.linearView.setVisibility(View.INVISIBLE);
            this.cubicView.setVisibility(View.VISIBLE);
        }
    }
}
