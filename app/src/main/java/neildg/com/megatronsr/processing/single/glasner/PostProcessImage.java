package neildg.com.megatronsr.processing.single.glasner;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/22/2016.
 */
public class PostProcessImage implements IOperator {
    private final static String TAG = "PostProcessImage";

    private Mat hrMat;

    public PostProcessImage(Mat hrMat) {
        this.hrMat = hrMat;
    }
    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Postprocessing", "Smoothing and refining HR image");
        this.performLaplace();

        FileImageWriter.getInstance().saveMatrixToImage(this.hrMat, FilenameConstants.RESULTS_DIR, FilenameConstants.RESULTS_GLASNER_SHARPEN, ImageFileAttribute.FileType.JPEG);
    }

    private void performLaplace() {
        //remove noise with gaussian
        Imgproc.GaussianBlur(this.hrMat, this.hrMat, new Size(3,3), 0, 0, Core.BORDER_DEFAULT);

        //convert to grayscale
        Mat greyScaleMat = new Mat(this.hrMat.size(), this.hrMat.type());
        Mat rgbMat = new Mat(this.hrMat.size(), this.hrMat.type());
        Imgproc.cvtColor(this.hrMat, greyScaleMat, Imgproc.COLOR_BGR2GRAY);
        this.hrMat.copyTo(rgbMat);

        Imgproc.Laplacian(greyScaleMat, this.hrMat, CvType.CV_16S, 3, 1, 0);
        Core.convertScaleAbs(this.hrMat, this.hrMat);
        FileImageWriter.getInstance().saveMatrixToImage(this.hrMat, "laplace_filter", FilenameConstants.RESULTS_DIR, ImageFileAttribute.FileType.JPEG);

        Imgproc.cvtColor(this.hrMat, this.hrMat, Imgproc.COLOR_GRAY2BGR);
        Core.addWeighted(this.hrMat, -0.25, rgbMat, 1.0, 0, this.hrMat);

        greyScaleMat.release();
        rgbMat.release();
    }
}
