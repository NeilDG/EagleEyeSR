package neildg.com.megatronsr.processing.multiple.resizing;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Converts LR to HR images and saves them. This serves as generation of different HR images for comparison
 * Created by NeilDG on 3/6/2016.
 */
public class LRToHROperator implements IOperator {
    private static String TAG = "LRToHROperator";

    private Mat lrMat;
    private int selectedIndex;
    public LRToHROperator(Mat lrMat, int index) {
        this.lrMat = lrMat;
        this.selectedIndex = index;
    }

    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Debugging", "Creating HR image by interpolation");

        //only interpolate the first reference image
        Mat hrMat = Mat.ones(this.lrMat.rows() * ParameterConfig.getScalingFactor(), this.lrMat.cols() * ParameterConfig.getScalingFactor(), this.lrMat.type());

        Imgproc.resize(this.lrMat, hrMat, hrMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_NEAREST);
        ImageWriter.getInstance().saveMatrixToImage(hrMat, FilenameConstants.INITIAL_HR_NEAREST + "_" + this.selectedIndex, ImageFileAttribute.FileType.JPEG);

        Imgproc.resize(this.lrMat, hrMat, hrMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        ImageWriter.getInstance().saveMatrixToImage(hrMat, FilenameConstants.INITIAL_HR_CUBIC + "_" + this.selectedIndex, ImageFileAttribute.FileType.JPEG);

        Mat zeroFillMat = ImageOperator.performZeroFill(this.lrMat, ParameterConfig.getScalingFactor(), 0, 0);
        ImageWriter.getInstance().saveMatrixToImage(zeroFillMat, FilenameConstants.INITIAL_HR_ZERO_FILLED_STRING + "_" +this.selectedIndex, ImageFileAttribute.FileType.JPEG);

        hrMat.release();

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
