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
        sharedInstance.sharpnessList.clear();
        sharedInstance = null;
    }

    private List<Double> sharpnessList = new ArrayList<>();

    private SharpnessMeasure() {

    }

    public void measureSharpness(Mat edgeMat) {
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

    /*
     * Trims the input mat list using the mean value of the computed sharpness measure.
     */
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
    }
}
