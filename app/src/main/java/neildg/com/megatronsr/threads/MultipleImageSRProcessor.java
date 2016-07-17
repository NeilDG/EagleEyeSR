package neildg.com.megatronsr.threads;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.model.multiple.ProcessedImageRepo;
import neildg.com.megatronsr.processing.filters.YangFilter;
import neildg.com.megatronsr.processing.imagetools.ColorSpaceOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.multiple.fusion.MeanFusionOperator;
import neildg.com.megatronsr.processing.multiple.postprocess.ChannelMergeOperator;
import neildg.com.megatronsr.processing.multiple.refinement.DenoisingOperator;
import neildg.com.megatronsr.processing.multiple.resizing.DegradationOperator;
import neildg.com.megatronsr.processing.multiple.resizing.DownsamplingOperator;
import neildg.com.megatronsr.processing.multiple.warping.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.warping.LRWarpingOperator;
import neildg.com.megatronsr.processing.multiple.resizing.LRToHROperator;
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

        ProgressDialogHandler.getInstance().hideDialog();

        //simulate degradation
        //DegradationOperator degradationOperator = new DegradationOperator();
        //degradationOperator.perform();

        //load images and use Y channel as input for succeeding operators
        Mat[] inputMatList = new Mat[BitmapURIRepository.getInstance().getNumImagesSelected()];
        Mat[] yuvRefMat = ColorSpaceOperator.convertRGBToYUV(ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (0), ImageFileAttribute.FileType.JPEG));
        for(int i = 0; i < inputMatList.length; i++) {
            Mat lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
            Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(lrMat);
            inputMatList[i] = yuvMat[ColorSpaceOperator.Y_CHANNEL];
        }

        //perform denoising
        DenoisingOperator denoisingOperator = new DenoisingOperator(inputMatList);
        denoisingOperator.perform();

        //extract features
        YangFilter yangFilter = new YangFilter(denoisingOperator.getResult());
        yangFilter.perform();

        LRToHROperator lrToHROperator = new LRToHROperator(ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (0), ImageFileAttribute.FileType.JPEG));
        lrToHROperator.perform();

        ProcessedImageRepo.initialize();

        //perform feature matching of LR images against the first image as reference mat.
        inputMatList = denoisingOperator.getResult();
        Mat[] succeedingMatList =new Mat[inputMatList.length - 1];
        for(int i = 1; i < inputMatList.length; i++) {
            succeedingMatList[i - 1] = inputMatList[i];
        }
        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator(inputMatList[0], succeedingMatList);
        matchingOperator.perform();

        LRWarpingOperator warpingOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), succeedingMatList, matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        warpingOperator.perform();

        ProgressDialogHandler.getInstance().showDialog("Resizing", "Resizing input images");
        Mat initialMat = ImageOperator.performInterpolation(inputMatList[0], ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        Mat[] warpedMatList = ProcessedImageRepo.getSharedInstance().getWarpedMatList();
        Mat[] combinedMatList = new Mat[warpedMatList.length + 1];
        combinedMatList[0] = initialMat;
        for(int i = 1; i < combinedMatList.length; i++) {
            combinedMatList[i] = ImageOperator.performInterpolation(warpedMatList[i - 1], ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        }
        ProgressDialogHandler.getInstance().hideDialog();


        /*OpticalFlowZeroFillOperator opticalFlowZeroFillOperator = new OpticalFlowZeroFillOperator(yMat, comparingMatList);
        opticalFlowZeroFillOperator.perform();

        ProgressDialogHandler.getInstance().showDialog("Resizing", "Resizing input images");
        Mat initialMat = ImageOperator.performZeroFill(yMat, ParameterConfig.getScalingFactor(), 0 , 0);
        Mat[] zeroFilledMatList = ProcessedImageRepo.getSharedInstance().getZeroFilledMatList();
        Mat[] combinedMatList = new Mat[zeroFilledMatList.length + 1];
        combinedMatList[0] = initialMat;
        for(int i = 1; i < combinedMatList.length; i++) {
            combinedMatList[i] = zeroFilledMatList[i - 1];
        }
        ProgressDialogHandler.getInstance().hideDialog();*/

        MeanFusionOperator meanFusionOperator = new MeanFusionOperator(combinedMatList, "Fusing", "Fusing images using mean");
        meanFusionOperator.perform();

        for(int i = 1; i < combinedMatList.length; i++) {
            combinedMatList[i].release();
        }

        ChannelMergeOperator mergeOperator = new ChannelMergeOperator(meanFusionOperator.getResult(), yuvRefMat[ColorSpaceOperator.U_CHANNEL], yuvRefMat[ColorSpaceOperator.V_CHANNEL]);
        mergeOperator.perform();

        //deallocate some classes
        ProcessedImageRepo.destroy();
        ProgressDialogHandler.getInstance().hideDialog();
    }

}
