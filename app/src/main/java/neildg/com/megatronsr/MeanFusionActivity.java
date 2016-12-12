package neildg.com.megatronsr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import neildg.com.megatronsr.io.DirectoryStorage;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

public class MeanFusionActivity extends AppCompatActivity {
    private final static String TAG = "MeanFusionActivity";

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV reloaded for mean fusion processing");
                    MeanFusionActivity.this.onSuccessInitialize();
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
        setContentView(R.layout.activity_mean_fusion);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    @Override
    protected void onDestroy() {
        ProgressDialogHandler.destroy();
        FileImageReader.destroy();
        super.onDestroy();
    }

    private void onSuccessInitialize() {
        ProgressDialogHandler.initialize(this);
        DirectoryStorage.getSharedInstance().refreshProposedPath();
        FileImageWriter.initialize(this);
        FileImageReader.initialize(this);

        System.gc();
        this.performMeanFusion();
    }

    private void performMeanFusion() {
        /*ProgressDialogHandler.getInstance().showUserDialog("Processing", "Fusing images");

        int numImages = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.WARPED_IMAGES_LENGTH_KEY, 0);
        Mat[] warpedMatList = new Mat[numImages];

        for(int i = 0; i < numImages; i++) {
            warpedMatList[i] = ImageReader.getInstance().imReadOpenCV("warp_"+i, ImageFileAttribute.FileType.JPEG);
        }

        MeanFusionOperator fusionOperator = new MeanFusionOperator(warpedMatList, "Fusing", "Fusing images using mean");
        fusionOperator.perform();
        ImageWriter.getInstance().saveMatrixToImage(fusionOperator.getResult(), "rgb_merged", ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().hideUserDialog();*/
    }

}
