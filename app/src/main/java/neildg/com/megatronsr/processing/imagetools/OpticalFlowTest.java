package neildg.com.megatronsr.processing.imagetools;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.DenseOpticalFlow;
import org.opencv.video.Video;

import java.util.LinkedList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.MatWriter;
import neildg.com.megatronsr.processing.ITest;
import neildg.com.megatronsr.processing.multiple.DownsamplingOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 6/4/2016.
 */
public class OpticalFlowTest implements ITest {
    private final static String TAG = "OpticalFlowTest";

    public OpticalFlowTest() {

    }

    @Override
    public void performTest() {
        ProgressDialogHandler.getInstance().showDialog("Debug mode", "Performing optical flow test");

        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        Mat imageMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);

        Mat xPoints = new Mat(imageMat.size(), CvType.CV_32FC1);
        Mat yPoints = new Mat(imageMat.size(), CvType.CV_32FC1);
        //perform simple remapping to the right
        for(int row = 0; row < imageMat.rows(); row++) {
            for(int col = 0; col < imageMat.cols(); col++) {
                xPoints.put(row,col, col - 10);
                yPoints.put(row,col,row);
            }
        }

        Mat offsetMat = new Mat(imageMat.size(), imageMat.type());

        Imgproc.remap(imageMat, offsetMat, xPoints, yPoints, Imgproc.INTER_CUBIC);
        ImageWriter.getInstance().saveMatrixToImage(offsetMat, "right", ImageFileAttribute.FileType.JPEG);

        Mat prevMat = new Mat(); Mat nextMat = new Mat();
        Imgproc.cvtColor(imageMat, prevMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(offsetMat, nextMat, Imgproc.COLOR_RGB2GRAY);

        Mat flowMat = new Mat();
        Video.calcOpticalFlowFarneback(prevMat, nextMat, flowMat, 0.25, 5, 5, 3, 5, 1.5, Video.MOTION_TRANSLATION);
        //Video.createOptFlow_DualTVL1().calc(prevMat, nextMat, flowMat);
        MatWriter.writeMat(flowMat, "flow");

        xPoints.release(); yPoints.release();
        xPoints = new Mat(imageMat.size(), CvType.CV_32FC1);
        yPoints = new Mat(imageMat.size(), CvType.CV_32FC1);

        for(int row = 0; row < imageMat.rows(); row++) {
            for(int col = 0; col < imageMat.cols(); col++) {
                //Log.d(TAG, "flowMat x: " +x+ " row: " +row+ " channels: "+flowMat.channels()+ " value: " +flowMat.get(row,x)[0]+" "+flowMat.get(row,x)[1]);
                Point pt = new Point(col + flowMat.get(row,col)[0], row + flowMat.get(row,col)[1]);
                xPoints.put(row,col, pt.x);
                yPoints.put(row,col, pt.y);
            }
        }

        Mat outputMat = Mat.zeros(offsetMat.size(), offsetMat.type());
        Imgproc.remap(offsetMat, outputMat, xPoints, yPoints, Imgproc.INTER_CUBIC, Core.BORDER_TRANSPARENT, Scalar.all(0));
        ImageWriter.getInstance().saveMatrixToImage(outputMat, "test_remap_output", ImageFileAttribute.FileType.JPEG);

        Imgproc.remap(offsetMat, offsetMat, xPoints, yPoints, Imgproc.INTER_CUBIC, Core.BORDER_TRANSPARENT, Scalar.all(0));
        ImageWriter.getInstance().saveMatrixToImage(offsetMat, "test_remap", ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().showDialog("Debug mode", "Blend images test");

        //test merging
        List<Mat> toBlend = new LinkedList<>();
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 0, 0));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 0, 1));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 0, 2));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 0, 3));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 1, 0));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 1, 1));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 1, 2));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 1, 3));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 2, 0));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 2, 1));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 2, 2));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 2, 3));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 3, 0));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 3, 1));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 3, 2));
        toBlend.add(ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 3, 3));


        outputMat = ImageOperator.blendImages(toBlend);
        ImageWriter.getInstance().saveMatrixToImage(outputMat, "test_blend", ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().hideDialog();

    }
}
