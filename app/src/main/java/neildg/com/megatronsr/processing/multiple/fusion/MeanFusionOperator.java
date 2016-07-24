package neildg.com.megatronsr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

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

    private String title;
    private String message;

    public MeanFusionOperator(Mat[] combineMatList, String title, String message) {
        this.combineMatList = combineMatList;

        this.title = title;
        this.message = message;
    }

    @Override
    public void perform() {

        int rows = this.combineMatList[0].rows();
        int cols = this.combineMatList[0].cols();
        this.outputMat = Mat.zeros(rows, cols, CvType.CV_32FC1);

        ProgressDialogHandler.getInstance().showDialog(this.title, this.message);

        //add values to the warped images' borders
        for(int i = 1; i < this.combineMatList.length; i++) {
            Mat inverseMask = ImageOperator.produceOppositeMask(this.combineMatList[i]);
            this.combineMatList[0].copyTo(this.combineMatList[i], inverseMask);
            ImageWriter.getInstance().saveMatrixToImage(this.combineMatList[i], "filled_warp_"+i, ImageFileAttribute.FileType.JPEG);
        }

        //divide only by the number of known pixel values. do not consider zero pixels
        Mat sumMat = Mat.zeros(this.combineMatList[0].size(), CvType.CV_32FC1);
        Mat divMat = Mat.zeros(this.combineMatList[0].size(), CvType.CV_32FC1);
        for(int i = 0; i < this.combineMatList.length; i++) {
            this.combineMatList[i].convertTo(this.combineMatList[i], CvType.CV_32FC1);
            Mat maskMat = ImageOperator.produceMask(this.combineMatList[i]);

            Log.d(TAG, "CombineMat size: " +this.combineMatList[i].size().toString() +" sum Mat size: " +sumMat.size().toString());
            Core.add(this.combineMatList[i], sumMat, sumMat, maskMat);

            maskMat.convertTo(maskMat, CvType.CV_32FC1);
            Core.add(maskMat, divMat, divMat);
        }

        Core.divide(sumMat, divMat, this.outputMat);
        this.outputMat.convertTo(this.outputMat, CvType.CV_8UC1);

        ProgressDialogHandler.getInstance().hideDialog();

    }

    public Mat getResult() {
        return this.outputMat;
    }
}
