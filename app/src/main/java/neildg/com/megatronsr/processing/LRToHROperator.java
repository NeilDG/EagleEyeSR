package neildg.com.megatronsr.processing;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConstants;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.MetricsLogger;
import neildg.com.megatronsr.preprocessing.BitmapURIRepository;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Converts LR to HR images and saves them
 * Created by NeilDG on 3/6/2016.
 */
public class LRToHROperator {
    private static String TAG = "LRToHROperator";

    private Mat hrMat;
    public LRToHROperator() {

    }

    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Converting to HR images", "Pixels are \"stretched\" for each LR image.");
        int numImages = BitmapURIRepository.getInstance().getNumImages();

        //only interpolate the first reference image
        Mat lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 0 + ".jpg");
        this.hrMat = Mat.ones(lrMat.rows() * ParameterConstants.SCALING_FACTOR, lrMat.cols() * ParameterConstants.SCALING_FACTOR, lrMat.type());
        //Imgproc.resize(lrMat, this.hrMat, this.hrMat.size(), ParameterConstants.SCALING_FACTOR, ParameterConstants.SCALING_FACTOR, Imgproc.INTER_CUBIC);
        this.copyMatToHR(lrMat, 0, 0);
        ImageWriter.getInstance().saveMatrixToImage(this.hrMat, FilenameConstants.INITIAL_HR_PREFIX_STRING + 0);

        this.hrMat.release();
        lrMat.release();

        ProgressDialogHandler.getInstance().hideDialog();
    }

    /*
   Inserts the referenceMat in the HR matrix
    */
    private void copyMatToHR(Mat fromMat, int xOffset, int yOffset) {
        int pixelSpace = ParameterConstants.SCALING_FACTOR;

        for(int row = 0; row < fromMat.rows(); row++) {
            for(int col = 0; col < fromMat.cols(); col++) {
                double[] lrPixelData = fromMat.get(row, col);

                int resultRow = (row * pixelSpace) + yOffset;
                int resultCol = (col * pixelSpace) + xOffset;

                if(resultRow < this.hrMat.rows() && resultCol < this.hrMat.cols()) {
                    this.hrMat.put(resultRow, resultCol, lrPixelData);
                }

            }
        }

    }
}
