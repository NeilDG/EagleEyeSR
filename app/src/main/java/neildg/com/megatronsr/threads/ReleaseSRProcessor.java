package neildg.com.megatronsr.threads;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.multiple.ProcessedImageRepo;
import neildg.com.megatronsr.model.multiple.SharpnessMeasure;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.multiple.resizing.TransferToDirOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * SR processor for release mode
 * Created by NeilDG on 9/10/2016.
 */
public class ReleaseSRProcessor extends Thread{
    private final static String TAG = "ReleaseSRProcessor";

    public ReleaseSRProcessor() {

    }

    @Override
    public void run() {
        ProgressDialogHandler.getInstance().showDialog("Interpolating images", "Upsampling image using nearest-neighbor, bilinear and bicubic");
        TransferToDirOperator transferToDirOperator = new TransferToDirOperator(BitmapURIRepository.getInstance().getNumImagesSelected());
        transferToDirOperator.perform();

        Mat inputMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);

        Mat outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_NEAREST);
        ImageWriter.getInstance().saveMatrixToImage(outputMat, "nearest", ImageFileAttribute.FileType.JPEG);
        outputMat.release();

        outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        ImageWriter.getInstance().saveMatrixToImage(outputMat, "bicubic", ImageFileAttribute.FileType.JPEG);
        outputMat.release();

        inputMat.release();
        System.gc();

        ProgressDialogHandler.getInstance().hideDialog();

        ProgressDialogHandler.getInstance().showDialog("", "Processing image");

        //initialize storage classes
        ProcessedImageRepo.initialize();
        SharpnessMeasure.initialize();

        ProgressDialogHandler.getInstance().hideDialog();

        System.gc();
    }
}
