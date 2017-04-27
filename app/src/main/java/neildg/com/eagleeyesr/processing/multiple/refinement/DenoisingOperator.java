package neildg.com.eagleeyesr.processing.multiple.refinement;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.photo.Photo;

import java.util.concurrent.Semaphore;

import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ColorSpaceOperator;
import neildg.com.eagleeyesr.threads.FlaggingThread;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;

/**
 * Class that handles denoising operations
 * Created by NeilDG on 7/10/2016.
 */
public class DenoisingOperator implements IOperator{
    private final static String TAG = "DenoisingOperator";

    private Mat[] matList;
    private Mat[] outputMatList;

    public DenoisingOperator(Mat[] matList) {
        this.matList = matList;
        this.outputMatList = new Mat[this.matList.length];
    }

    @Override
    public void perform() {
        for(int i = 0; i < this.matList.length; i++) {
            ProgressDialogHandler.getInstance().showProcessDialog("Denoising", "Denoising image " +i, ProgressDialogHandler.getInstance().getProgress());

            //perform denoising on energy channel only
            Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(this.matList[i]);
            Mat denoisedMat = new Mat();
            MatOfFloat h = new MatOfFloat(6.0f);
            Photo.fastNlMeansDenoising(yuvMat[ColorSpaceOperator.Y_CHANNEL], denoisedMat, h, 7, 21, Core.NORM_L1);

            FileImageWriter.getInstance().saveMatrixToImage(yuvMat[ColorSpaceOperator.Y_CHANNEL], "noise_" +i, ImageFileAttribute.FileType.JPEG);
            FileImageWriter.getInstance().saveMatrixToImage(denoisedMat, "denoise_" +i, ImageFileAttribute.FileType.JPEG);

            //merge channel then convert back to RGB
            yuvMat[ColorSpaceOperator.Y_CHANNEL].release();
            yuvMat[ColorSpaceOperator.Y_CHANNEL] = denoisedMat;

            this.outputMatList[i] = ColorSpaceOperator.convertYUVtoRGB(yuvMat);

            ProgressDialogHandler.getInstance().hideProcessDialog();
        }

        /*DenoisingWorker[] denoisingWorkers = new DenoisingWorker[this.matList.length];
        Semaphore denoiseSem = new Semaphore(this.matList.length);

        ProgressDialogHandler.getInstance().showProcessDialog("Denoising", "Performing multithreaded denoising", ProgressDialogHandler.getInstance().getProgress());

        //perform multithreaded denoising
        try {
            for(int i = 0; i < this.matList.length; i++) {
                denoisingWorkers[i] = new DenoisingWorker(this.matList[i], i, denoiseSem);
                denoisingWorkers[i].startWork();
            }

            //wait for all threads to finish by acquiring all permits here.
            denoiseSem.acquire(this.matList.length);
            for(int i = 0; i < this.matList.length; i++) {
                this.outputMatList[i] = denoisingWorkers[i].getOutputMat();
            }

        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        ProgressDialogHandler.getInstance().hideProcessDialog();*/
    }

    public Mat[] getResult() {
        return this.outputMatList;
    }

    private class DenoisingWorker extends FlaggingThread {

        private Mat inputMat;
        private int indexCount;

        private Mat outputMat;


        public DenoisingWorker(Mat inputMat, int count, Semaphore semaphore) {
            super(semaphore);

            this.inputMat = inputMat;
            this.indexCount = count;
        }

        @Override
        public void run() {
            Log.i(TAG, "Started denoising for image index " +this.indexCount);

            //perform denoising on energy channel only
            Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(this.inputMat);
            MatOfFloat h = new MatOfFloat(6.0f);

            Mat denoisedMat = new Mat();
            Photo.fastNlMeansDenoising(yuvMat[ColorSpaceOperator.Y_CHANNEL], denoisedMat, h, 7, 21, Core.NORM_L1);

            FileImageWriter.getInstance().saveMatrixToImage(yuvMat[ColorSpaceOperator.Y_CHANNEL], "noise_" +this.indexCount, ImageFileAttribute.FileType.JPEG);
            FileImageWriter.getInstance().saveMatrixToImage(denoisedMat, "denoise_" +this.indexCount, ImageFileAttribute.FileType.JPEG);

            //merge channel then convert back to RGB
            yuvMat[ColorSpaceOperator.Y_CHANNEL].release();
            yuvMat[ColorSpaceOperator.Y_CHANNEL] = denoisedMat;

            this.outputMat = ColorSpaceOperator.convertYUVtoRGB(yuvMat);

            this.finishWork();
            Log.i(TAG, "Finished denoising for image index " +this.indexCount);

        }

        public Mat getOutputMat() {
            return this.outputMat;
        }
    }
}
