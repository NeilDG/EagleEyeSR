package neildg.com.eagleeyesr.processing.single.gaussian;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Semaphore;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.model.AttributeHolder;
import neildg.com.eagleeyesr.model.AttributeNames;
import neildg.com.eagleeyesr.model.single_gaussian.GausianPatchTable;
import neildg.com.eagleeyesr.model.single_gaussian.LoadedImagePatch;
import neildg.com.eagleeyesr.number.MathUtils;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ImageMeasures;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;
import neildg.com.eagleeyesr.processing.single.gaussian.listeners.ThreadFinishedListener;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;

/**
 * Upsamples the input mat by inteprolation and replaces similar patches with better HR versions
 * Created by NeilDG on 5/23/2016.
 */
public class ImageHRCreator implements IOperator, ThreadFinishedListener {
    private final static String TAG = "ImageHRCreator";

    private final int MAX_NUM_THREADS = 10;

    private Semaphore semaphore;
    private Mat hrMat;

    public ImageHRCreator() {

    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showProcessDialog("Upsampling", "Upsampling image");
        int scalingFactor = ParameterConfig.getScalingFactor();

        Mat originalMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_GAUSSIAN_DIR + "/" + FilenameConstants.INPUT_FILE_NAME, ImageFileAttribute.FileType.JPEG);
        this.hrMat = ImageOperator.performInterpolation(originalMat, scalingFactor, Imgproc.INTER_CUBIC);

        originalMat.release();

        FileImageWriter.getInstance().saveMatrixToImage(hrMat, FilenameConstants.RESULTS_DIR, FilenameConstants.RESULTS_CUBIC, ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().showProcessDialog("Replacing patches", "Searching for found patches in table and replacing them.");
        this.createPatchPairs();
    }

    public Mat getHrMat() {
        return this.hrMat;
    }

    private void createPatchPairs() {
        int divisionOfWork = this.hrMat.rows() / MAX_NUM_THREADS;
        int lowerX = 0;
        int upperX = divisionOfWork;
        int threadCreated = 0;

        while(lowerX <= this.hrMat.rows()) {

            PatchReplaceWorker patchReplaceWorker = new PatchReplaceWorker(this.hrMat, lowerX, upperX, this);
            patchReplaceWorker.start();

            threadCreated++;
            lowerX = upperX + 1;
            upperX += divisionOfWork;
            upperX = MathUtils.clamp(upperX, lowerX, this.hrMat.rows());
        }

        this.semaphore = new Semaphore(0);

        try {
            this.semaphore.acquire(threadCreated);
            ProgressDialogHandler.getInstance().hideProcessDialog();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onThreadCompleted() {
        this.semaphore.release();
    }

    private class PatchReplaceWorker extends Thread {

        private Mat outputMat;

        private int lowerIndex;
        private int upperindex;

        private ThreadFinishedListener finishedListener;

        public PatchReplaceWorker(Mat outputMat, int lowerIndex, int upperIndex, ThreadFinishedListener finishedListener) {
            this.outputMat = outputMat;
            this.lowerIndex = lowerIndex;
            this.upperindex = upperIndex;

            this.finishedListener = finishedListener;
        }
        @Override
        public void run() {
            int patchSize = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.PATCH_SIZE_KEY, 0);

            for(int col = 0; col < this.outputMat.cols(); col+=patchSize) {
                for(int row = lowerIndex; row < upperindex; row+=patchSize) {

                    LoadedImagePatch imagePatch = new LoadedImagePatch(this.outputMat, patchSize, col, row);
                    this.searchForSimilarPatches(imagePatch);
                }
            }

            this.finishedListener.onThreadCompleted();
        }

        private void searchForSimilarPatches(LoadedImagePatch imagePatch) {
            GausianPatchTable gausianPatchTable = GausianPatchTable.getSharedInstance();

            double threshold = (double) AttributeHolder.getSharedInstance().getValue(AttributeNames.SIMILARITY_THRESHOLD_KEY, 0);

            for(int i = 0; i < gausianPatchTable.getCount(); i++) {
                double similarity = ImageMeasures.measureMATSimilarity(imagePatch.getPatchMat(), gausianPatchTable.getLRPatchAt(i).getPatchMat());
                if(similarity <= threshold) {
                    this.replacePatchOnROI(imagePatch, gausianPatchTable.getHRPatchAt(i));
                    Log.d(TAG, "Image patch has a similar LR patch. Similarity: "+similarity);
                }
            }
        }

        private void replacePatchOnROI(LoadedImagePatch lrPatch, LoadedImagePatch replacementPatch) {

            if(lrPatch.getColStart() >= 0 && lrPatch.getRowEnd() < this.outputMat.rows() && lrPatch.getRowStart() >= 0 && lrPatch.getColEnd() < this.outputMat.cols()) {
                Mat subMat = this.outputMat.submat(lrPatch.getColStart(),lrPatch.getRowEnd(), lrPatch.getRowStart(), lrPatch.getColEnd());

                /*Mat test = Mat.ones(80,80,subMat.type());
                test.copyTo(subMat);*/
                replacementPatch.getPatchMat().copyTo(subMat);
            }
        }
    }
}
