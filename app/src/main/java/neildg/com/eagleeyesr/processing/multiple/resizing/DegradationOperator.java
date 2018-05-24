package neildg.com.eagleeyesr.processing.multiple.resizing;

import org.opencv.core.Mat;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.io.BitmapURIRepository;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;

/**
 * Imposes a degradation operator on a downsampled image. Induces blur and noise.
 * Created by NeilDG on 7/7/2016.
 */
public class DegradationOperator implements IOperator{
    private final static String TAG = "BlurImposeOperator";

    private Mat[] inputMatList;

    public DegradationOperator(Mat[] rgbInputMatList) {
        this.inputMatList = rgbInputMatList;
    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showProcessDialog("Debugging", "Imposing degradation operators", ProgressDialogHandler.getInstance().getProgress());

        for(int i = 0; i < this.inputMatList.length; i++) {
            Mat inputMat =  this.inputMatList[i];
            //Imgproc.blur(inputMat, inputMat, new Size(5,5));
            inputMat = ImageOperator.induceNoise(inputMat);
            FileImageWriter.getInstance().saveMatrixToImage(inputMat, FilenameConstants.INPUT_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
            //inputMat.release();
        }
    }
}
