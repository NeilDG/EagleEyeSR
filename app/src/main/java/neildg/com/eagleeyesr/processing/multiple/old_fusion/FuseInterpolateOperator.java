package neildg.com.eagleeyesr.processing.multiple.old_fusion;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.MetricsLogger;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ImageMeasures;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;

/**
 * Fuses warp images together in the reference LR frame, and then performs interpolation.
 * EXPERIMENTAL
 * Created by NeilDG on 5/23/2016.
 */
public class FuseInterpolateOperator implements IOperator {
    private final static String TAG = "FuseInterpolateOperator";

    private List<Mat> warpedMatrixList = null;

    private Mat groundTruthMat;
    private Mat fusedMat;
    private Mat outputMat;

    public FuseInterpolateOperator(List<Mat> warpedMatrixList) {
        this.warpedMatrixList = warpedMatrixList;
    }

    @Override
    public void perform() {
       this.loadImages();
        this.fuseImages();
    }

    private void loadImages() {
        this.groundTruthMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.GROUND_TRUTH_PREFIX_STRING, ImageFileAttribute.FileType.JPEG);
        this.fusedMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);

        Mat initialHRMat= FileImageReader.getInstance().imReadOpenCV(FilenameConstants.HR_CUBIC + 0, ImageFileAttribute.FileType.JPEG);
        Mat nearestMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.HR_NEAREST, ImageFileAttribute.FileType.JPEG);
        MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_nearest", this.groundTruthMat, "GroundTruth", nearestMat,
                "NearestMat", "Ground truth vs Nearest");

        MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_initial_hr", this.groundTruthMat, "GroundTruth", initialHRMat,
                "InterCubicHR", "Ground truth vs Intercubic");
    }

    public void fuseImages() {

        double threshold = Double.MAX_VALUE;
        List<Mat> blendMatList = new LinkedList<>();
        blendMatList.add(this.fusedMat);

        for(int i = 0; i < this.warpedMatrixList.size(); i++) {
            Mat baseWarpMat = this.warpedMatrixList.get(i);
            Mat maskMat = ImageOperator.produceMask(baseWarpMat);

            double newRMSE = ImageMeasures.measureRMSENoise(baseWarpMat);
            if(newRMSE <= threshold) {
                threshold = newRMSE;
                blendMatList.add(baseWarpMat);
            }
        }

        this.fusedMat = ImageOperator.blendImages(blendMatList);
        this.outputMat = Mat.zeros(this.fusedMat.rows() * ParameterConfig.getScalingFactor(), this.fusedMat.cols() * ParameterConfig.getScalingFactor(), this.fusedMat.type());

        Imgproc.resize(this.fusedMat, this.outputMat, this.outputMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        FileImageWriter.getInstance().saveMatrixToImage(this.outputMat, FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG);

        /*MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_result", this.groundTruthMat, "GroundTruth", this.fusedMat,
                "FusedHR", "Ground truth vs Result");*/
        MetricsLogger.getSharedInstance().debugPSNRTable();
        MetricsLogger.getSharedInstance().logResultsToJSON(FilenameConstants.METRICS_NAME_STRING);
    }

    public Mat getOutputMat() {
        return this.outputMat;
    }
}
