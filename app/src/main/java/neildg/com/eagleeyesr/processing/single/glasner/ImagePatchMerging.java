package neildg.com.eagleeyesr.processing.single.glasner;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Semaphore;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.model.AttributeHolder;
import neildg.com.eagleeyesr.model.AttributeNames;
import neildg.com.eagleeyesr.model.single_glasner.HRPatchAttribute;
import neildg.com.eagleeyesr.model.single_glasner.HRPatchAttributeTable;
import neildg.com.eagleeyesr.model.single_glasner.ImagePatch;
import neildg.com.eagleeyesr.model.single_glasner.ImagePatchPool;
import neildg.com.eagleeyesr.model.single_glasner.PatchAttribute;
import neildg.com.eagleeyesr.model.single_glasner.PatchRelation;
import neildg.com.eagleeyesr.model.single_glasner.PatchRelationTable;
import neildg.com.eagleeyesr.number.MathUtils;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ImageMeasures;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/15/2016.
 */
public class ImagePatchMerging implements IOperator {
    private final static String TAG = "";

    //private Mat hrIntensityMat; //mat of intensity value only
    private Mat originalHRMat;

    private final int MAX_NUM_THREADS = 5;


    private Semaphore semaphore;

    public ImagePatchMerging() {
        HRPatchAttributeTable.initialize();
    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showProcessDialog("Creating HR image", "Creating HR image");


        String fullImagePath = FilenameConstants.PYRAMID_DIR +"/"+ FilenameConstants.PYRAMID_IMAGE_PREFIX + "0";
        Mat lrMat = FileImageReader.getInstance().imReadOpenCV(fullImagePath, ImageFileAttribute.FileType.JPEG);
        this.originalHRMat = Mat.zeros(lrMat.rows() * ParameterConfig.getScalingFactor(), lrMat.cols() * ParameterConfig.getScalingFactor(), lrMat.type());
        Imgproc.resize(lrMat, this.originalHRMat, this.originalHRMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_LANCZOS4);
        FileImageWriter.getInstance().saveMatrixToImage(this.originalHRMat, FilenameConstants.RESULTS_DIR, FilenameConstants.RESULTS_CUBIC, ImageFileAttribute.FileType.JPEG);


        ProgressDialogHandler.getInstance().hideProcessDialog();

        this.extractHRPatches();
        ImagePatchPool.getInstance().unloadAllPatches();
        System.gc();

        this.identifyPatchSimilarities();

        FileImageWriter.getInstance().saveMatrixToImage(this.originalHRMat, FilenameConstants.RESULTS_DIR, FilenameConstants.RESULTS_GLASNER, ImageFileAttribute.FileType.JPEG);

    }
    public Mat getOriginalHRMat() {
        return this.originalHRMat;
    }

    private void extractHRPatches() {
        ProgressDialogHandler.getInstance().showProcessDialog("Extracting image patches", "Extracting image patches in upsampled image.");

        int divisionOfWork = this.originalHRMat.rows() / MAX_NUM_THREADS;
        int lowerX = 0;
        int upperX = divisionOfWork;
        int threadsCreated = 0;

        while(lowerX <= this.originalHRMat.rows()) {
            HRPatchExtractor extractor = new HRPatchExtractor(this.originalHRMat, lowerX, upperX, threadsCreated, this);
            extractor.start();

            threadsCreated++;
            lowerX = upperX + 1;
            upperX += divisionOfWork;
            upperX = MathUtils.clamp(upperX, lowerX, this.originalHRMat.rows());
        }

        this.semaphore = new Semaphore(0);

        try {
            this.semaphore.acquire(threadsCreated);
            ProgressDialogHandler.getInstance().hideProcessDialog();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void identifyPatchSimilarities() {
        ProgressDialogHandler.getInstance().showProcessDialog("Replacing patches", "Replacing patches with better ones.");

        int numHRPatches = HRPatchAttributeTable.getInstance().getHRPatchCount();
        int divisionOfWork = numHRPatches / MAX_NUM_THREADS;
        int lowerX = 0;
        int upperX = divisionOfWork;
        int threadCreated = 0;

        while(lowerX <= numHRPatches) {

            PatchReplaceWorker patchReplaceWorker = new PatchReplaceWorker(lowerX, upperX, threadCreated, this, this.originalHRMat);
            patchReplaceWorker.start();

            threadCreated++;
            lowerX = upperX + 1;
            upperX += divisionOfWork;
            upperX = MathUtils.clamp(upperX, lowerX, numHRPatches);
        }

        this.semaphore = new Semaphore(0);

        try {
            this.semaphore.acquire(threadCreated);
            ProgressDialogHandler.getInstance().hideProcessDialog();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onReportCompleted() {
       this.semaphore.release();
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
            int patchSize = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.PATCH_SIZE_KEY, 0);
            for(int col = 0; col < this.hrMat.cols(); col+=patchSize) {
                for(int row = lowerIndex; row < upperIndex; row+=patchSize) {

                    Point point = new Point(col, row);
                    Mat patchMat = new Mat();
                    Imgproc.getRectSubPix(this.hrMat, new Size(patchSize,patchSize), point, patchMat);

                    String patchDir = PatchExtractCommander.PATCH_DIR + "hr";
                    String patchImageName = PatchExtractCommander.PATCH_PREFIX +col+"_"+row;
                    String patchImagePath =  patchDir + "/" +patchImageName;
                    //ImageWriter.getInstance().saveMatrixToImage(patchMat, patchDir,patchImageName, ImageFileAttribute.FileType.JPEG);
                    HRPatchAttributeTable.getInstance().addPatchAttribute(col, row, col + patchSize, row + patchSize, patchImageName, patchMat);
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
                HRPatchAttribute hrPatchAttrib = HRPatchAttributeTable.getInstance().getPatchAttributeAt(i);
                if(hrPatchAttrib== null) {
                    continue;
                }

                int pairCount = relationTable.getPairCount();
                for(int relation = 0; relation < pairCount; relation++) {
                    PatchRelationTable.PatchRelationList relationList = relationTable.getPatchRelationAt(relation);
                    PatchRelation patchRelation = relationList.getPatchRelationAt(0); //test only. TODO: check  all patch relations found as well

                    //load LR patch and see if it's similar to the HR patch
                    ImagePatch lrPatch = ImagePatchPool.getInstance().loadPatch(patchRelation.getLrAttrib());

                    double similarity = ImageMeasures.measureMATSimilarity(hrPatchAttrib.getPatchMat(), lrPatch.getPatchMat());
                    if(similarity <= similarityThreshold) {
                        Log.d(TAG , "Found a similar patch in relation table by thread " +this.ID+ ". Similarity " +similarity);
                        this.replacePatchOnROI(hrPatchAttrib, patchRelation.getHrAttrib());
                        break;
                    }

                    //ImagePatchPool.getInstance().unloadPatch(patchRelation.getLrAttrib());
                }
            }

            this.patchMerging.onReportCompleted();
        }

        private void replacePatchOnROI(HRPatchAttribute hrPatchAttrib, PatchAttribute hrReplacementAttrib) {

            if(hrPatchAttrib.getRowStart() >= 0 && hrPatchAttrib.getRowEnd() < this.hrMat.rows() && hrPatchAttrib.getColStart() >= 0 && hrPatchAttrib.getColEnd() < this.hrMat.cols()) {
                Mat subMat = this.hrMat.submat(hrPatchAttrib.getRowStart(),hrPatchAttrib.getRowEnd(), hrPatchAttrib.getColStart(), hrPatchAttrib.getColEnd());
                ImagePatch hrPatch = ImagePatchPool.getInstance().loadPatch(hrReplacementAttrib);

                /*Mat test = Mat.ones(80,80,subMat.type());
                test.copyTo(subMat);*/
                hrPatch.getPatchMat().copyTo(subMat);

            }
        }

    }
}
