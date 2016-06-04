package neildg.com.megatronsr.processing.imagetools;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.metrics.ImageMetrics;

/**
 * Miscellaneous image operators
 * Created by NeilDG on 5/23/2016.
 */
public class ImageOperator {

    public static Mat produceMask(Mat inputMat) {
        Mat baseMaskMat = new Mat();
        inputMat.copyTo(baseMaskMat);

        Imgproc.cvtColor(baseMaskMat, baseMaskMat, Imgproc.COLOR_BGR2GRAY);
        baseMaskMat.convertTo(baseMaskMat, CvType.CV_8UC1);
        Imgproc.threshold(baseMaskMat, baseMaskMat, 1, 255, Imgproc.THRESH_BINARY);

        return baseMaskMat;
    }

    public static double measureRMSENoise(Mat mat) {
        Mat medianMat = new Mat();
        Imgproc.medianBlur(mat, medianMat, 3);

        double rmse = ImageMetrics.getRMSE(mat, medianMat);
        medianMat.release();

        return rmse;
    }

    public static Mat blendImages(List<Mat> matList) {
        Mat matInput = matList.get(0);
        Mat mergedMat = new Mat(matInput.size(), matInput.type(), new Scalar(0));
        //Add each image from a vector<Mat> inputImages with weight 1.0/n where n is number of images to merge
        for (int i = 0; i < matList.size(); i++) {
            Mat mat = matList.get(i);
            Core.addWeighted(mergedMat, 1, mat, 1.0/matList.size(), 0, mergedMat);

            ImageWriter.getInstance().saveMatrixToImage(mergedMat, "fusion", "fuse_"+i, ImageFileAttribute.FileType.JPEG);
        }

        return mergedMat;
    }

    /*
     * Performs zero-filling upsample of a given mat
     */
    public static Mat performZeroFill(Mat fromMat, int scaling, int xOffset, int yOffset) {
        Mat hrMat = Mat.zeros(fromMat.rows() * scaling, fromMat.cols() * scaling, fromMat.type());

        for(int row = 0; row < fromMat.rows(); row++) {
            for(int col = 0; col < fromMat.cols(); col++) {
                double[] lrPixelData = fromMat.get(row, col);

                int resultRow = (row * scaling) + yOffset;
                int resultCol = (col * scaling) + xOffset;

                if(resultRow < hrMat.rows() && resultCol < hrMat.cols()) {
                    hrMat.put(resultRow, resultCol, lrPixelData);
                }
            }
        }

        return hrMat;
    }

    public static void copyMat(Mat fromMat, Mat hrMat, int xOffset, int yOffset) {
        int pixelSpace = ParameterConfig.getScalingFactor();

        for (int row = 0; row < fromMat.rows(); row++) {
            for (int col = 0; col < fromMat.cols(); col++) {
                double[] lrPixelData = fromMat.get(row, col);

                int resultRow = (row * pixelSpace) + yOffset;
                int resultCol = (col * pixelSpace) + xOffset;

                if (resultRow < hrMat.rows() && resultCol < hrMat.cols()) {
                    hrMat.put(resultRow, resultCol, lrPixelData);
                }

            }
        }
    }

    /*
     * Performs interpolation using an existing interpolation algo by OPENCV
     */
    public static Mat performInterpolation(Mat fromMat, int scaling, int interpolationType) {
        Mat hrMat = Mat.zeros(fromMat.rows() * scaling, fromMat.cols() * scaling, fromMat.type());

        Imgproc.resize(fromMat, hrMat, hrMat.size(), scaling, scaling, interpolationType);

        return hrMat;
    }
}
