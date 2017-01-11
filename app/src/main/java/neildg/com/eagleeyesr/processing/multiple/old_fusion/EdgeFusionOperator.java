package neildg.com.eagleeyesr.processing.multiple.old_fusion;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;

/**
 * Performs mean fusion but only at the edges
 * Created by NeilDG on 7/24/2016.
 */
public class EdgeFusionOperator implements IOperator {
    private final static String TAG = "EdgeFusionOperator";

    private Mat[] combineMatList;
    private Mat edgeMat;
    private Mat outputMat;

    public EdgeFusionOperator(Mat[] combineMatList, Mat edgeMat) {
        this.combineMatList = combineMatList;
        this.edgeMat = edgeMat;
    }

    @Override
    public void perform() {
        int rows = this.combineMatList[0].rows();
        int cols = this.combineMatList[0].cols();

        this.outputMat = Mat.zeros(rows, cols, CvType.CV_32FC1);

        //resize edge mat
        this.edgeMat = ImageOperator.performInterpolation(this.edgeMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        Mat edgeMaskMat = ImageOperator.produceMask(this.edgeMat); //produce mask by the edge mat provided. only add using that mask

        //divide only by the number of known pixel values. do not consider zero pixels
        Mat sumMat = Mat.zeros(this.combineMatList[0].size(), CvType.CV_32FC1);
        this.combineMatList[0].copyTo(sumMat);
        sumMat.convertTo(sumMat, CvType.CV_32FC1);
        Mat divMat = ImageOperator.produceMask(sumMat);
        divMat.convertTo(divMat, CvType.CV_32FC1);

        for(int i = 1; i < this.combineMatList.length; i++) {
            this.combineMatList[i].convertTo(this.combineMatList[i], CvType.CV_32FC1);
            Mat maskMat = ImageOperator.produceMask(this.combineMatList[i]);
            Core.bitwise_and(maskMat, edgeMaskMat, maskMat); //filter existing mask with edge mask

            Core.add(this.combineMatList[i], sumMat, sumMat, maskMat);

            maskMat.convertTo(maskMat, CvType.CV_32FC1);
            Core.add(maskMat, divMat, divMat);
        }

        Core.divide(sumMat, divMat, this.outputMat);
        this.outputMat.convertTo(this.outputMat, CvType.CV_8UC1);
    }

    public Mat getResult() {
        return this.outputMat;
    }
}
