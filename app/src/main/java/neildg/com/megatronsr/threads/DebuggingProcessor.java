package neildg.com.megatronsr.threads;

import neildg.com.megatronsr.processing.ITest;

/**
 * Created by NeilDG on 6/2/2016.
 */
public class DebuggingProcessor extends Thread {
    private final static String TAG = "";

    private ITest test;
    public DebuggingProcessor(ITest test) {
        this.test = test;
    }

    @Override
    public void run() {
        this.test.performTest();
    }

}
