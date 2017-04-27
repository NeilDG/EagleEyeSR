package neildg.com.eagleeyesr.processing.multiple.assessment;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.concurrent.Semaphore;

import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.ImageInputMap;
import neildg.com.eagleeyesr.processing.imagetools.ColorSpaceOperator;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;
import neildg.com.eagleeyesr.threads.FlaggingThread;

/**
 * Reads a given input image, downsamples it and converts it to energy mat
 * Created by NeilDG on 1/11/2017.
 */

public class InputImageEnergyReader extends FlaggingThread {
    private final static String TAG = "InputImageEnergyReader";
    private String inputImagePath;
    private Mat outputMat;

    public InputImageEnergyReader(Semaphore semaphore, String inputImagePath) {
        super(semaphore);
        this.inputImagePath = inputImagePath;
    }

    @Override
    public void run() {
        Log.d(TAG, "Started energy reading for " +this.inputImagePath);

        Mat inputMat = FileImageReader.getInstance().imReadFullPath(this.inputImagePath);
        inputMat = ImageOperator.downsample(inputMat, 0.125f); //downsample

        Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(inputMat);

        this.outputMat = yuvMat[ColorSpaceOperator.Y_CHANNEL];
        inputMat.release();

        this.finishWork();

        Log.d(TAG, "Ended energy reading! Success!");
    }

    public Mat getOutputMat() {
        return this.outputMat;
    }
}
