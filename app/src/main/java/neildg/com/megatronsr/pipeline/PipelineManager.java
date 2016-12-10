package neildg.com.megatronsr.pipeline;

import android.util.Log;

import neildg.com.megatronsr.model.multiple.ProcessingQueue;
import neildg.com.megatronsr.platformtools.notifications.NotificationCenter;
import neildg.com.megatronsr.platformtools.notifications.Notifications;
import neildg.com.megatronsr.platformtools.notifications.Parameters;

/**
 * The pipeline manager holds the worker threads for performing super-res
 * Created by NeilDG on 12/10/2016.
 */

public class PipelineManager implements WorkerListener {
    private final static String TAG = "PipelineManager";

    public final static String SHARPNESS_MEASURE_WORKER = "SHARPNESS_MEASURE_WORKER";
    public final static String DENOISING_WORKER = "DENOISING_WORKER";

    private static PipelineManager sharedInstance = null;
    public static PipelineManager getInstance() {
        return sharedInstance;
    }

    public static void initialize() {
        sharedInstance = new PipelineManager();
    }

    public static void destroy() {
        sharedInstance.sharpnessMeasureWorker.interrupt();
        sharedInstance = null;
    }

    //properties associated with image workers
    private SharpnessMeasureWorker sharpnessMeasureWorker = new SharpnessMeasureWorker(SHARPNESS_MEASURE_WORKER, this);

    public void startWorkers() {
        this.sharpnessMeasureWorker.start();
    }

    /*
     * Adds an image entry at the start of the pipeline stage (sharpness measure)
     */
    public void addImageEntry(String imageName) {
        this.sharpnessMeasureWorker.getIngoingProperties().putExtra(SharpnessMeasureWorker.IMAGE_INPUT_NAME_KEY, imageName);

        //signal sharpness measure worker here
        this.sharpnessMeasureWorker.signal();
    }

    private void initiateDenoising(String imageName) {
        Log.d(TAG, "Initiating denoising for "+imageName);

        //TODO: temporary end of pipeline. Unqueue image name from processing queue
        //once finished, dequeue image name, then broadcast dequeue event
        Parameters parameters = new Parameters();
        parameters.putExtra(ProcessingQueue.IMAGE_NAME_KEY, imageName);
        NotificationCenter.getInstance().postNotification(Notifications.ON_IMAGE_DEQUEUED, parameters);
    }

    @Override
    public void onWorkerCompleted(String workerName, ImageProperties properties) {
        if(workerName == SHARPNESS_MEASURE_WORKER) {
            //pass input image to denoising worker
            String imageName = properties.getStringExtra(SharpnessMeasureWorker.IMAGE_INPUT_NAME_KEY, null);
            boolean hasPassed = properties.getBooleanExtra(SharpnessMeasureWorker.HAS_PASSED_MEASURE_KEY, false);

            if(hasPassed) {
                this.initiateDenoising(imageName);
            }
        }
    }
}
