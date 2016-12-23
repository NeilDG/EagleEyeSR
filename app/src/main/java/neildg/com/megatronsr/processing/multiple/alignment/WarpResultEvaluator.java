package neildg.com.megatronsr.processing.multiple.alignment;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.JSONSaver;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;

/**
 * Operator that verifies the quality of warped images by measuring its norm against the first reference  LR image.
 * If it's above the threshold, it will be filtered out.
 * Created by NeilDG on 12/12/2016.
 */

public class WarpResultEvaluator implements IOperator {
    private final static String TAG = "WarpResultEvaluator";
    private final static int MAX_THRESHOLD = 200000;

    private Mat referenceMat;

    private String referenceImageName;
    private String[] warpedMatNames;
    private String[] medianAlignedNames;

    private String[] chosenAlignedNames; //output the chosen aligned names for mean fusion here

    public WarpResultEvaluator(String referenceImageName, String[] warpedMatNames, String[] medianAlignedNames) {
        this.referenceImageName = referenceImageName;
        this.warpedMatNames = warpedMatNames;
        this.medianAlignedNames = medianAlignedNames;
        this.chosenAlignedNames = new String[this.warpedMatNames.length];
    }

    @Override
    public void perform() {
        this.referenceMat = FileImageReader.getInstance().imReadOpenCV(referenceImageName, ImageFileAttribute.FileType.JPEG);

        this.referenceMat.convertTo(this.referenceMat,  CvType.CV_16UC(this.referenceMat.channels()));
        int sobelReferenceMeasure = ImageOperator.edgeSobelMeasure(this.referenceMat, true);

        int[] warpedDifferenceList = this.measureDifference(this.referenceMat, sobelReferenceMeasure, this.warpedMatNames);
        int[] medianDifferenceList = this.measureDifference(this.referenceMat, sobelReferenceMeasure, this.medianAlignedNames);

        this.referenceMat.release();
        //assessWarpedImages(sobelReferenceMeasure, warpedDifferenceList, this.warpedMatNames);
        this.chooseAlignedImages(warpedDifferenceList, medianDifferenceList, this.warpedMatNames, this.medianAlignedNames);

    }

    private int[] measureDifference(Mat referenceMat, int referenceSobelMeasure, String[] compareNames) {
        int[] warpedDifferenceList = new int[compareNames.length];
        for(int i = 0; i < compareNames.length; i++) {
            Mat warpedMat = FileImageReader.getInstance().imReadOpenCV(compareNames[i], ImageFileAttribute.FileType.JPEG);

            Mat maskMat = ImageOperator.produceMask(warpedMat);
            warpedMat.convertTo(warpedMat, CvType.CV_16UC(warpedMat.channels()));

            Log.e(TAG, "Reference mat type: " +CvType.typeToString(this.referenceMat.type()) + " Warped mat type: " +CvType.typeToString(warpedMat.type())
                    + " Reference mat name: " +this.referenceImageName+ " Warped mat name: " +compareNames[i]);
            Core.add(referenceMat, warpedMat, warpedMat);

            maskMat.release();

            warpedDifferenceList[i] = ImageOperator.edgeSobelMeasure(warpedMat, true) - referenceSobelMeasure;
            Log.d(TAG, "Non zero elems in difference mat for "+compareNames[i]+ " : " +warpedDifferenceList[i]);

            warpedMat.release();
        }

        return warpedDifferenceList;
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

        int warpChoice = ParameterConfig.getPrefsInt(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.BEST_ALIGNMENT);
        JSONSaver.debugWriteEdgeConsistencyMeasure(warpChoice, warpedResults, sobelReferenceDifferences, warpedMatNames);
    }


    private void chooseAlignedImages(int[] warpedResults, int[] medianAlignedResults, String[] warpedMatNames, String[] medianAlignedNames) {

        float warpedMean = 0.0f;

        for(int i = 0; i < warpedResults.length; i++) {
            warpedMean += warpedResults[i];
        }

        warpedMean = (warpedMean * 1.0f) / warpedResults.length;

        for(int i = 0; i < this.chosenAlignedNames.length; i++) {
            float absDiffFromMean = Math.abs(warpedResults[i] - warpedMean);
            float medianAlignDiff = Math.abs(medianAlignedResults[i] - warpedMean);
            if(warpedResults[i] < medianAlignedResults[i] && absDiffFromMean < MAX_THRESHOLD) {
                this.chosenAlignedNames[i] = warpedMatNames[i];
            }
            else {
                this.chosenAlignedNames[i] = medianAlignedNames[i];
            }
            Log.d(TAG, "Chosen image name: " +this.chosenAlignedNames[i]);
        }
    }

    public String[] getChosenAlignedNames() {
        return this.chosenAlignedNames;
    }
}
