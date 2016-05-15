package neildg.com.megatronsr.processing.single;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/15/2016.
 */
public class ImagePatchMerging implements IOperator {
    private final static String TAG = "";

    private Mat hrMat;

    public ImagePatchMerging() {

    }

    @Override
    public void perform() {
        //ImageWriter.getInstance().saveBitmapImage(originalBitmap, FilenameConstants.PYRAMID_DIR, FilenameConstants.PYRAMID_IMAGE_PREFIX + "0", ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().showDialog("Creating HR image", "Creating HR image");

        String fullImagePath = FilenameConstants.PYRAMID_DIR +"/"+ FilenameConstants.PYRAMID_IMAGE_PREFIX + "0";
        Mat lrMat = ImageReader.getInstance().imReadOpenCV(fullImagePath, ImageFileAttribute.FileType.JPEG);
        this.hrMat = Mat.zeros(lrMat.rows() * ParameterConfig.getScalingFactor(), lrMat.cols() * ParameterConfig.getScalingFactor(), lrMat.type());
        Imgproc.resize(lrMat, this.hrMat, this.hrMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);

        ImageWriter.getInstance().saveMatrixToImage(this.hrMat, FilenameConstants.RESULTS_DIR, FilenameConstants.RESULTS_CUBIC, ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
