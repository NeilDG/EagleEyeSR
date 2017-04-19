package neildg.com.eagleeyesr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.ImageInputMap;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;

/**
 * Experiment on new proposed method for performing mean fusion for images that have been warped and interpolated.
 * Created by NeilDG on 12/9/2016.
 */

public class MeanFusionOperator implements IOperator {
    private final static String TAG  = "OptimizedFusionOperator";

    private String[] imageMatPathList;
    private Mat outputMat;
    private Mat initialMat;


    public MeanFusionOperator(Mat initialMat, String[] imageMatPathList) {
        this.imageMatPathList = imageMatPathList;
        this.initialMat = initialMat;
    }

    @Override
    public void perform() {

        ImageInputMap.deletePlaceholderImages(); //delete acquired images if camera app was used.

        int scale = ParameterConfig.getScalingFactor();
        this.outputMat = new Mat();
        this.initialMat.convertTo(initialMat, CvType.CV_16UC(initialMat.channels())); //convert to CV_16UC
        Log.d(TAG, "Initial image for fusion Size:" +initialMat.size() + " Scale: " +scale);

        Mat sumMat = ImageOperator.performInterpolation(initialMat, scale, Imgproc.INTER_LINEAR); //perform linear interpolation for initial HR
        //sumMat.convertTo(this.outputMat, CvType.CV_8UC(sumMat.channels()));
        //FileImageWriter.getInstance().saveMatrixToImage(this.outputMat, FilenameConstants.HR_ITERATION_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);
        this.initialMat.release();
        this.outputMat.release();

        for(int i = 0; i < this.imageMatPathList.length; i++) {
            //load second mat
            this.initialMat = FileImageReader.getInstance().imReadOpenCV(this.imageMatPathList[i], ImageFileAttribute.FileType.JPEG);
            //delete file. no longer needed.
            FileImageWriter.getInstance().deleteImage(this.imageMatPathList[i], ImageFileAttribute.FileType.JPEG);

            Log.d(TAG, "Initial image for fusion. Name: "+this.imageMatPathList[i] + " Size:" +this.initialMat.size() + " Scale: " +scale);

            ProgressDialogHandler.getInstance().updateProgress(ProgressDialogHandler.getInstance().getProgress() + 5.0f);

            //perform interpolation
            this.initialMat = ImageOperator.performInterpolation(this.initialMat, scale, Imgproc.INTER_CUBIC); //perform cubic interpolation
            Mat maskMat = ImageOperator.produceMask(this.initialMat);

            Core.add(sumMat, this.initialMat, sumMat, maskMat, CvType.CV_16UC(this.initialMat.channels()));
            //Core.divide(sumMat, Scalar.all(2), sumMat);

            Log.d(TAG, "sumMat size: " +sumMat.size().toString());
            //sumMat.convertTo(this.outputMat, CvType.CV_8UC(sumMat.channels()));
            //FileImageWriter.getInstance().saveMatrixToImage(this.outputMat, FilenameConstants.HR_ITERATION_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);

            this.initialMat.release();
            maskMat.release();
        }

        Core.divide(sumMat, Scalar.all(this.imageMatPathList.length + 1), sumMat);

        sumMat.convertTo(this.outputMat, CvType.CV_8UC(sumMat.channels()));
        sumMat.release();
    }



    public Mat getResult() {
        return this.outputMat;
    }
}
