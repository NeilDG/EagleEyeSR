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

    public final static String IMAGE_NAME_KEY = "IMAGE_NAME_KEY";
    public final static String PIPELINE_STAGE_KEY = "PIPELINE_STAGE_KEY";

    public final static String SHARPNESS_MEASURE_WORKER = "SHARPNESS_MEASURE_WORKER";
    public final static String DENOISING_WORKER = "DENOISING_WORKER";

    public final static String PROCESSING_QUEUE_STAGE = "In Queue";
    public final static String INITIAL_HR_CREATION = "Upsampling Stage";
    public final static String SHARPNESS_MEASURE_STAGE = "Sharpness Measure";
    public final static String DENOISING_STAGE = "Denoising Stage";
    public final static String IMAGE_ALIGNMENT_STAGE = "Image Alignment";
    public final static String IMAGE_FUSION_STAGE = "Image Fusion";

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
        this.broadcastPipelineUpdate(imageName, PipelineManager.SHARPNESS_MEASURE_STAGE);
    }

    /*
     * Instructs the pipeline manager to request for a new image.
     */
    public void requestForNewImage() {
        NotificationCenter.getInstance().postNotification(Notifications.ON_PIPELINE_REQUEST_NEW_IMAGE);
    }

    private void initiateDenoising(String imageName) {
        Log.d(TAG, "Initiating denoising for "+imageName);

        broadcastPipelineUpdate(imageName, PipelineManager.DENOISING_STAGE);

        //TODO: temporary end of pipeline. Unqueue image name from processing queue
        //once finished, dequeue image name, then broadcast dequeue event
        String dequeueImageName = ProcessingQueue.getInstance().dequeueImageName();
        Parameters parameters = new Parameters();
        parameters.putExtra(PipelineManager.IMAGE_NAME_KEY, dequeueImageName);
        NotificationCenter.getInstance().postNotification(Notifications.ON_IMAGE_DEQUEUED, parameters);
    }


    @Override
    public void onWorkerCompleted(String workerName, ImageProperties properties) {
        if(workerName == SHARPNESS_MEASURE_WORKER) {
            //pass input image to denoising worker
            String imageName = properties.getStringExtra(SharpnessMeasureWorker.IMAGE_INPUT_NAME_KEY, null);
            boolean hasPassed = properties.getBooleanExtra(SharpnessMeasureWorker.HAS_PASSED_MEASURE_KEY, false);

            if(hasPassed) {
                Log.d(TAG, imageName + " has passed!");
            }
            else {
                Log.d(TAG, imageName + " did  not pass sharpness measure.");
            }

            this.initiateDenoising(imageName);
            this.requestForNewImage();
        }
    }

    public static void broadcastPipelineUpdate(String imageName, String pipelineStage) {
        Parameters parameters = new Parameters();
        parameters.putExtra(PipelineManager.IMAGE_NAME_KEY, imageName);
        parameters.putExtra(PipelineManager.PIPELINE_STAGE_KEY, pipelineStage);
        NotificationCenter.getInstance().postNotification(Notifications.ON_IMAGE_STAGE_UPDATED, parameters);
    }
}
