package neildg.com.megatronsr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.AlignExposures;
import org.opencv.photo.AlignMTB;
import org.opencv.photo.CalibrateDebevec;
import org.opencv.photo.MergeDebevec;
import org.opencv.photo.MergeMertens;
import org.opencv.photo.Photo;
import org.opencv.photo.TonemapDurand;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Experiment with HDR fusion
 * Created by NeilDG on 7/3/2016.
 */
public class HDRFusionOperator implements IOperator {
    private final static String TAG = "HDRFusionOperator";

    private List<Mat> processMatList = new ArrayList<>();

    private Mat originalMat;
    private Mat[] warpedMatList;

    public HDRFusionOperator(Mat originalMat, Mat[] warpedMatList) {
        this.originalMat = originalMat;
        this.warpedMatList = warpedMatList;
    }

    @Override
    public void perform() {

        ProgressDialogHandler.getInstance().showDialog("Resizing images", "Performing image resizing");

        this.processMatList.add(ImageOperator.performInterpolation(this.originalMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC));
        this.originalMat.release();

        for(int i = 0; i < this.warpedMatList.length; i++) {
            this.processMatList.add(ImageOperator.performInterpolation(this.warpedMatList[i], ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC));
            this.warpedMatList[i].release();
        }

        ProgressDialogHandler.getInstance().showDialog("Fusing images", "Fusing images");

        AlignMTB aligner = Photo.createAlignMTB();
        aligner.process(this.processMatList, this.processMatList);

        Mat fusionMat = new Mat();
        MergeMertens fusionMerger = Photo.createMergeMertens();
        fusionMerger.process(this.processMatList, fusionMat);

        Mat multMat = new Mat(fusionMat.size(), fusionMat.type(), Scalar.all(255));
        fusionMat = fusionMat.mul(multMat);
        fusionMat.convertTo(fusionMat, CvType.CV_8UC3);
        Log.d(TAG, "FusionMap type: " + CvType.typeToString(fusionMat.type()));

        ImageWriter.getInstance().saveMatrixToImage(fusionMat, "exposure_fusion", ImageFileAttribute.FileType.JPEG);

        fusionMat.release();
        multMat.release();

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
