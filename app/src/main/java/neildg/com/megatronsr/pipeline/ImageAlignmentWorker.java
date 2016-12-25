package neildg.com.megatronsr.pipeline;

import org.opencv.core.Mat;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.processing.imagetools.MatMemory;
import neildg.com.megatronsr.processing.multiple.alignment.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.alignment.LRWarpingOperator;
import neildg.com.megatronsr.processing.multiple.alignment.MedianAlignmentOperator;
import neildg.com.megatronsr.processing.multiple.alignment.WarpingConstants;

/**
 * Handles the image alignment technique.
 * Created by NeilDG on 12/25/2016.
 */

public class ImageAlignmentWorker extends AImageWorker {
    public final static String TAG = "ImageAlignmentWorker";

    public final static String IMAGE_REFERENCE_NAME_KEY = "IMAGE_REFERENCE_NAME_KEY";
    public final static String IMAGE_COMPARE_NAME_KEY = "IMAGE_COMPARE_NAME_KEY";
    public final static String IS_FIRST_IMAGE_KEY = "IS_FIRST_IMAGE_KEY";

    private String referenceImageName;
    private String comparingImageName;

    public ImageAlignmentWorker(String workerName, WorkerListener workerListener) {
        super(workerName, workerListener);
    }
    @Override
    public void perform() {
        //perform feature matching of LR images against the first image as reference mat.
        int warpChoice = ParameterConfig.getPrefsInt(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.BEST_ALIGNMENT);
        Mat[] rgbInputMatList = new Mat[2];
        rgbInputMatList[0] = FileImageReader.getInstance().imReadOpenCV(this.referenceImageName, ImageFileAttribute.FileType.JPEG);
        rgbInputMatList[1] = FileImageReader.getInstance().imReadOpenCV(this.referenceImageName, ImageFileAttribute.FileType.JPEG);

        Mat[] succeedingMatList =new Mat[rgbInputMatList.length - 1];
        for(int i = 1; i < rgbInputMatList.length; i++) {
            succeedingMatList[i - 1] = rgbInputMatList[i];
        }

        if(warpChoice == WarpingConstants.BEST_ALIGNMENT) {
            this.performMedianAlignment(rgbInputMatList);
            this.performPerspectiveWarping(rgbInputMatList[0], succeedingMatList, succeedingMatList);
        }
        else if(warpChoice == WarpingConstants.PERSPECTIVE_WARP) {
            this.performPerspectiveWarping(rgbInputMatList[0], succeedingMatList, succeedingMatList);
        }
        else if(warpChoice == WarpingConstants.MEDIAN_ALIGNMENT){
            this.performMedianAlignment(rgbInputMatList);
        }
    }

    @Override
    public boolean evaluateCondition() {
        this.referenceImageName = this.ingoingProperties.getStringExtra(IMAGE_REFERENCE_NAME_KEY, null);
        this.comparingImageName = this.ingoingProperties.getStringExtra(IMAGE_COMPARE_NAME_KEY, null);
        this.ingoingProperties.clearAll();

        return (this.referenceImageName != null && this.comparingImageName != null && this.referenceImageName != this.comparingImageName); //image alignment will start for the respective image if  it's not the first image.
    }

    private void performPerspectiveWarping(Mat referenceMat, Mat[] candidateMatList, Mat[] imagesToWarpList) {
        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator(referenceMat, candidateMatList);
        matchingOperator.perform();

        LRWarpingOperator perspectiveWarpOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), imagesToWarpList, matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        perspectiveWarpOperator.perform();

        //release images
        matchingOperator.getRefKeypoint().release();
        MatMemory.releaseAll(matchingOperator.getdMatchesList(), false);
        MatMemory.releaseAll(matchingOperator.getLrKeypointsList(), false);
        MatMemory.releaseAll(candidateMatList, false);
        MatMemory.releaseAll(imagesToWarpList, false);

        Mat[] warpedMatList = perspectiveWarpOperator.getWarpedMatList();
        MatMemory.releaseAll(warpedMatList, false);
    }

    private void performMedianAlignment(Mat[] imagesToAlignList) {
        //perform exposure alignment
        MedianAlignmentOperator medianAlignmentOperator = new MedianAlignmentOperator(imagesToAlignList);
        medianAlignmentOperator.perform();

        //MatMemory.releaseAll(imagesToAlignList, true);
    }
}
