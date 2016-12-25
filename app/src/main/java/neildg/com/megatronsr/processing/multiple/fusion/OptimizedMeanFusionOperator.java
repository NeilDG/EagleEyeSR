package neildg.com.megatronsr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Experiment on new proposed method for performing mean fusion for images that have been warped and interpolated.
 * Created by NeilDG on 12/9/2016.
 */

public class OptimizedMeanFusionOperator implements IOperator {
    private final static String TAG  = "OptimizedFusionOperator";

    private String[] imageMatPathList;
    private Mat outputMat;


    public OptimizedMeanFusionOperator(String[] imageMatPathList) {
        this.imageMatPathList = imageMatPathList;
    }

    @Override
    public void perform() {

        int scale = ParameterConfig.getScalingFactor();
        this.outputMat = new Mat();
        Mat initialMat = FileImageReader.getInstance().imReadOpenCV(this.imageMatPathList[0], ImageFileAttribute.FileType.JPEG);
        initialMat.convertTo(initialMat, CvType.CV_16UC(initialMat.channels())); //convert to CV_16UC
        Log.d(TAG, "Initial image for fusion: "+this.imageMatPathList[0]+ " Size:" +initialMat.size() + " Scale: " +scale);

        Mat sumMat = ImageOperator.performInterpolation(initialMat, scale, Imgproc.INTER_CUBIC); //perform cubic interpolation for initial HR
        //sumMat.convertTo(this.outputMat, CvType.CV_8UC(sumMat.channels()));
        //FileImageWriter.getInstance().saveMatrixToImage(this.outputMat, FilenameConstants.HR_ITERATION_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);
        initialMat.release();
        this.outputMat.release();

        for(int i = 1; i < this.imageMatPathList.length; i++) {
            //load second mat
            initialMat = FileImageReader.getInstance().imReadOpenCV(this.imageMatPathList[i], ImageFileAttribute.FileType.JPEG);

            ProgressDialogHandler.getInstance().updateProgress(ProgressDialogHandler.getInstance().getProgress() + 5.0f);

            //perform interpolation
            initialMat = ImageOperator.performInterpolation(initialMat, scale, Imgproc.INTER_CUBIC); //perform cubic interpolation
            Mat maskMat = ImageOperator.produceMask(initialMat);

            Core.add(sumMat, initialMat, sumMat, maskMat, CvType.CV_16UC(initialMat.channels()));
            Core.divide(sumMat, Scalar.all(2), sumMat);

            Log.d(TAG, "sumMat size: " +sumMat.size().toString());
            //sumMat.convertTo(this.outputMat, CvType.CV_8UC(sumMat.channels()));
            //FileImageWriter.getInstance().saveMatrixToImage(this.outputMat, FilenameConstants.HR_ITERATION_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);

            initialMat.release();
            maskMat.release();
        }

        sumMat.convertTo(this.outputMat, CvType.CV_8UC(sumMat.channels()));
        sumMat.release();
    }



    public Mat getResult() {
        return this.outputMat;
    }
}
