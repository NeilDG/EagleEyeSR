package neildg.com.eagleeyesr.processing.single.glasner;

import android.util.Log;

import java.util.concurrent.Semaphore;

import neildg.com.eagleeyesr.model.AttributeHolder;
import neildg.com.eagleeyesr.model.AttributeNames;
import neildg.com.eagleeyesr.model.single_glasner.ImagePatch;
import neildg.com.eagleeyesr.model.single_glasner.ImagePatchPool;
import neildg.com.eagleeyesr.model.single_glasner.PatchAttribute;
import neildg.com.eagleeyesr.model.single_glasner.PatchAttributeTable;
import neildg.com.eagleeyesr.model.single_glasner.PatchRelationTable;
import neildg.com.eagleeyesr.number.MathUtils;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/9/2016.
 */
public class PatchSimilaritySearch implements IOperator {
    private final static String TAG = "PatchSimilaritySearch";

    private final int MAX_NUM_THREADS = 20;

    private Semaphore semaphore;

    public PatchSimilaritySearch() {
        ImagePatchPool.initialize();
        PatchRelationTable.initialize();
    }

    @Override
    public void perform() {
        int maxPyrDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);

        //create HR-LR relationship dictionary
        int patchesInLR = PatchAttributeTable.getInstance().getNumPatchesAtDepth(0);
        int divisionOfWork = patchesInLR / MAX_NUM_THREADS;
        int lowerX = 0;
        int upperX = divisionOfWork;
        int numThreadsCreated = 0;

        ProgressDialogHandler.getInstance().showProcessDialog("Comparing input image patches to its pyramid", "Running " +MAX_NUM_THREADS+ " threads.");
        this.semaphore = new Semaphore(0);
        while(lowerX <= patchesInLR) {

            PatchSearchWorker searchWorker =  new PatchSearchWorker(lowerX, upperX, numThreadsCreated, this);
            searchWorker.start();

            numThreadsCreated++;
            lowerX = upperX + 1;
            upperX += divisionOfWork;
            upperX = MathUtils.clamp(upperX, lowerX, patchesInLR);
        }

        try {
            this.semaphore.acquire(numThreadsCreated);
            PatchRelationTable.getSharedInstance().sort();
            PatchRelationTable.getSharedInstance().saveMapToJSON();
            ProgressDialogHandler.getInstance().hideProcessDialog();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void onReportCompleted() {
        this.semaphore.release();
    }

    public class PatchSearchWorker extends Thread {
        private final static String TAG = "PatchSearchWorker";

        private int lowerIndex = 0;
        private int upperIndex = 0;
        private int threadID = 0;
        private PatchSimilaritySearch similaritySearch;

        public PatchSearchWorker(int lowerIndex, int upperIndex, int threadID, PatchSimilaritySearch similaritySearch) {
            this.lowerIndex = lowerIndex;
            this.upperIndex = upperIndex;
            this.threadID = threadID;
            this.similaritySearch = similaritySearch;
        }

        @Override
        public void run() {
            double similarityThreshold = (double) AttributeHolder.getSharedInstance().getValue(AttributeNames.SIMILARITY_THRESHOLD_KEY, 0);
            for(int i = lowerIndex; i < upperIndex; i++) {
                PatchAttribute candidatePatchAttrib = PatchAttributeTable.getInstance().getPatchAttributeAt(0, i);
                ImagePatch candidatePatch = ImagePatchPool.getInstance().loadPatch(candidatePatchAttrib);

                int maxPyrDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);

                for(int depth = 1; depth < maxPyrDepth; depth++) {
                    int patchesAtDepth = PatchAttributeTable.getInstance().getNumPatchesAtDepth(depth);

                    for(int p = 0; p < patchesAtDepth; p++) {
                        PatchAttribute comparingPatchAttrib = PatchAttributeTable.getInstance().getPatchAttributeAt(depth, p);

                        /*if(comparingPatchAttrib != null)*/ {
                            ImagePatch comparingPatch = ImagePatchPool.getInstance().loadPatch(comparingPatchAttrib);

                            double similarity = ImagePatchPool.getInstance().measureSimilarity(candidatePatch, comparingPatch);

                            if(similarity < similarityThreshold) {
                                similarity = similarityThreshold;
                                PatchRelationTable.getSharedInstance().addPairwisePatch(comparingPatchAttrib, candidatePatchAttrib, similarity);
                                Log.d(TAG, "Found by: "+this.threadID+ " Patch " +candidatePatch.getImageName()+ " vs Patch " +comparingPatch.getImageName()+ " similarity: " +similarity);
                                break;
                            }
                        }

                    }
                }
            }

            this.similaritySearch.onReportCompleted();


        }

    }
}
