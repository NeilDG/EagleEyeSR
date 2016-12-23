package neildg.com.megatronsr.threads;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

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
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.imagetools.MatMemory;
import neildg.com.megatronsr.processing.listeners.IProcessListener;
import neildg.com.megatronsr.processing.multiple.alignment.MedianAlignmentOperator;
import neildg.com.megatronsr.processing.multiple.fusion.OptimizedMeanFusionOperator;
import neildg.com.megatronsr.processing.multiple.refinement.DenoisingOperator;
import neildg.com.megatronsr.processing.multiple.resizing.TransferToDirOperator;
import neildg.com.megatronsr.processing.multiple.alignment.AffineWarpingOperator;
import neildg.com.megatronsr.processing.multiple.alignment.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.alignment.LRWarpingOperator;
import neildg.com.megatronsr.processing.multiple.alignment.WarpResultEvaluator;
import neildg.com.megatronsr.processing.multiple.alignment.WarpingConstants;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * SR processor for release mode
 * Created by NeilDG on 9/10/2016.
 */
public class ReleaseSRProcessor extends Thread{
    private final static String TAG = "ReleaseSRProcessor";

    private IProcessListener processListener;
    public ReleaseSRProcessor(IProcessListener processListener) {
        this.processListener = processListener;
    }

    @Override
    public void run() {

        ProgressDialogHandler.getInstance().showProcessDialog("Pre-process", "Creating backup copy for processing.", 0.0f);

        TransferToDirOperator transferToDirOperator = new TransferToDirOperator(BitmapURIRepository.getInstance().getNumImagesSelected());
        transferToDirOperator.perform();

        ProgressDialogHandler.getInstance().showProcessDialog("Pre-process", "Interpolating images and extracting energy channel", 10.0f);
        this.interpolateFirstImage();

        //initialize classes
        SharpnessMeasure.initialize();

        //load images and use Y channel as input for succeeding operators
        Mat[] energyInputMatList = new Mat[BitmapURIRepository.getInstance().getNumImagesSelected()];
        Mat inputMat = null;

        for(int i = 0; i < energyInputMatList.length; i++) {
            inputMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
            inputMat = ImageOperator.downsample(inputMat, 0.125f); //downsample

            FileImageWriter.getInstance().saveMatrixToImage(inputMat, "downsample_"+i, ImageFileAttribute.FileType.JPEG);

            Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(inputMat);
            energyInputMatList[i] = yuvMat[ColorSpaceOperator.Y_CHANNEL];

            inputMat.release();

        }

        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Assessing sharpness measure of images", 15.0f);

        //extract features
        YangFilter yangFilter = new YangFilter(energyInputMatList);
        yangFilter.perform();

        //release energy input mat list
        MatMemory.releaseAll(energyInputMatList, false);

        //remeasure sharpness result without the image ground-truth
        SharpnessMeasure.SharpnessResult sharpnessResult = SharpnessMeasure.getSharedInstance().measureSharpness(yangFilter.getEdgeMatList());

        //trim the input list from the measured sharpness mean
        Integer[] inputIndices = SharpnessMeasure.getSharedInstance().trimMatList(BitmapURIRepository.getInstance().getNumImagesSelected(), sharpnessResult, 0.0);
        Mat[] rgbInputMatList = new Mat[inputIndices.length];

        int bestIndex = 0;
        //load RGB inputs
        for(int i = 0; i < inputIndices.length; i++) {
            rgbInputMatList[i] = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (inputIndices[i]), ImageFileAttribute.FileType.JPEG);
            if(sharpnessResult.getBestIndex() == inputIndices[i]) {
                bestIndex = i;
            }
        }

        Log.d(TAG, "RGB INPUT LENGTH: "+rgbInputMatList.length);

        this.performActualSuperres(rgbInputMatList, inputIndices, bestIndex);
        this.processListener.onProcessCompleted();
    }

    public void performActualSuperres(Mat[] rgbInputMatList, Integer[] inputIndices, int bestIndex) {
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
        int warpChoice = ParameterConfig.getPrefsInt(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.AFFINE_WARP);

        if(warpChoice == WarpingConstants.PERSPECTIVE_WARP) {
            //perform perspective warping and alignment
            Mat[] succeedingMatList =new Mat[rgbInputMatList.length - 1];
            for(int i = 1; i < rgbInputMatList.length; i++) {
                succeedingMatList[i - 1] = rgbInputMatList[i];
            }

            this.performMedianAlignment(rgbInputMatList, inputIndices[0]);
            this.performPerspectiveWarping(rgbInputMatList[bestIndex], succeedingMatList, succeedingMatList);
        }
        else if(warpChoice == WarpingConstants.AFFINE_WARP) {
            Mat[] succeedingMatList =new Mat[rgbInputMatList.length - 1];
            for(int i = 1; i < rgbInputMatList.length; i++) {
                succeedingMatList[i - 1] = rgbInputMatList[i];
            }

            //perform affine warping
            this.performAffineWarping(rgbInputMatList[0], succeedingMatList, succeedingMatList);
        }
        else {
            this.performMedianAlignment(rgbInputMatList, inputIndices[0]);
        }



        //deallocate some classes
        SharpnessMeasure.destroy();

        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Refining image warping results", 70.0f);
        String[] alignedImageNames = this.assessImageWarpResults(inputIndices[0], (warpChoice == WarpingConstants.MEDIAN_ALIGNMENT));

        ProgressDialogHandler.getInstance().showProcessDialog("Mean fusion", "Performing image fusion", 80.0f);
        this.performMeanFusion(inputIndices[0], bestIndex, alignedImageNames);
        //this.performMeanFusion(sharpnessResult.getBestIndex(), sharpnessResult.getBestIndex(), alignedImageNames);


        ProgressDialogHandler.getInstance().showProcessDialog("Mean fusion", "Performing image fusion", 100.0f);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ProgressDialogHandler.getInstance().hideProcessDialog();

        System.gc();
    }

    private void interpolateFirstImage() {
        boolean outputComparisons = ParameterConfig.getPrefsBoolean(ParameterConfig.DEBUGGING_FLAG_KEY, false);

        if(outputComparisons) {
            Mat inputMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);

            Mat outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_NEAREST);
            FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_NEAREST, ImageFileAttribute.FileType.JPEG);
            outputMat.release();

        /*outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_LINEAR);
        FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_LINEAR, ImageFileAttribute.FileType.JPEG);
        outputMat.release();*/

            outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
            FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_CUBIC, ImageFileAttribute.FileType.JPEG);
            outputMat.release();

            inputMat.release();
            System.gc();
        }
        else {
            Log.d(TAG, "Debugging mode disabled. Will skip output interpolated images.");
        }
    }

    private String[] assessImageWarpResults(int index, boolean medianAlignOnly) {

        int numImages = AttributeHolder.getSharedInstance().getValue(AttributeNames.WARPED_IMAGES_LENGTH_KEY, 0);
        String[] warpedImageNames = new String[numImages];
        String[] medianAlignedNames = new String[numImages];

        for(int i = 0; i < numImages; i++) {
            warpedImageNames[i] = FilenameConstants.WARP_PREFIX +i;
            medianAlignedNames[i] = FilenameConstants.MEDIAN_ALIGNMENT_PREFIX + i;
        }

        if(medianAlignOnly) {
            return medianAlignedNames;
        }
        else {
            WarpResultEvaluator warpResultEvaluator = new WarpResultEvaluator(FilenameConstants.INPUT_PREFIX_STRING + index, warpedImageNames, medianAlignedNames);
            warpResultEvaluator.perform();
            return warpResultEvaluator.getChosenAlignedNames();
        }
    }

    private void performAffineWarping(Mat referenceMat, Mat[] candidateMatList, Mat[] imagesToWarpList) {
        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Performing image warping", 30.0f);

        //perform affine warping
        AffineWarpingOperator warpingOperator = new AffineWarpingOperator(referenceMat, candidateMatList, imagesToWarpList);
        warpingOperator.perform();

        MatMemory.releaseAll(candidateMatList, false);
        MatMemory.releaseAll(imagesToWarpList, false);
        MatMemory.releaseAll(warpingOperator.getWarpedMatList(), true);
    }

    private void performPerspectiveWarping(Mat referenceMat, Mat[] candidateMatList, Mat[] imagesToWarpList) {
        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Performing feature matching against first image", 40.0f);
        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator(referenceMat, candidateMatList);
        matchingOperator.perform();

        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Performing image warping", 50.0f);

        LRWarpingOperator perspectiveWarpOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), imagesToWarpList, matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        perspectiveWarpOperator.perform();

        //release images
        matchingOperator.getRefKeypoint().release();
        MatMemory.releaseAll(matchingOperator.getdMatchesList(), false);
        MatMemory.releaseAll(matchingOperator.getLrKeypointsList(), false);
        MatMemory.releaseAll(candidateMatList, false);
        MatMemory.releaseAll(imagesToWarpList, false);

        Mat[] warpedMatList = perspectiveWarpOperator.getWarpedMatList();
        MatMemory.releaseAll(warpedMatList, true);
    }

    private void performMedianAlignment(Mat[] imagesToAlignList, int inputIndex) {
        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Performing exposure alignment", 30.0f);
        //perform exposure alignment
        MedianAlignmentOperator medianAlignmentOperator = new MedianAlignmentOperator(imagesToAlignList, inputIndex);
        medianAlignmentOperator.perform();

        //MatMemory.releaseAll(imagesToAlignList, true);
    }

    private void performMeanFusion(int index, int bestIndex, String[] alignedImageNames) {
        ArrayList<String> imagePathList = new ArrayList<>();

        if(alignedImageNames.length == 1) {
            Log.d(TAG, "Best index selected for image HR: " +bestIndex);
            imagePathList.add(FilenameConstants.INPUT_PREFIX_STRING + bestIndex); //no need to perform image fusion, just use the best image.
        }
        else {
            //add initial input HR image
            imagePathList.add(FilenameConstants.INPUT_PREFIX_STRING + index);
            for(int i = 0; i < alignedImageNames.length; i++) {
                imagePathList.add(alignedImageNames[i]);
            }
        }


        OptimizedMeanFusionOperator fusionOperator = new OptimizedMeanFusionOperator(imagePathList.toArray(new String[imagePathList.size()]));
        fusionOperator.perform();
        FileImageWriter.getInstance().saveMatrixToImage(fusionOperator.getResult(), FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG);

    }
}
