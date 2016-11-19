package neildg.com.megatronsr.processing.single.gaussian;

import android.graphics.Bitmap;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Associates a given input image to its corresponding LR representation sampled by blur
 * Created by NeilDG on 5/23/2016.
 */
public class InputImageAssociator implements IOperator {
    private final static String TAG = "InputImageAssociator";

    public InputImageAssociator() {

    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Formulating blurred image", "Formulating blurred image");

        Bitmap originalBitmap = BitmapURIRepository.getInstance().getOriginalBitmap(0);
        FileImageWriter.getInstance().saveBitmapImage(originalBitmap, FilenameConstants.INPUT_GAUSSIAN_DIR, FilenameConstants.INPUT_FILE_NAME, ImageFileAttribute.FileType.JPEG);

        Mat originalMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_GAUSSIAN_DIR + "/" + FilenameConstants.INPUT_FILE_NAME, ImageFileAttribute.FileType.JPEG);
        Mat blurredMat = new Mat();
        Imgproc.GaussianBlur(originalMat, blurredMat, new Size(3,3), 0.55, 0.55); //0.55 as stated by paper

        FileImageWriter.getInstance().saveMatrixToImage(blurredMat, FilenameConstants.INPUT_GAUSSIAN_DIR, FilenameConstants.INPUT_BLUR_FILENAME, ImageFileAttribute.FileType.JPEG);
        ProgressDialogHandler.getInstance().hideDialog();
    }
}
