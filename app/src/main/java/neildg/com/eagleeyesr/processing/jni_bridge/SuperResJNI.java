package neildg.com.eagleeyesr.processing.jni_bridge;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Class that acts as a wrapper and bridge to the JNI part
 * Created by NeilDG on 5/4/2017.
 */

public class SuperResJNI {
    private final static String TAG = "SuperResJNI";

    private static SuperResJNI sharedInstance = null;
    public static SuperResJNI getInstance() {
        if(sharedInstance == null) {
            sharedInstance = new SuperResJNI();
        }

        return sharedInstance;
    }

    private SuperResJNI() {

    }

    public Mat getOutputMat() {
        Mat mat1 = Mat.ones(500, 500, CvType.CV_8UC1);
        Mat mat2 = Mat.ones(500, 500, CvType.CV_8UC1);
        Mat outputMat = new Mat();

        return new Mat(SuperResJNI.n_processMat(mat1.nativeObj, mat2.nativeObj, outputMat.nativeObj));
    }

    public int testSum(int a, int b) {
        return SuperResJNI.n_testSum(a,b);
    }

    private static native long n_processMat(long matAddr_1, long matAddr_2, long outputMatAddr);
    private static native int n_testSum(int a, int b);
}
