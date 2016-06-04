package neildg.com.megatronsr.threads;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.MatWriter;
import neildg.com.megatronsr.processing.ITest;
import neildg.com.megatronsr.processing.multiple.DownsamplingOperator;
import neildg.com.megatronsr.processing.multiple.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.LRWarpingOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

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
