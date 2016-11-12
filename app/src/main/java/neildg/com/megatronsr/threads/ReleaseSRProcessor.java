package neildg.com.megatronsr.threads;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.model.multiple.SharpnessMeasure;
import neildg.com.megatronsr.processing.filters.YangFilter;
import neildg.com.megatronsr.processing.imagetools.ColorSpaceOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.imagetools.MatMemory;
import neildg.com.megatronsr.processing.listeners.IProcessListener;
import neildg.com.megatronsr.processing.multiple.fusion.MeanFusionOperator;
import neildg.com.megatronsr.processing.multiple.resizing.TransferToDirOperator;
import neildg.com.megatronsr.processing.multiple.warping.AffineWarpingOperator;
import neildg.com.megatronsr.processing.multiple.warping.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.warping.LRWarpingOperator;
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

        ProgressDialogHandler.getInstance().showUserDialog("", "Processing image");


        TransferToDirOperator transferToDirOperator = new TransferToDirOperator(BitmapURIRepository.getInstance().getNumImagesSelected());
        transferToDirOperator.perform();
        transferToDirOperator = null;

        this.interpolateFirstImage();

        //initialize storage classes
        //ProcessedImageRepo.initialize();
        SharpnessMeasure.initialize();

        //load images and use Y channel as input for succeeding operators
        Mat[] energyInputMatList = new Mat[BitmapURIRepository.getInstance().getNumImagesSelected()];
        Mat inputMat = null;

        for(int i = 0; i < energyInputMatList.length; i++) {
            inputMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
            inputMat = ImageOperator.downsample(inputMat, 0.125f); //downsample

            ImageWriter.getInstance().saveMatrixToImage(inputMat, "downsample_"+i, ImageFileAttribute.FileType.JPEG);

            Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(inputMat);
            energyInputMatList[i] = yuvMat[ColorSpaceOperator.Y_CHANNEL];

            inputMat.release();

        }

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

        //load RGB inputs
        for(int i = 0; i < inputIndices.length; i++) {
            rgbInputMatList[i] = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + (inputIndices[i]), ImageFileAttribute.FileType.JPEG);
        }

        Log.d(TAG, "RGB INPUT LENGTH: "+rgbInputMatList.length);

        //perform feature matching of LR images against the first image as reference mat.
        Mat[] succeedingMatList =new Mat[rgbInputMatList.length - 1];
        for(int i = 1; i < rgbInputMatList.length; i++) {
            succeedingMatList[i - 1] = rgbInputMatList[i];
        }

        //perform affine warping
        AffineWarpingOperator warpingOperator = new AffineWarpingOperator(rgbInputMatList[0], succeedingMatList);
        warpingOperator.perform();

        succeedingMatList = warpingOperator.getWarpedMatList();

        //perform perspective warping
        /*FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator(rgbInputMatList[0], succeedingMatList);
        matchingOperator.perform();

        LRWarpingOperator perspectiveWarpOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), succeedingMatList, matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        perspectiveWarpOperator.perform();

        //release images
        matchingOperator.getRefKeypoint().release();
        MatMemory.releaseAll(matchingOperator.getdMatchesList(), false);
        MatMemory.releaseAll(matchingOperator.getLrKeypointsList(), false);
        MatMemory.releaseAll(succeedingMatList, false);
        MatMemory.releaseAll(rgbInputMatList, false);

        Mat[] warpedMatList = perspectiveWarpOperator.getWarpedMatList();
        MatMemory.releaseAll(warpedMatList, true);*/

        MatMemory.releaseAll(rgbInputMatList, false);
        MatMemory.releaseAll(succeedingMatList, false);

        //deallocate some classes
        SharpnessMeasure.destroy();
        this.performMeanFusion();

        ProgressDialogHandler.getInstance().hideUserDialog();

        System.gc();

        this.processListener.onProcessCompleted();
    }

    private void interpolateFirstImage() {
        //ProgressDialogHandler.getInstance().showUserDialog("Interpolating images", "Upsampling image using nearest-neighbor, bilinear and bicubic");

        Mat inputMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);

        Mat outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_NEAREST);
        ImageWriter.getInstance().saveMatrixToImage(outputMat, "nearest", ImageFileAttribute.FileType.JPEG);
        outputMat.release();

        outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        ImageWriter.getInstance().saveMatrixToImage(outputMat, "bicubic", ImageFileAttribute.FileType.JPEG);
        outputMat.release();

        inputMat.release();
        System.gc();

        //ProgressDialogHandler.getInstance().hideUserDialog();
    }

    private void performMeanFusion() {
        int numImages = AttributeHolder.getSharedInstance().getValue(AttributeNames.IMAGE_LENGTH_KEY, 0);
        String[] imagePathList = new String[numImages];
        for(int i = 0; i < numImages; i++) {
            imagePathList[i] = "affine_warp_"+i;
        }
        MeanFusionOperator fusionOperator = new MeanFusionOperator(imagePathList, "Fusing", "Fusing images using mean");
        fusionOperator.perform();
        ImageWriter.getInstance().saveMatrixToImage(fusionOperator.getResult(), "rgb_merged", ImageFileAttribute.FileType.JPEG);

    }
}
