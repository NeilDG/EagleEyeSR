package neildg.com.megatronsr.processing;

import neildg.com.megatronsr.constants.ParameterConstants;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * SRProcessor main entry point
 * Created by NeilDG on 3/5/2016.
 */
public class SRProcessor extends Thread {
    private static SRProcessor sharedInstance = null;
    public static SRProcessor getSharedInstance() {
        if(sharedInstance == null) {
            sharedInstance = new SRProcessor();
        }

        return sharedInstance;
    }

    private SRProcessor() {

    }

    @Override
    public void run() {
        ProgressDialogHandler.getInstance().showDialog("Downsampling images", "Downsampling images selected and saving them in file.");
        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConstants.SCALING_FACTOR);
        downsamplingOperator.perform();
        ProgressDialogHandler.getInstance().hideDialog();

        ProgressDialogHandler.getInstance().showDialog("Shift add fusion", "Performing shift add fusion.");
        ShiftAddFusionOperator fusionOperator = new ShiftAddFusionOperator();
        fusionOperator.perform();
        ProgressDialogHandler.getInstance().hideDialog();
    }

}