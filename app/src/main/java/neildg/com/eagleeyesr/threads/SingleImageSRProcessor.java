package neildg.com.eagleeyesr.threads;

import neildg.com.eagleeyesr.processing.single.gaussian.ImageHRCreator;
import neildg.com.eagleeyesr.processing.single.gaussian.InputImageAssociator;
import neildg.com.eagleeyesr.processing.single.gaussian.PatchPairGenerator;
import neildg.com.eagleeyesr.processing.single.glasner.PostProcessImage;
import neildg.com.eagleeyesr.processing.single.glasner.GlasnerVariableSetup;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/4/2016.
 */
public class SingleImageSRProcessor extends Thread {
    private final static String TAG = "SingleSRPRocessor";

    public SingleImageSRProcessor() {

    }

    @Override
    public void run() {
        GlasnerVariableSetup setup = new GlasnerVariableSetup();
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
        imageHRCreator.perform();

        PostProcessImage postProcessImage = new PostProcessImage(imageHRCreator.getHrMat());
        postProcessImage.perform();

        ProgressDialogHandler.getInstance().hideProcessDialog();
    }
}
