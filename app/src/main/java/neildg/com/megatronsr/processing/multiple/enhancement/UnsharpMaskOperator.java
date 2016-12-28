package neildg.com.megatronsr.processing.multiple.enhancement;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.processing.IOperator;

/**
 * Created by NeilDG on 12/28/2016.
 */

public class UnsharpMaskOperator implements IOperator {
    private final static String TAG = "UnsharpMaskOperator";

    private Mat inputMat;
    private String inputMatName;

    private Mat outputMat;

    public UnsharpMaskOperator(String inputMatName) {
        this.inputMatName = inputMatName;
    }

    @Override
    public void perform() {
        if(this.inputMat == null) {
            this.inputMat = FileImageReader.getInstance().imReadOpenCV(this.inputMatName, ImageFileAttribute.FileType.JPEG);
        }

        Mat blurMat = new Mat();
        this.outputMat = new Mat();
        Imgproc.blur(this.inputMat, blurMat, new Size(25,25));

        FileImageWriter.getInstance().saveMatrixToImage(blurMat, this.inputMatName + "_blur", ImageFileAttribute.FileType.JPEG);

        Core.addWeighted(this.inputMat, 1.75, blurMat, -0.75, 0, this.outputMat);
        FileImageWriter.getInstance().saveMatrixToImage(this.outputMat, this.inputMatName + "_sharp", ImageFileAttribute.FileType.JPEG);

        blurMat.release();
        this.inputMat.release();
    }

    public Mat getResult() {
        return this.outputMat;
    }
}
