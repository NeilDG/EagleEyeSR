package neildg.com.megatronsr.model.single;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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

    private HashMap<PatchAttribute, PatchRelationList> pairwiseTable = new HashMap<PatchAttribute, PatchRelationList>();
    private List<PatchRelationList> pairList = new ArrayList<PatchRelationList>();

    public void addPairwisePatch(PatchAttribute lrAttrib, PatchAttribute hrAttrib, double similarity) {
        if(this.pairwiseTable.containsKey(lrAttrib) == false) {
            PatchRelationList patchRelationList = new PatchRelationList();
            patchRelationList.addPatchRelation(lrAttrib, hrAttrib, similarity);
            this.pairwiseTable.put(lrAttrib, patchRelationList);
            this.pairList.add(patchRelationList);
        }
        else {
            PatchRelationList patchRelationList = this.pairwiseTable.get(lrAttrib);
            patchRelationList.addPatchRelation(lrAttrib, hrAttrib, similarity);

            //Log.e(TAG, "Pairwise table of " +lrAttrib.getImageName()+ " and its HR match " +hrAttrib.getImageName()+ " already exists. Adding to list");
        }
    }

    public void sort() {
        /*List<PatchAttribute> keys = new ArrayList<>(this.pairwiseTable.keySet());
        for(int i = 0; i < keys.size(); i++) {
            this.pairwiseTable.get(keys).sort();
        }*/

        for(int i = 0; i < this.pairList.size(); i++) {
            this.pairList.get(i).sort();
        }
    }

    public boolean hasHRAttribute(PatchAttribute lrAttrib) {
        return this.pairwiseTable.containsKey(lrAttrib);
    }

    public PatchAttribute getHRAttribute(PatchAttribute lrAttrib, int index) {
        if(this.hasHRAttribute(lrAttrib)) {
            PatchRelation patchRelation = this.pairwiseTable.get(lrAttrib).getPatchRelationAt(index);
            return patchRelation.getHrAttrib();
        }
        else {
            Log.e(TAG, " LR attribute " +lrAttrib.getImageName()+ " does not exist.");
            return null;
        }
    }

    public void saveMapToJSON() {
        JSONSaver.writeSimilarPatches("patch_table", this.pairwiseTable);
    }

    public int getPairCount() {
        return this.pairwiseTable.size();
    }

    public PatchRelationList getPatchRelationAt(int index) {
        return this.pairList.get(index);
    }



    public class PatchRelationList implements Comparator<PatchRelation> {
        private List<PatchRelation> patchRelations = new ArrayList<PatchRelation>();

        public PatchRelationList() {

        }

        public void addPatchRelation(PatchAttribute lrAttrib, PatchAttribute hrAttrib, double similarity) {
            PatchRelation patchRelation = new PatchRelation(lrAttrib, hrAttrib, similarity);
            this.patchRelations.add(patchRelation);
        }

        public void deleteHRPatch(PatchRelation patchRelation) {
            this.patchRelations.remove(patchRelation);
        }

        public void clear() {
            this.patchRelations.clear();
        }

        public int getCount() {
            return this.patchRelations.size();
        }

        public boolean hasPatches() {
            return (this.getCount() > 0);
        }

        public void sort() {
            Collections.sort(this.patchRelations, this);
        }

        @Override
        public int compare(PatchRelation t0, PatchRelation t1) {
            if(t0.getSimilarity() < t1.getSimilarity()) {
                return -1;
            }
            else if(t0.getSimilarity() > t1.getSimilarity()) {
                return 1;
            }
            else {
                return 0;
            }

        }

        public PatchRelation getPatchRelationAt(int index) {
            return this.patchRelations.get(index);
        }
    }
}
