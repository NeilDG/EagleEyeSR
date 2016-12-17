package neildg.com.megatronsr.processing.multiple.alignment;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.imagetools.MatMemory;

/**
 * Operator that verifies the quality of warped images by measuring its norm against the first reference  LR image.
 * If it's above the threshold, it will be filtered out.
 * Created by NeilDG on 12/12/2016.
 */

public class WarpResultEvaluator implements IOperator {
    private final static String TAG = "WarpResultEvaluator";

    private Mat referenceMat;
    private Mat[] warpedMatList;

    String referenceImageName;
    String[] warpedMatNames;

    public WarpResultEvaluator(String referenceImageName, String[] warpedMatNames) {
        this.referenceImageName = referenceImageName;
        this.warpedMatNames = warpedMatNames;
    }

    @Override
    public void perform() {
        this.referenceMat = FileImageReader.getInstance().imReadOpenCV(referenceImageName, ImageFileAttribute.FileType.JPEG);
        this.warpedMatList = new Mat[this.warpedMatNames.length];

        this.referenceMat.convertTo(this.referenceMat,  CvType.CV_8UC(this.referenceMat.channels()));

        int[] compareResultList = new int[this.warpedMatNames.length];

        Mat fuseMat = new Mat();
        Mat diffMat = new Mat();
        for(int i = 0; i < this.warpedMatNames.length; i++) {
            this.warpedMatList[i] = FileImageReader.getInstance().imReadOpenCV(this.warpedMatNames[i], ImageFileAttribute.FileType.JPEG);

            Mat maskMat = ImageOperator.produceMask(this.warpedMatList[i]);
            this.warpedMatList[i].convertTo(this.warpedMatList[i], CvType.CV_32FC(this.warpedMatList[i].channels()));
            Core.add(this.referenceMat, this.warpedMatList[i], fuseMat, maskMat, CvType.CV_32FC(this.warpedMatList[i].channels()));

            Core.divide(fuseMat, Scalar.all(2), fuseMat, CvType.CV_8UC(this.warpedMatList[i].channels()));

            maskMat.release();
            //measure extreme values
            fuseMat.convertTo(fuseMat,  CvType.CV_8UC(this.warpedMatList[i].channels()));
            Core.absdiff(this.referenceMat, fuseMat, diffMat);
            fuseMat.release();

            diffMat = ImageOperator.produceMask(diffMat, 175);
            Log.d(TAG, "Non zero elems in difference mat for "+this.warpedMatNames[i]+ " : " +Core.countNonZero(diffMat));

            diffMat.release();

        }

        this.referenceMat.release();
        MatMemory.releaseAll(this.warpedMatList, true);
    }
}
