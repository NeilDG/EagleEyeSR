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

    public WarpedToHROperator(List<Mat> warpedMatrixList) {
        this.warpedMatrixList = warpedMatrixList;
        this.groundTruthMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.GROUND_TRUTH_PREFIX_STRING + ".jpg");
        this.outputMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INITIAL_HR_PREFIX_STRING + 0 + ".jpg");

        MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_initial_hr", this.groundTruthMat, "GroundTruth", this.outputMat,
                "InterCubicHR", "Ground truth vs Intercubic");
    }

    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Transforming warped images to HR", "Warping base image");
        Mat baseWarpMKat = this.warpedMatrixList.get(0);
        Mat baseHRWarpMat = Mat.zeros(baseWarpMKat.rows() * ParameterConstants.SCALING_FACTOR, baseWarpMKat.cols() * ParameterConstants.SCALING_FACTOR, baseWarpMKat.type());
        this.copyMatToHR(baseWarpMKat, baseHRWarpMat, 0, 0);

        this.baseMaskMat = new Mat(baseHRWarpMat.rows(), baseHRWarpMat.cols(), CvType.CV_8UC1);
        baseHRWarpMat.convertTo(this.baseMaskMat, CvType.CV_8UC1);
        Imgproc.cvtColor(this.baseMaskMat, this.baseMaskMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(this.baseMaskMat, this.baseMaskMat, 1, 255, Imgproc.THRESH_BINARY);

        ImageWriter.getInstance().saveMatrixToImage(this.baseMaskMat, "mask_" + 0);

        baseHRWarpMat.copyTo(this.outputMat, this.baseMaskMat);
        ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "result_0");

        for(int i = 1; i < this.warpedMatrixList.size(); i++) {
            ProgressDialogHandler.getInstance().showDialog("Transforming warped images to HR", "Warped image " + i + " pixel stretching");

            Mat warpedMat = this.warpedMatrixList.get(i);
            Mat hrWarpedMat =  Mat.zeros(warpedMat.rows() * ParameterConstants.SCALING_FACTOR, warpedMat.cols() * ParameterConstants.SCALING_FACTOR, warpedMat.type());

            //Imgproc.resize(warpedMat, hrWarpedMat, hrWarpedMat.size(), ParameterConstants.SCALING_FACTOR, ParameterConstants.SCALING_FACTOR, Imgproc.INTER_NEAREST);
            this.copyMatToHR(warpedMat, hrWarpedMat, 0, 0);

            ImageWriter.getInstance().saveMatrixToImage(hrWarpedMat, "hrwarp_" + i);

            ProgressDialogHandler.getInstance().showDialog("Merging with reference HR", "Warped image " + i + " is being merged to the HR image.");
            Mat maskHRMat = new Mat(hrWarpedMat.rows(), hrWarpedMat.cols(), CvType.CV_8UC1);
            hrWarpedMat.convertTo(maskHRMat, CvType.CV_8UC1);
            Imgproc.cvtColor(maskHRMat, maskHRMat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(maskHRMat, maskHRMat, 1, 255, Imgproc.THRESH_BINARY);

            //perform filtering of mask
            Mat comparingMat = new Mat();
            Core.bitwise_not(this.baseMaskMat, comparingMat);
            Core.bitwise_and(comparingMat, maskHRMat, maskHRMat);

            hrWarpedMat.copyTo(this.outputMat, maskHRMat);
            ImageWriter.getInstance().saveMatrixToImage(maskHRMat, "mask_" + i);
            ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "result_" + i);

            MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_result_" + i, this.groundTruthMat, "GroundTruth", this.outputMat,
                    "Result_" + i, "Ground truth vs Result_" + i);

            //perform OR operation to merge the mask mat with the base MAT
            Core.bitwise_or(this.baseMaskMat, maskHRMat, this.baseMaskMat);

            maskHRMat.release();
            hrWarpedMat.release();
            warpedMat.release();
            comparingMat.release();
        }

        this.warpedMatrixList.clear();
        this.baseMaskMat.release();

        ProgressDialogHandler.getInstance().showDialog("Denoising", "Denoising final image.");


        //this.performCLAHERGB();
        Mat enhancedMat = new Mat();
        System.gc();

        Photo.fastNlMeansDenoisingColored(this.outputMat, enhancedMat);
        ImageWriter.getInstance().saveMatrixToImage(enhancedMat, "FINAL_RESULT");
        ProgressDialogHandler.getInstance().hideDialog();

        MetricsLogger.getSharedInstance().takeMetrics("ground_truth_vs_final", this.groundTruthMat, "GroundTruth", enhancedMat,
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
