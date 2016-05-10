package neildg.com.megatronsr.model.single;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;

/**
 * Created by neil.dg on 5/5/16.
 */
public class ImagePatchPool {
    private final static String TAG = "ImagePatchPool";
    private static ImagePatchPool sharedInstance = new ImagePatchPool();

    public static ImagePatchPool getInstance() {
        return sharedInstance;
    }

    public static void initialize() {
        sharedInstance = new ImagePatchPool();
    }

    public static void destroy() {
        sharedInstance = null;
    }

    public static final int MAX_LOADED_PATCHES = 1000;

    private int pyramidDepth = 0;
    private int loadedPatches = 0;

    private List<HashMap<String, ImagePatch>> patchPyramidList = new ArrayList<HashMap<String, ImagePatch>>();

    private ImagePatchPool() {
        this.pyramidDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);

        for(int i = 0; i < pyramidDepth; i++) {
            HashMap<String, ImagePatch> patchTable = new HashMap<>();
            this.patchPyramidList.add(patchTable);
        }
    }

    public ImagePatch loadPatch(PatchAttribute patchAttribute) {
        return this.loadPatch(patchAttribute.getPyramidDepth(), patchAttribute.getCol(), patchAttribute.getRow(), patchAttribute.getImageName(), patchAttribute.getImagePath());
    }

    public ImagePatch loadPatch(int pyramidDepth, int col, int row, String imageName, String imagePath) {
        ImagePatch patch  = new ImagePatch(col, row, imageName, imagePath);

        HashMap<String,ImagePatch> patchTable = this.patchPyramidList.get(pyramidDepth);

        if(!patchTable.containsKey(imageName) && this.loadedPatches < MAX_LOADED_PATCHES) {
            patchTable.put(imageName, patch);
            patch.loadPatchMatIfNull();
            this.loadedPatches++;
            return patch;
        }
        else if(this.loadedPatches >= MAX_LOADED_PATCHES) {
            Log.d(TAG, "Patch limit exceeded. Unloading a random patch.");
            this.unloadRandomPatch(pyramidDepth);

            patchTable.put(imageName, patch);
            this.loadedPatches++;
            return patch;
        }
        else {
            Log.e(TAG, "Patch " +imageName+ "already exists.");
            return null;
        }
    }

    public int getLoadedPatches() {
        return this.loadedPatches;
    }

    private void unloadRandomPatch(int pyramidDepth) {

        Random rand = new Random();
        HashMap<String,ImagePatch> patchTable = this.patchPyramidList.get(pyramidDepth);

        List keys = new ArrayList(patchTable.keySet());
        if(keys.size() > 0) {
            int randomIndex = rand.nextInt(keys.size());
            ImagePatch imagePatch = patchTable.get(keys.get(randomIndex));
            imagePatch.releaseMat();
            patchTable.remove(keys.get(randomIndex));
            this.loadedPatches--;
        }
    }

    public void unloadPatch(int pyramidDepth, String imageName) {
        HashMap<String,ImagePatch> patchTable = this.patchPyramidList.get(pyramidDepth);

        if(patchTable.containsKey(imageName)) {
            patchTable.get(imageName).releaseMat();
            patchTable.remove(imageName);
            this.loadedPatches--;
        }
    }

    public boolean containsPatch(int pyramidDepth, ImagePatch patch) {
        HashMap<String, ImagePatch> patchTable = this.patchPyramidList.get(pyramidDepth);

        return (patchTable.containsKey(patch.getImageName()));
    }

    public  double measureSimilarity(ImagePatch patch1, ImagePatch patch2) {
        Mat resultMat = new Mat();
        Imgproc.matchTemplate(patch1.getPatchMat(), patch2.getPatchMat(), resultMat,Imgproc.TM_SQDIFF_NORMED);

        double value = Core.norm(resultMat, Core.NORM_L1);
        resultMat.release();

        return value;
    }
}
