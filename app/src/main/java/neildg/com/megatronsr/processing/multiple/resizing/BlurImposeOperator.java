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

/**
 * Imposes a blur operator on a downsampled image
 * Created by NeilDG on 7/7/2016.
 */
public class BlurImposeOperator implements IOperator{
    private final static String TAG = "BlurImposeOperator";


    public BlurImposeOperator() {

    }

    @Override
    public void perform() {
        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();

        for(int i = 0; i < numImages; i++) {
            Mat inputMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
            Imgproc.blur(inputMat, inputMat, new Size(5,5));
            ImageWriter.getInstance().saveMatrixToImage(inputMat, FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
        }
    }
}
