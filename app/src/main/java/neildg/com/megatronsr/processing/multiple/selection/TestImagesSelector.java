package neildg.com.megatronsr.processing.multiple.selection;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.multiple.SharpnessMeasure;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

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
        ProgressDialogHandler.getInstance().showDialog("Assessment method", "Finding appropriate ground-truth");

        int bestIndex = this.sharpnessResult.getBestIndex();
        Bitmap bitmap = BitmapURIRepository.getInstance().getOriginalBitmap(bestIndex);
        ImageWriter.getInstance().saveBitmapImage(bitmap, FilenameConstants.GROUND_TRUTH_PREFIX_STRING+bestIndex, ImageFileAttribute.FileType.JPEG);
        bitmap.recycle();

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
                ImageWriter.getInstance().deleteImage(FilenameConstants.DOWNSAMPLE_PREFIX_STRING+i, ImageFileAttribute.FileType.JPEG);
            }
        }
        this.outputMatList = filteredMatList.toArray(new Mat[filteredMatList.size()]);
        this.edgeMatList = edgeFilteredList.toArray(new Mat[edgeFilteredList.size()]);

        ProgressDialogHandler.getInstance().hideDialog();
    }

    public Mat[] getProposedList() {
        return this.outputMatList;
    }

    public Mat[] getProposedEdgeList() {
        return this.edgeMatList;
    }
}
