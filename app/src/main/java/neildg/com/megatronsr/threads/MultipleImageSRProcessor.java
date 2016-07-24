package neildg.com.megatronsr.threads;

import android.media.Image;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.multiple.DisplacementValue;
import neildg.com.megatronsr.model.multiple.ProcessedImageRepo;
import neildg.com.megatronsr.model.multiple.SharpnessMeasure;
import neildg.com.megatronsr.processing.filters.YangFilter;
import neildg.com.megatronsr.processing.imagetools.ColorSpaceOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.processing.multiple.fusion.EdgeFusionOperator;
import neildg.com.megatronsr.processing.multiple.fusion.HDRFusionOperator;
import neildg.com.megatronsr.processing.multiple.fusion.MeanFusionOperator;
import neildg.com.megatronsr.processing.multiple.postprocess.ChannelMergeOperator;
import neildg.com.megatronsr.processing.multiple.refinement.DenoisingOperator;
import neildg.com.megatronsr.processing.multiple.resizing.DegradationOperator;
import neildg.com.megatronsr.processing.multiple.resizing.DownsamplingOperator;
import neildg.com.megatronsr.processing.multiple.warping.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.warping.LRWarpingOperator;
import neildg.com.megatronsr.processing.multiple.resizing.LRToHROperator;
import neildg.com.megatronsr.processing.multiple.warping.OpticalFlowZeroFillOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * SRProcessor main entry point
 * Created by NeilDG on 3/5/2016.
 */
public class MultipleImageSRProcessor extends Thread {
    private final static String TAG = "MultipleImageSR";

    public MultipleImageSRProcessor() {

    }

    @Override
    public void run() {
        ProgressDialogHandler.getInstance().showDialog("Downsampling images", "Downsampling images selected and saving them in file.");

        //initialize storage classes
        ProcessedImageRepo.initialize();
        SharpnessMeasure.initialize();

        //downsample
        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        ProgressDialogHandler.getInstance().hideDialog();

        //simulate degradation
        //DegradationOperator degradationOperator = new DegradationOperator();
        //degradationOperator.perform();

        //load images and use Y channel as input for succeeding operators
        Mat[] inputMatList = new Mat[BitmapURIRepository.getInstance().getNumImagesSelected()];
        Mat[] yMatList = new Mat[inputMatList.length];
        for(int i = 0; i < inputMatList.length; i++) {
            Mat lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (i), ImageFileAttribute.FileType.JPEG);
            inputMatList[i] = lrMat;
            yMatList[i] = ColorSpaceOperator.convertRGBToYUV(lrMat)[ColorSpaceOperator.Y_CHANNEL];
        }

        //extract features
        YangFilter yangFilter = new YangFilter(yMatList);
        yangFilter.perform();

        //trim the input list from the measured sharpness mean
        SharpnessMeasure.SharpnessResult sharpnessResult = SharpnessMeasure.getSharedInstance().getLatestResult();
        inputMatList = SharpnessMeasure.getSharedInstance().trimMatList(inputMatList, sharpnessResult);

        DenoisingOperator denoisingOperator = new DenoisingOperator(inputMatList);
        denoisingOperator.perform();

        LRToHROperator lrToHROperator = new LRToHROperator(ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + (0), ImageFileAttribute.FileType.JPEG));
        lrToHROperator.perform();

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
        Mat[] warpedMatList = ProcessedImageRepo.getSharedInstance().getWarpedMatList();

        HDRFusionOperator fusionOperator = new HDRFusionOperator(inputMatList[0], warpedMatList);
        fusionOperator.perform();

        //deallocate some classes
        ProcessedImageRepo.destroy();
        SharpnessMeasure.destroy();
        ProgressDialogHandler.getInstance().hideDialog();
    }

}
