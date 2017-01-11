package neildg.com.eagleeyesr.model.single_gaussian;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains a pairwise dictionary of LR-HR patches wherein LR are sampled using gaussian blur from a given
 * original image, which the HR patches are retrieved.
 * Created by NeilDG on 5/23/2016.
 */
public class GausianPatchTable {
    private final static String TAG = "GaussianPatchTable";

    private static GausianPatchTable sharedInstance = null;
    public static GausianPatchTable getSharedInstance() {
        return sharedInstance;
    }

    public static void initialize() {
        sharedInstance = new GausianPatchTable();
    }

    public static void destroy() {
        sharedInstance = null;
    }

    private List<LoadedImagePatchPair> patchPairs = new LinkedList<>();

    private GausianPatchTable() {

    }

    public void addPairwisePatch(LoadedImagePatch lrPatch, LoadedImagePatch hrPatch) {
        LoadedImagePatchPair patchPair = new LoadedImagePatchPair(lrPatch, hrPatch);
        this.patchPairs.add(patchPair);

        Log.d(TAG, "Patch pair generated. Length: "+ this.patchPairs.size()+ " LR:  " +lrPatch.getPatchMat().elemSize()+ " HR: " +hrPatch.getPatchMat().elemSize());
    }

    public int getCount() {
        return this.patchPairs.size();
    }

    public LoadedImagePatch getLRPatchAt(int index) {
        if(index < this.patchPairs.size()) {
            return this.patchPairs.get(index).getLRPatch();
        }
        else {
            Log.e(TAG, "Index value of " +index+ " exceeds the size of " +this.patchPairs.size());
            return null;
        }

    }

    public LoadedImagePatch getHRPatchAt(int index) {
        if(index < this.patchPairs.size()) {
            return this.patchPairs.get(index).getHRPatch();
        }
        else {
            Log.e(TAG, "Index value of " +index+ " exceeds the size of " +this.patchPairs.size());
            return null;
        }
    }

    public class LoadedImagePatchPair {
        private LoadedImagePatch lr;
        private LoadedImagePatch hr;
        public LoadedImagePatchPair(LoadedImagePatch lrPatch, LoadedImagePatch hrPatch) {
            this.lr = lrPatch;
            this.hr = hrPatch;
        }

        public LoadedImagePatch getLRPatch() {
            return this.lr;
        }

        public LoadedImagePatch getHRPatch() {
            return this.hr;
        }

    }

}
