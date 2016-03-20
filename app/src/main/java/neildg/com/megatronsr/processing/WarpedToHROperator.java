package neildg.com.megatronsr.processing;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConstants;
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
    private double currentPSNR = 0.0;

    public WarpedToHROperator(List<Mat> warpedMatrixList) {
        this.warpedMatrixList = warpedMatrixList;
        this.groundTruthMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.GROUND_TRUTH_PREFIX_STRING + ".jpg");
        this.outputMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INITIAL_HR_PREFIX_STRING + 0 + ".jpg");

        MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_initial_hr", this.groundTruthMat, "GroundTruth", this.outputMat,
                "InterCubicHR", "Ground truth vs Intercubic");
    }

    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Transforming warped images to HR", "Warping base image");
        Mat baseWarpMat = this.warpedMatrixList.get(0);
        Mat baseHRWarpMat = Mat.zeros(baseWarpMat.rows() * ParameterConstants.SCALING_FACTOR, baseWarpMat.cols() * ParameterConstants.SCALING_FACTOR, baseWarpMat.type());
        this.copyMatToHR(baseWarpMat, baseHRWarpMat, 0, 0);

        baseHRWarpMat.copyTo(this.baseMaskMat);
        Imgproc.cvtColor(this.baseMaskMat, this.baseMaskMat, Imgproc.COLOR_BGR2GRAY);
        baseHRWarpMat.convertTo(this.baseMaskMat, CvType.CV_8UC1);
        Imgproc.threshold(this.baseMaskMat, this.baseMaskMat, 1, 255, Imgproc.THRESH_BINARY);

        Mat candidateHRMat = new Mat();
        this.outputMat.copyTo(candidateHRMat);
        baseHRWarpMat.copyTo(candidateHRMat, this.baseMaskMat);
        MetricsLogger.getSharedInstance().takeMetrics("mat_initial_vs_mat_1", this.outputMat, "Mat_initial", candidateHRMat,
                "Mat_0", "PSNR compare Mat");

        baseHRWarpMat.copyTo(this.outputMat, this.baseMaskMat); //replace initial output

        ImageWriter.getInstance().saveMatrixToImage(candidateHRMat, "candidate_" + 0);

        for(int i = 1; i < this.warpedMatrixList.size(); i++) {
            ProgressDialogHandler.getInstance().showDialog("Transforming warped images to HR", "Warping image " +i);
            baseWarpMat = this.warpedMatrixList.get(i);
            Mat.zeros(baseWarpMat.rows() * ParameterConstants.SCALING_FACTOR, baseWarpMat.cols() * ParameterConstants.SCALING_FACTOR, baseWarpMat.type());
            this.copyMatToHR(baseWarpMat, baseHRWarpMat, 0, 0);

            baseHRWarpMat.copyTo(this.baseMaskMat);
            Imgproc.cvtColor(this.baseMaskMat, this.baseMaskMat, Imgproc.COLOR_BGR2GRAY);
            baseHRWarpMat.convertTo(this.baseMaskMat, CvType.CV_8UC1);
            Imgproc.threshold(this.baseMaskMat, this.baseMaskMat, 1, 255, Imgproc.THRESH_BINARY);

            baseHRWarpMat.copyTo(candidateHRMat, this.baseMaskMat); //store on candidate mat to compare PSNR with initial output


            double newPSNR = ImageMetrics.getPSNR(this.outputMat, candidateHRMat);
            MetricsLogger.getSharedInstance().takeMetrics("mat_"+(i-1)+ "vs_mat_"+i, this.outputMat, "Mat "+(i-1), candidateHRMat,
                    "Mat "+i, "PSNR compare Mat");

            Log.d(TAG, "PSNR new: " +newPSNR+ " old: " +this.currentPSNR);
            //is the new PSNR "better" than the current PSNR, then replace
            if(newPSNR >= this.currentPSNR) {
                baseHRWarpMat.copyTo(this.outputMat, this.baseMaskMat);
                this.currentPSNR = newPSNR;
            }
            else {
                Log.d(TAG, "PSNR is not better. Doing nothing");
            }

            ImageWriter.getInstance().saveMatrixToImage(candidateHRMat, "candidate_" + i);
            ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "result_" + i);
        }

        this.warpedMatrixList.clear();
        this.baseMaskMat.release();

        ProgressDialogHandler.getInstance().showDialog("Denoising", "Denoising final image.");

        System.gc();

        ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "FINAL_RESULT");
        ProgressDialogHandler.getInstance().hideDialog();

        MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_final", this.groundTruthMat, "GroundTruth", this.outputMat,
                "Final", "Ground truth vs Final");

        MetricsLogger.getSharedInstance().debugPSNRTable();
        MetricsLogger.getSharedInstance().logResultsToJSON(FilenameConstants.METRICS_NAME_STRING);
    }

    private void performCLAHERGB() {
        Mat labColorMat = new Mat();
        Imgproc.cvtColor(this.outputMat, labColorMat, Imgproc.COLOR_BGR2Lab);

        List<Mat> labSeparated = new ArrayList<Mat>();
        Core.split(labColorMat, labSeparated);

        Mat claheMat = new Mat();
        Imgproc.createCLAHE(1, new Size(4,4)).apply(labSeparated.get(0), claheMat);
        claheMat.copyTo(labSeparated.get(0)); claheMat.release(); claheMat = null;
        Core.merge(labSeparated, labColorMat); labSeparated.clear();

        Imgproc.cvtColor(labColorMat, this.outputMat, Imgproc.COLOR_Lab2BGR);
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
        int pixelSpace = ParameterConstants.SCALING_FACTOR;

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
