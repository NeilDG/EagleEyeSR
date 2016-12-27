package neildg.com.megatronsr.pipeline;

import android.util.Log;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.pipeline.workers.DenoisingWorker;
import neildg.com.megatronsr.pipeline.workers.ImageAlignmentWorker;
import neildg.com.megatronsr.pipeline.workers.SharpnessMeasureWorker;
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

    public final static String PROCESSING_QUEUE_STAGE = "In Queue";
    public final static String INITIAL_HR_CREATION = "Upsampling Stage";
    public final static String SHARPNESS_MEASURE_STAGE = "Sharpness Measure";
    public final static String DENOISING_STAGE = "Denoising Stage";
    public final static String IMAGE_ALIGNMENT_STAGE = "Image Alignment";
    public final static String IMAGE_FUSION_STAGE = "Image Fusion";
    public final static String STALLED_STAGE_PREFIX = "Stalled: ";

    private final static String REFERENCE_IMAGE_NAME = FilenameConstants.INPUT_PREFIX_STRING + 0;
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
    private SharpnessMeasureWorker sharpnessMeasureWorker = new SharpnessMeasureWorker(this);
    private ImageAlignmentWorker imageAlignmentWorker = new ImageAlignmentWorker(this);
    private DenoisingWorker denoisingWorker = new DenoisingWorker(this);

    private WorkerQueueTable  workerQueueTable = WorkerQueueTable.createInstance();

    public void startWorkers() {
        this.sharpnessMeasureWorker.start();
        this.denoisingWorker.start();
        this.imageAlignmentWorker.start();
    }

    /*
     * Adds an image entry at the start of the pipeline stage (sharpness measure)
     */
    public void addImageEntry(String imageName) {
        if(this.sharpnessMeasureWorker.isProcessing()) {
            return;
        }

        this.sharpnessMeasureWorker.getIngoingProperties().putExtra(SharpnessMeasureWorker.IMAGE_INPUT_NAME_KEY, imageName);
        Log.d(TAG, "Adding image entry  " +imageName);
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

    private void initiateDenoising() {
        if(this.denoisingWorker.isProcessing()) {
            return;
        }

        String imageName = this.workerQueueTable.dequeueImageFromWorker(DenoisingWorker.TAG);

        this.denoisingWorker.getIngoingProperties().putExtra(DenoisingWorker.IMAGE_INPUT_NAME_KEY, imageName);
        this.denoisingWorker.signal();
        broadcastPipelineUpdate(imageName, PipelineManager.DENOISING_STAGE);

    }

    private void performImageAlignment() {
        if(this.imageAlignmentWorker.isProcessing()) {
            return;
        }

        String compareImageName = this.workerQueueTable.dequeueImageFromWorker(ImageAlignmentWorker.TAG);
        this.imageAlignmentWorker.getIngoingProperties().putExtra(ImageAlignmentWorker.IMAGE_REFERENCE_NAME_KEY, REFERENCE_IMAGE_NAME);
        this.imageAlignmentWorker.getIngoingProperties().putExtra(ImageAlignmentWorker.IMAGE_COMPARE_NAME_KEY, compareImageName);

        this.imageAlignmentWorker.signal();
        this.broadcastPipelineUpdate(compareImageName, PipelineManager.IMAGE_ALIGNMENT_STAGE);
    }


    @Override
    public synchronized void onWorkerCompleted(String workerName, ImageProperties properties) {
        if(workerName == SharpnessMeasureWorker.TAG) {
            //pass input image to denoising worker
            String imageName = properties.getStringExtra(SharpnessMeasureWorker.IMAGE_INPUT_NAME_KEY, null);
            boolean hasPassed = properties.getBooleanExtra(SharpnessMeasureWorker.HAS_PASSED_MEASURE_KEY, false);

            if(hasPassed) {
                Log.d(TAG, imageName + " has passed!");
                this.workerQueueTable.enqueueImageToWorker(DenoisingWorker.TAG, imageName);
                broadcastPipelineUpdate(imageName, PipelineManager.STALLED_STAGE_PREFIX + DENOISING_STAGE);
                this.initiateDenoising();
            }
            else {
                Log.d(TAG, imageName + " did  not pass sharpness measure.");
            }

            this.requestForNewImage();
        }
        else if(workerName == DenoisingWorker.TAG) {
            //pass input to alignment worker
            String compareImageName = properties.getStringExtra(DenoisingWorker.IMAGE_OUTPUT_NAME_KEY, null);
            this.workerQueueTable.enqueueImageToWorker(ImageAlignmentWorker.TAG, compareImageName);
            broadcastPipelineUpdate(compareImageName, PipelineManager.STALLED_STAGE_PREFIX + IMAGE_ALIGNMENT_STAGE);
            this.performImageAlignment();
        }
        else if(workerName == ImageAlignmentWorker.TAG) {
            //TODO: temporary end of pipeline. Unqueue image name from processing queue
            //once finished, dequeue image name, then broadcast dequeue event
            String dequeueImageName = properties.getStringExtra(ImageAlignmentWorker.IMAGE_COMPARE_NAME_KEY, null);
            Parameters parameters = new Parameters();
            parameters.putExtra(PipelineManager.IMAGE_NAME_KEY, dequeueImageName);
            NotificationCenter.getInstance().postNotification(Notifications.ON_IMAGE_EXITED_PIPELINE, parameters);

            Log.d(TAG, workerName + " finished. Removing image " +dequeueImageName+ " from queue.");

            String outputImageName = properties.getStringExtra(ImageAlignmentWorker.IMAGE_OUTPUT_NAME_KEY, null);
            Log.d(TAG, workerName + " selected " +outputImageName+ " as best aligned.");

        }

        //everytime a worker is completed, check the worker queue table for pending tasks
        this.doPendingTasks();
    }

    private void doPendingTasks() {
        //denoising worker
        if(this.workerQueueTable.hasPendingTasksForWorker(DenoisingWorker.TAG)) {
            this.initiateDenoising();
        }
        //imagealignment worker
        if(this.workerQueueTable.hasPendingTasksForWorker(ImageAlignmentWorker.TAG)) {
           this.performImageAlignment();
        }
    }

    public static void broadcastPipelineUpdate(String imageName, String pipelineStage) {
        Parameters parameters = new Parameters();
        parameters.putExtra(PipelineManager.IMAGE_NAME_KEY, imageName);
        parameters.putExtra(PipelineManager.PIPELINE_STAGE_KEY, pipelineStage);
        NotificationCenter.getInstance().postNotification(Notifications.ON_IMAGE_STAGE_UPDATED, parameters);
    }
}
