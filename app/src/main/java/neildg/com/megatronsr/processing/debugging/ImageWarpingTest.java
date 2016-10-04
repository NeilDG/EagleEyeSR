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
import neildg.com.megatronsr.processing.ITest;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.multiple.resizing.DownsamplingOperator;
import neildg.com.megatronsr.processing.multiple.warping.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.warping.LRWarpingOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 6/4/2016.
 */
public class ImageWarpingTest implements ITest {
    private final static String TAG = "ImageWarpingTest";

    @Override
    public void performTest() {
        ProgressDialogHandler.getInstance().showDialog("Debug mode", "Performing image warping test. Please wait...");

        //ProcessedImageRepo.initialize();

        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();

        List<Mat> testMatList = new LinkedList<>();
        for (int i = 0; i < numImages; i++) {
            Mat imageMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            imageMat = ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 0, 0);

            ImageWriter.getInstance().saveMatrixToImage(imageMat, FilenameConstants.INPUT_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            testMatList.add(imageMat);
        }

        Mat referenceMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + "0", ImageFileAttribute.FileType.JPEG);
        Mat[] comparingMatList = new Mat[BitmapURIRepository.getInstance().getNumImagesSelected()];
        for(int i = 0; i < comparingMatList.length; i++) {
            comparingMatList[i] = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (i+1), ImageFileAttribute.FileType.JPEG);
        }

        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator(referenceMat, comparingMatList);
        matchingOperator.perform();

        LRWarpingOperator warpingOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), comparingMatList, matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        warpingOperator.perform();

        Mat[] warpedMatList= warpingOperator.getWarpedMatList();
        Mat testOutputMat = new Mat();
        for(int i = 0; i < warpedMatList.length; i++) {
            Mat warpedMat = warpedMatList[i];
            Mat maskMat = Mat.zeros(warpedMat.size(), warpedMat.type());

            warpedMat.copyTo(maskMat);
            Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_RGB2GRAY);
            maskMat.convertTo(maskMat, CvType.CV_8UC1);
            Imgproc.threshold(maskMat, maskMat, 1, 255, Imgproc.THRESH_BINARY);

            warpedMat.copyTo(testOutputMat, maskMat);

            warpedMat.release();
            maskMat.release();
        }

        ImageWriter.getInstance().saveMatrixToImage(testOutputMat, "test", ImageFileAttribute.FileType.JPEG);

        //ProcessedImageRepo.destroy();
        ProgressDialogHandler.getInstance().hideDialog();
    }
}
