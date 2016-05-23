package neildg.com.megatronsr.threads;

import neildg.com.megatronsr.processing.single.gaussian.ImageHRCreator;
import neildg.com.megatronsr.processing.single.gaussian.InputImageAssociator;
import neildg.com.megatronsr.processing.single.gaussian.PatchPairGenerator;
import neildg.com.megatronsr.processing.single.glasner.ImagePatchMerging;
import neildg.com.megatronsr.processing.single.glasner.ImagePyramidBuilder;
import neildg.com.megatronsr.processing.single.glasner.PatchExtractCommander;
import neildg.com.megatronsr.processing.single.glasner.PatchSimilaritySearch;
import neildg.com.megatronsr.processing.single.glasner.PostProcessImage;
import neildg.com.megatronsr.processing.single.glasner.VariableSetup;
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

        /*ImagePyramidBuilder pyramidBuilder = new ImagePyramidBuilder();
        pyramidBuilder.perform();

        PatchExtractCommander commander = new PatchExtractCommander();
        commander.perform();

        PatchSimilaritySearch similaritySearch = new PatchSimilaritySearch();
        similaritySearch.perform();

        ImagePatchMerging patchMerging = new ImagePatchMerging();
        patchMerging.perform();*/

        InputImageAssociator associator = new InputImageAssociator();
        associator.perform();

        PatchPairGenerator generator = new PatchPairGenerator();
        generator.perform();

        ImageHRCreator imageHRCreator = new ImageHRCreator();
        generator.perform();

        PostProcessImage postProcessImage = new PostProcessImage(imageHRCreator.getHrMat());
        postProcessImage.perform();

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
