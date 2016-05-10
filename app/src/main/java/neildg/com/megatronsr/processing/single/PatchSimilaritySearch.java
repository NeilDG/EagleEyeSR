package neildg.com.megatronsr.processing.single;

import android.util.Log;

import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.model.single.ImagePatch;
import neildg.com.megatronsr.model.single.ImagePatchPool;
import neildg.com.megatronsr.model.single.PatchAttribute;
import neildg.com.megatronsr.model.single.PatchAttributeTable;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/9/2016.
 */
public class PatchSimilaritySearch implements IOperator {
    private final static String TAG = "PatchSimilaritySearch";


    public PatchSimilaritySearch() {
        ImagePatchPool.initialize();
    }

    @Override
    public void perform() {
        Thread.currentThread().setName("PatchSimilaritySearch");
        int pyramidDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);

        //TODO: testing only
        //PATCH_DIR + this.index, PATCH_PREFIX+col+"_"+row

        ProgressDialogHandler.getInstance().showDialog("Test loading patches", "");

        for(int i = 0; i < pyramidDepth; i++) {
            int numPatches = PatchAttributeTable.getInstance().getNumPatchesAtDepth(i);
            for(int patchIndex = 0; patchIndex < numPatches; patchIndex++) {
               PatchAttribute attribute = PatchAttributeTable.getInstance().getPatchAttributeAt(i, patchIndex);

                ImagePatchPool.getInstance().loadPatch(i, attribute.getCol(), attribute.getRow(), attribute.getImageName(), attribute.getImagePath());
                Log.d(TAG, "Image patch size: " +ImagePatchPool.getInstance().getLoadedPatches());
            }
        }

        ProgressDialogHandler.getInstance().hideDialog();

    }
}
