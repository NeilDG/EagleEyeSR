package neildg.com.megatronsr.threads;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.model.multiple.ProcessedImageRepo;
import neildg.com.megatronsr.processing.imagetools.ColorSpaceOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.multiple.fusion.MeanFusionOperator;
import neildg.com.megatronsr.processing.multiple.postprocess.ChannelMergeOperator;
import neildg.com.megatronsr.processing.multiple.resizing.BlurImposeOperator;
import neildg.com.megatronsr.processing.multiple.resizing.DownsamplingOperator;
import neildg.com.megatronsr.processing.multiple.warping.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.warping.LRWarpingOperator;
import neildg.com.megatronsr.processing.multiple.resizing.LRToHROperator;
import neildg.com.megatronsr.processing.multiple.fusion.WarpedToHROperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * SRProcessor main entry point
 * Created by NeilDG on 3/5/2016.
 */
public class MultipleImageSRProcessor extends Thread {

    public MultipleImageSRProcessor() {

    }

    @Override
    public void run() {
        ProgressDialogHandler.getInstance().showDialog("Downsampling images", "Downsampling images selected and saving them in file.");

        //downsample
        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        //add custom blur
        BlurImposeOperator blurImposeOperator = new BlurImposeOperator();
        blurImposeOperator.perform();

        ProgressDialogHandler.getInstance().hideDialog();

        LRToHROperator lrToHROperator = new LRToHROperator();
        lrToHROperator.perform();

        ProcessedImageRepo.initialize();

        //load the images
        Mat yMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + "0", ImageFileAttribute.FileType.JPEG);
        Mat[] yuvRefMat = ColorSpaceOperator.convertRGBToYUV(yMat);
        yMat = yuvRefMat[ColorSpaceOperator.Y_CHANNEL];

        Mat[] comparingMatList = new Mat[BitmapURIRepository.getInstance().getNumImagesSelected() - 1];
        for(int i = 0; i < comparingMatList.length; i++) {
            Mat lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (i+1), ImageFileAttribute.FileType.JPEG);
            Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(lrMat);
            comparingMatList[i] = yuvMat[ColorSpaceOperator.Y_CHANNEL];
        }

        //perform feature matching of LR images against the first image as reference mat.
        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator(yMat, comparingMatList);
        matchingOperator.perform();

        LRWarpingOperator warpingOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), comparingMatList, matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        warpingOperator.perform();

        //WarpedToHROperator warpedToHROperator = new WarpedToHROperator(initialMat, ProcessedImageRepo.getSharedInstance().getWarpedMatList());
        //warpedToHROperator.perform();

        Mat initialMat = ImageOperator.performInterpolation(yMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        Mat[] warpedMatList = ProcessedImageRepo.getSharedInstance().getWarpedMatList();
        Mat[] combinedMatList = new Mat[warpedMatList.length + 1];
        combinedMatList[0] = initialMat;
        for(int i = 1; i < combinedMatList.length; i++) {
            combinedMatList[i] = warpedMatList[i - 1];
        }
        MeanFusionOperator meanFusionOperator = new MeanFusionOperator(combinedMatList);
        meanFusionOperator.perform();

        ChannelMergeOperator mergeOperator = new ChannelMergeOperator(meanFusionOperator.getResult(), yuvRefMat[ColorSpaceOperator.U_CHANNEL], yuvRefMat[ColorSpaceOperator.V_CHANNEL]);
        mergeOperator.perform();

        //HDRFusionOperator hdrFusionOperator = new HDRFusionOperator(referenceMat, ProcessedImageRepo.getSharedInstance().getWarpedMatList());
        //hdrFusionOperator.perform();

        //deallocate some classes
        ProcessedImageRepo.destroy();
        ProgressDialogHandler.getInstance().hideDialog();
    }

}
