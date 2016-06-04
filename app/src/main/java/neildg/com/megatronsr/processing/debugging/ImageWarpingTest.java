package neildg.com.megatronsr.processing.debugging;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.ITest;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.multiple.DownsamplingOperator;
import neildg.com.megatronsr.processing.multiple.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.LRWarpingOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 6/4/2016.
 */
public class ImageWarpingTest implements ITest {
    private final static String TAG = "ImageWarpingTest";

    @Override
    public void performTest() {
        ProgressDialogHandler.getInstance().showDialog("Debug mode", "Performing image warping test. Please wait...");

        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();

        List<Mat> testMatList = new LinkedList<>();
        for (int i = 0; i < numImages; i++) {
            Mat imageMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            imageMat = ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 0, 0);

            ImageWriter.getInstance().saveMatrixToImage(imageMat, FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            testMatList.add(imageMat);
        }

        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator();
        matchingOperator.perform();

        LRWarpingOperator warpingOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        warpingOperator.perform();

        List<Mat> warpedMatrixList = warpingOperator.getWarpedMatrixList();
        Mat testOutputMat = new Mat();
        for(int i = 0; i < warpedMatrixList.size(); i++) {
            Mat warpedMat = warpedMatrixList.get(i);
            Mat maskMat = Mat.zeros(warpedMat.size(), warpedMat.type());

            warpedMat.copyTo(maskMat);
            Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_RGB2GRAY);
            maskMat.convertTo(maskMat, CvType.CV_8UC1);
            Imgproc.threshold(maskMat, maskMat, 1, 255, Imgproc.THRESH_BINARY);

            warpedMat.copyTo(testOutputMat, maskMat);

            warpedMat.release();
            maskMat.release();
        }

        warpedMatrixList.clear();
        ImageWriter.getInstance().saveMatrixToImage(testOutputMat, "test", ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
