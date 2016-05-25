package neildg.com.megatronsr.model.single_glasner;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;

/**
 * Container for extracted HR patches
 * Created by NeilDG on 5/15/2016.
 */
public class HRPatchAttributeTable {
    private final static String TAG = "HRPatchAttributeTable";

    private static HRPatchAttributeTable sharedInstance = null;
    public static HRPatchAttributeTable getInstance() {
        return sharedInstance;
    }

    public static void initialize() {
        sharedInstance = new HRPatchAttributeTable();
    }

    public static void destroy() {
        sharedInstance.patchTable.clear();
        sharedInstance = null;
    }

    private HashMap<String, HRPatchAttribute> patchTable = new HashMap<String, HRPatchAttribute>();
    private List<HRPatchAttribute> patchList = new LinkedList<>();

    private int depthIndex;

    private HRPatchAttributeTable() {
        this.depthIndex = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);
    }

    public void addPatchAttribute(int colStart, int rowStart, int colEnd, int rowEnd, String imageName, Mat patchMat) {

        if(this.patchTable.containsKey(imageName) == false) {
            HRPatchAttribute patchAttribute = new HRPatchAttribute(colStart, rowStart, colEnd, rowEnd, imageName, patchMat);
            this.patchTable.put(imageName, patchAttribute);
            this.patchList.add(patchAttribute);
        }
        else {
            Log.e(TAG, "Patch attribute "+imageName+ " already exists in the table!");
        }
    }

    public int getHRPatchCount() {
       return this.patchTable.size();
    }

    /*
    Returns the patch attribute at a specified index. This is in no particular order. It depends on
    how the hashmap points the index to a specified key.
     */
    public HRPatchAttribute getPatchAttributeAt(int patchIndex) {

        if(patchIndex < this.patchList.size()) {
            return this.patchList.get(patchIndex);
        }
        else {
            Log.e(TAG, "Patch index of " +patchIndex+ " exceeds the key size which is " +this.patchList.size());
            return null;
        }
    }
}
