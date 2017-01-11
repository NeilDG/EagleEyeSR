package neildg.com.eagleeyesr.threads;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.pipeline.ProcessingQueue;
import neildg.com.eagleeyesr.pipeline.PipelineManager;
import neildg.com.eagleeyesr.platformtools.notifications.NotificationCenter;
import neildg.com.eagleeyesr.platformtools.notifications.NotificationListener;
import neildg.com.eagleeyesr.platformtools.notifications.Notifications;
import neildg.com.eagleeyesr.platformtools.notifications.Parameters;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;

/**
 * The main entry point for the SR functionality that is triggered via image captured.
 * Created by NeilDG on 11/27/2016.
 */

public class CaptureSRProcessor extends Thread implements NotificationListener {
    private final static String TAG = "CaptureSR_Pipeline";

    private Lock processLock = new ReentrantLock();

    private final Condition hasImage = processLock.newCondition();
    private final Condition pipelineManagerFlag = processLock.newCondition();

    private boolean firstImage = true;
    private boolean active = false;

    public CaptureSRProcessor() {

    }
    public void startBackgroundThread() {
        if(this.active == false) {
            NotificationCenter.getInstance().addObserver(Notifications.ON_SR_AWAKE, this);
            NotificationCenter.getInstance().addObserver(Notifications.ON_PIPELINE_REQUEST_NEW_IMAGE, this);
            this.active = true;
            this.start();
            PipelineManager.initialize();
            PipelineManager.getInstance().startWorkers();
        }
    }

    public void stopBackgroundThread() {
        if(this.active) {
            this.active = false;
            NotificationCenter.getInstance().removeObserver(Notifications.ON_SR_AWAKE, this);
            NotificationCenter.getInstance().removeObserver(Notifications.ON_PIPELINE_REQUEST_NEW_IMAGE, this);
            this.interrupt();
            PipelineManager.destroy();
        }
    }

    @Override
    public void run() {
        try {

            //thread is always alive
            while(true) {
                while (ProcessingQueue.getInstance().getInputLength() == 0) {
                    Log.d(TAG, "No images to process yet. Awaiting images.");
                    this.processLock.lock();
                    this.hasImage.await();
                    this.processLock.unlock();
                }

                //perform code here
                if(this.firstImage) {
                    String imageName = ProcessingQueue.getInstance().dequeueImageName();
                    Log.d(TAG, "Interpolating as initial HR "+imageName);
                    PipelineManager.broadcastPipelineUpdate(imageName, PipelineManager.INITIAL_HR_CREATION);
                    this.produceInitialHRImage(imageName);
                    this.firstImage = false;

                    //pass to pipeline manager
                    PipelineManager.getInstance().addImageEntry(imageName);
                }
                else {
                    String imageName = ProcessingQueue.getInstance().dequeueImageName();
                    PipelineManager.getInstance().addImageEntry(imageName);
                }

                Log.d(TAG, "Awaiting for signal from pipeline manager");
                this.processLock.lock();
                this.pipelineManagerFlag.await(); //await for signal from pipeline manager.
                this.processLock.unlock();
                Log.d(TAG, "CaptureSR awaked!");

            }

        } catch (InterruptedException e) {
            Log.e(TAG , "SR processor terminated.");
        }
        finally {
            this.processLock.unlock();
        }
    }

    private void produceInitialHRImage(String fileName) {
        boolean isDebugMode = ParameterConfig.getPrefsBoolean(ParameterConfig.DEBUGGING_FLAG_KEY, false);

        //produce initial HR using cubic interpolation
        Mat inputMat = FileImageReader.getInstance().imReadOpenCV(fileName, ImageFileAttribute.FileType.JPEG);
        Mat outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_LINEAR);
        FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_ITERATION_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);

        if(isDebugMode) {
            //create cubic interpolation copy for comparison
            FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_CUBIC, ImageFileAttribute.FileType.JPEG);
            outputMat.release();

            //produce other types of known interpolation techniques for comparison
            outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_NEAREST);
            FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_NEAREST, ImageFileAttribute.FileType.JPEG);
            outputMat.release();

            outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
            FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_LINEAR, ImageFileAttribute.FileType.JPEG);
            outputMat.release();

            inputMat.release();
            System.gc();
        }

    }


    @Override
    public void onNotify(String notificationString, Parameters params) {
        if(notificationString == Notifications.ON_SR_AWAKE) {
            this.processLock.lock();
            this.hasImage.signal();
            this.processLock.unlock();
        }
        else if(notificationString == Notifications.ON_PIPELINE_REQUEST_NEW_IMAGE) {
            this.processLock.lock();
            this.pipelineManagerFlag.signal();
            this.processLock.unlock();
        }
    }
}
