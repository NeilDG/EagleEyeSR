package neildg.com.eagleeyesr.processing.imagetools;

import org.opencv.core.Mat;

import java.util.List;

/**
 * Created by NeilDG on 9/12/2016.
 */
public class MatMemory {

    public static void releaseAll(String[] matNames, boolean forceGC) {
        for(int i = 0; i < matNames.length; i++) {
            if(matNames[i] != null) {
                matNames[i] = null;
            }
        }

        if(forceGC) {
            System.gc();
            System.runFinalization();
        }
    }
    public static void releaseAll(Mat[] matList, boolean forceGC) {
        for(int i = 0; i < matList.length; i++) {
            if(matList[i] != null) {
                matList[i].release();
                matList[i] = null;
            }
        }

        if(forceGC) {
            System.gc();
            System.runFinalization();
        }
    }

    public static void releaseAll(List<Mat> matList, boolean forceGC) {
        for(int i = 0; i < matList.size(); i++) {
            matList.get(i).release();
        }

        matList.clear();

        if(forceGC) {
            System.gc();
            System.runFinalization();
        }
    }

    public static void cleanMemory() {
        System.gc();
        System.runFinalization();
    }
}
