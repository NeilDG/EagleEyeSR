package neildg.com.eagleeyesr.threads;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.BitmapURIRepository;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.model.multiple.SharpnessMeasure;
import neildg.com.eagleeyesr.processing.filters.YangFilter;
import neildg.com.eagleeyesr.processing.imagetools.ColorSpaceOperator;
import neildg.com.eagleeyesr.processing.imagetools.MatMemory;
import neildg.com.eagleeyesr.processing.listeners.IProcessListener;
import neildg.com.eagleeyesr.processing.multiple.resizing.DownsamplingOperator;
import neildg.com.eagleeyesr.processing.multiple.selection.TestImagesSelector;
import neildg.com.eagleeyesr.processing.multiple.resizing.LRToHROperator;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;

/**
 * SRProcessor debugging main entry point. This processor has the downsampling operator included.
 * Created by NeilDG on 3/5/2016.
 */
public class DebugSRProcessor extends Thread {
    private final static String TAG = "MultipleImageSR";

    private IProcessListener processListener;
    public DebugSRProcessor(IProcessListener  processListener) {
        this.processListener = processListener;
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

        /*int index = 0;
        for(int i = 0; i < BitmapURIRepository.getInstance().getNumImagesSelected(); i++) {
            index = i;
            if(FileImageReader.getInstance().doesImageExists(FilenameConstants.INPUT_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG)) {
                break;
            }
        }*/

        int index = sharpnessResult.getLeastIndex();

        //simulate degradation
        //DegradationOperator degradationOperator = new DegradationOperator();
        //degradationOperator.perform();

        //reload images again. degradation has been imposed in input images.
        for(int i = 0; i < rgbInputMatList.length; i++) {
            rgbInputMatList[i] = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
        }

        Log.d(TAG, "Index for interpolation: " +index);
        LRToHROperator lrToHROperator = new LRToHROperator(FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (index), ImageFileAttribute.FileType.JPEG), index);
        lrToHROperator.perform();

        //remeasure sharpness result without the image ground-truth
        sharpnessResult = SharpnessMeasure.getSharedInstance().measureSharpness(testImagesSelector.getProposedEdgeList());

        //trim the input list from the measured sharpness mean
        Integer[] inputIndices = SharpnessMeasure.getSharedInstance().trimMatList(rgbInputMatList.length, sharpnessResult, 0.0);
        ArrayList<Mat> newInputMatList = new ArrayList<>();
        ArrayList<Integer> inputIndexList = new ArrayList<>();
        int bestIndex = 1;
        //refine input indices and remove ground-truth (if any).
        for(int i = 0; i < inputIndices.length; i++) {
            if(FileImageReader.getInstance().doesImageExists(FilenameConstants.INPUT_PREFIX_STRING + (inputIndices[i]), ImageFileAttribute.FileType.JPEG)) {
               inputIndexList.add(inputIndices[i]);

                if(sharpnessResult.getBestIndex() == inputIndices[i]) {
                    bestIndex = inputIndices[i];
                }
            }
        }

        inputIndices = inputIndexList.toArray(new Integer[inputIndexList.size()]);
        //load RGB inputs
        for(int i = 0; i < inputIndices.length; i++) {
            Mat inputMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (inputIndices[i]), ImageFileAttribute.FileType.JPEG);
            /*UnsharpMaskOperator unsharpMaskOperator =  new UnsharpMaskOperator(inputMat, inputIndices[i]);
            unsharpMaskOperator.perform();
            newInputMatList.add(unsharpMaskOperator.getResult());*/
            newInputMatList.add(inputMat);
        }

        rgbInputMatList = newInputMatList.toArray(new Mat[newInputMatList.size()]);
        Log.d(TAG, "RGB INPUT LENGTH: "+rgbInputMatList.length + " Best index: " +bestIndex);

        ReleaseSRProcessor releaseSRProcessor = new ReleaseSRProcessor(this.processListener);
        releaseSRProcessor.performActualSuperres(rgbInputMatList, inputIndices, bestIndex, true);
        this.processListener.onProcessCompleted();

        ProgressDialogHandler.getInstance().hideProcessDialog();
    }

}
