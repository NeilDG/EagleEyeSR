package neildg.com.megatronsr.model.single;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;

/**
 * Table holder and search utility class for patch attributes using indexing method.
 * Created by NeilDG on 5/10/2016.
 */
public class PatchAttributeTable {
    private final static String TAG = "PatchAttributeTable";
    private static PatchAttributeTable ourInstance = null;

    public static PatchAttributeTable getInstance() {
        return ourInstance;
    }

    private List<HashMap<String, PatchAttribute>> patchTable = new LinkedList<>();
    private List<List<PatchAttribute>> patchGroupList = new LinkedList<>();

    private int pyramidDepth = 0;

    private PatchAttributeTable() {
        this.pyramidDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);

        for(int i = 0; i <= this.pyramidDepth; i++) {
            this.patchTable.add(new HashMap<String, PatchAttribute>());
            this.patchGroupList.add(new LinkedList<PatchAttribute>());
        }
    }

    public static void initialize() {
        ourInstance = new PatchAttributeTable();
    }

    public static void destroy() {
        ourInstance.patchTable.clear();
        ourInstance.patchGroupList.clear();
        ourInstance = null;
    }

    public void addPatchAttribute(int pyramidDepth, int colStart, int rowStart, int colEnd, int rowEnd, String imageName, String imagePath) {
        HashMap<String, PatchAttribute> patchTable = this.patchTable.get(pyramidDepth);

        PatchAttribute patchAttribute = new PatchAttribute(pyramidDepth, colStart, rowStart, colEnd, rowEnd, imageName, imagePath);
        if(patchTable.containsKey(imageName) == false) {
            patchTable.put(imageName, patchAttribute);
            List<PatchAttribute> patchList = this.patchGroupList.get(pyramidDepth);
            patchList.add(patchAttribute);
        }
        else {
            Log.e(TAG, "Patch attribute "+imageName+ " already exists in the table!");
        }
    }

    public int getNumPatchesAtDepth(int pyramidDepth) {
        HashMap<String, PatchAttribute> patchTable = this.patchTable.get(pyramidDepth);
        return patchTable.size();
    }

    /*
    Returns the patch attribute at a specified index. This is in no particular order. It depends on
    how the hashmap points the index to a specified key.
     */
    public PatchAttribute getPatchAttributeAt(int pyramidDepth, int patchIndex) {
        List<PatchAttribute> patchList = this.patchGroupList.get(pyramidDepth);

        if(patchIndex < patchList.size()) {
            return patchList.get(patchIndex);
        }
        else {
            Log.e(TAG, "Patch index of " +patchIndex+ " exceeds the key size which is " +patchList.size());
            return null;
        }
    }
}
