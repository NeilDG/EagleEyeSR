package neildg.com.megatronsr.processing.multiple.resizing;

import org.opencv.core.Mat;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Imposes a degradation operator on a downsampled image. Induces blur and noise.
 * Created by NeilDG on 7/7/2016.
 */
public class DegradationOperator implements IOperator{
    private final static String TAG = "BlurImposeOperator";


    public DegradationOperator() {

    }

    @Override
    public void perform() {
        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();

        ProgressDialogHandler.getInstance().showProcessDialog("Debugging", "Imposing degradation operators", ProgressDialogHandler.getInstance().getProgress());

        for(int i = 0; i < numImages; i++) {
            Mat inputMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
            //Imgproc.blur(inputMat, inputMat, new Size(5,5));
            inputMat = ImageOperator.induceNoise(inputMat);
            FileImageWriter.getInstance().saveMatrixToImage(inputMat, FilenameConstants.INPUT_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
            inputMat.release();
        }
    }
}
