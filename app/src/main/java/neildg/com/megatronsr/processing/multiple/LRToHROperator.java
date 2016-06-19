package neildg.com.megatronsr.processing.multiple;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Converts LR to HR images and saves them
 * Created by NeilDG on 3/6/2016.
 */
public class LRToHROperator implements IOperator {
    private static String TAG = "LRToHROperator";

    private Mat hrMat;
    public LRToHROperator() {

    }

    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Debugging", "Creating HR image by interpolation");
        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();

        //only interpolate the first reference image
        Mat lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);
        this.hrMat = Mat.ones(lrMat.rows() * ParameterConfig.getScalingFactor(), lrMat.cols() * ParameterConfig.getScalingFactor(), lrMat.type());

        Imgproc.resize(lrMat, this.hrMat, this.hrMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_NEAREST);
        ImageWriter.getInstance().saveMatrixToImage(this.hrMat, FilenameConstants.INITIAL_HR_NEAREST, ImageFileAttribute.FileType.JPEG);

        Imgproc.resize(lrMat, this.hrMat, this.hrMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        ImageWriter.getInstance().saveMatrixToImage(this.hrMat, FilenameConstants.INITIAL_HR_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);

        Mat zeroFillMat = Mat.zeros(lrMat.rows() * ParameterConfig.getScalingFactor(), lrMat.cols() * ParameterConfig.getScalingFactor(), lrMat.type());
        this.copyMatToHR(lrMat, zeroFillMat, 0, 0);
        ImageWriter.getInstance().saveMatrixToImage(zeroFillMat, FilenameConstants.INITIAL_HR_ZERO_FILLED_STRING, ImageFileAttribute.FileType.JPEG);

        this.hrMat.release();
        lrMat.release();

        ProgressDialogHandler.getInstance().hideDialog();
    }

    /*
   Inserts the referenceMat in the HR matrix
    */
    private void copyMatToHR(Mat fromMat, Mat toMat,  int xOffset, int yOffset) {
        int pixelSpace = ParameterConfig.getScalingFactor();

        for(int row = 0; row < fromMat.rows(); row++) {
            for(int col = 0; col < fromMat.cols(); col++) {
                double[] lrPixelData = fromMat.get(row, col);

                int resultRow = (row * pixelSpace) + yOffset;
                int resultCol = (col * pixelSpace) + xOffset;

                if(resultRow < toMat.rows() && resultCol < toMat.cols()) {
                    toMat.put(resultRow, resultCol, lrPixelData);
                }

            }
        }

    }
}
