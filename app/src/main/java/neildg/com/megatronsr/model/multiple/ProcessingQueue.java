package neildg.com.megatronsr.model.multiple;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import neildg.com.megatronsr.platformtools.notifications.Parameters;

/**
 * Singleton instance that contains a queue of input string filenames that will be processed by the capture processor, and shown by the processing queue screen.
 * Created by NeilDG on 12/3/2016.
 */

public class ProcessingQueue {
    private final static String TAG = "ProcessingQueue";
    public final static String IMAGE_NAME_KEY = "IMAGE_NAME_KEY";

    private static ProcessingQueue sharedInstance = null;
    public static ProcessingQueue getInstance() {
        return sharedInstance;
    }

    public static void initialize() {
        sharedInstance = new ProcessingQueue();
    }

    public static void destroy() {
        sharedInstance = null;
    }

    private Queue<String> inputQueue = new LinkedList<>();

    private int counter = 0;

    private ProcessingQueue() {
        this.counter = 0;
    }

    public void enqueueImageName(String imageName) {
        this.inputQueue.add(imageName);
        this.counter++;
        Log.d(TAG, "Queue: Added image "+imageName+ " to input queue.");
    }

    public String dequeueImageName() {
        if(this.inputQueue.size() > 0) {
            return this.inputQueue.remove();
        }
        else {
            Log.d(TAG, "Input queue is already empty!");
            return null;
        }
    }

    public String peekImageName() {
        if(this.inputQueue.size() > 0) {
            return this.inputQueue.peek();
        }
        else {
            Log.d(TAG, "Input queue is already empty!");
            return null;
        }
    }

    public String getLatestImageName() {
        if(this.inputQueue.size() > 0) {
            LinkedList<String> inputList = (LinkedList<String>) this.inputQueue;
            return inputList.getLast();
        }
        else {
            Log.d(TAG, "Input queue is already empty!");
            return null;
        }
    }

    public boolean hasElements() {
        return this.inputQueue.size() > 0;
    }

    public int getCounter() {
        return this.counter;
    }
    public int getInputLength() {
        return this.inputQueue.size();
    }
}
