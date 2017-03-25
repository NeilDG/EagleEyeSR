package neildg.com.eagleeyesr.processing.multiple.selection;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.io.BitmapURIRepository;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.model.multiple.SharpnessMeasure;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.threads.ReleaseSRProcessor;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;

/**
 * Handles the scheme of selecting test input images and selecting a ground-truth based on sharpness index.
 * Ground-truth is removed from the input images to avoid bias. Use this operator for assessment mode only.
 * By: NeilDG
 * Created by NeilDG on 7/30/2016.
 */
public class TestImagesSelector implements IOperator {
    private final static String TAG = "TestImagesSelector";

    private Mat[] initialMatList;
    private Mat[] edgeMatList;

    private Mat[] outputMatList;
    private Mat[] outputEdgeList;

    private SharpnessMeasure.SharpnessResult sharpnessResult;

    public TestImagesSelector(Mat[] initialMatList, Mat[] edgeMatList, SharpnessMeasure.SharpnessResult sharpnessResult) {
        this.initialMatList = initialMatList;
        this.edgeMatList = edgeMatList;
        this.sharpnessResult = sharpnessResult;

        this.outputMatList = new Mat[this.initialMatList.length - 1];
    }

    @Override
    public void perform() {
        //get the best sharpness index and use it as ground-truth
        ProgressDialogHandler.getInstance().showProcessDialog("Assessment method", "Finding appropriate ground-truth");

        int bestIndex = this.sharpnessResult.getBestIndex();
        Bitmap groundTruthBitmap = BitmapURIRepository.getInstance().getOriginalBitmap(bestIndex);
        Bitmap referenceBitmap = BitmapURIRepository.getInstance().getOriginalBitmap(0);
        FileImageWriter.getInstance().saveBitmapImage(referenceBitmap, "reference_image", ImageFileAttribute.FileType.JPEG);
        referenceBitmap.recycle();

        FileImageWriter.getInstance().saveBitmapImage(groundTruthBitmap, FilenameConstants.GROUND_TRUTH_PREFIX_STRING+bestIndex, ImageFileAttribute.FileType.JPEG);
        groundTruthBitmap.recycle();

        Mat referenceMat = FileImageReader.getInstance().imReadOpenCV("reference_image", ImageFileAttribute.FileType.JPEG);
        Mat groundTruthMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.GROUND_TRUTH_PREFIX_STRING+bestIndex, ImageFileAttribute.FileType.JPEG);

        this.performGroundTruthAlignment(referenceMat, groundTruthMat, bestIndex);

        //remove best mat from input list
        List<Mat> filteredMatList = new ArrayList<>();
        List<Mat> edgeFilteredList = new ArrayList<>();
        for(int i = 0; i < this.initialMatList.length; i++) {
            if(i != bestIndex) {
                filteredMatList.add(this.initialMatList[i]);
                edgeFilteredList.add(this.edgeMatList[i]);
                Log.d(TAG, "Added image " +i);
            }
            else {
                FileImageWriter.getInstance().deleteImage(FilenameConstants.INPUT_PREFIX_STRING +i, ImageFileAttribute.FileType.JPEG);
            }
        }
        this.outputMatList = filteredMatList.toArray(new Mat[filteredMatList.size()]);
        this.edgeMatList = edgeFilteredList.toArray(new Mat[edgeFilteredList.size()]);
    }

    private void performGroundTruthAlignment(Mat referenceMat, Mat groundTruthMat, int index) {
        String[] warpResultnames = new String[1];
        warpResultnames[0] = FilenameConstants.GROUND_TRUTH_PREFIX_STRING + index + "_aligned";

        Mat[] imagesToAlignList = new Mat[1];
        imagesToAlignList[0] = groundTruthMat;

        ReleaseSRProcessor.performPerspectiveWarping(referenceMat, imagesToAlignList, imagesToAlignList, warpResultnames);
    }

    public Mat[] getProposedList() {
        return this.outputMatList;
    }

    public Mat[] getProposedEdgeList() {
        return this.edgeMatList;
    }
}
