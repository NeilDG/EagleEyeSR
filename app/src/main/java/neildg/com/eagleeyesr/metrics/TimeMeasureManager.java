package neildg.com.eagleeyesr.metrics;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Debug class for measuring processing time
 * Created by NeilDG on 1/11/2017.
 */

public class TimeMeasureManager {

    public final static String MEASURE_SR_TIME = "MEASURE_SR_TIME";

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
}