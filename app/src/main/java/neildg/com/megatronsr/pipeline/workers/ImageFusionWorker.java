package neildg.com.megatronsr.pipeline.workers;

import org.opencv.core.Mat;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.pipeline.ImageProperties;
import neildg.com.megatronsr.pipeline.WorkerListener;
import neildg.com.megatronsr.processing.imagetools.MatMemory;
import neildg.com.megatronsr.processing.multiple.fusion.OptimizedMeanFusionOperator;
import neildg.com.megatronsr.processing.multiple.fusion.PipelinedMeanFusionOperator;

/**
 * Handles the image fusion
 * Created by NeilDG on 12/27/2016.
 */

public class ImageFusionWorker extends AImageWorker {
    public final static String TAG = "ImageFusionWorker";

    public final static String IMAGE_INPUT_NAME_KEY = "IMAGE_INPUT_NAME_KEY";
    public final static String IMAGE_OUTPUT_NAME_KEY = "IMAGE_OUTPUT_NAME_KEY";

    public final static String IMAGE_REFERENCE_NAME_KEY = "IMAGE_REFERENCE_NAME_KEY";

    private String inputImageName;
    private String referenceImageName;
    private String previousHRImageName;
    private String outputImageName;

    private int imageCounter = 0;
    public ImageFusionWorker(WorkerListener workerListener) {
        super(TAG, workerListener);
    }

    @Override
    public void perform() {
        boolean doesFileExists = FileImageReader.getInstance().doesImageExists(this.previousHRImageName, ImageFileAttribute.FileType.JPEG);

        if(doesFileExists) {
            PipelinedMeanFusionOperator fusionOperator = new PipelinedMeanFusionOperator(this.previousHRImageName, this.inputImageName);
            fusionOperator.perform();
            this.imageCounter++;

            Mat result = fusionOperator.getResult();
            this.outputImageName = FilenameConstants.HR_ITERATION_PREFIX_STRING + this.imageCounter;
            FileImageWriter.getInstance().saveMatrixToImage(result,this.outputImageName, ImageFileAttribute.FileType.JPEG);
            FileImageWriter.getInstance().saveMatrixToImage(result,FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG);

            result.release();
            MatMemory.cleanMemory();
        }
        else {

        }
    }

    @Override
    public boolean evaluateCondition() {
        this.inputImageName = this.getIngoingProperties().getStringExtra(IMAGE_INPUT_NAME_KEY, null);
        this.referenceImageName = this.getIngoingProperties().getStringExtra(IMAGE_REFERENCE_NAME_KEY, null);

        this.previousHRImageName = FilenameConstants.HR_ITERATION_PREFIX_STRING + this.imageCounter;
        this.ingoingProperties.clearAll();

        return (this.inputImageName != null && this.referenceImageName != null); //image alignment will start for the respective image if  it's not the first image.
    }

    @Override
    public void populateOutgoingProperties(ImageProperties outgoingProperties) {
        outgoingProperties.putExtra(IMAGE_INPUT_NAME_KEY, this.inputImageName);
        outgoingProperties.putExtra(IMAGE_OUTPUT_NAME_KEY, this.outputImageName);
        outgoingProperties.putExtra(IMAGE_REFERENCE_NAME_KEY, this.referenceImageName);
    }
}
