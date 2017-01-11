package neildg.com.eagleeyesr.pipeline.workers;

import android.util.Log;

import org.opencv.core.Mat;

import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.model.multiple.SharpnessMeasure;
import neildg.com.eagleeyesr.pipeline.ImageProperties;
import neildg.com.eagleeyesr.pipeline.WorkerListener;
import neildg.com.eagleeyesr.processing.filters.YangFilter;
import neildg.com.eagleeyesr.processing.imagetools.ColorSpaceOperator;
import neildg.com.eagleeyesr.processing.imagetools.MatMemory;

/**
 * Handles the measurement of image sharpness and determining if image is a worthy candidate for mean fusion
 * Created by NeilDG on 12/10/2016.
 */

public class SharpnessMeasureWorker extends AImageWorker {
    public final static String TAG = "SharpnessMeasureWorker";

    public final static String IMAGE_INPUT_NAME_KEY = "IMAGE_INPUT_NAME_KEY";
    public final static String HAS_PASSED_MEASURE_KEY = "HAS_PASSED_MEASURE_KEY";

    private double lastMeasuredSharpness = 0.0;

    private String inputName;
    private boolean result = false;

    public SharpnessMeasureWorker(WorkerListener workerListener) {
        super(TAG, workerListener);
    }

    @Override
    public void perform() {
        Mat[] inputMatList = new Mat[1];
        Mat inputMat = FileImageReader.getInstance().imReadOpenCV(this.inputName, ImageFileAttribute.FileType.JPEG);
        Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(inputMat);

        inputMatList[0] = yuvMat[ColorSpaceOperator.Y_CHANNEL];

        //extract features
        YangFilter yangFilter = new YangFilter(inputMatList);
        yangFilter.perform();

        //get the sharpness measure of input image
        double currentSharpness = SharpnessMeasure.measure(yangFilter.getEdgeMatList()[0]);

        //release energy input mat list
        MatMemory.releaseAll(inputMatList, false);
        MatMemory.releaseAll(yuvMat, false);

        if(this.lastMeasuredSharpness <= currentSharpness) {
            //replace last measured sharpness with mean
            this.lastMeasuredSharpness = (this.lastMeasuredSharpness + currentSharpness) / 2.0f;
            Log.d(TAG, "Sharpness measure updated! New value: " +this.lastMeasuredSharpness);
            this.result = true;
        }
        else {
            this.result = false;
        }

    }

    @Override
    public boolean evaluateCondition() {
        this.inputName = this.ingoingProperties.getStringExtra(IMAGE_INPUT_NAME_KEY, null);
        this.ingoingProperties.clearAll(); //initiate clear all after evaluating.

        if(this.inputName != null) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void populateOutgoingProperties(ImageProperties outgoingProperties) {
        outgoingProperties.putExtra(IMAGE_INPUT_NAME_KEY, this.inputName);
        outgoingProperties.putExtra(HAS_PASSED_MEASURE_KEY, this.result);
    }
}
