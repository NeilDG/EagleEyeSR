package neildg.com.megatronsr.model.single;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public static final int MAX_LOADED_PATCHES = 200;

    private int loadedPatches = 0;

    private List<HashMap<String, ImagePatch>> patchPyramidList = new ArrayList<HashMap<String, ImagePatch>>();

    private ImagePatchPool() {
        int pyramidDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);

        for(int i = 0; i < pyramidDepth; i++) {
            HashMap<String, ImagePatch> patchTable = new HashMap<>();
            this.patchPyramidList.add(patchTable);
        }
    }

    public void addPatch(int pyramidDepth, int col, int row, String imageName, String imagePath) {
        ImagePatch patch  = new ImagePatch(col, row, imagePath);

        HashMap<String,ImagePatch> patchTable = this.patchPyramidList.get(0);
        patchTable.put(imageName, patch);
    }
}
