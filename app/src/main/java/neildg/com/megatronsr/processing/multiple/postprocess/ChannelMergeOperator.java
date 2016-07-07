package neildg.com.megatronsr.processing.multiple.postprocess;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.Arrays;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ColorSpaceOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

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
        ProgressDialogHandler.getInstance().showDialog("Finalizing", "Converting YUV back to RGB");

        //perform bicubic interpolation on u and v mat
        int scaling = ParameterConfig.getScalingFactor();
        this.yuvMat[U_CHANNEL] = ImageOperator.performInterpolation(this.yuvMat[U_CHANNEL], scaling, Imgproc.INTER_CUBIC);
        this.yuvMat[V_CHANNEL] = ImageOperator.performInterpolation(this.yuvMat[V_CHANNEL], scaling, Imgproc.INTER_CUBIC);

        //merge the three channels
        Mat mergedMat = new Mat();
        Core.merge(Arrays.asList(this.yuvMat), mergedMat);

        ImageWriter.getInstance().saveMatrixToImage(mergedMat, "yuv_merged", ImageFileAttribute.FileType.JPEG);
        Imgproc.cvtColor(mergedMat, mergedMat, Imgproc.COLOR_YUV2BGR);
        ImageWriter.getInstance().saveMatrixToImage(mergedMat, "rgb_merged", ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
