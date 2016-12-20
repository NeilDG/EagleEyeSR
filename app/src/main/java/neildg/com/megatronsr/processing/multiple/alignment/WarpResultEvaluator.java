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
import neildg.com.megatronsr.io.JSONSaver;
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
        int sobelReferenceMeasure = ImageOperator.edgeSobelMeasure(this.referenceMat, true);

        int[] compareResultList = new int[this.warpedMatNames.length];
        for(int i = 0; i < this.warpedMatNames.length; i++) {
            Mat warpedMat = FileImageReader.getInstance().imReadOpenCV(this.warpedMatNames[i], ImageFileAttribute.FileType.JPEG);

            Mat maskMat = ImageOperator.produceMask(warpedMat);
            warpedMat.convertTo(warpedMat, CvType.CV_16UC(warpedMat.channels()));

            Log.e(TAG, "Reference mat type: " +CvType.typeToString(this.referenceMat.type()) + " Warped mat type: " +CvType.typeToString(warpedMat.type())
            + " Reference mat name: " +this.referenceImageName+ " Warped mat name: " +this.warpedMatNames[i]);
            Core.add(this.referenceMat, warpedMat, warpedMat);

            maskMat.release();

            compareResultList[i] = ImageOperator.edgeSobelMeasure(warpedMat, true, "sobel_grad_"+i);

            warpedMat.release();
        }

        this.referenceMat.release();
        assessWarpedImages(sobelReferenceMeasure, compareResultList, this.warpedMatNames);
    }

    private static void assessWarpedImages(int referenceSobelMeasure, int[] warpedResults, String[] warpedMatNames) {
        float average = 0.0f; int sum = 0;
        for(int i = 0; i < warpedResults.length; i++) {
            Log.d(TAG, "Non zero elems in edge sobel mat for "+warpedMatNames[i]+ " : " +warpedResults[i]);
            sum += warpedResults[i];
        }

        average = (sum * 1.0f) / warpedResults.length;
        Log.d(TAG, "Average non zero difference: " +average);

        int[] sobelReferenceDifferences = new int[warpedResults.length]; //difference from the reference sobel measure
        for(int i = 0; i < warpedResults.length; i++) {
            sobelReferenceDifferences[i] = warpedResults[i] - referenceSobelMeasure;
            Log.d(TAG, "Non zero elems in difference mat for "+warpedMatNames[i]+ " : " +sobelReferenceDifferences[i]);
        }

        int warpChoice = ParameterConfig.getPrefsInt(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.PERSPECTIVE_WARP);
        JSONSaver.debugWriteEdgeConsistencyMeasure(warpChoice, warpedResults, sobelReferenceDifferences, warpedMatNames);
    }
}
