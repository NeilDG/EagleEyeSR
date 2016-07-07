package neildg.com.megatronsr.processing.debugging;

import org.opencv.core.Mat;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.MatWriter;
import neildg.com.megatronsr.processing.ITest;
import neildg.com.megatronsr.processing.multiple.resizing.DownsamplingOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 6/4/2016.
 */
public class MatSavingTest implements ITest {
    private final static String TAG = "MatSavingTest";

    public MatSavingTest() {

    }

    @Override
    public void performTest() {

        ProgressDialogHandler.getInstance().showDialog("Debug mode", "Performing mat saving test. Please wait...");

        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();
        for (int i = 0; i < numImages; i++) {
            Mat imageMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            MatWriter.writeMat(imageMat, FilenameConstants.DEBUG_DIR, FilenameConstants.MAT_VALUE_PREFIX + i);
        }

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
