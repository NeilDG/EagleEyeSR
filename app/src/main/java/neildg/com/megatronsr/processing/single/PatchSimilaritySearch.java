package neildg.com.megatronsr.processing.single;

import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.model.single.ImagePatch;
import neildg.com.megatronsr.model.single.ImagePatchPool;
import neildg.com.megatronsr.processing.IOperator;

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
        int pyramidDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);
        
    }
}
