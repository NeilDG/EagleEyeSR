package neildg.com.megatronsr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.io.MetricsLogger;
import neildg.com.megatronsr.metrics.ImageMetrics;
import neildg.com.megatronsr.processing.imagetools.ColorSpaceOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by neil.dg on 3/10/16.
 */
public class WarpedToHROperator {
    private final static String TAG = "WarpedHROperator";

    private Mat[] warpedMatrixList;

    private Mat groundTruthMat;
    private Mat outputMat;

    private Mat baseMaskMat = new Mat();
    //private double currentPSNR = 0.0;

    private double rmse = 0.0;

    public WarpedToHROperator(Mat initialOutputMat, Mat[] warpedMatrixList) {
        this.warpedMatrixList = warpedMatrixList;
        this.outputMat = initialOutputMat;
    }

    public void perform() {
        this.groundTruthMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.GROUND_TRUTH_PREFIX_STRING, ImageFileAttribute.FileType.JPEG);

        int scalingFactor = ParameterConfig.getScalingFactor();
        ProgressDialogHandler.getInstance().showDialog("Transforming warped images to HR", "Warping base image");
        Mat baseWarpMat = this.warpedMatrixList[0];
        Mat baseHRWarpMat = Mat.zeros(baseWarpMat.rows() * scalingFactor, baseWarpMat.cols() *scalingFactor, baseWarpMat.type());
        ImageOperator.copyMat(baseWarpMat, baseHRWarpMat, scalingFactor, 0, 0);

        baseHRWarpMat.copyTo(this.baseMaskMat);
        this.baseMaskMat = ColorSpaceOperator.rgbToGray(this.baseMaskMat);
        baseHRWarpMat.convertTo(this.baseMaskMat, CvType.CV_8UC1);
        Imgproc.threshold(this.baseMaskMat, this.baseMaskMat, 1, 255, Imgproc.THRESH_BINARY);

        Mat medianMat = new Mat();
        Mat candidateHRMat = new Mat(); this.outputMat.copyTo(candidateHRMat);

        baseHRWarpMat.copyTo(candidateHRMat, this.baseMaskMat);
        Imgproc.medianBlur(candidateHRMat, medianMat, 3);

        baseHRWarpMat.copyTo(this.outputMat, this.baseMaskMat); //replace initial output

        FileImageWriter.getInstance().saveMatrixToImage(candidateHRMat, "candidate_" + 0, ImageFileAttribute.FileType.JPEG);
        FileImageWriter.getInstance().saveMatrixToImage(medianMat, "candidate_median_" + 0, ImageFileAttribute.FileType.JPEG);
        this.rmse = ImageMetrics.getRMSE(candidateHRMat, medianMat);

        MetricsLogger.getSharedInstance().takeMetrics("Noise_evaluate_0", candidateHRMat, "candidate_0", medianMat,
                "candidate_median_filter_0", "Candidate_Vs_Median_RMSE");

        for(int i = 1; i < this.warpedMatrixList.length; i++) {
            ProgressDialogHandler.getInstance().showDialog("Transforming warped images to HR", "Warping image " +i);
            baseWarpMat = this.warpedMatrixList[i];
            Mat.zeros(baseWarpMat.rows() * scalingFactor, baseWarpMat.cols() * scalingFactor, baseWarpMat.type());
            ImageOperator.copyMat(baseWarpMat, baseHRWarpMat, scalingFactor, 0, 0);

            baseHRWarpMat.copyTo(this.baseMaskMat);
            this.baseMaskMat = ColorSpaceOperator.rgbToGray(this.baseMaskMat);
            baseHRWarpMat.convertTo(this.baseMaskMat, CvType.CV_8UC1);
            Imgproc.threshold(this.baseMaskMat, this.baseMaskMat, 1, 255, Imgproc.THRESH_BINARY);

            baseHRWarpMat.copyTo(candidateHRMat, this.baseMaskMat); //store on candidate mat to compare PSNR with initial output
            Imgproc.medianBlur(candidateHRMat, medianMat, 3);

            FileImageWriter.getInstance().saveMatrixToImage(candidateHRMat, "candidate_" + i, ImageFileAttribute.FileType.JPEG);
            FileImageWriter.getInstance().saveMatrixToImage(medianMat, "candidate_median_" + i, ImageFileAttribute.FileType.JPEG);

            MetricsLogger.getSharedInstance().takeMetrics("Noise_evaluate_"+i, candidateHRMat, "candidate_" + i, medianMat,
                    "candidate_median_filter_" + i, "Candidate_Vs_Median_RMSE");

            double newRMSE = ImageMetrics.getRMSE(candidateHRMat, medianMat);
            Log.d(TAG, "New RMSE: " +newRMSE+ " Old RMSE: " +this.rmse);
            //if RMSE is lower than previous, candidate is reliable. That means its noise is not too much.
            if(newRMSE <= this.rmse) {
                baseHRWarpMat.copyTo(this.outputMat, this.baseMaskMat);
                //medianMat.copyTo(this.outputMat, this.baseMaskMat); //TODO: test only
                this.rmse = newRMSE;
            }
            else {
                Log.d(TAG, "RMSE is not lesser. Doing nothing");
            }

            this.outputMat.copyTo(candidateHRMat); //overwrite as new candidate
            FileImageWriter.getInstance().saveMatrixToImage(this.outputMat, "result_" + i, ImageFileAttribute.FileType.JPEG);

            this.warpedMatrixList[i].release();
        }

        this.warpedMatrixList = null;
        this.baseMaskMat.release();

        ProgressDialogHandler.getInstance().showDialog("Denoising", "Denoising final image.");

        System.gc();

        FileImageWriter.getInstance().saveMatrixToImage(this.outputMat, "warped_to_hr_result", ImageFileAttribute.FileType.JPEG);
        ProgressDialogHandler.getInstance().hideDialog();
    }

    public Mat getResult() {
        return this.outputMat;
    }
}
