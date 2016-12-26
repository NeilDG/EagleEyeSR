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

    public void startWorkers() {
        this.sharpnessMeasureWorker.start();
        this.denoisingWorker.start();
        this.imageAlignmentWorker.start();
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

        this.denoisingWorker.getIngoingProperties().putExtra(DenoisingWorker.IMAGE_INPUT_NAME_KEY, imageName);
        this.denoisingWorker.signal();
        broadcastPipelineUpdate(imageName, PipelineManager.DENOISING_STAGE);

    }

    private void performImageAlignment(String referenceImageName, String compareImageName) {
        this.imageAlignmentWorker.getIngoingProperties().putExtra(ImageAlignmentWorker.IMAGE_REFERENCE_NAME_KEY, referenceImageName);
        this.imageAlignmentWorker.getIngoingProperties().putExtra(ImageAlignmentWorker.IMAGE_COMPARE_NAME_KEY, compareImageName);

        this.imageAlignmentWorker.signal();
        this.broadcastPipelineUpdate(compareImageName, PipelineManager.IMAGE_ALIGNMENT_STAGE);

        Log.d(TAG, "Initiating image alignment for "+compareImageName+ " against " +referenceImageName);
    }


    @Override
    public synchronized void onWorkerCompleted(String workerName, ImageProperties properties) {
        if(workerName == SharpnessMeasureWorker.TAG) {
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
        else if(workerName == DenoisingWorker.TAG) {
            //pass input to alignment worker
            String referenceImageName = FilenameConstants.INPUT_PREFIX_STRING + 0;
            String compareImageName = properties.getStringExtra(DenoisingWorker.IMAGE_OUTPUT_NAME_KEY, null);
            this.performImageAlignment(referenceImageName, compareImageName);
        }
        else if(workerName == ImageAlignmentWorker.TAG) {
            //TODO: temporary end of pipeline. Unqueue image name from processing queue
            //once finished, dequeue image name, then broadcast dequeue event
            String dequeueImageName = properties.getStringExtra(ImageAlignmentWorker.IMAGE_COMPARE_NAME_KEY, null);
            Parameters parameters = new Parameters();
            parameters.putExtra(PipelineManager.IMAGE_NAME_KEY, dequeueImageName);
            NotificationCenter.getInstance().postNotification(Notifications.ON_IMAGE_EXITED_PIPELINE, parameters);

            Log.d(TAG, workerName + " finished. Removing image " +dequeueImageName+ " from queue.");

        }
    }

    public static void broadcastPipelineUpdate(String imageName, String pipelineStage) {
        Parameters parameters = new Parameters();
        parameters.putExtra(PipelineManager.IMAGE_NAME_KEY, imageName);
        parameters.putExtra(PipelineManager.PIPELINE_STAGE_KEY, pipelineStage);
        NotificationCenter.getInstance().postNotification(Notifications.ON_IMAGE_STAGE_UPDATED, parameters);
    }
}
