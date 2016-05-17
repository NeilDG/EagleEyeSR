package neildg.com.megatronsr.model.single;

import android.util.Log;

import java.util.ArrayList;
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
        sharedInstance.patchList.clear();
        sharedInstance = null;
    }

    private HashMap<String, PatchAttribute> patchList = new HashMap<String, PatchAttribute>();
    private int depthIndex;

    private HRPatchAttributeTable() {
        this.depthIndex = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);
    }

    public void addPatchAttribute(int colStart, int rowStart, int colEnd, int rowEnd, String imageName, String imagePath) {

        if(this.patchList.containsKey(imageName) == false) {
            PatchAttribute patchAttribute = new PatchAttribute(this.depthIndex, colStart, rowStart, colEnd, rowEnd, imageName, imagePath);
            this.patchList.put(imageName, patchAttribute);
        }
        else {
            Log.e(TAG, "Patch attribute "+imageName+ " already exists in the table!");
        }
    }

    public int getHRPatchCount() {
       return this.patchList.size();
    }

    /*
    Returns the patch attribute at a specified index. This is in no particular order. It depends on
    how the hashmap points the index to a specified key.
     */
    public PatchAttribute getPatchAttributeAt(int patchIndex) {

        List keys = new ArrayList(this.patchList.keySet());

        if(patchIndex < keys.size()) {
            return this.patchList.get(keys.get(patchIndex));
        }
        else {
            Log.e(TAG, "Patch index of " +patchIndex+ " exceeds the key size which is " +keys.size());
            return null;
        }
    }
}
