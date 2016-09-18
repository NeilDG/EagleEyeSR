package neildg.com.megatronsr.processing.imagetools;

import org.opencv.core.Mat;

/**
 * Created by NeilDG on 9/12/2016.
 */
public class MatMemory {
    public static void releaseAll(Mat[] matList, boolean forceGC) {
        for(int i = 0; i < matList.length; i++) {
            matList[i].release();
            matList[i] = null;
        }

        if(forceGC) {
            Runtime.getRuntime().gc();
        }
    }
}
