package neildg.com.megatronsr.processing.debugging;

import org.opencv.core.Mat;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.MatWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.ITest;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.multiple.DownsamplingOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 6/4/2016.
 */
public class ZeroFillingTest implements ITest {
    private final static String TAG = "ZeroFillTest";

    @Override
    public void performTest() {
        ProgressDialogHandler.getInstance().showDialog("Debug mode", "Performing zero filling test. Please wait...");

        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();
        for (int i = 0; i < numImages; i++) {
            Mat imageMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            imageMat = ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 0, 0);

            MatWriter.writeMat(imageMat, FilenameConstants.DEBUG_DIR, "zero_fill_val_" + i);
            ImageWriter.getInstance().saveMatrixToImage(imageMat, FilenameConstants.DEBUG_DIR, "zero_fill_" + i, ImageFileAttribute.FileType.JPEG);

            imageMat.release();
        }

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
