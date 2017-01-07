package neildg.com.eagleeyesr.pipeline.workers;

import org.opencv.core.Mat;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.pipeline.ImageProperties;
import neildg.com.eagleeyesr.pipeline.WorkerListener;
import neildg.com.eagleeyesr.processing.imagetools.MatMemory;
import neildg.com.eagleeyesr.processing.multiple.alignment.FeatureMatchingOperator;
import neildg.com.eagleeyesr.processing.multiple.alignment.LRWarpingOperator;
import neildg.com.eagleeyesr.processing.multiple.alignment.MedianAlignmentOperator;
import neildg.com.eagleeyesr.processing.multiple.alignment.WarpingConstants;
import neildg.com.eagleeyesr.threads.ReleaseSRProcessor;

/**
 * Handles the image alignment technique.
 * Created by NeilDG on 12/25/2016.
 */

public class ImageAlignmentWorker extends AImageWorker {
    public final static String TAG = "ImageAlignmentWorker";

    public final static String IMAGE_REFERENCE_NAME_KEY = "IMAGE_REFERENCE_NAME_KEY";
    public final static String IMAGE_COMPARE_NAME_KEY = "IMAGE_COMPARE_NAME_KEY";

    public final static String IMAGE_OUTPUT_NAME_KEY = "IMAGE_OUTPUT_NAME_KEY";

    private String referenceImageName;
    private String comparingImageName;

    private String selectedAlignedName;

    private int imageCounter = 0;

    public ImageAlignmentWorker(WorkerListener workerListener) {
        super(TAG, workerListener);
    }
    @Override
    public void perform() {
        //perform feature matching of LR images against the first image as reference mat.
        int warpChoice = ParameterConfig.getPrefsInt(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.BEST_ALIGNMENT);
        Mat[] rgbInputMatList = new Mat[2];
        rgbInputMatList[0] = FileImageReader.getInstance().imReadOpenCV(this.referenceImageName, ImageFileAttribute.FileType.JPEG);
        rgbInputMatList[1] = FileImageReader.getInstance().imReadOpenCV(this.comparingImageName, ImageFileAttribute.FileType.JPEG);

        String[] medianResultNames = new String[1]; String[] warpResultNames = new String[1];
        medianResultNames[0] = FilenameConstants.MEDIAN_ALIGNMENT_PREFIX + this.imageCounter;
        warpResultNames[0] = FilenameConstants.WARP_PREFIX + this.imageCounter;

        Mat[] succeedingMatList =new Mat[rgbInputMatList.length - 1];
        for(int i = 1; i < rgbInputMatList.length; i++) {
            succeedingMatList[i - 1] = rgbInputMatList[i];
        }

        if(warpChoice == WarpingConstants.BEST_ALIGNMENT) {
            this.performMedianAlignment(rgbInputMatList,medianResultNames);
            this.performPerspectiveWarping(rgbInputMatList[0], succeedingMatList, succeedingMatList, warpResultNames);


        }
        else if(warpChoice == WarpingConstants.PERSPECTIVE_WARP) {
            this.performPerspectiveWarping(rgbInputMatList[0], succeedingMatList, succeedingMatList, warpResultNames);
        }
        else if(warpChoice == WarpingConstants.MEDIAN_ALIGNMENT){
            this.performMedianAlignment(rgbInputMatList,medianResultNames);
        }

        String[] alignedImageNames = ReleaseSRProcessor.assessImageWarpResults(0, warpChoice, warpResultNames, medianResultNames, true);
        this.selectedAlignedName = alignedImageNames[0];

        this.imageCounter++;
        MatMemory.cleanMemory();
    }

    @Override
    public boolean evaluateCondition() {
        this.referenceImageName = this.ingoingProperties.getStringExtra(IMAGE_REFERENCE_NAME_KEY, null);
        this.comparingImageName = this.ingoingProperties.getStringExtra(IMAGE_COMPARE_NAME_KEY, null);
        this.ingoingProperties.clearAll();

        return (this.referenceImageName != null && this.comparingImageName != null && this.referenceImageName != this.comparingImageName); //image alignment will start for the respective image if  it's not the first image.
    }

    @Override
    public void populateOutgoingProperties(ImageProperties outgoingProperties) {
        outgoingProperties.putExtra(IMAGE_COMPARE_NAME_KEY, this.comparingImageName);
        outgoingProperties.putExtra(IMAGE_OUTPUT_NAME_KEY, this.selectedAlignedName);
    }

    private void performPerspectiveWarping(Mat referenceMat, Mat[] candidateMatList, Mat[] imagesToWarpList, String[] resultNames) {
        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator(referenceMat, candidateMatList);
        matchingOperator.perform();

        LRWarpingOperator perspectiveWarpOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), imagesToWarpList, resultNames, matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
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

    private void performMedianAlignment(Mat[] imagesToAlignList, String[] resultNames) {
        //perform exposure alignment
        MedianAlignmentOperator medianAlignmentOperator = new MedianAlignmentOperator(imagesToAlignList, resultNames);
        medianAlignmentOperator.perform();

        //MatMemory.releaseAll(imagesToAlignList, true);
    }
}
