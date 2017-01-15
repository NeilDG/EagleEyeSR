package neildg.com.eagleeyesr.processing.multiple.resizing;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;

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
        ProgressDialogHandler.getInstance().showProcessDialog("Debugging", "Creating HR image by interpolation", ProgressDialogHandler.getInstance().getProgress());

        //only interpolate the first reference image
        Mat hrMat = Mat.ones(this.lrMat.rows() * ParameterConfig.getScalingFactor(), this.lrMat.cols() * ParameterConfig.getScalingFactor(), this.lrMat.type());

        Imgproc.resize(this.lrMat, hrMat, hrMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_NEAREST);
        FileImageWriter.getInstance().saveMatrixToImage(hrMat, FilenameConstants.HR_NEAREST, ImageFileAttribute.FileType.JPEG);

        Imgproc.resize(this.lrMat, hrMat, hrMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_LINEAR);
        FileImageWriter.getInstance().saveMatrixToImage(hrMat, FilenameConstants.HR_LINEAR, ImageFileAttribute.FileType.JPEG);

        Imgproc.resize(this.lrMat, hrMat, hrMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        FileImageWriter.getInstance().saveMatrixToImage(hrMat, FilenameConstants.HR_CUBIC, ImageFileAttribute.FileType.JPEG);

        //Mat zeroFillMat = ImageOperator.performZeroFill(this.lrMat, ParameterConfig.getScalingFactor(), 0, 0);
        //FileImageWriter.getInstance().saveMatrixToImage(zeroFillMat, FilenameConstants.HR_ZERO_FILL, ImageFileAttribute.FileType.JPEG);

        hrMat.release();
    }
}
