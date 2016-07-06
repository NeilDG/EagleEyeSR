package neildg.com.megatronsr.threads;

import org.opencv.core.Mat;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.model.multiple.ProcessedImageRepo;
import neildg.com.megatronsr.processing.multiple.DownsamplingOperator;
import neildg.com.megatronsr.processing.multiple.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.LRWarpingOperator;
import neildg.com.megatronsr.processing.multiple.OpticalFlowOperator;
import neildg.com.megatronsr.processing.multiple.LRToHROperator;
import neildg.com.megatronsr.processing.multiple.fusion.HDRFusionOperator;
import neildg.com.megatronsr.processing.multiple.fusion.MotionFusionOperator;
import neildg.com.megatronsr.processing.multiple.fusion.MultiplePatchFusionOperator;
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

        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        ProgressDialogHandler.getInstance().hideDialog();

        LRToHROperator lrToHROperator = new LRToHROperator();
        lrToHROperator.perform();

        ProcessedImageRepo.initialize();


        //load the images
        Mat referenceMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + "0", ImageFileAttribute.FileType.JPEG);
        Mat[] comparingMatList = new Mat[BitmapURIRepository.getInstance().getNumImagesSelected() - 1];
        for(int i = 0; i < comparingMatList.length; i++) {
            comparingMatList[i] = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (i+1), ImageFileAttribute.FileType.JPEG);
        }

        //perform feature matching of LR images against the first image as reference mat.
        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator(referenceMat, comparingMatList);
        matchingOperator.perform();

        LRWarpingOperator warpingOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), comparingMatList, matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        warpingOperator.perform();

        Mat originMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);

        HDRFusionOperator hdrFusionOperator = new HDRFusionOperator(originMat, ProcessedImageRepo.getSharedInstance().getWarpedMatList());
        hdrFusionOperator.perform();

        //deallocate some classes
        ProcessedImageRepo.destroy();
        ProgressDialogHandler.getInstance().hideDialog();
    }

}
