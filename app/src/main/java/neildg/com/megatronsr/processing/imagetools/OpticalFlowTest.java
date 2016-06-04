package neildg.com.megatronsr.processing.imagetools;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.DenseOpticalFlow;
import org.opencv.video.Video;

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
                xPoints.put(row,col, col - 5);
                yPoints.put(row,col,row + 5);
            }
        }

        Mat offsetMat = new Mat(imageMat.size(), imageMat.type());

        Imgproc.remap(imageMat, offsetMat, xPoints, yPoints, Imgproc.INTER_CUBIC);
        ImageWriter.getInstance().saveMatrixToImage(offsetMat, "right", ImageFileAttribute.FileType.JPEG);

        Mat prevMat = new Mat(); Mat nextMat = new Mat();
        Imgproc.cvtColor(imageMat, prevMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(offsetMat, nextMat, Imgproc.COLOR_RGB2GRAY);

        Mat flowMat = new Mat();
        //Video.calcOpticalFlowFarneback(prevMat, nextMat, flowMat, 0.5, 5, 1, 3, 5, 1.5, Video.MOTION_TRANSLATION);
        Video.createOptFlow_DualTVL1().calc(prevMat, nextMat, flowMat);
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

        Imgproc.remap(offsetMat, offsetMat, xPoints, yPoints, Imgproc.INTER_CUBIC, Core.BORDER_CONSTANT, Scalar.all(0));
        ImageWriter.getInstance().saveMatrixToImage(offsetMat, "test_remap", ImageFileAttribute.FileType.JPEG);
        ProgressDialogHandler.getInstance().hideDialog();

    }
}
