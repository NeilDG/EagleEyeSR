package neildg.com.eagleeyesr.pipeline.workers;

import android.util.Log;

import org.opencv.core.Mat;

import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.pipeline.ImageProperties;
import neildg.com.eagleeyesr.pipeline.WorkerListener;
import neildg.com.eagleeyesr.processing.imagetools.MatMemory;
import neildg.com.eagleeyesr.processing.multiple.refinement.DenoisingOperator;

/**
 * Worker that performs denoising
 * Created by NeilDG on 12/26/2016.
 */

public class DenoisingWorker extends AImageWorker {
    public final static String TAG  = "DenoisingWorker";

    public final static String IMAGE_INPUT_NAME_KEY = "IMAGE_INPUT_NAME_KEY";
    public final static String IMAGE_OUTPUT_NAME_KEY = "IMAGE_OUTPUT_NAME_KEY";

    private String inputName;

    public DenoisingWorker(WorkerListener workerListener) {
        super(TAG, workerListener);
    }
    @Override
    public void perform() {

        boolean performDenoising = ParameterConfig.getPrefsBoolean(ParameterConfig.DENOISE_FLAG_KEY, false);

        if(performDenoising) {
            Mat[] rgbInputMatList = new Mat[1];
            rgbInputMatList[0] = FileImageReader.getInstance().imReadOpenCV(this.inputName, ImageFileAttribute.FileType.JPEG);

            //perform denoising on original input list
            DenoisingOperator denoisingOperator = new DenoisingOperator(rgbInputMatList);
            denoisingOperator.perform();
            MatMemory.releaseAll(rgbInputMatList, false);

            rgbInputMatList = denoisingOperator.getResult();

            FileImageWriter.getInstance().saveMatrixToImage(rgbInputMatList[0], this.inputName, ImageFileAttribute.FileType.JPEG); //overwrite input with denoised file.

            MatMemory.releaseAll(rgbInputMatList, false);
        }
        else {
            Log.d(TAG, "Denoising will be skipped for " +this.inputName);
        }
    }


    @Override
    public boolean evaluateCondition() {
        this.inputName = this.ingoingProperties.getStringExtra(IMAGE_INPUT_NAME_KEY, null);
        this.ingoingProperties.clearAll(); //initiate clear all after evaluating.

        return (this.inputName != null);
    }

    @Override
    public void populateOutgoingProperties(ImageProperties outgoingProperties) {
        outgoingProperties.putExtra(IMAGE_OUTPUT_NAME_KEY, this.inputName);
    }
}
