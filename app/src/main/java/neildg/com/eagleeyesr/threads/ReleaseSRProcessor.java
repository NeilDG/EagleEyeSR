package neildg.com.eagleeyesr.threads;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageInputMap;
import neildg.com.eagleeyesr.metrics.TimeMeasure;
import neildg.com.eagleeyesr.metrics.TimeMeasureManager;
import neildg.com.eagleeyesr.model.AttributeHolder;
import neildg.com.eagleeyesr.model.AttributeNames;
import neildg.com.eagleeyesr.model.multiple.SharpnessMeasure;
import neildg.com.eagleeyesr.processing.filters.YangFilter;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;
import neildg.com.eagleeyesr.processing.imagetools.MatMemory;
import neildg.com.eagleeyesr.processing.listeners.IProcessListener;
import neildg.com.eagleeyesr.processing.multiple.alignment.MedianAlignmentOperator;
import neildg.com.eagleeyesr.processing.multiple.assessment.InputImageEnergyReader;
import neildg.com.eagleeyesr.processing.multiple.enhancement.UnsharpMaskOperator;
import neildg.com.eagleeyesr.processing.multiple.fusion.FusionConstants;
import neildg.com.eagleeyesr.processing.multiple.fusion.MeanFusionOperator;
import neildg.com.eagleeyesr.processing.multiple.refinement.DenoisingOperator;
import neildg.com.eagleeyesr.processing.multiple.alignment.AffineWarpingOperator;
import neildg.com.eagleeyesr.processing.multiple.alignment.FeatureMatchingOperator;
import neildg.com.eagleeyesr.processing.multiple.alignment.LRWarpingOperator;
import neildg.com.eagleeyesr.processing.multiple.alignment.WarpResultEvaluator;
import neildg.com.eagleeyesr.processing.multiple.alignment.WarpingConstants;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;

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

        TimeMeasure srTimeMeasure = TimeMeasureManager.getInstance().newTimeMeasure(TimeMeasureManager.MEASURE_SR_TIME);
        TimeMeasure edgeDetectionMeasure = TimeMeasureManager.getInstance().newTimeMeasure(TimeMeasureManager.EDGE_DETECTION_TIME);
        TimeMeasure selectionMeasure = TimeMeasureManager.getInstance().newTimeMeasure(TimeMeasureManager.IMAGE_SELECTION_TIME);
        TimeMeasure sharpeningMeasure = TimeMeasureManager.getInstance().newTimeMeasure(TimeMeasureManager.SHARPENING_TIME);
        TimeMeasure denoisingMeasure = TimeMeasureManager.getInstance().newTimeMeasure(TimeMeasureManager.DENOISING_TIME);
        TimeMeasure imageAlignmentMeasure = TimeMeasureManager.getInstance().newTimeMeasure(TimeMeasureManager.IMAGE_ALIGNMENT_TIME);
        TimeMeasure alignmentSelectMeasure = TimeMeasureManager.getInstance().newTimeMeasure(TimeMeasureManager.ALIGNMENT_SELECTION_TIME);
        TimeMeasure fusionMeasuure = TimeMeasureManager.getInstance().newTimeMeasure(TimeMeasureManager.IMAGE_FUSION_TIME);

        srTimeMeasure.timeStart();

        ProgressDialogHandler.getInstance().showProcessDialog("Pre-process", "Creating backup copy for processing.", 0.0f);

        //TransferToDirOperator transferToDirOperator = new TransferToDirOperator(BitmapURIRepository.getInstance().getNumImagesSelected());
        //transferToDirOperator.perform();

        ProgressDialogHandler.getInstance().showProcessDialog("Pre-process", "Analyzing images", 10.0f);

        //initialize classes
        SharpnessMeasure.initialize();
        edgeDetectionMeasure.timeStart();

        Mat[] energyInputMatList = new Mat[ImageInputMap.numImages()];
        InputImageEnergyReader[] energyReaders = new InputImageEnergyReader[energyInputMatList.length];
        //load images and use Y channel as input for succeeding operators
        try {
            Semaphore energySem = new Semaphore(energyInputMatList.length);
            for(int i = 0; i < energyReaders.length; i++) {
                energyReaders[i] = new InputImageEnergyReader(energySem, ImageInputMap.getInputImage(i));
                energyReaders[i].startWork();
            }

            energySem.acquire(energyInputMatList.length);
            for(int i = 0; i < energyReaders.length; i++) {
                energyInputMatList[i] = energyReaders[i].getOutputMat();
            }


        } catch(InterruptedException e) {
            e.printStackTrace();
        }


        ProgressDialogHandler.getInstance().showProcessDialog("Pre-process", "Analyzing images", 15.0f);

        //extract features
        YangFilter yangFilter = new YangFilter(energyInputMatList);
        yangFilter.perform();

        edgeDetectionMeasure.timeEnd();

        //release energy input mat list
        MatMemory.releaseAll(energyInputMatList, false);

        selectionMeasure.timeStart();
        //remeasure sharpness result without the image ground-truth
        SharpnessMeasure.SharpnessResult sharpnessResult = SharpnessMeasure.getSharedInstance().measureSharpness(yangFilter.getEdgeMatList());

        //trim the input list from the measured sharpness mean
        Integer[] inputIndices = SharpnessMeasure.getSharedInstance().trimMatList(ImageInputMap.numImages(), sharpnessResult, 0.0);
        Mat[] rgbInputMatList = new Mat[inputIndices.length];

        selectionMeasure.timeEnd();


        this.interpolateImage(sharpnessResult.getLeastIndex());

        int bestIndex = 0;
        //load RGB inputs
        Mat inputMat;
        sharpeningMeasure.timeStart();
        for(int i = 0; i < inputIndices.length; i++) {
            //rgbInputMatList[i] = FileImageReader.getInstance().imReadFullPath(ImageInputMap.getInputImage(inputIndices[i]));
            //perform unsharp masking
            inputMat = FileImageReader.getInstance().imReadFullPath(ImageInputMap.getInputImage(inputIndices[i]));
            UnsharpMaskOperator unsharpMaskOperator =  new UnsharpMaskOperator(inputMat, inputIndices[i]);
            unsharpMaskOperator.perform();
            rgbInputMatList[i] = unsharpMaskOperator.getResult();
            if(sharpnessResult.getBestIndex() == inputIndices[i]) {
                bestIndex = i;
            }
        }
        sharpeningMeasure.timeEnd();
        Log.d(TAG, "RGB INPUT LENGTH: "+rgbInputMatList.length+ " Best index: " +bestIndex);

        this.performActualSuperres(rgbInputMatList, inputIndices, bestIndex, false);
        this.processListener.onProcessCompleted();

        srTimeMeasure.timeEnd();
        Log.d(TAG,"Total processing time is " +TimeMeasureManager.convertDeltaToString(srTimeMeasure.getDeltaDifference()));
        Log.d(TAG, "Edge Detection time: " +TimeMeasureManager.convertDeltaToString(edgeDetectionMeasure.getDeltaDifference()));
        Log.d(TAG, "Image Selection time: " +TimeMeasureManager.convertDeltaToSeconds(selectionMeasure.getDeltaDifference()));
        Log.d(TAG, "Denoising time: " +TimeMeasureManager.convertDeltaToString(denoisingMeasure.getDeltaDifference()));
        Log.d(TAG, "Image Sharpening time: " +TimeMeasureManager.convertDeltaToString(sharpeningMeasure.getDeltaDifference()));
        Log.d(TAG, "Image Alignment time: " +TimeMeasureManager.convertDeltaToString(imageAlignmentMeasure.getDeltaDifference()));
        Log.d(TAG, "Alignment Selection time: " +TimeMeasureManager.convertDeltaToString(alignmentSelectMeasure.getDeltaDifference()));
        Log.d(TAG, "Image Fusion time: " +TimeMeasureManager.convertDeltaToString(fusionMeasuure.getDeltaDifference()));
    }

    public void performActualSuperres(Mat[] rgbInputMatList, Integer[] inputIndices, int bestIndex, boolean debugMode) {
        boolean performDenoising = ParameterConfig.getPrefsBoolean(ParameterConfig.DENOISE_FLAG_KEY, false);

        TimeMeasure denoisingMeasure = TimeMeasureManager.getInstance().getTimeMeasure(TimeMeasureManager.DENOISING_TIME);
        denoisingMeasure.timeStart();
        if(performDenoising) {
            ProgressDialogHandler.getInstance().showProcessDialog("Denoising", "Performing denoising", 15.0f);

            //perform denoising on original input list
            DenoisingOperator denoisingOperator = new DenoisingOperator(rgbInputMatList);
            denoisingOperator.perform();
            MatMemory.releaseAll(rgbInputMatList, false);
            rgbInputMatList = denoisingOperator.getResult();

        }
        else {
            Log.d(TAG, "Denoising will be skipped!");
        }
        denoisingMeasure.timeEnd();


        int srChoice = ParameterConfig.getPrefsInt(ParameterConfig.SR_CHOICE_KEY, FusionConstants.FULL_SR_MODE);
        if(srChoice == FusionConstants.FULL_SR_MODE) {
            this.performFullSRMode(rgbInputMatList, inputIndices, bestIndex, debugMode);
        }
        else {
            MatMemory.releaseAll(rgbInputMatList, false);
            MatMemory.cleanMemory();
            this.performFastSRMode(bestIndex, debugMode);
        }


    }

    private void performFullSRMode(Mat[] rgbInputMatList, Integer[] inputIndices, int bestIndex, boolean debug) {
        //perform feature matching of LR images against the first image as reference mat.
        int warpChoice = ParameterConfig.getPrefsInt(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.BEST_ALIGNMENT);
        //perform perspective warping and alignment
        Mat[] succeedingMatList =new Mat[rgbInputMatList.length - 1];
        for(int i = 1; i < rgbInputMatList.length; i++) {
            succeedingMatList[i - 1] = rgbInputMatList[i];
        }

        String[] medianResultNames = new String[succeedingMatList.length];
        for(int i = 0; i < medianResultNames.length; i++) {
            medianResultNames[i] = FilenameConstants.MEDIAN_ALIGNMENT_PREFIX + i;
        }

        String[] warpResultnames = new String[succeedingMatList.length];
        for(int i = 0; i < medianResultNames.length; i++) {
            warpResultnames[i] = FilenameConstants.WARP_PREFIX + i;
        }

        TimeMeasure alignmentMeasure = TimeMeasureManager.getInstance().getTimeMeasure(TimeMeasureManager.IMAGE_ALIGNMENT_TIME);
        alignmentMeasure.timeStart();
        if(warpChoice == WarpingConstants.BEST_ALIGNMENT) {
            this.performMedianAlignment(rgbInputMatList, medianResultNames);
            this.performPerspectiveWarping(rgbInputMatList[0], succeedingMatList, succeedingMatList, warpResultnames);
        }
        else if(warpChoice == WarpingConstants.PERSPECTIVE_WARP) {
            //perform perspective warping
            this.performPerspectiveWarping(rgbInputMatList[0], succeedingMatList, succeedingMatList, warpResultnames);
        }
        else {
            this.performMedianAlignment(rgbInputMatList, medianResultNames);
        }
        alignmentMeasure.timeEnd();

        //deallocate some classes
        SharpnessMeasure.destroy();
        MatMemory.cleanMemory();

        int numImages = AttributeHolder.getSharedInstance().getValue(AttributeNames.WARPED_IMAGES_LENGTH_KEY, 0);
        String[] warpedImageNames = new String[numImages];
        String[] medianAlignedNames = new String[numImages];

        for(int i = 0; i < numImages; i++) {
            warpedImageNames[i] = FilenameConstants.WARP_PREFIX +i;
            medianAlignedNames[i] = FilenameConstants.MEDIAN_ALIGNMENT_PREFIX + i;
        }

        TimeMeasure alignSelectMeasure = TimeMeasureManager.getInstance().getTimeMeasure(TimeMeasureManager.ALIGNMENT_SELECTION_TIME);
        alignSelectMeasure.timeStart();
        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Aligning images", 60.0f);
        String[] alignedImageNames = assessImageWarpResults(inputIndices[0], warpChoice, warpedImageNames, medianAlignedNames, debug);
        alignSelectMeasure.timeEnd();
        MatMemory.cleanMemory();

        TimeMeasure fusionMeasure = TimeMeasureManager.getInstance().getTimeMeasure(TimeMeasureManager.IMAGE_FUSION_TIME);
        fusionMeasure.timeStart();
        ProgressDialogHandler.getInstance().showProcessDialog("Image fusion", "Performing image fusion", 70.0f);
        this.performMeanFusion(inputIndices[0], bestIndex, alignedImageNames, debug);
        fusionMeasure.timeEnd();

        ProgressDialogHandler.getInstance().showProcessDialog("Image fusion", "Performing image fusion", 100.0f);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ProgressDialogHandler.getInstance().hideProcessDialog();

        MatMemory.cleanMemory();
    }

    private void performFastSRMode(int bestIndex, boolean debugMode) {
        ProgressDialogHandler.getInstance().showProcessDialog("Image fusion", "Performing image fusion", 60.0f);
        Mat initialMat;
        if(debugMode) {
            initialMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + bestIndex, ImageFileAttribute.FileType.JPEG);
        }
        else {
            initialMat = FileImageReader.getInstance().imReadFullPath(ImageInputMap.getInputImage(bestIndex));
        }

        initialMat = ImageOperator.performInterpolation(initialMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        FileImageWriter.getInstance().saveMatrixToImage(initialMat, FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG);
        initialMat.release();
        ProgressDialogHandler.getInstance().showProcessDialog("Image fusion", "Performing image fusion", 100.0f);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ProgressDialogHandler.getInstance().hideProcessDialog();

        MatMemory.cleanMemory();
    }

    private void interpolateImage(int index) {
        boolean outputComparisons = ParameterConfig.getPrefsBoolean(ParameterConfig.DEBUGGING_FLAG_KEY, false);

        if(outputComparisons) {
            Mat inputMat = FileImageReader.getInstance().imReadFullPath(ImageInputMap.getInputImage(index));

            Mat outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_LINEAR);
            FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_LINEAR, ImageFileAttribute.FileType.JPEG);
            outputMat.release();

            inputMat.release();
            System.gc();
        }
        else {
            Log.d(TAG, "Debugging mode disabled. Will skip output interpolated images.");
        }
    }

    public static String[] assessImageWarpResults(int index, int alignmentUsed, String[] warpedImageNames, String[] medianAlignedNames, boolean useLocalDir) {
        if(alignmentUsed == WarpingConstants.BEST_ALIGNMENT) {
            Mat referenceMat;

            if(useLocalDir) {
                referenceMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + index, ImageFileAttribute.FileType.JPEG);
            }
            else {
              referenceMat  = FileImageReader.getInstance().imReadFullPath(ImageInputMap.getInputImage(index));
            }

            WarpResultEvaluator warpResultEvaluator = new WarpResultEvaluator(referenceMat, warpedImageNames, medianAlignedNames);
            warpResultEvaluator.perform();
            return warpResultEvaluator.getChosenAlignedNames();
        }
        else if(alignmentUsed == WarpingConstants.MEDIAN_ALIGNMENT) {
            return medianAlignedNames;
        }
        else {
            return warpedImageNames;
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

    private void performPerspectiveWarping(Mat referenceMat, Mat[] candidateMatList, Mat[] imagesToWarpList, String[] resultNames) {
        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Aligning images", 30.0f);
        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator(referenceMat, candidateMatList);
        matchingOperator.perform();

        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Aligning images", 40.0f);

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
        ProgressDialogHandler.getInstance().showProcessDialog("Processing", "Aligning images", 50.0f);
        //perform exposure alignment
        MedianAlignmentOperator medianAlignmentOperator = new MedianAlignmentOperator(imagesToAlignList, resultNames);
        medianAlignmentOperator.perform();

        //MatMemory.releaseAll(imagesToAlignList, true);
    }

    private void performMeanFusion(int index, int bestIndex, String[] alignedImageNames, boolean debugMode) {

        if(alignedImageNames.length == 1) {
            Log.d(TAG, "Best index selected for image HR: " +bestIndex);
            Mat resultMat;
            if(debugMode) {
                resultMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + bestIndex, ImageFileAttribute.FileType.JPEG);
            }
            else {
                resultMat = FileImageReader.getInstance().imReadFullPath(ImageInputMap.getInputImage(bestIndex));
            }
            //no need to perform image fusion, just use the best image.
            resultMat = ImageOperator.performInterpolation(resultMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
            FileImageWriter.getInstance().saveMatrixToImage(resultMat, FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG);

            resultMat.release();
        }
        else {
            ArrayList<String> imagePathList = new ArrayList<>();
            //add initial input HR image
            Mat inputMat;
            if(debugMode) {
                inputMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + index, ImageFileAttribute.FileType.JPEG);
            }
            else {
                inputMat = FileImageReader.getInstance().imReadFullPath(ImageInputMap.getInputImage(index));
            }

            for(int i = 0; i < alignedImageNames.length; i++) {
                imagePathList.add(alignedImageNames[i]);
            }

            MeanFusionOperator fusionOperator = new MeanFusionOperator(inputMat, imagePathList.toArray(new String[imagePathList.size()]));
            fusionOperator.perform();
            FileImageWriter.getInstance().saveMatrixToImage(fusionOperator.getResult(), FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG);
            FileImageWriter.getInstance().saveHRResultToUserDir(fusionOperator.getResult(), ImageFileAttribute.FileType.JPEG);

            fusionOperator.getResult().release();
        }




    }
}
