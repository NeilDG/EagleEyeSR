package neildg.com.megatronsr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.imagetools.MatMemory;
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
        ProgressDialogHandler.getInstance().showDialog(this.title, this.message);

        int rows = this.combineMatList[0].rows();
        int cols = this.combineMatList[0].cols();
        int scale = ParameterConfig.getScalingFactor();

        //divide only by the number of known pixel values. do not consider zero pixels
        Mat sumMat = Mat.zeros(rows * scale, cols * scale, CvType.CV_32FC(this.combineMatList[0].channels()));
        Mat divMat = Mat.zeros(rows * scale, cols * scale, CvType.CV_32FC1);
        Mat maskMat = new Mat();

        for(int i = 0; i < this.combineMatList.length; i++) {
            Mat hrMat = ImageOperator.performInterpolation(this.combineMatList[i], scale, Imgproc.INTER_CUBIC);
            this.combineMatList[i].release();

            hrMat.convertTo(hrMat, CvType.CV_32FC(hrMat.channels()));
            ImageOperator.produceMask(hrMat, maskMat);

            Log.d(TAG, "CombineMat size: " +hrMat.size().toString() +" sumMat size: " +sumMat.size().toString());
            Core.add(hrMat, sumMat, sumMat, maskMat, CvType.CV_32FC(hrMat.channels()));

            maskMat.convertTo(maskMat, CvType.CV_32FC1);
            Core.add(maskMat, divMat, divMat);

            hrMat.release();
        }

        maskMat.release();
        List<Mat> splittedSumMat = new ArrayList<>();
        Core.split(sumMat, splittedSumMat);

        for(int i = 0; i < splittedSumMat.size(); i++) {
            Core.divide(splittedSumMat.get(i), divMat, splittedSumMat.get(i));
        }

        divMat.release();
        sumMat.release();

        this.outputMat = Mat.zeros(rows, cols, CvType.CV_32FC(this.combineMatList[0].channels()));
        Core.merge(splittedSumMat, this.outputMat);
        splittedSumMat.clear();
        //Core.divide(sumMat, divMat, this.outputMat);

        this.outputMat.convertTo(this.outputMat, CvType.CV_8UC(this.combineMatList[0].channels()));
        ProgressDialogHandler.getInstance().hideDialog();
    }

    public Mat getResult() {
        return this.outputMat;
    }
}
