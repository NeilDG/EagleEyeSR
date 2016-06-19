package neildg.com.megatronsr.processing.single.glasner;

import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.processing.IOperator;

/**
 * Created by neil.dg on 5/19/16.
 */
public class GlasnerVariableSetup implements IOperator{
    private final static String TAG = "VariableSetup";

    public GlasnerVariableSetup() {

    }

    @Override
    public void perform() {
        AttributeHolder.getSharedInstance().reset();
        AttributeHolder.getSharedInstance().putValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 7);
        AttributeHolder.getSharedInstance().putValue(AttributeNames.SIMILARITY_THRESHOLD_KEY, 0.0001);
        AttributeHolder.getSharedInstance().putValue(AttributeNames.PATCH_SIZE_KEY, 5);
    }
}
