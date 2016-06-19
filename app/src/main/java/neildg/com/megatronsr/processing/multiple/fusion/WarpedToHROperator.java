package neildg.com.megatronsr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.MetricsLogger;
import neildg.com.megatronsr.metrics.ImageMetrics;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by neil.dg on 3/10/16.
 */
public class WarpedToHROperator {
    private final static String TAG = "WarpedHROperator";

    private List<Mat> warpedMatrixList = null;

    private Mat groundTruthMat;
    private Mat outputMat;

    private Mat baseMaskMat = new Mat();
    //private double currentPSNR = 0.0;

    private double rmse = 0.0;

    public WarpedToHROperator(List<Mat> warpedMatrixList) {
        this.warpedMatrixList = warpedMatrixList;
    }

    public void perform() {
        this.groundTruthMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.GROUND_TRUTH_PREFIX_STRING, ImageFileAttribute.FileType.JPEG);
        this.outputMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INITIAL_HR_CUBIC + 0, ImageFileAttribute.FileType.JPEG);

        Mat nearestMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INITIAL_HR_NEAREST, ImageFileAttribute.FileType.JPEG);
        MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_nearest", this.groundTruthMat, "GroundTruth", nearestMat,
                "NearestMat", "Ground truth vs Nearest");

        MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_initial_hr", this.groundTruthMat, "GroundTruth", this.outputMat,
                "InterCubicHR", "Ground truth vs Intercubic");

        int scalingFactor = ParameterConfig.getScalingFactor();
        ProgressDialogHandler.getInstance().showDialog("Transforming warped images to HR", "Warping base image");
        Mat baseWarpMat = this.warpedMatrixList.get(0);
        Mat baseHRWarpMat = Mat.zeros(baseWarpMat.rows() * scalingFactor, baseWarpMat.cols() *scalingFactor, baseWarpMat.type());
        this.copyMatToHR(baseWarpMat, baseHRWarpMat, 0, 0);

        baseHRWarpMat.copyTo(this.baseMaskMat);
        Imgproc.cvtColor(this.baseMaskMat, this.baseMaskMat, Imgproc.COLOR_BGR2GRAY);
        baseHRWarpMat.convertTo(this.baseMaskMat, CvType.CV_8UC1);
        Imgproc.threshold(this.baseMaskMat, this.baseMaskMat, 1, 255, Imgproc.THRESH_BINARY);

        Mat medianMat = new Mat();
        Mat candidateHRMat = new Mat(); this.outputMat.copyTo(candidateHRMat);

        baseHRWarpMat.copyTo(candidateHRMat, this.baseMaskMat);
        Imgproc.medianBlur(candidateHRMat, medianMat, 3);

        baseHRWarpMat.copyTo(this.outputMat, this.baseMaskMat); //replace initial output

        ImageWriter.getInstance().saveMatrixToImage(candidateHRMat, "candidate_" + 0, ImageFileAttribute.FileType.JPEG);
        ImageWriter.getInstance().saveMatrixToImage(medianMat, "candidate_median_" + 0, ImageFileAttribute.FileType.JPEG);
        this.rmse = ImageMetrics.getRMSE(candidateHRMat, medianMat);

        MetricsLogger.getSharedInstance().takeMetrics("Noise_evaluate_0", candidateHRMat, "candidate_0", medianMat,
                "candidate_median_filter_0", "Candidate_Vs_Median_RMSE");

        for(int i = 1; i < this.warpedMatrixList.size(); i++) {
            ProgressDialogHandler.getInstance().showDialog("Transforming warped images to HR", "Warping image " +i);
            baseWarpMat = this.warpedMatrixList.get(i);
            Mat.zeros(baseWarpMat.rows() * scalingFactor, baseWarpMat.cols() * scalingFactor, baseWarpMat.type());
            this.copyMatToHR(baseWarpMat, baseHRWarpMat, 0, 0);

            baseHRWarpMat.copyTo(this.baseMaskMat);
            Imgproc.cvtColor(this.baseMaskMat, this.baseMaskMat, Imgproc.COLOR_BGR2GRAY);
            baseHRWarpMat.convertTo(this.baseMaskMat, CvType.CV_8UC1);
            Imgproc.threshold(this.baseMaskMat, this.baseMaskMat, 1, 255, Imgproc.THRESH_BINARY);

            baseHRWarpMat.copyTo(candidateHRMat, this.baseMaskMat); //store on candidate mat to compare PSNR with initial output
            Imgproc.medianBlur(candidateHRMat, medianMat, 3);

            ImageWriter.getInstance().saveMatrixToImage(candidateHRMat, "candidate_" + i, ImageFileAttribute.FileType.JPEG);
            ImageWriter.getInstance().saveMatrixToImage(medianMat, "candidate_median_" + i, ImageFileAttribute.FileType.JPEG);

            MetricsLogger.getSharedInstance().takeMetrics("Noise_evaluate_"+i, candidateHRMat, "candidate_" + i, medianMat,
                    "candidate_median_filter_" + i, "Candidate_Vs_Median_RMSE");

            double newRMSE = ImageMetrics.getRMSE(candidateHRMat, medianMat);
            Log.d(TAG, "New RMSE: " +newRMSE+ " Old RMSE: " +this.rmse);
            //if RMSE is lower than previous, candidate is reliable. That means its noise is not too much.
            if(newRMSE <= this.rmse) {
                //baseHRWarpMat.copyTo(this.outputMat, this.baseMaskMat);
                medianMat.copyTo(this.outputMat, this.baseMaskMat); //TODO: test only
                this.rmse = newRMSE;
            }
            else {
                Log.d(TAG, "RMSE is not lesser. Doing nothing");
            }



            this.outputMat.copyTo(candidateHRMat); //overwrite as new candidate

            ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "result_" + i, ImageFileAttribute.FileType.JPEG);
        }

        this.warpedMatrixList.clear();
        this.baseMaskMat.release();

        ProgressDialogHandler.getInstance().showDialog("Denoising", "Denoising final image.");

        System.gc();

        ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "FINAL_RESULT", ImageFileAttribute.FileType.JPEG);
        ProgressDialogHandler.getInstance().hideDialog();

        MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_final", this.groundTruthMat, "GroundTruth", this.outputMat,
                "Final", "Ground truth vs Final");

        MetricsLogger.getSharedInstance().debugPSNRTable();
        MetricsLogger.getSharedInstance().logResultsToJSON(FilenameConstants.METRICS_NAME_STRING);
    }

    private void debugMat(Mat mat) {
        Log.d(TAG, mat.toString());
        for(int row = 0; row < mat.rows(); row++) {
            for (int col = 0; col < mat.cols(); col++) {
                double data = mat.get(row, col)[0];
                Log.d(TAG, "Row " + row + " Col " + col + " Value: " + data);
            }
        }

    }

    /*
  Inserts the referenceMat in the HR matrix
   */
    private void copyMatToHR(Mat fromMat, Mat hrMat, int xOffset, int yOffset) {
        int pixelSpace = ParameterConfig.getScalingFactor();

        for(int row = 0; row < fromMat.rows(); row++) {
            for(int col = 0; col < fromMat.cols(); col++) {
                double[] lrPixelData = fromMat.get(row, col);

                int resultRow = (row * pixelSpace) + yOffset;
                int resultCol = (col * pixelSpace) + xOffset;

                if(resultRow < hrMat.rows() && resultCol < hrMat.cols()) {
                    hrMat.put(resultRow, resultCol, lrPixelData);
                }

            }
        }

    }
}
