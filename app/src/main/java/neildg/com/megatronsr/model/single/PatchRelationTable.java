package neildg.com.megatronsr.model.single;

import android.util.Log;

import java.util.HashMap;

import neildg.com.megatronsr.io.JSONSaver;

/**
 *
 * Holds the HR-LR patch dictionary pairing
 * Created by NeilDG on 5/10/2016.
 */
public class PatchRelationTable {
    private final static String TAG = "PatchRelationTable";

    private static PatchRelationTable sharedInstance = null;
    public static PatchRelationTable  getSharedInstance() {
        return sharedInstance;
    }
    public static void initialize() {
        sharedInstance = new PatchRelationTable();
    }

    public static void destroy() {
        sharedInstance = null;
    }

    private HashMap<PatchAttribute, PatchAttribute> pairwiseTable = new HashMap<PatchAttribute, PatchAttribute>();

    public void addPairwisePatch(PatchAttribute lrAttrib, PatchAttribute hrAttrib) {
        if(this.pairwiseTable.containsKey(lrAttrib) == false) {
            this.pairwiseTable.put(lrAttrib, hrAttrib);
        }
        else {
            Log.e(TAG, "Pairwise table of " +lrAttrib.getImageName()+ " and its HR match " +this.pairwiseTable.get(lrAttrib).getImageName()+ " already exists.");
        }
    }

    public boolean hasHRAttribute(PatchAttribute lrAttrib) {
        return this.pairwiseTable.containsKey(lrAttrib);
    }

    public PatchAttribute getHRAttribute(PatchAttribute lrAttrib) {
        if(this.hasHRAttribute(lrAttrib)) {
            return this.pairwiseTable.get(lrAttrib);
        }
        else {
            Log.e(TAG, " LR attribute " +lrAttrib.getImageName()+ " does not exist.");
            return null;
        }
    }

    public void saveMapToJSON() {
        JSONSaver.writeSimilarPatches("patch_table", this.pairwiseTable);
    }

}
