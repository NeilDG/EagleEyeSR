package neildg.com.megatronsr.threads;

import org.opencv.core.Mat;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.MatWriter;
import neildg.com.megatronsr.processing.multiple.DownsamplingOperator;
import neildg.com.megatronsr.processing.operators.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 6/2/2016.
 */
public class DebuggingProcessor extends Thread {
    private final static String TAG = "";

    public enum DebugType {
        DEBUG_SAVE_MAT,
        ZERO_FILLING
    }

    private DebugType debugType;

    public DebuggingProcessor(DebugType debugType) {
        this.debugType = debugType;
    }

    @Override
    public void run() {
        ProgressDialogHandler.getInstance().showDialog("Debug mode", "Performing debug operation. Please wait...");

        if(this.debugType == DebugType.DEBUG_SAVE_MAT) {
            this.performDebugSaving();
        }
        else if(this.debugType == DebugType.ZERO_FILLING) {
            this.performZeroFilling();
        }

        ProgressDialogHandler.getInstance().hideDialog();
    }

    private void performDebugSaving() {
        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();
        for (int i = 0; i < numImages; i++) {
            Mat imageMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            MatWriter.writeMat(imageMat, FilenameConstants.DEBUG_DIR, FilenameConstants.MAT_VALUE_PREFIX + i);
        }
    }

    private void performZeroFilling() {
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
    }
}
