package neildg.com.eagleeyesr.processing.debugging;

import org.opencv.core.Mat;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.BitmapURIRepository;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.MatWriter;
import neildg.com.eagleeyesr.processing.ITest;
import neildg.com.eagleeyesr.processing.multiple.resizing.DownsamplingOperator;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;

/**
 * Created by NeilDG on 6/4/2016.
 */
public class MatSavingTest implements ITest {
    private final static String TAG = "MatSavingTest";

    public MatSavingTest() {

    }

    @Override
    public void performTest() {

        ProgressDialogHandler.getInstance().showProcessDialog("Debug mode", "Performing mat saving test. Please wait...", 0.0f);

        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();
        for (int i = 0; i < numImages; i++) {
            Mat imageMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            MatWriter.writeMat(imageMat, FilenameConstants.DEBUG_DIR, FilenameConstants.MAT_VALUE_PREFIX + i);
        }

        ProgressDialogHandler.getInstance().hideProcessDialog();
    }
}
