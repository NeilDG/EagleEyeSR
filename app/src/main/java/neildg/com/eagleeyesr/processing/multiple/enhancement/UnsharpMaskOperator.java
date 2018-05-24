package neildg.com.eagleeyesr.processing.multiple.enhancement;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Semaphore;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.threads.FlaggingThread;

/**
 * Created by NeilDG on 12/28/2016.
 */

public class UnsharpMaskOperator implements IOperator {
    private final static String TAG = "UnsharpMaskOperator";

    private Mat inputMat;
    private Mat outputMat;

    private int index = 0;

    public UnsharpMaskOperator(Mat inputMat, int index) {
        this.inputMat = inputMat;
        this.index = index;
    }

    @Override
    public void perform() {
        Mat blurMat = new Mat();
        this.outputMat = new Mat();
        Imgproc.blur(this.inputMat, blurMat, new Size(25,25));

        Core.addWeighted(this.inputMat, 2.25, blurMat, -1.25, 0, this.outputMat, CvType.CV_8UC(this.inputMat.channels()));
        FileImageWriter.getInstance().debugSaveMatrixToImage(this.outputMat, FilenameConstants.INPUT_PREFIX_SHARPEN_STRING + index, ImageFileAttribute.FileType.JPEG);

        blurMat.release();
        this.inputMat.release();
    }

    public Mat getResult() {
        return this.outputMat;
    }
}
