package neildg.com.eagleeyesr.threads;

import java.util.concurrent.Semaphore;

/**
 * A class for running a short process that requires a semaphore that adds a counter when this thread has finished
 * Created by NeilDG on 1/11/2017.
 */

public class FlaggingThread extends Thread {
    private final static String TAG = "FlaggingThread";

    protected Semaphore semaphore;

    public FlaggingThread(Semaphore semaphore) {
        this.semaphore = semaphore;
    }
}
