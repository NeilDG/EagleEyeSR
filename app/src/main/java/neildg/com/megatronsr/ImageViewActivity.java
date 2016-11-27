package neildg.com.megatronsr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.ImageFileAttribute;

public class ImageViewActivity extends AppCompatActivity {
    private final static String TAG = "ImageViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.loadImageView();
    }

    private void loadImageView() {
        SubsamplingScaleImageView scaleImageView = (SubsamplingScaleImageView) this.findViewById(R.id.subscale_image_view);
        String imageSource = FileImageReader.getInstance().getDecodedFilePath(FilenameConstants.HR_PROCESSED_STRING, ImageFileAttribute.FileType.JPEG);
        scaleImageView.setImage(ImageSource.uri(imageSource));
    }
}
