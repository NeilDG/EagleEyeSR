package neildg.com.megatronsr.model.multiple;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.LinkedList;
import java.util.List;

/**
 * Bookkeeping class to store processed image for future use
 * Created by NeilDG on 6/18/2016.
 */
public class ProcessedImageRepo {
    private final static String TAG = "ProcessedImageRepo";

    private static ProcessedImageRepo sharedInstance = null;
    public static ProcessedImageRepo getSharedInstance() {
        return sharedInstance;
    }

    private List<Mat> zeroFilledMatList = new LinkedList<>();
    private List<Mat> warpedMatList = new LinkedList<>();

    private Mat[] referenceMatYUV;

    private ProcessedImageRepo() {

    }

    public static void initialize() {
        sharedInstance = new ProcessedImageRepo();
    }

    public static void destroy() {
        for(int i = 0; i < sharedInstance.zeroFilledMatList.size(); i++) {
            sharedInstance.zeroFilledMatList.get(i).release();
        }

        sharedInstance.zeroFilledMatList.clear();
        sharedInstance = null;
    }
}
