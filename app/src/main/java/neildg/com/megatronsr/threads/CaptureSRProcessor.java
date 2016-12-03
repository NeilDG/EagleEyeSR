package neildg.com.megatronsr.threads;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.camera2.capture.CaptureProcessor;
import neildg.com.megatronsr.constants.DialogConstants;
import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * The main entry point for the SR functionality that is triggered via image captured.
 * Created by NeilDG on 11/27/2016.
 */

public class CaptureSRProcessor extends Thread {
    private final static String TAG = "CaptureSRProcessor";

    public CaptureSRProcessor() {

    }

    @Override
    public void run() {
        //this.interpolateFirstImage();
    }

    private void interpolateFirstImage() {
        ProgressDialogHandler.getInstance().showProcessDialog(DialogConstants.DIALOG_PROGRESS_TITLE, "Upsampling image using nearest-neighbor.", 40.0f);

        Mat inputMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING, ImageFileAttribute.FileType.JPEG);

        Mat outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_NEAREST);
        FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_NEAREST, ImageFileAttribute.FileType.JPEG);
        outputMat.release();

        ProgressDialogHandler.getInstance().showProcessDialog(DialogConstants.DIALOG_PROGRESS_TITLE, "Upsampling image using linear.", 60.0f);

        outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_LINEAR);
        FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_LINEAR, ImageFileAttribute.FileType.JPEG);
        outputMat.release();

        ProgressDialogHandler.getInstance().showProcessDialog(DialogConstants.DIALOG_PROGRESS_TITLE, "Upsampling image using bicubic.", 80.0f);

        outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_CUBIC, ImageFileAttribute.FileType.JPEG);
        outputMat.release();

        ProgressDialogHandler.getInstance().showProcessDialog(DialogConstants.DIALOG_PROGRESS_TITLE, "Upsampling image using bicubic.", 95.0f);

        inputMat.release();
        System.gc();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ProgressDialogHandler.getInstance().hideProcessDialog();
    }
}
