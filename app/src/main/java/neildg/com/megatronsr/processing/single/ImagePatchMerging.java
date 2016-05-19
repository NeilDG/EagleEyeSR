package neildg.com.megatronsr.processing.single;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Semaphore;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.model.single.HRPatchAttributeTable;
import neildg.com.megatronsr.model.single.ImagePatch;
import neildg.com.megatronsr.model.single.ImagePatchPool;
import neildg.com.megatronsr.model.single.PatchAttribute;
import neildg.com.megatronsr.model.single.PatchAttributeTable;
import neildg.com.megatronsr.model.single.PatchRelation;
import neildg.com.megatronsr.model.single.PatchRelationTable;
import neildg.com.megatronsr.number.MathUtils;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/15/2016.
 */
public class ImagePatchMerging implements IOperator {
    private final static String TAG = "";

    private Mat hrMat;
    private final int MAX_NUM_THREADS = 20;

    private int actualThreadCreated = 0;
    private int finishedThreads = 0;

    private Semaphore semaphore = new Semaphore(0);

    public ImagePatchMerging() {
        HRPatchAttributeTable.initialize();
    }

    @Override
    public void perform() {
        //ImageWriter.getInstance().saveBitmapImage(originalBitmap, FilenameConstants.PYRAMID_DIR, FilenameConstants.PYRAMID_IMAGE_PREFIX + "0", ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().showDialog("Creating HR image", "Creating HR image");

        String fullImagePath = FilenameConstants.PYRAMID_DIR +"/"+ FilenameConstants.PYRAMID_IMAGE_PREFIX + "0";
        Mat lrMat = ImageReader.getInstance().imReadOpenCV(fullImagePath, ImageFileAttribute.FileType.JPEG);
        this.hrMat = Mat.zeros(lrMat.rows() * ParameterConfig.getScalingFactor(), lrMat.cols() * ParameterConfig.getScalingFactor(), lrMat.type());
        Imgproc.resize(lrMat, this.hrMat, this.hrMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);

        ImageWriter.getInstance().saveMatrixToImage(this.hrMat, FilenameConstants.RESULTS_DIR, FilenameConstants.RESULTS_CUBIC, ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().hideDialog();

        this.extractHRPatches();
        ImagePatchPool.getInstance().unloadAllPatches();
        System.gc();

        this.identifyPatchSimilarities();
        ImageWriter.getInstance().saveMatrixToImage(this.hrMat, FilenameConstants.RESULTS_DIR, FilenameConstants.RESULTS_GLASNER, ImageFileAttribute.FileType.JPEG);
    }

    private void extractHRPatches() {
        ProgressDialogHandler.getInstance().showDialog("Extracting image patches", "Extracting image patches in upsampled image.");
        /*for(int col = 0; col < this.hrMat.cols(); col+=80) {
            for(int row = 0; row < this.hrMat.rows(); row+=80) {

                Point point = new Point(col, row);
                Mat patchMat = new Mat();
                Imgproc.getRectSubPix(this.hrMat, new Size(80,80), point, patchMat);

                String patchDir = PatchExtractCommander.PATCH_DIR + "hr";
                String patchImageName = PatchExtractCommander.PATCH_PREFIX +col+"_"+row;
                String patchImagePath =  patchDir + "/" +patchImageName;
                ImageWriter.getInstance().saveMatrixToImage(patchMat, patchDir,patchImageName, ImageFileAttribute.FileType.JPEG);
                HRPatchAttributeTable.getInstance().addPatchAttribute(col, row, patchImageName, patchImagePath);
                patchMat.release();
            }

        }
                ProgressDialogHandler.getInstance().hideDialog();
                */

        int divisionOfWork = this.hrMat.rows() / MAX_NUM_THREADS;
        int lowerX = 0;
        int upperX = divisionOfWork;
        int threadID = 0;

        while(lowerX <= this.hrMat.rows()) {
            HRPatchExtractor extractor = new HRPatchExtractor(this.hrMat, lowerX, upperX, threadID, this);
            extractor.start();

            threadID++; this.actualThreadCreated++;
            lowerX = upperX + 1;
            upperX += divisionOfWork;
            upperX = MathUtils.clamp(upperX, lowerX, this.hrMat.rows());
        }

        try {
            this.semaphore.acquire();
            ProgressDialogHandler.getInstance().hideDialog();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void identifyPatchSimilarities() {
        ProgressDialogHandler.getInstance().showDialog("Replacing patches", "Replacing patches with better ones.");

        int numHRPatches = HRPatchAttributeTable.getInstance().getHRPatchCount();
        int divisionOfWork = numHRPatches / MAX_NUM_THREADS;
        int lowerX = 0;
        int upperX = divisionOfWork;
        int threadID = 0;

        while(lowerX <= numHRPatches) {

            PatchReplaceWorker patchReplaceWorker = new PatchReplaceWorker(lowerX, upperX, threadID, this, this.hrMat);
            patchReplaceWorker.start();

            threadID++; this.actualThreadCreated++;
            lowerX = upperX + 1;
            upperX += divisionOfWork;
            upperX = MathUtils.clamp(upperX, lowerX, numHRPatches);
        }

        try {
            this.semaphore.acquire();
            ProgressDialogHandler.getInstance().hideDialog();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onReportCompleted() {
        this.finishedThreads++;

        if(this.finishedThreads >= this.actualThreadCreated) {
            this.semaphore.release();
            this.actualThreadCreated = 0;
            this.finishedThreads = 0;
        }
    }

    private class HRPatchExtractor extends Thread {
        private final static String TAG = "HRPatchExtractor";

        private int lowerIndex;
        private int upperIndex;
        private int ID;
        private ImagePatchMerging patchMerging;
        private Mat hrMat;

        public HRPatchExtractor(Mat hrMat, int lowerIndex, int upperIndex, int ID, ImagePatchMerging patchMerging) {
            this.hrMat = hrMat;
            this.lowerIndex = lowerIndex;
            this.upperIndex = upperIndex;
            this.ID = ID;
            this.patchMerging = patchMerging;
        }

        @Override
        public void run() {
            for(int col = 0; col < this.hrMat.cols(); col+=80) {
                for(int row = lowerIndex; row < upperIndex; row+=80) {

                    Point point = new Point(col, row);
                    Mat patchMat = new Mat();
                    Imgproc.getRectSubPix(this.hrMat, new Size(80,80), point, patchMat);

                    String patchDir = PatchExtractCommander.PATCH_DIR + "hr";
                    String patchImageName = PatchExtractCommander.PATCH_PREFIX +col+"_"+row;
                    String patchImagePath =  patchDir + "/" +patchImageName;
                    ImageWriter.getInstance().saveMatrixToImage(patchMat, patchDir,patchImageName, ImageFileAttribute.FileType.JPEG);
                    HRPatchAttributeTable.getInstance().addPatchAttribute(col, row, col + 80, row + 80, patchImageName, patchImagePath);
                    patchMat.release();
                }
            }

            this.patchMerging.onReportCompleted();
        }
    }

    private class PatchReplaceWorker extends Thread {
        private final static String TAG = "PatchReplaceWorker";

        private int lowerIndex;
        private int upperIndex;
        private int ID;
        private ImagePatchMerging patchMerging;
        private Mat hrMat;

        public PatchReplaceWorker(int lowerIndex, int upperIndex, int ID, ImagePatchMerging patchMerging, Mat hrMat) {
            this.lowerIndex = lowerIndex;
            this.upperIndex = upperIndex;
            this.ID = ID;
            this.patchMerging = patchMerging;
            this.hrMat = hrMat;
        }

        @Override
        public void run() {
            double similarityThreshold = (double) AttributeHolder.getSharedInstance().getValue(AttributeNames.SIMILARITY_THRESHOLD_KEY, 0);

            PatchRelationTable relationTable = PatchRelationTable.getSharedInstance();

            for(int i = lowerIndex; i < upperIndex; i++) {
                PatchAttribute hrPatchAttrib = HRPatchAttributeTable.getInstance().getPatchAttributeAt(i);
                ImagePatch initialHRPatch = ImagePatchPool.getInstance().loadPatch(hrPatchAttrib);

                int pairCount = relationTable.getPairCount();
                for(int relation = 0; relation < pairCount; relation++) {
                    PatchRelationTable.PatchRelationList relationList = relationTable.getPatchRelationAt(relation);
                    PatchRelation patchRelation = relationList.getPatchRelationAt(0); //test only. TODO: check  all patch relations found as well

                    //load LR patch and see if it's similar to the HR patch
                    ImagePatch lrPatch = ImagePatchPool.getInstance().loadPatch(patchRelation.getLrAttrib());

                    double similarity = ImagePatchPool.getInstance().measureSimilarity(initialHRPatch, lrPatch);
                    if(similarity <= similarityThreshold) {
                        Log.d(TAG , "Found a similar patch in relation table by thread " +this.ID+ ". Similarity " +similarity);
                        this.replacePatchOnROI(hrPatchAttrib, patchRelation.getHrAttrib());
                        break;
                    }
                }
            }

            this.patchMerging.onReportCompleted();
        }

        private void replacePatchOnROI(PatchAttribute hrPatchAttrib, PatchAttribute hrReplacementAttrib) {

            if(hrPatchAttrib.getRowStart() >= 0 && hrPatchAttrib.getRowEnd() < this.hrMat.rows() && hrPatchAttrib.getColStart() >= 0 && hrPatchAttrib.getColEnd() < this.hrMat.cols()) {
                Mat subMat = this.hrMat.submat(hrPatchAttrib.getRowStart(),hrPatchAttrib.getRowEnd(), hrPatchAttrib.getColStart(), hrPatchAttrib.getColEnd());
                ImagePatch hrPatch = ImagePatchPool.getInstance().loadPatch(hrReplacementAttrib);

                Mat test = Mat.ones(80,80,subMat.type());
                test.copyTo(subMat);

            }
        }

    }
}
