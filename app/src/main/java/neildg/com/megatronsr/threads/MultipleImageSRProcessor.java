package neildg.com.megatronsr.threads;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.model.multiple.ProcessedImageRepo;
import neildg.com.megatronsr.processing.multiple.DownsamplingOperator;
import neildg.com.megatronsr.processing.multiple.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.OpticalFlowOperator;
import neildg.com.megatronsr.processing.multiple.fusion.FuseInterpolateOperator;
import neildg.com.megatronsr.processing.multiple.LRToHROperator;
import neildg.com.megatronsr.processing.multiple.LRWarpingOperator;
import neildg.com.megatronsr.processing.multiple.fusion.MotionFusionOperator;
import neildg.com.megatronsr.processing.multiple.fusion.WarpedToHROperator;
import neildg.com.megatronsr.processing.multiple.fusion.ZeroFillFusionOperator;
import neildg.com.megatronsr.processing.single.glasner.PostProcessImage;
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

        OpticalFlowOperator flowOperator = new OpticalFlowOperator();
        flowOperator.perform();

        MotionFusionOperator motionFusionOperator = new MotionFusionOperator(ProcessedImageRepo.getSharedInstance().getZeroFilledMatList());
        motionFusionOperator.perform();

        /*FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator();
        matchingOperator.perform();

        LRWarpingOperator warpingOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        warpingOperator.perform();*/

        //WarpedToHROperator warpedToHROperator = new WarpedToHROperator(warpingOperator.getWarpedMatrixList());
        //warpedToHROperator.perform();
        //FuseInterpolateOperator fuseInterpolateOperator = new FuseInterpolateOperator(warpingOperator.getWarpedMatrixList());
        //fuseInterpolateOperator.perform();

        //PostProcessImage postProcessImage = new PostProcessImage(fuseInterpolateOperator.getOutputMat());
        //postProcessImage.perform();

        //deallocate some classes
        ProcessedImageRepo.destroy();
        ProgressDialogHandler.getInstance().hideDialog();
    }

}
