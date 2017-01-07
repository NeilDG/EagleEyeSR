package neildg.com.eagleeyesr.pipeline;

/**
 * Created by NeilDG on 12/10/2016.
 */

public interface WorkerListener {
    void onWorkerCompleted(String workerName, ImageProperties properties);
}
