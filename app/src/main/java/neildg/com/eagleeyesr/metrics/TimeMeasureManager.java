package neildg.com.eagleeyesr.metrics;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Debug class for measuring processing time
 * Created by NeilDG on 1/11/2017.
 */

public class TimeMeasureManager {

    public final static String MEASURE_SR_TIME = "MEASURE_SR_TIME";
    public final static String EDGE_DETECTION_TIME = "EDGE_DETECTION_TIME";
    public final static String IMAGE_SELECTION_TIME = "IMAGE_SELECTION_TIME";
    public final static String IMAGE_ALIGNMENT_TIME = "IMAGE_ALIGNMENT_TIME";
    public final static String ALIGNMENT_SELECTION_TIME = "ALIGNMENT_SELECTION_TIME";
    public final static String IMAGE_FUSION_TIME = "IMAGE_FUSION_TIME";

    //optional measures
    public final static String DENOISING_TIME = "DENOISING_TIME";
    public final static String SHARPENING_TIME = "SHARPENING_TIME";

    private static TimeMeasureManager sharedInstance = null;
    public static TimeMeasureManager getInstance() {
        if(sharedInstance == null) {
            sharedInstance = new TimeMeasureManager();
        }

        return sharedInstance;
    }

    private HashMap<String, TimeMeasure> timeMeasureTable = new HashMap<>();

    private TimeMeasureManager() {

    }

    /*
     * Returns a new time measure instantiated key and stores it in the table for later use.
     */
    public TimeMeasure newTimeMeasure(String key) {
        TimeMeasure timeMeasure = new TimeMeasure();
        this.timeMeasureTable.put(key, timeMeasure);

        return timeMeasure;
    }

    public TimeMeasure getTimeMeasure(String key) {
        return this.timeMeasureTable.get(key);
    }

    public static String convertDeltaToString(long deltaMillis) {
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(deltaMillis),
                TimeUnit.MILLISECONDS.toSeconds(deltaMillis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(deltaMillis))
        );
    }

    public static String convertDeltaToSeconds(long deltaMillis) {
        return String.format("%f sec", deltaMillis / 60.0f);
    }
}