package neildg.com.megatronsr.processing.operators;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by NeilDG on 5/23/2016.
 */
public class ImageMeasures {

    public static double measureMATSimilarity(Mat mat1, Mat mat2) {
        Mat resultMat = new Mat();
        Imgproc.matchTemplate(mat1, mat2, resultMat,Imgproc.TM_SQDIFF_NORMED);

        double value = Core.norm(resultMat, Core.NORM_L1);
        resultMat.release();

        return value;
    }
}
