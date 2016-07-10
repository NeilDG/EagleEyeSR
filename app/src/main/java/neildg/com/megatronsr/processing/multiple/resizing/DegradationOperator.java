package neildg.com.megatronsr.processing.multiple.resizing;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
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

        ProgressDialogHandler.getInstance().showDialog("Debugging", "Imposing degradation operators");

        for(int i = 0; i < numImages; i++) {
            Mat inputMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
            Imgproc.blur(inputMat, inputMat, new Size(5,5));
            inputMat = ImageOperator.induceNoise(inputMat);
            ImageWriter.getInstance().saveMatrixToImage(inputMat, FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
        }
    }
}
