package neildg.com.megatronsr.processing.multiple.alignment;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
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

    String referenceImageName;
    String[] warpedMatNames;

    public WarpResultEvaluator(String referenceImageName, String[] warpedMatNames) {
        this.referenceImageName = referenceImageName;
        this.warpedMatNames = warpedMatNames;
    }

    @Override
    public void perform() {
        this.referenceMat = FileImageReader.getInstance().imReadOpenCV(referenceImageName, ImageFileAttribute.FileType.JPEG);

        this.referenceMat.convertTo(this.referenceMat,  CvType.CV_16UC(this.referenceMat.channels()));

        int[] compareResultList = new int[this.warpedMatNames.length];
        int threshold = ParameterConfig.getPrefsInt(ParameterConfig.FUSION_THRESHOLD_KEY, 0);
        for(int i = 0; i < this.warpedMatNames.length; i++) {
            Mat warpedMat = FileImageReader.getInstance().imReadOpenCV(this.warpedMatNames[i], ImageFileAttribute.FileType.JPEG);

            Mat maskMat = ImageOperator.produceMask(warpedMat);
            warpedMat.convertTo(warpedMat, CvType.CV_16UC(warpedMat.channels()));

            Log.e(TAG, "Reference mat type: " +CvType.typeToString(this.referenceMat.type()) + " Warped mat type: " +CvType.typeToString(warpedMat.type())
            + " Reference mat name: " +this.referenceImageName+ " Warped mat name: " +this.warpedMatNames[i]);
            Core.add(this.referenceMat, warpedMat, warpedMat);

            maskMat.release();
            Imgproc.blur(warpedMat, warpedMat, new Size(3,3));
            Mat gradX = new Mat(); Mat gradY = new Mat();

            Imgproc.Sobel(warpedMat, gradX, CvType.CV_16S, 1, 0, 3, 1, 0, Core.BORDER_DEFAULT);
            Imgproc.Sobel(warpedMat, gradY, CvType.CV_16S, 0, 1, 3, 1, 0, Core.BORDER_DEFAULT);

            gradX.convertTo(gradX, CvType.CV_8UC(gradX.channels())); gradY.convertTo(gradY, CvType.CV_8UC(gradX.channels()));
            Core.addWeighted(gradX, 0.5, gradY, 0.5, 0, warpedMat);

            FileImageWriter.getInstance().saveMatrixToImage(warpedMat, "sobel_grad_"+i, ImageFileAttribute.FileType.JPEG);
            //Core.absdiff(this.referenceMat, warpedMat, warpedMat);
            warpedMat = ImageOperator.produceMask(warpedMat);
            compareResultList[i] = Core.countNonZero(warpedMat);


            warpedMat.release();
        }

        this.referenceMat.release();
        assessWarpedImages(compareResultList, this.warpedMatNames);
    }

    private static void assessWarpedImages(int[] warpedResults, String[] warpedMatNames) {
        float average = 0.0f; int sum = 0;
        for(int i = 0; i < warpedResults.length; i++) {
            Log.d(TAG, "Non zero elems in difference mat for "+warpedMatNames[i]+ " : " +warpedResults[i]);
            sum += warpedResults[i];
        }

        average = (sum * 1.0f) / warpedResults.length;
        Log.d(TAG, "Average non zero difference: " +average);
    }
}
