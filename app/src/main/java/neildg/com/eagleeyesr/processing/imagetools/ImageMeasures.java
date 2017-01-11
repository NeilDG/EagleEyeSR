package neildg.com.eagleeyesr.processing.imagetools;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.eagleeyesr.metrics.ImageMetrics;

/**
 * Created by NeilDG on 5/23/2016.
 */
public class ImageMeasures {

    private final static String TAG = "ImageMeasures";

    public static double measureRMSENoise(Mat mat) {
        int nonZeroes = Core.countNonZero(ImageOperator.produceMask(mat));
        if(nonZeroes < mat.elemSize() / 2) {
            Log.d(TAG, "Nonzeroes is " +nonZeroes+". RMSE set to max");
            return Double.MAX_VALUE;
        }

        Mat medianMat = new Mat();
        Imgproc.medianBlur(mat, medianMat, 3);

        double rmse = ImageMetrics.getRMSE(mat, medianMat);
        medianMat.release();

        return rmse;
    }

    public static double measureMATSimilarity(Mat mat1, Mat mat2) {
        Mat resultMat = new Mat();
        Imgproc.matchTemplate(mat1, mat2, resultMat,Imgproc.TM_SQDIFF_NORMED);

        double value = Core.norm(resultMat, Core.NORM_L1);
        resultMat.release();

        return value;
    }
}
