package neildg.com.megatronsr.processing.imagetools;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NeilDG on 7/2/2016.
 */
public class ColorSpaceOperator {
    private final static String TAG = "ColorSpaceOperator";

    public static int Y_CHANNEL = 0;
    public static int U_CHANNEL = 1;
    public static int V_CHANNEL = 2;

    public static int R_CHANNEL = 2;
    public static int G_CHANNEL = 1;
    public static int B_CHANNEL = 0;
    public static int A_CHANNEL = 3;

    /*
     * Converts a mat into its YUV format and separated into channels
     */
    public static Mat[] convertRGBToYUV(Mat mat) {
        Mat yuvMat = new Mat();
        Imgproc.cvtColor(mat, yuvMat, Imgproc.COLOR_RGB2YUV);

        List matList = new ArrayList();
        Core.split(yuvMat, matList);

        return (Mat[]) matList.toArray(new Mat[matList.size()]);
    }

    public static Mat rgbToGray(Mat inputMat) {
        Mat grayScaleMat = new Mat();
        if(inputMat.channels() == 3 || inputMat.channels() == 4) {
            Imgproc.cvtColor(inputMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY);
        }
        else {
            grayScaleMat.release();
            grayScaleMat = inputMat;
        }

        return grayScaleMat;
    }
}
