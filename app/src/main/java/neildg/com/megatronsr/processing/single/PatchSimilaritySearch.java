package neildg.com.megatronsr.processing.single;

import android.util.Log;

import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.model.single.ImagePatch;
import neildg.com.megatronsr.model.single.ImagePatchPool;
import neildg.com.megatronsr.model.single.PatchAttribute;
import neildg.com.megatronsr.model.single.PatchAttributeTable;
import neildg.com.megatronsr.model.single.PatchRelationTable;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/9/2016.
 */
public class PatchSimilaritySearch implements IOperator {
    private final static String TAG = "PatchSimilaritySearch";

    private final int MAX_NUM_THREADS = 20;
    private int partition = 0;

    public PatchSimilaritySearch() {
        ImagePatchPool.initialize();
        PatchRelationTable.initialize();
    }

    @Override
    public void perform() {
        int maxPyrDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);

        //TODO: testing only
        //PATCH_DIR + this.index, PATCH_PREFIX+col+"_"+row

        /*ProgressDialogHandler.getInstance().showDialog("Test loading patches", "");

        for(int i = 0; i < pyramidDepth; i++) {
            int numPatches = PatchAttributeTable.getInstance().getNumPatchesAtDepth(i);
            for(int patchIndex = 0; patchIndex < numPatches; patchIndex++) {
               PatchAttribute attribute = PatchAttributeTable.getInstance().getPatchAttributeAt(i, patchIndex);

                ImagePatchPool.getInstance().loadPatch(i, attribute.getCol(), attribute.getRow(), attribute.getImageName(), attribute.getImagePath());
                Log.d(TAG, "Image patch size: " +ImagePatchPool.getInstance().getLoadedPatches());
            }
        }

        ProgressDialogHandler.getInstance().hideDialog();*/

        //create HR-LR relationship dictionary
        int patchesInLR = PatchAttributeTable.getInstance().getNumPatchesAtDepth(0);

        for(int i = 0; i < patchesInLR; i++) {
            ProgressDialogHandler.getInstance().showDialog("Comparing input image patch  to its pyramid", "Patch " +i);
            PatchAttribute candidatePatchAttrib = PatchAttributeTable.getInstance().getPatchAttributeAt(0, i);
            ImagePatch candidatePatch = ImagePatchPool.getInstance().loadPatch(candidatePatchAttrib);

            for(int depth = 1; depth < maxPyrDepth; depth++) {
                int patchesAtDepth = PatchAttributeTable.getInstance().getNumPatchesAtDepth(depth);

                for(int p = 0; p < patchesAtDepth; p++) {
                    PatchAttribute comparingPatchAttrib = PatchAttributeTable.getInstance().getPatchAttributeAt(depth, p);
                    ImagePatch comparingPatch = ImagePatchPool.getInstance().loadPatch(comparingPatchAttrib);

                    double similarity = ImagePatchPool.getInstance().measureSimilarity(candidatePatch, comparingPatch);

                    if(similarity <= 0.0005) {
                        Log.d(TAG, "Patch " +candidatePatch.getImageName()+ " vs Patch " +comparingPatch.getImageName()+ " similarity: " +similarity);

                    }

                }
            }
            ProgressDialogHandler.getInstance().hideDialog();
        }
    }
}
