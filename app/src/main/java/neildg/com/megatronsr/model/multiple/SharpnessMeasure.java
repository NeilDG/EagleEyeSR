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
        //qqqq testing. add strictness parameter
        //sharpnessResult.mean += (sharpnessResult.mean * 0.005);

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

        /*sharpnessResult.trimmedValues = new double[trimMatList.size()];
        for(int i = 0; i < sharpnessResult.trimmedValues.length; i++) {
            sharpnessResult.trimmedValues[i] = trimMatList.get(i);
        }*/

        //get best
        double bestSharpness = 0;
        for(int i = 0; i < sharpnessResult.sharpnessValues.length; i++) {
            if(sharpnessResult.sharpnessValues[i] >= bestSharpness) {
                sharpnessResult.bestIndex = i;
                bestSharpness = sharpnessResult.sharpnessValues[i];
            }
        }

        /*bestSharpness = 0;
        for(int i = 0; i < sharpnessResult.trimmedValues.length; i++) {
            if(sharpnessResult.trimmedValues[i] >= bestSharpness) {
                sharpnessResult.bestIndexTrimmed = i;
                bestSharpness = sharpnessResult.trimmedValues[i];
            }
        }*/

        this.latestResult = sharpnessResult;

        trimMatList.clear();
        indexList.clear();

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
     * Trims the mat list based from the sharpness result. Optional weight is added to make
     * trimming more strict (adds to sharpness mean). Weight range is 0.0 to 1.0 where 1.0 means the mean threshold is doubled (dangerous).
     */
    public Mat[] trimMatList(Mat[] inputMatList, SharpnessResult sharpnessResult, double weight) {

        List<Mat> trimMatList = new ArrayList<>();
        double weightedMean = sharpnessResult.mean + (sharpnessResult.mean * weight);
        for(int i = 0; i < inputMatList.length; i++) {
            Log.d(TAG, "Value: " +sharpnessResult.sharpnessValues[i] + " Mean: " +sharpnessResult.mean+ " Weighted mean: " +weightedMean);
            if(sharpnessResult.sharpnessValues[i] >= weightedMean) {
                Log.d(TAG, "Selected input mat: " +i);
                trimMatList.add(inputMatList[i]);
            }
            else {
                inputMatList[i].release();
                inputMatList[i] = null;
            }
        }

        return trimMatList.toArray(new Mat[trimMatList.size()]);
    }

    /*
     * Trims the mat list based from the sharpness result but only returns image indices for optimization. Optional weight is added to make
     * trimming more strict (adds to sharpness mean).
     */
    public Integer[] trimMatList(int inputLength, SharpnessResult sharpnessResult, double weight) {

        List<Integer> trimMatList = new ArrayList<>();
        for(int i = 0; i < inputLength; i++) {
            if(sharpnessResult.sharpnessValues[i] >= sharpnessResult.mean + weight) {
                trimMatList.add(i);
            }
        }

        return trimMatList.toArray(new Integer[trimMatList.size()]);
    }

    public class SharpnessResult {
        private double[] sharpnessValues;
        //private double[] trimmedValues; //those who do not meet the mean.
        private int[] trimmedIndexes;

        private double mean;

        private int bestIndex;
        //private int bestIndexTrimmed;

        /*public double[] getSharpnessValues() {
            return this.sharpnessValues;
        }*/

        public double getMean() {
            return this.mean;
        }

        public int getBestIndex() {
            return this.bestIndex;
        }

        /*public int getBestIndexTrimmed() {
            return this.bestIndexTrimmed;
        }*/

        public int[] getTrimmedIndexes() {
            return this.trimmedIndexes;
        }
    }
}
