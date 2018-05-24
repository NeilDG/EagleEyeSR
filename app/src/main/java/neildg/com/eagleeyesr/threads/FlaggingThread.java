package neildg.com.eagleeyesr.threads;

import java.util.concurrent.Semaphore;

/**
 * A class for running a short process that requires a semaphore that adds a counter when this thread has finished
 * Created by NeilDG on 1/11/2017.
 */

public abstract class FlaggingThread extends Thread {
    private final static String TAG = "FlaggingThread";

    protected Semaphore semaphore;

    public FlaggingThread(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    /*
     * Starts the work of the thread while acquiring the semaphore permit
     */
    public void startWork() {
        try {
            this.semaphore.acquire();
            this.start();
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * Stops the work of the thread. IMPORTANT. This must be called at the end of the overrided run() function to ensure that the semaphore permit is released.
     */
    public void finishWork() {
        this.semaphore.release();
    }
}
