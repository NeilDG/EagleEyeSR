package neildg.com.megatronsr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Performs a meanwise fusion on all interpolated images
 * Created by NeilDG on 7/7/2016.
 */
public class MeanFusionOperator implements IOperator {
    private final static String TAG = "MeanFusionOperator";

    private Mat[] combineMatList;
    private Mat outputMat;

    public MeanFusionOperator(Mat[] combineMatList) {
        this.combineMatList = combineMatList;
    }

    @Override
    public void perform() {

        int rows = this.combineMatList[0].rows();
        int cols = this.combineMatList[0].cols();
        this.outputMat = Mat.zeros(rows, cols, CvType.CV_32FC1);

        ProgressDialogHandler.getInstance().showDialog("Fusing", "Fusing images using mean");
       /* for(int row = 0; row < rows; row++) {
            for(int col = 0; col < cols; col++) {

                float sum = 0.0f;
                float mean = 0.0f;
                for(int i = 0; i < this.combineMatList.length; i++) {
                    double[] data = this.combineMatList[i].get(row,col);
                    if(data != null) {
                        sum += data[0];
                    }

                }

                mean = sum / this.combineMatList.length * 1.0f;
                this.outputMat.put(row,col,mean);
            }
        }*/

        //divide only by the number of known pixel values. do not consider zero pixels
        Mat sumMat = Mat.zeros(this.combineMatList[0].size(), CvType.CV_32FC1);
        Mat divMat = Mat.zeros(this.combineMatList[0].size(), CvType.CV_32FC1);
        for(int i = 0; i < this.combineMatList.length; i++) {
            this.combineMatList[i].convertTo(this.combineMatList[i], CvType.CV_32FC1);
            Mat maskMat = ImageOperator.produceMask(this.combineMatList[i]);
            Core.add(this.combineMatList[i], sumMat, sumMat, maskMat);

            maskMat.convertTo(maskMat, CvType.CV_32FC1);
            Core.add(maskMat, divMat, divMat);
        }

        Core.divide(sumMat, divMat, this.outputMat);
        this.outputMat.convertTo(this.outputMat, CvType.CV_8UC1);

        ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "mean_fusion", ImageFileAttribute.FileType.JPEG);
        ProgressDialogHandler.getInstance().hideDialog();

    }

    public Mat getResult() {
        return this.outputMat;
    }
}
