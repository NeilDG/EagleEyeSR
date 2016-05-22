package neildg.com.megatronsr.threads;

import neildg.com.megatronsr.processing.single.ImagePatchMerging;
import neildg.com.megatronsr.processing.single.ImagePyramidBuilder;
import neildg.com.megatronsr.processing.single.PatchExtractCommander;
import neildg.com.megatronsr.processing.single.PatchSimilaritySearch;
import neildg.com.megatronsr.processing.single.PostProcessImage;
import neildg.com.megatronsr.processing.single.VariableSetup;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/4/2016.
 */
public class SingleImageSRProcessor extends Thread {
    private final static String TAG = "SingleSRPRocessor";

    public SingleImageSRProcessor() {

    }

    @Override
    public void run() {
        VariableSetup setup = new VariableSetup();
        setup.perform();

        ImagePyramidBuilder pyramidBuilder = new ImagePyramidBuilder();
        pyramidBuilder.perform();

        PatchExtractCommander commander = new PatchExtractCommander();
        commander.perform();

        PatchSimilaritySearch similaritySearch = new PatchSimilaritySearch();
        similaritySearch.perform();

        ImagePatchMerging patchMerging = new ImagePatchMerging();
        patchMerging.perform();

        PostProcessImage postProcessImage = new PostProcessImage(patchMerging.getOriginalHRMat());
        postProcessImage.perform();

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
