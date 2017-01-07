package neildg.com.eagleeyesr.pipeline;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import neildg.com.eagleeyesr.pipeline.workers.DenoisingWorker;
import neildg.com.eagleeyesr.pipeline.workers.ImageAlignmentWorker;
import neildg.com.eagleeyesr.pipeline.workers.ImageFusionWorker;

/**
 * Contains queues that is used by different worker threads. Denoising, image alignment and image fusion workers hold queues
 * to store images that are about to enter the said pipeline stages.
 * Created by NeilDG on 12/26/2016.
 */

public class WorkerQueueTable {
    private final static String TAG = "Pipeline_WorkerQueue";

    private HashMap<String, Queue<String>> imageQueueTable = new HashMap<String, Queue<String>>();

    public static WorkerQueueTable createInstance() {
        return new WorkerQueueTable();
    }

    private WorkerQueueTable() {
        //setup different image queues here.
        this.imageQueueTable.put(DenoisingWorker.TAG, new LinkedList<String>());
        this.imageQueueTable.put(ImageAlignmentWorker.TAG, new LinkedList<String>());
        this.imageQueueTable.put(ImageFusionWorker.TAG, new LinkedList<String>());
    }

    public synchronized void enqueueImageToWorker(String workerName, String imageName) {
        if(this.imageQueueTable.containsKey(workerName)) {
            this.imageQueueTable.get(workerName).add(imageName);
            Log.d(TAG, "Enqueued " +imageName + "  to " +workerName+ " queue.");
        }
        else {
            Log.e(TAG, workerName + " does not exist. ");
        }
    }

    public synchronized String dequeueImageFromWorker(String workerName) {
        if(this.hasPendingTasksForWorker(workerName)) {
            Log.d(TAG, "Dequeued image from " +workerName+ " queue.");
            return this.imageQueueTable.get(workerName).remove();
        }
        else {
            Log.e(TAG, workerName + " does not exist or the queue is empty.");
            return null;
        }
    }

    public synchronized boolean hasPendingTasksForWorker(String workerName) {
        return (this.imageQueueTable.containsKey(workerName) && this.imageQueueTable.get(workerName).isEmpty() == false);
    }
}
