package neildg.com.eagleeyesr.subroutine;

import android.util.Log;

/**
 * Behaves like Unity's coroutine. Spawns thread upon request
 * Created by NeilDG on 11/1/2016.
 */

public class CoroutineCreator {
    private final static String TAG = "CoroutineCreator";
    private final static int DEFAULT_MAX_THREADS = 5;

    private static CoroutineCreator sharedInstance = null;
    public static CoroutineCreator getInstance() {
        if(sharedInstance == null) {
            sharedInstance = new CoroutineCreator();
        }
        return sharedInstance;
    }

    private int activeThreads = 0;
    private int maxThreads = DEFAULT_MAX_THREADS;

    private CoroutineCreator() {

    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void startCoroutine(String name, ICoroutine routine, long delay) {

        if(this.activeThreads == DEFAULT_MAX_THREADS) {
            Log.e(TAG, "Cannot start new coroutine. Max limit reached!");
        }

        this.activeThreads++;
        RoutineThread routineThread = new RoutineThread(name, routine, delay);
        routineThread.start();
    }

    private void decreaseCounter() {
        if(this.activeThreads > 0) {
            this.activeThreads--;
        }
    }

    private class RoutineThread extends Thread {
        private ICoroutine routine;
        private long delay;

        public RoutineThread(String name, ICoroutine routine, long delayToStart) {
            this.setName(name);
            this.routine = routine;
            this.delay = delayToStart;
        }

        @Override
        public void run() {
            try {
                this.routine.onCoroutineStarted();

                Thread.sleep(this.delay);
                this.routine.perform();
                this.routine.onCoroutineEnded();
                CoroutineCreator.getInstance().decreaseCounter();
            }
            catch (InterruptedException e) {
                Log.d(TAG, "Thread "+this.getName()+ " was interrupted.");
            }
        }
    }
}
