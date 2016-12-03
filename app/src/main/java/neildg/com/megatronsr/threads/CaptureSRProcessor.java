package neildg.com.megatronsr.threads;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import neildg.com.megatronsr.camera2.capture.CaptureProcessor;
import neildg.com.megatronsr.constants.DialogConstants;
import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.model.multiple.ProcessingQueue;
import neildg.com.megatronsr.platformtools.notifications.NotificationCenter;
import neildg.com.megatronsr.platformtools.notifications.NotificationListener;
import neildg.com.megatronsr.platformtools.notifications.Notifications;
import neildg.com.megatronsr.platformtools.notifications.Parameters;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * The main entry point for the SR functionality that is triggered via image captured.
 * Created by NeilDG on 11/27/2016.
 */

public class CaptureSRProcessor extends Thread implements NotificationListener {
    private final static String TAG = "CaptureSRProcessor";

    private Lock processLock = new ReentrantLock();
    private final Condition hasImage = processLock.newCondition();
    private boolean active = false;

    public CaptureSRProcessor() {

    }

    public void startBackgroundThread() {
        if(this.active == false) {
            NotificationCenter.getInstance().addObserver(Notifications.ON_SR_AWAKE, this);
            this.active = true;
            this.start();
        }
    }

    public void stopBackgroundThread() {
        if(this.active) {
            this.active = false;
            NotificationCenter.getInstance().removeObserver(Notifications.ON_SR_AWAKE, this);
            this.interrupt();
        }
    }

    @Override
    public void run() {
        //this.interpolateFirstImage();

        this.processLock.lock();

        try {

            //thread is always alive
            while(true) {

                while (ProcessingQueue.getInstance().getInputLength() == 0) {
                    Log.d(TAG, "No images to process yet. Awaiting images.");
                    this.hasImage.await();
                }

                //perform code here
                Log.d(TAG, "I can now process  "+ProcessingQueue.getInstance().peekImageName()); //NOTE: should be peek image name. for testing only.

                Thread.sleep(4000); //testing

                //once finished, dequeue image name, then broadcast dequeue event
                String imageName = ProcessingQueue.getInstance().dequeueImageName();
                Parameters parameters = new Parameters();
                parameters.putExtra(ProcessingQueue.IMAGE_NAME_KEY, imageName);
                NotificationCenter.getInstance().postNotification(Notifications.ON_IMAGE_DEQUEUED, parameters);
            }

        } catch (InterruptedException e) {
            Log.e(TAG , "SR processor terminated.");
        }
        finally {
            this.processLock.unlock();
        }
    }

    private void interpolateFirstImage() {
        ProgressDialogHandler.getInstance().showProcessDialog(DialogConstants.DIALOG_PROGRESS_TITLE, "Upsampling image using nearest-neighbor.", 40.0f);

        Mat inputMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.INPUT_PREFIX_STRING, ImageFileAttribute.FileType.JPEG);

        Mat outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_NEAREST);
        FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_NEAREST, ImageFileAttribute.FileType.JPEG);
        outputMat.release();

        ProgressDialogHandler.getInstance().showProcessDialog(DialogConstants.DIALOG_PROGRESS_TITLE, "Upsampling image using linear.", 60.0f);

        outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_LINEAR);
        FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_LINEAR, ImageFileAttribute.FileType.JPEG);
        outputMat.release();

        ProgressDialogHandler.getInstance().showProcessDialog(DialogConstants.DIALOG_PROGRESS_TITLE, "Upsampling image using bicubic.", 80.0f);

        outputMat = ImageOperator.performInterpolation(inputMat, ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        FileImageWriter.getInstance().saveMatrixToImage(outputMat, FilenameConstants.HR_CUBIC, ImageFileAttribute.FileType.JPEG);
        outputMat.release();

        ProgressDialogHandler.getInstance().showProcessDialog(DialogConstants.DIALOG_PROGRESS_TITLE, "Upsampling image using bicubic.", 95.0f);

        inputMat.release();
        System.gc();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ProgressDialogHandler.getInstance().hideProcessDialog();
    }

    @Override
    public void onNotify(String notificationString, Parameters params) {
        if(notificationString == Notifications.ON_SR_AWAKE) {
            this.processLock.lock();
            this.hasImage.signal();
            this.processLock.unlock();
        }
    }
}
