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

    public static final int MAX_LOADED_PATCHES = 100000;

    private int pyramidDepth = 0;
    private int loadedPatches = 0;

    private List<HashMap<String, ImagePatch>> patchPyramidTable = new ArrayList<HashMap<String, ImagePatch>>();
    private List<List<ImagePatch>> patchPyramidList = new ArrayList<List<ImagePatch>>();

    private ImagePatchPool() {
        this.pyramidDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0) + 1; //last depth is used for HR image

        for(int i = 0; i < pyramidDepth; i++) {
            HashMap<String, ImagePatch> patchTable = new HashMap<>();
            List<ImagePatch> patchList = new ArrayList<ImagePatch>();
            this.patchPyramidTable.add(patchTable);
            this.patchPyramidList.add(patchList);
        }
    }

    public ImagePatch loadPatch(PatchAttribute patchAttribute) {
        return this.loadPatch(patchAttribute.getPyramidDepth(), patchAttribute.getColStart(), patchAttribute.getRowStart(), patchAttribute.getImageName(), patchAttribute.getImagePath());
    }

    public ImagePatch loadPatch(int pyramidDepth, int col, int row, String imageName, String imagePath) {
        ImagePatch patch  = new ImagePatch(col, row, imageName, imagePath);

        HashMap<String,ImagePatch> patchTable = this.patchPyramidTable.get(pyramidDepth);
        List<ImagePatch> patchList = this.patchPyramidList.get(0);

        if(!patchTable.containsKey(imageName) && this.loadedPatches < MAX_LOADED_PATCHES) {
            patchTable.put(imageName, patch);
            patchList.add(patch);
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
            return patchTable.get(imageName);
        }
    }

    public int getLoadedPatches() {
        return this.loadedPatches;
    }

    private synchronized void unloadRandomPatch(int pyramidDepth) {

        List<ImagePatch> patchList = this.patchPyramidList.get(0);
        HashMap<String,ImagePatch> patchTable = this.patchPyramidTable.get(pyramidDepth);
        Random rand = new Random();

        int oldListSize = patchList.size(); int oldTableSize = patchTable.size();

        int randomIndex = rand.nextInt(patchList.size());
        ImagePatch imagePatch = patchList.get(randomIndex);
        imagePatch.releaseMat();
        patchTable.remove(imagePatch.getImageName());
        patchList.remove(randomIndex);
        this.loadedPatches--;

        Log.d(TAG, "Removed patch. Old list size " +oldListSize+ " New list size: " +patchList.size()+ " Old table size: " +oldTableSize+ " New table size: " +patchTable.size());
    }

    public void unloadPatch(int pyramidDepth, String imageName) {
        HashMap<String,ImagePatch> patchTable = this.patchPyramidTable.get(pyramidDepth);

        if(patchTable.containsKey(imageName)) {
            patchTable.get(imageName).releaseMat();
            patchTable.remove(imageName);
            this.loadedPatches--;
        }
    }

    public void unloadAllPatches() {
        for(int i = 0; i < this.patchPyramidTable.size(); i++) {
            HashMap<String, ImagePatch> imagePatchMap = this.patchPyramidTable.get(i);

            for(ImagePatch patch : imagePatchMap.values()) {
                patch.releaseMat();
            }

            imagePatchMap.clear();
        }
    }

    public boolean containsPatch(int pyramidDepth, ImagePatch patch) {
        HashMap<String, ImagePatch> patchTable = this.patchPyramidTable.get(pyramidDepth);

        return (patchTable.containsKey(patch.getImageName()));
    }

    public  double measureSimilarity(ImagePatch patch1, ImagePatch patch2) {
        Mat resultMat = new Mat();
        Imgproc.matchTemplate(patch1.getPatchMat(), patch2.getPatchMat(), resultMat,Imgproc.TM_SQDIFF_NORMED);

        double value = Core.norm(resultMat, Core.NORM_L1);
        resultMat.release();

        return value;
    }

    public static double measureMATSimilarity(Mat mat1, Mat mat2) {
        Mat resultMat = new Mat();
        Imgproc.matchTemplate(mat1, mat2, resultMat,Imgproc.TM_SQDIFF_NORMED);

        double value = Core.norm(resultMat, Core.NORM_L1);
        resultMat.release();

        return value;
    }
}
