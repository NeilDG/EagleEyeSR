package neildg.com.megatronsr.threads;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.model.multiple.SharpnessMeasure;
import neildg.com.megatronsr.processing.filters.YangFilter;
import neildg.com.megatronsr.processing.imagetools.ColorSpaceOperator;
import neildg.com.megatronsr.processing.imagetools.MatMemory;
import neildg.com.megatronsr.processing.multiple.fusion.MeanFusionOperator;
import neildg.com.megatronsr.processing.multiple.fusion.OptimizedBaseFusionOperator;
import neildg.com.megatronsr.processing.multiple.refinement.DenoisingOperator;
import neildg.com.megatronsr.processing.multiple.resizing.DegradationOperator;
import neildg.com.megatronsr.processing.multiple.resizing.DownsamplingOperator;
import neildg.com.megatronsr.processing.multiple.selection.TestImagesSelector;
import neildg.com.megatronsr.processing.multiple.warping.AffineWarpingOperator;
import neildg.com.megatronsr.processing.multiple.warping.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.warping.LRWarpingOperator;
import neildg.com.megatronsr.processing.multiple.resizing.LRToHROperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * SRProcessor debugging main entry point. This processor has the downsampling operator included.
 * Created by NeilDG on 3/5/2016.
 */
public class MultipleImageSRProcessor extends Thread {
    private final static String TAG = "MultipleImageSR";

    public MultipleImageSRProcessor() {

    }

    @Override
    public void run() {
        ProgressDialogHandler.getInstance().showProcessDialog("Downsampling images", "Downsampling images selected and saving them in file.", 0.0f);

        //initialize storage classes
        //ProcessedImageRepo.initialize();
        SharpnessMeasure.initialize();

        //downsample
        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        ProgressDialogHandler.getInstance().hideProcessDialog();


        //load images and use Y channel as input for succeeding operators
        Mat[] rgbInputMatList = new Mat[BitmapURIRepository.getInstance().getNumImagesSelected()];
        Mat[] energyInputMatList = new Mat[BitmapURIRepository.getInstance().getNumImagesSelected()];

        for(int i = 0; i < energyInputMatList.length; i++) {
            rgbInputMatList[i] = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
            Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(rgbInputMatList[i]);
            energyInputMatList[i] = yuvMat[ColorSpaceOperator.Y_CHANNEL];
        }

        //extract features
        YangFilter yangFilter = new YangFilter(energyInputMatList);
        yangFilter.perform();

        SharpnessMeasure.getSharedInstance().measureSharpness(yangFilter.getEdgeMatList());
        SharpnessMeasure.SharpnessResult sharpnessResult = SharpnessMeasure.getSharedInstance().getLatestResult();

        //release energy input mat list
        MatMemory.releaseAll(energyInputMatList, false);

        //find appropriate ground-truth
        TestImagesSelector testImagesSelector = new TestImagesSelector(rgbInputMatList, yangFilter.getEdgeMatList(), sharpnessResult);
        testImagesSelector.perform();
        rgbInputMatList = testImagesSelector.getProposedList();

        int index = 0;
        for(int i = 0; i < BitmapURIRepository.getInstance().getNumImagesSelected(); i++) {
            index = i;
            if(FileImageReader.getInstance().doesImageExists(FilenameConstants.INPUT_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG)) {
                break;
            }
        }

        //simulate degradation
        DegradationOperator degradationOperator = new DegradationOperator();
        degradationOperator.perform();

        //reload images again. degradation has been imposed in input images.
        for(int i = 0; i < rgbInputMatList.length; i++) {
            rgbInputMatList[i] = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
        }

        Log.d(TAG, "First index: " +index);
        LRToHROperator lrToHROperator = new LRToHROperator(FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (index), ImageFileAttribute.FileType.JPEG), index);
        lrToHROperator.perform();

        //remeasure sharpness result without the image ground-truth
        sharpnessResult = SharpnessMeasure.getSharedInstance().measureSharpness(testImagesSelector.getProposedEdgeList());

        //trim the input list from the measured sharpness mean
        rgbInputMatList = SharpnessMeasure.getSharedInstance().trimMatList(rgbInputMatList, sharpnessResult, 0.0);

        boolean performDenoising = ParameterConfig.getPrefsBoolean(ParameterConfig.DENOISE_FLAG_KEY, false);

        if(performDenoising) {
            ProgressDialogHandler.getInstance().showProcessDialog("Denoising", "Performing denoising", 20.0f);

            //perform denoising on original input list
            DenoisingOperator denoisingOperator = new DenoisingOperator(rgbInputMatList);
            denoisingOperator.perform();
            MatMemory.releaseAll(rgbInputMatList, false);
            rgbInputMatList = denoisingOperator.getResult();

        }
        else {
            Log.d(TAG, "Denoising will be skipped!");
        }

        //perform feature matching of LR images against the first image as reference mat.
        Mat[] succeedingMatList =new Mat[rgbInputMatList.length - 1];
        for(int i = 1; i < rgbInputMatList.length; i++) {
            succeedingMatList[i - 1] = rgbInputMatList[i];
        }
        //perform affine warping
        this.performAffineWarping(rgbInputMatList, rgbInputMatList[0], succeedingMatList);

        //perform perspective warping
        //this.performPerspectiveWarping(rgbInputMatList, rgbInputMatList[0], succeedingMatList);

        //deallocate some classes
        SharpnessMeasure.destroy();

        //ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Refining image warping results", 70.0f);
        //this.assessImageWarpResults();

        ProgressDialogHandler.getInstance().showProcessDialog("Mean fusion", "Performing image fusion", 80.0f);
        this.performMeanFusion();

        ProgressDialogHandler.getInstance().showProcessDialog("Mean fusion", "Performing image fusion", 100.0f);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ProgressDialogHandler.getInstance().hideProcessDialog();
    }

    private void performAffineWarping(Mat[] rgbInputMatList, Mat referenceMat, Mat[] succeedingMatList) {
        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Performing image warping", 30.0f);

        //perform affine warping
        AffineWarpingOperator warpingOperator = new AffineWarpingOperator(referenceMat, succeedingMatList);
        warpingOperator.perform();

        MatMemory.releaseAll(succeedingMatList, false);
        MatMemory.releaseAll(rgbInputMatList, false);
        MatMemory.releaseAll(warpingOperator.getWarpedMatList(), true);
    }

    private void performPerspectiveWarping(Mat[] rgbInputMatList, Mat referenceMat, Mat[] succeedingMatList) {
        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Performing feature matching against first image", 30.0f);
        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator(referenceMat, succeedingMatList);
        matchingOperator.perform();

        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Performing image warping", 60.0f);

        LRWarpingOperator perspectiveWarpOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), succeedingMatList, matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        perspectiveWarpOperator.perform();

        //release images
        matchingOperator.getRefKeypoint().release();
        MatMemory.releaseAll(matchingOperator.getdMatchesList(), false);
        MatMemory.releaseAll(matchingOperator.getLrKeypointsList(), false);
        MatMemory.releaseAll(succeedingMatList, false);
        MatMemory.releaseAll(rgbInputMatList, false);

        Mat[] warpedMatList = perspectiveWarpOperator.getWarpedMatList();
        MatMemory.releaseAll(warpedMatList, true);
    }

    private void performMeanFusion() {
        int numImages = AttributeHolder.getSharedInstance().getValue(AttributeNames.AFFINE_WARPED_IMAGES_LENGTH_KEY, 0);
        ArrayList<String> imagePathList = new ArrayList<>();

        //add initial input HR image
        imagePathList.add(FilenameConstants.INPUT_PREFIX_STRING + 0);
        for(int i = 0; i < numImages; i++) {
            imagePathList.add(FilenameConstants.AFFINE_WARP_PREFIX+i);
        }

        OptimizedBaseFusionOperator fusionOperator = new OptimizedBaseFusionOperator(imagePathList.toArray(new String[imagePathList.size()]));
        fusionOperator.perform();
        FileImageWriter.getInstance().saveMatrixToImage(fusionOperator.getResult(), FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG);

    }

}
