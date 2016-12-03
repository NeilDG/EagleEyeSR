package neildg.com.megatronsr.model.multiple;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Singleton instance that contains a queue of input string filenames that will be processed by the capture processor, and shown by the processing queue screen.
 * Created by NeilDG on 12/3/2016.
 */

public class ProcessingQueue {
    private final static String TAG = "ProcessingQueue";

    private static ProcessingQueue sharedInstance = null;
    public static ProcessingQueue getInstance() {
        if(sharedInstance == null) {
            sharedInstance = new ProcessingQueue();
        }

        return sharedInstance;
    }

    private Queue<String> inputQueue = new LinkedList<>();

    private ProcessingQueue() {

    }

    public void enqueueImageName(String imageName) {
        this.inputQueue.add(imageName);
        Log.d(TAG, "Added image "+imageName+ " to input queue.");
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

    public boolean hasElements() {
        return this.inputQueue.size() > 0;
    }

    public int getInputLength() {
        return this.inputQueue.size();
    }
}
