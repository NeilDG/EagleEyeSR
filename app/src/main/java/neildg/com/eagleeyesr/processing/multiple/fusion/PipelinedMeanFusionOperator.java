package neildg.com.eagleeyesr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;

/**
 * Mean fusion operator solely for the pipeline manager
 * Created by NeilDG on 12/27/2016.
 */

public class PipelinedMeanFusionOperator implements IOperator {
    private final static String TAG ="PipelinedFusionOperator";

    private String initialHRName;
    private String candidateImageName;

    private Mat outputMat;

    public PipelinedMeanFusionOperator(String initialHRName, String candidateImageName) {
        this.initialHRName = initialHRName;
        this.candidateImageName = candidateImageName;
    }

    @Override
    public void perform() {
        int scale = ParameterConfig.getScalingFactor();
        this.outputMat = new Mat();
        Mat initialHRMat = FileImageReader.getInstance().imReadOpenCV(this.initialHRName, ImageFileAttribute.FileType.JPEG);
        initialHRMat.convertTo(initialHRMat, CvType.CV_16UC(initialHRMat.channels())); //convert to CV_16UC
        Mat candidateMat = FileImageReader.getInstance().imReadOpenCV(this.candidateImageName, ImageFileAttribute.FileType.JPEG);
        candidateMat = ImageOperator.performInterpolation(candidateMat, scale, Imgproc.INTER_LINEAR); //perform cubic interpolation for the candidate mat

        Mat maskMat = ImageOperator.produceMask(candidateMat);

        Core.add(initialHRMat, candidateMat, initialHRMat, maskMat, CvType.CV_16UC(initialHRMat.channels()));
        Core.divide(initialHRMat, Scalar.all(2), initialHRMat);

        Log.d(TAG, "Size: " +initialHRMat.size().toString());

        initialHRMat.convertTo(this.outputMat, CvType.CV_8UC(initialHRMat.channels()));
        initialHRMat.release();
        maskMat.release();
        candidateMat.release();
    }

    public Mat getResult() {
        return this.outputMat;
    }
}
