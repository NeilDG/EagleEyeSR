package neildg.com.eagleeyesr;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;
import neildg.com.eagleeyesr.ui.views.ImageProgressScreen;

public class ImageViewActivity extends AppCompatActivity {
    private final static String TAG = "ImageViewActivity";

    private enum ImageViewType {
        INTERPOLATED,
        SUPER_RES
    }

    private SubsamplingScaleImageView interpolateView;
    private SubsamplingScaleImageView srView;

    private ImageProgressScreen imageProgressScreen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        this.imageProgressScreen = new ImageProgressScreen(this.findViewById(R.id.image_progress_view));
        this.imageProgressScreen.initialize();
        this.imageProgressScreen.hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.loadImageView();
        this.setupRadioButtons();
        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.image_view_radio_group);
        RadioButton cubicBtn = (RadioButton) radioGroup.findViewById(R.id.interpolate_radio_btn);
        cubicBtn.setChecked(true);

        ProgressDialogHandler.getInstance().setProgressImplementor(this.imageProgressScreen);
        this.imageProgressScreen.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void loadImageView() {
        String imageSource = FileImageReader.getInstance().getDecodedFilePath(FilenameConstants.HR_LINEAR, ImageFileAttribute.FileType.JPEG);
        this.interpolateView = (SubsamplingScaleImageView) this.findViewById(R.id.interpolated_image_view);
        this.interpolateView.setImage(ImageSource.uri(imageSource));

        this.srView = (SubsamplingScaleImageView) this.findViewById(R.id.sr_image_view);
        imageSource = FileImageReader.getInstance().getDecodedFilePath(FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG);
        this.srView.setImage(ImageSource.uri(imageSource));
    }

    private void setupRadioButtons() {
        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.image_view_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.interpolate_radio_btn) {
                    ImageViewActivity.this.setImageViewType(ImageViewType.INTERPOLATED);
                }
                else if(checkedId == R.id.sr_radio_btn) {
                    ImageViewActivity.this.setImageViewType(ImageViewType.SUPER_RES);
                }
            }
        });

        RadioButton interpolateBtn = (RadioButton) radioGroup.findViewById(R.id.interpolate_radio_btn);
        RadioButton srBtn = (RadioButton) radioGroup.findViewById(R.id.sr_radio_btn);

        if(FileImageReader.getInstance().doesImageExists(FilenameConstants.HR_LINEAR, ImageFileAttribute.FileType.JPEG)) {
            interpolateBtn.setEnabled(true);
        }
        else {
            interpolateBtn.setEnabled(false);
        }

        if(FileImageReader.getInstance().doesImageExists(FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG)) {
            srBtn.setEnabled(true);
        }
        else {
            srBtn.setEnabled(false);
        }

    }

    private void setImageViewType(ImageViewType imageViewType) {
        if(imageViewType == ImageViewType.INTERPOLATED) {
            this.interpolateView.setVisibility(View.VISIBLE);
            this.srView.setVisibility(View.INVISIBLE);
        }
        else if(imageViewType == ImageViewType.SUPER_RES) {
            this.interpolateView.setVisibility(View.INVISIBLE);
            this.srView.setVisibility(View.VISIBLE);
        }
    }
}
