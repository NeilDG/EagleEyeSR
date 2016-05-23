package neildg.com.megatronsr.processing.single.gaussian;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.concurrent.Semaphore;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.model.single_gaussian.GausianPatchTable;
import neildg.com.megatronsr.model.single_gaussian.LoadedImagePatch;
import neildg.com.megatronsr.model.single_glasner.HRPatchAttributeTable;
import neildg.com.megatronsr.number.MathUtils;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.single.gaussian.listeners.ThreadFinishedListener;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/23/2016.
 */
public class PatchPairGenerator implements IOperator, ThreadFinishedListener{
    private final static String TAG = "PatchPairGenerator";

    private final int MAX_NUM_THREADS = 10;
    private Semaphore semaphore;
    public PatchPairGenerator() {

    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Associating LR-HR", "Generating pairwise patches");

        GausianPatchTable.initialize();

        Mat originalMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_GAUSSIAN_DIR + "/" + FilenameConstants.INPUT_FILE_NAME, ImageFileAttribute.FileType.JPEG);
        Mat blurredMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_GAUSSIAN_DIR + "/" + FilenameConstants.INPUT_BLUR_FILENAME, ImageFileAttribute.FileType.JPEG);

        this.createPatchPairs(originalMat, blurredMat);
    }

    private void createPatchPairs(Mat originalMat, Mat blurredMat) {
        int numHRPatches = HRPatchAttributeTable.getInstance().getHRPatchCount();
        int divisionOfWork = numHRPatches / MAX_NUM_THREADS;
        int lowerX = 0;
        int upperX = divisionOfWork;
        int threadCreated = 0;

        while(lowerX <= numHRPatches) {

            PatchPairWorker patchPairWorker = new PatchPairWorker(originalMat, blurredMat, lowerX, upperX, this);
            patchPairWorker.start();

            threadCreated++;
            lowerX = upperX + 1;
            upperX += divisionOfWork;
            upperX = MathUtils.clamp(upperX, lowerX, numHRPatches);
        }

        this.semaphore = new Semaphore(0);

        try {
            this.semaphore.acquire(threadCreated);
            ProgressDialogHandler.getInstance().hideDialog();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onThreadCompleted() {
        this.semaphore.release();
    }

    private class PatchPairWorker extends Thread {

        private Mat originalMat;
        private Mat blurredMat;

        private int lowerIndex;
        private int upperindex;

        private ThreadFinishedListener finishedListener;

        public PatchPairWorker(Mat originalMat, Mat blurredMat, int lowerIndex, int upperIndex, ThreadFinishedListener finishedListener) {
            this.originalMat = originalMat;
            this.blurredMat = blurredMat;
            this.lowerIndex = lowerIndex;
            this.upperindex = upperIndex;

            this.finishedListener = finishedListener;
        }
        @Override
        public void run() {
            int patchSize = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.PATCH_SIZE_KEY, 0);

            for(int col = 0; col < this.originalMat.cols(); col+= patchSize) {
                for(int row = lowerIndex; row < upperindex; row+= patchSize) {

                    Size size = new Size(patchSize, patchSize);

                    LoadedImagePatch lrPatch = new LoadedImagePatch(this.blurredMat, size, row, col);
                    LoadedImagePatch hrPatch = new LoadedImagePatch(this.originalMat,size, row, col);

                    GausianPatchTable.getSharedInstance().addPairwisePatch(lrPatch, hrPatch);
                }
            }

            this.finishedListener.onThreadCompleted();
        }
    }
}
