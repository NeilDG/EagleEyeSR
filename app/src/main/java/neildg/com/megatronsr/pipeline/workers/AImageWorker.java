package neildg.com.megatronsr.pipeline.workers;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.pipeline.ImageProperties;
import neildg.com.megatronsr.pipeline.WorkerListener;

/**
 * An abstract image worker class that is inherited by different pipeline workers that perform some image processing.
 * Accepts input as string, then loads it as Mat, or accepts as Mat.
 * By the end of the process, the image worker releases the input mat and outputs a new mat.
 * Created by NeilDG on 12/10/2016.
 */

public abstract class AImageWorker extends Thread {
    private final static String TAG = "AImageWorker";

    private Lock processLock = new ReentrantLock();
    private Condition propertyCondition = this.processLock.newCondition();
    private boolean processing = false;

    protected ImageProperties ingoingProperties = new ImageProperties(); //properties to be used for performing image processing
    protected ImageProperties outgoingProperties = new ImageProperties(); //properties to be used as output.

    private WorkerListener workerListener;

    protected String workerName;

    public AImageWorker(String workerName, WorkerListener workerListener) {
        this.workerListener = workerListener;
        this.workerName = workerName;
    }

    /*
     * Assigns properties to be used for image processing
     */
    public ImageProperties getIngoingProperties() {
       return this.ingoingProperties;
    }

    public ImageProperties getOutgoingProperties() {
        return this.outgoingProperties;
    }

    @Override
    public void run() {
        try {
            this.processLock.lock();

            //an image worker class will always run
            while(true) {
                while(this.evaluateCondition() == false) {
                    Log.d(TAG, "Condition is not met in the properties file. Thread "+this.workerName+ " will sleep.");
                    this.processing = false;
                    this.propertyCondition.await();
                }
                this.processing = true;
                this.perform();
                this.populateOutgoingProperties(this.outgoingProperties);
                this.workerListener.onWorkerCompleted(this.workerName, this.outgoingProperties);
            }

        } catch(InterruptedException e) {
            Log.e(TAG, "Worker " +this.workerName+ " interrupted. Stopping process.");

        } finally {
            this.processLock.unlock();
        }
    }

    public void signal() {
        if(this.processing == false) {
            this.processLock.lock();
            this.propertyCondition.signal();
            this.processLock.unlock();
        }

    }

    public boolean isProcessing() {
        return this.processing;
    }

    public abstract void perform(); //perform image processing here.
    public abstract boolean evaluateCondition(); //evaluates a given condition in the properties file, if it's true, the worker starts processing. Otherwise, it will await for the next signal.
    public abstract void populateOutgoingProperties(ImageProperties outgoingProperties); //outgoing properties file should be populated. called at the end of the pipeline worker stage.

}
