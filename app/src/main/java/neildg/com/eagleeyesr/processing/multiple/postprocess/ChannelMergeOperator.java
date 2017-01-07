package neildg.com.eagleeyesr.processing.multiple.postprocess;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ColorSpaceOperator;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;

/**
 * Operator that merges the YUV channels of the mat and converts it back to RGB format (if needed).
 * U and V channel are upsampled using bicubic interpolation. Y channel is assumed to have been processed by an SR method.
 * Created by NeilDG on 7/7/2016.
 */
public class ChannelMergeOperator implements IOperator{
    private final static String TAG = "ChannelMergeOperator";

    private Mat[] yuvMat;

    private static int Y_CHANNEL;
    private static int U_CHANNEL;
    private static int V_CHANNEL;

    /*
     * yMat is the processed mat (already upsampled)
     * uMat and vMat are initial u and v mat. This operator will perform the corresponding interpolation upsampling.
     */
    public ChannelMergeOperator(Mat yMat, Mat uMat, Mat vMat) {
        this.yuvMat = new Mat[3];

        Y_CHANNEL = ColorSpaceOperator.Y_CHANNEL;
        U_CHANNEL = ColorSpaceOperator.U_CHANNEL;
        V_CHANNEL = ColorSpaceOperator.V_CHANNEL;

        this.yuvMat[Y_CHANNEL] = yMat;
        this.yuvMat[U_CHANNEL] = uMat;
        this.yuvMat[V_CHANNEL] = vMat;
    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showProcessDialog("Finalizing", "Converting YUV back to RGB", 90.0f);

        //perform bicubic interpolation on u and v mat
        int scaling = ParameterConfig.getScalingFactor();
        this.yuvMat[U_CHANNEL] = ImageOperator.performInterpolation(this.yuvMat[U_CHANNEL], scaling, Imgproc.INTER_CUBIC);
        this.yuvMat[V_CHANNEL] = ImageOperator.performInterpolation(this.yuvMat[V_CHANNEL], scaling, Imgproc.INTER_CUBIC);


        //Log.d(TAG, "Mean fusion result size: "+this.yuvMat[Y_CHANNEL].size().toString()+ " Depth: " +this.yuvMat[Y_CHANNEL].depth());
        //Log.d(TAG, "U channel result size: "+this.yuvMat[U_CHANNEL].size().toString()+ " Depth: " +this.yuvMat[ColorSpaceOperator.U_CHANNEL].depth());
        //Log.d(TAG, "V channel result size: "+this.yuvMat[V_CHANNEL].size().toString()+ " Depth: " +this.yuvMat[ColorSpaceOperator.V_CHANNEL].depth());

        //merge the three channels
        Mat mergedMat = new Mat();
        Core.merge(Arrays.asList(this.yuvMat), mergedMat);

        FileImageWriter.getInstance().saveMatrixToImage(mergedMat, "yuv_merged", ImageFileAttribute.FileType.JPEG);
        Imgproc.cvtColor(mergedMat, mergedMat, Imgproc.COLOR_YUV2BGR);
        FileImageWriter.getInstance().saveMatrixToImage(mergedMat, "rgb_merged", ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().hideProcessDialog();
    }
}
