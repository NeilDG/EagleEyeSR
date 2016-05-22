package neildg.com.megatronsr.processing.operators;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by NeilDG on 5/22/2016.
 */
public class IntensityMatConverter {
    private final static String TAG = "IntensityMatConverter";

    //converts the given mat to a single channel intensity mat (Y channel) in YUV.
    public static Mat convertMatToIntensity(Mat inputMat) {
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGR2YUV);
        List<Mat> splittedMat = new LinkedList<>();
        Core.split(inputMat, splittedMat);

        return splittedMat.get(0);
    }

    public static Mat replaceIntensityMat(Mat intensityMat, Mat originalMat) {
        Imgproc.cvtColor(originalMat, originalMat, Imgproc.COLOR_BGR2YUV);

        List<Mat> splittedMat = new LinkedList<>();
        Core.split(originalMat, splittedMat);

        splittedMat.get(0).release();
        intensityMat.copyTo(splittedMat.get(0));

        return originalMat;
    }

    public static Mat convertYUVToRGB(Mat inputMat) {
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_YUV2BGR);
        return inputMat;
    }
}
