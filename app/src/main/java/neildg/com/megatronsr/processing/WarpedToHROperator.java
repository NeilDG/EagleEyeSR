package neildg.com.megatronsr.processing;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConstants;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by neil.dg on 3/10/16.
 */
public class WarpedToHROperator {

    private List<Mat> warpedMatrixList = null;

    private Mat outputMat;

    public WarpedToHROperator(List<Mat> warpedMatrixList) {
        this.warpedMatrixList = warpedMatrixList;
        this.outputMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INITIAL_HR_PREFIX_STRING + 0 + ".jpg");
    }

    public void perform() {

        for(int i = 0; i < this.warpedMatrixList.size(); i++) {
            ProgressDialogHandler.getInstance().showDialog("Transforming warped images to HR", "Warped image " + i + " pixel stretching");

            Mat warpedMat = this.warpedMatrixList.get(i);
            Mat hrWarpedMat =  Mat.zeros(warpedMat.rows() * ParameterConstants.SCALING_FACTOR, warpedMat.cols() * ParameterConstants.SCALING_FACTOR, warpedMat.type());

            this.copyMatToHR(warpedMat, hrWarpedMat, 0, 0);
            ImageWriter.getInstance().saveMatrixToImage(hrWarpedMat, "hrwarp_" + i);

            ProgressDialogHandler.getInstance().showDialog("Merging with reference HR", "Warped image " + i + " is being merged to the HR image.");
            Mat maskHRMat = new Mat(hrWarpedMat.rows(), hrWarpedMat.cols(), CvType.CV_8U);
            hrWarpedMat.convertTo(maskHRMat, CvType.CV_8U);

            hrWarpedMat.copyTo(this.outputMat, maskHRMat);
            ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "result_"+i);
            //Imgproc.resize(warpedMat, hrWarpedMat, hrWarpedMat.size(), ParameterConstants.SCALING_FACTOR, ParameterConstants.SCALING_FACTOR, Imgproc.INTER_CUBIC);
        }

        ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "FINAL_RESULT");
        ProgressDialogHandler.getInstance().hideDialog();
    }

    /*
  Inserts the referenceMat in the HR matrix
   */
    private void copyMatToHR(Mat fromMat, Mat hrMat, int xOffset, int yOffset) {
        int pixelSpace = ParameterConstants.SCALING_FACTOR;

        for(int row = 0; row < fromMat.rows(); row++) {
            for(int col = 0; col < fromMat.cols(); col++) {
                double[] lrPixelData = fromMat.get(row, col);

                int resultRow = (row * pixelSpace) + yOffset;
                int resultCol = (col * pixelSpace) + xOffset;

                if(resultRow < hrMat.rows() && resultCol < hrMat.cols()) {
                    hrMat.put(resultRow, resultCol, lrPixelData);
                }

            }
        }

    }
}
