package neildg.com.megatronsr.threads;

import android.graphics.Bitmap;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.single.ImagePatchMerging;
import neildg.com.megatronsr.processing.single.ImagePyramidBuilder;
import neildg.com.megatronsr.processing.single.PatchExtractCommander;
import neildg.com.megatronsr.processing.single.PatchSimilaritySearch;

/**
 * Created by NeilDG on 5/4/2016.
 */
public class SingleImageSRProcessor extends Thread {
    private final static String TAG = "SingleSRPRocessor";

    public SingleImageSRProcessor() {

    }

    @Override
    public void run() {
        ImagePyramidBuilder pyramidBuilder = new ImagePyramidBuilder();
        pyramidBuilder.perform();

        PatchExtractCommander commander = new PatchExtractCommander();
        commander.perform();

        PatchSimilaritySearch similaritySearch = new PatchSimilaritySearch();
        similaritySearch.perform();

        ImagePatchMerging patchMerging = new ImagePatchMerging();
        patchMerging.perform();
    }
}
