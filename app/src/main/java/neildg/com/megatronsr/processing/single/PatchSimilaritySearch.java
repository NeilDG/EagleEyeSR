package neildg.com.megatronsr.processing.single;

import android.util.Log;

import java.util.concurrent.Semaphore;

import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.model.single.ImagePatch;
import neildg.com.megatronsr.model.single.ImagePatchPool;
import neildg.com.megatronsr.model.single.PatchAttribute;
import neildg.com.megatronsr.model.single.PatchAttributeTable;
import neildg.com.megatronsr.model.single.PatchRelationTable;
import neildg.com.megatronsr.number.MathUtils;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/9/2016.
 */
public class PatchSimilaritySearch implements IOperator {
    private final static String TAG = "PatchSimilaritySearch";

    private final int MAX_NUM_THREADS = 20;
    private int partition = 0;

    private int actualThreadCreated = 0;
    private int finishedThreads = 0;

    private Semaphore semaphore = new Semaphore(0);

    public PatchSimilaritySearch() {
        ImagePatchPool.initialize();
        PatchRelationTable.initialize();
    }

    @Override
    public void perform() {
        int maxPyrDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);

        //create HR-LR relationship dictionary
        int patchesInLR = PatchAttributeTable.getInstance().getNumPatchesAtDepth(0);

        /*for(int i = 0; i < patchesInLR; i++) {
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
        }*/

        int divisionOfWork = patchesInLR / MAX_NUM_THREADS;
        int lowerX = 0;
        int upperX = divisionOfWork;
        int threadID = 0;

        ProgressDialogHandler.getInstance().showDialog("Comparing input image patches to its pyramid", "Running " +MAX_NUM_THREADS+ " threads.");

        while(lowerX <= patchesInLR) {

            PatchSearchWorker searchWorker =  new PatchSearchWorker(lowerX, upperX, threadID, this);
            searchWorker.start();

            threadID++; this.actualThreadCreated++;
            lowerX = upperX + 1;
            upperX += divisionOfWork;
            upperX = MathUtils.clamp(upperX, lowerX, patchesInLR);
        }

        try {
            this.semaphore.acquire();
            PatchRelationTable.getSharedInstance().saveMapToJSON();
            ProgressDialogHandler.getInstance().hideDialog();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void onReportCompleted(PatchSearchWorker thread) {
        this.finishedThreads++;

        if(this.finishedThreads >= this.actualThreadCreated) {
            this.semaphore.release();
        }
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
            for(int i = lowerIndex; i < upperIndex; i++) {
                PatchAttribute candidatePatchAttrib = PatchAttributeTable.getInstance().getPatchAttributeAt(0, i);
                ImagePatch candidatePatch = ImagePatchPool.getInstance().loadPatch(candidatePatchAttrib);

                int maxPyrDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);

                for(int depth = 1; depth < maxPyrDepth; depth++) {
                    int patchesAtDepth = PatchAttributeTable.getInstance().getNumPatchesAtDepth(depth);

                    for(int p = 0; p < 5; p++) {
                        PatchAttribute comparingPatchAttrib = PatchAttributeTable.getInstance().getPatchAttributeAt(depth, p);

                        if(comparingPatchAttrib != null) {
                            ImagePatch comparingPatch = ImagePatchPool.getInstance().loadPatch(comparingPatchAttrib);

                            double similarity = ImagePatchPool.getInstance().measureSimilarity(candidatePatch, comparingPatch);

                            if(similarity <= 0.0005) {
                                PatchRelationTable.getSharedInstance().addPairwisePatch(comparingPatchAttrib, candidatePatchAttrib, similarity);
                                Log.d(TAG, "Found by: "+this.threadID+ " Patch " +candidatePatch.getImageName()+ " vs Patch " +comparingPatch.getImageName()+ " similarity: " +similarity);
                                break;
                            }
                        }

                    }
                }
            }

            this.similaritySearch.onReportCompleted(this);


        }

    }
}
