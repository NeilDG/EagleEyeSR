package neildg.com.megatronsr.pipeline;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import neildg.com.megatronsr.pipeline.workers.DenoisingWorker;
import neildg.com.megatronsr.pipeline.workers.ImageAlignmentWorker;

/**
 * Contains queues that is used by different worker threads. Denoising, image alignment and image fusion workers hold queues
 * to store images that are about to enter the said pipeline stages.
 * Created by NeilDG on 12/26/2016.
 */

public class WorkerQueueTable {
    private final static String TAG = "WorkerQueueTable";

    private HashMap<String, Queue<String>> imageQueueTable = new HashMap<String, Queue<String>>();

    private WorkerQueueTable() {
        //setup different image queues here.
        this.imageQueueTable.put(DenoisingWorker.TAG, new LinkedList<String>());
        this.imageQueueTable.put(ImageAlignmentWorker.TAG, new LinkedList<String>());
    }

    public void enqueueImageToWorker(String workerName, String imageName) {
        if(this.imageQueueTable.containsKey(workerName)) {
            this.imageQueueTable.get(workerName).add(imageName);
        }
        else {
            Log.e(TAG, workerName + " does not exist. ");
        }
    }

    public String dequeueImageFromWorker(String workerName) {
        if(this.imageQueueTable.containsKey(workerName) && this.imageQueueTable.get(workerName).isEmpty() == false) {
            return this.imageQueueTable.get(workerName).remove();
        }
        else {
            Log.e(TAG, workerName + " does not exist or the queue is empty.");
            return null;
        }
    }
}
