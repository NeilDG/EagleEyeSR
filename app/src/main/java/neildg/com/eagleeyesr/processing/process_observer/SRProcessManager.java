package neildg.com.eagleeyesr.processing.process_observer;

import android.app.Activity;
import android.util.Log;

/** Class that holds a process listener that informs any activity of the current SR process
 * Created by NeilDG on 4/29/2017.
 */

public class SRProcessManager {
    private final static String TAG = "SRProcessManager";

    private static SRProcessManager sharedInstance = null;

    public static SRProcessManager getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new SRProcessManager();
        }

        return sharedInstance;
    }

    private IProcessListener currentProcessListener;
    private Activity activity;

    private SRProcessManager() {

    }

    public void setProcessListener(IProcessListener processListener, Activity activity) {
        this.currentProcessListener = processListener;
        this.activity = activity;
    }

    public void initialHRProduced() {
        if(this.currentProcessListener != null) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentProcessListener.onProducedInitialHR();
                }
            });

        }
        else {
            Log.e(TAG, "No process listener has been assigned!");
        }
    }

    public void srProcessCompleted() {
        if(this.currentProcessListener != null) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentProcessListener.onProcessCompleted();
                }
            });

        }
        else {
            Log.e(TAG, "No process listener has been assigned!");
        }
    }

}
