package neildg.com.megatronsr.model.multiple;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.processing.imagetools.ImageOperator;

/**
 * Stores the sharpness measure of images. To retrieve a sharpness measure from a certain image, use the image index.
 * Created by NeilDG on 7/20/2016.
 */
public class SharpnessMeasure {
    private final static String TAG = "SharpnessMeasure";

    private static SharpnessMeasure sharedInstance = null;
    public static SharpnessMeasure getSharedInstance() {
        return sharedInstance;
    }

    public static void initialize() {
        sharedInstance = new SharpnessMeasure();
    }

    public static void destroy() {
        sharedInstance = null;
    }

    //private List<Double> sharpnessList = new ArrayList<>();
    private SharpnessResult latestResult;

    private SharpnessMeasure() {

    }

    public SharpnessResult measureSharpness(Mat[] edgeMatList) {
        SharpnessResult sharpnessResult = new SharpnessResult();
        sharpnessResult.sharpnessValues = new double[edgeMatList.length];

        double sum = 0;
        for(int i = 0; i < edgeMatList.length; i++) {
            sharpnessResult.sharpnessValues[i] = this.measure(edgeMatList[i]);
            sum += sharpnessResult.sharpnessValues[i];
        }

        //get mean
        sharpnessResult.mean = sum / edgeMatList.length;

        //trimmed values that do not meet the mean
        List<Double> trimMatList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();

        for(int i = 0; i <  edgeMatList.length; i++) {
            if(sharpnessResult.sharpnessValues[i] >= sharpnessResult.mean) {
                trimMatList.add(sharpnessResult.sharpnessValues[i]);
                indexList.add(i);
            }
        }

        //store index values
        sharpnessResult.trimmedIndexes = new int[indexList.size()];
        for(int i = 0; i < sharpnessResult.trimmedIndexes.length; i++) {
            sharpnessResult.trimmedIndexes[i] = indexList.get(i);
        }

        sharpnessResult.trimmedValues = new double[trimMatList.size()];
        for(int i = 0; i < sharpnessResult.trimmedValues.length; i++) {
            sharpnessResult.trimmedValues[i] = trimMatList.get(i);
        }

        //get best
        double bestSharpness = 0;
        for(int i = 0; i < sharpnessResult.sharpnessValues.length; i++) {
            if(sharpnessResult.sharpnessValues[i] >= bestSharpness) {
                sharpnessResult.bestIndex = i;
                bestSharpness = sharpnessResult.sharpnessValues[i];
            }
        }

        bestSharpness = 0;
        for(int i = 0; i < sharpnessResult.trimmedValues.length; i++) {
            if(sharpnessResult.trimmedValues[i] >= bestSharpness) {
                sharpnessResult.bestIndexTrimmed = i;
                bestSharpness = sharpnessResult.trimmedValues[i];
            }
        }

        this.latestResult = sharpnessResult;
        return this.latestResult;
    }

    public SharpnessResult getLatestResult() {
        return this.latestResult;
    }

    public double measure(Mat edgeMat) {
        int withValues = Core.countNonZero(edgeMat);
        int dimension = edgeMat.cols() * edgeMat.rows();

        double dSharpness = withValues * 1.0 / dimension;

        return dSharpness;
    }

    /*
     * Trims the mat list based from the sharpness result
     */
    public Mat[] trimMatList(Mat[] inputMatList, SharpnessResult sharpnessResult) {

        List<Mat> trimMatList = new ArrayList<>();
        for(int i = 0; i < sharpnessResult.trimmedIndexes.length; i++) {
            trimMatList.add(inputMatList[sharpnessResult.trimmedIndexes[i]]);
        }

        return trimMatList.toArray(new Mat[trimMatList.size()]);
    }

    /*public void measureSharpness(Mat edgeMat) {
        int withValues = Core.countNonZero(edgeMat);
        int dimension = edgeMat.cols() * edgeMat.rows();

        double dSharpness = withValues * 1.0 / dimension;

        this.sharpnessList.add(dSharpness);
    }

    public double getSharpnessValueAt(int imageIndex) {
        return this.sharpnessList.get(imageIndex);
    }

    public double[] getSharpnessValues() {
        double[] listArray = new double[this.sharpnessList.size()];
        for(int i = 0; i < listArray.length; i++) {
            listArray[i] = this.sharpnessList.get(i);
        }

        return listArray;
    }


    public Mat[] trimMatList(Mat[] inputMatList) {
        double mean = this.getMeanSharpness();

        List<Mat> trimMatList = new ArrayList<>();
        for(int i = 0; i <  inputMatList.length; i++) {
            if(this.getSharpnessValueAt(i) >= mean) {
                trimMatList.add(inputMatList[i]);
            }
        }

        return trimMatList.toArray(new Mat[trimMatList.size()]);
    }

    public Mat getBest(Mat[] inputMatList) {

        int currentHighest = 0;
        double highestSharpness = 0.0;
        for(int i = 0; i < this.sharpnessList.size(); i++) {
            if(this.getSharpnessValueAt(i) >= highestSharpness) {
                highestSharpness = this.getSharpnessValueAt(i);
                currentHighest = i;
            }
        }

        return inputMatList[currentHighest];
    }
    public double getMeanSharpness() {
        double sum = 0;
        for(int i = 0; i < this.sharpnessList.size(); i++) {
            Log.d(TAG, "Sharpness " +i+ " : " +this.sharpnessList.get(i));
            sum += this.sharpnessList.get(i);
        }

      return (sum / this.sharpnessList.size());
    }

    public void debugPrint() {
        Log.d(TAG, "Printing sharpness values");

        double sum = 0;
        for(int i = 0; i < this.sharpnessList.size(); i++) {
            Log.d(TAG, "Sharpness " +i+ " : " +this.sharpnessList.get(i));
            sum += this.sharpnessList.get(i);
        }

        Log.d(TAG, "Mean sharpness: " +(sum / this.sharpnessList.size()));
    }*/

    public class SharpnessResult {
        private double[] sharpnessValues;
        private double[] trimmedValues; //those who do not meet the mean.
        private int[] trimmedIndexes;

        private double mean;

        private int bestIndex;
        private int bestIndexTrimmed;

        public double[] getSharpnessValues() {
            return this.sharpnessValues;
        }

        public double getMean() {
            return this.mean;
        }

        public int getBestIndex() {
            return this.bestIndex;
        }

        public int getBestIndexTrimmed() {
            return this.bestIndexTrimmed;
        }

        public int[] getTrimmedIndexes() {
            return this.trimmedIndexes;
        }
    }
}
