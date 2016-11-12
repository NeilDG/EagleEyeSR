package neildg.com.megatronsr.processing.imagetools;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
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
        Imgproc.cvtColor(mat, yuvMat, Imgproc.COLOR_BGR2YUV);

        List matList = new ArrayList();
        Core.split(yuvMat, matList);

        return (Mat[]) matList.toArray(new Mat[matList.size()]);
    }

    /*
     * Converts the yuv mat (assuming channels were separated in different matrices), into its RGB mat form
     */
    public static Mat convertYUVtoRGB(Mat[] yuvMat) {
        Mat rgbMat = new Mat();
        Core.merge(Arrays.asList(yuvMat), rgbMat);

        Imgproc.cvtColor(rgbMat, rgbMat, Imgproc.COLOR_YUV2BGR);

        return rgbMat;
    }

    public static Mat rgbToGray(Mat inputMat) {
        Mat grayScaleMat = new Mat();
        if(inputMat.channels() == 3 || inputMat.channels() == 4) {
            Imgproc.cvtColor(inputMat, grayScaleMat, Imgproc.COLOR_BGR2GRAY);
        }
        else {
            inputMat.copyTo(grayScaleMat);
        }

        return grayScaleMat;
    }
}
