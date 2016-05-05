package neildg.com.megatronsr.processing.single;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.w3c.dom.Attr;

import java.io.File;
import java.util.concurrent.Semaphore;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/4/2016.
 */
public class PatchExtractCommander implements IOperator {
    private final static String TAG = "PatchExtractCommander";

    public final static String PATCH_DIR = "pyr_";
    public final static String PATCH_PREFIX = "patch_";

    private int currentFlags = 0;
    private int requiredFlags = 0;

    private Semaphore semaphore;
    public PatchExtractCommander() {
        this.semaphore = new Semaphore(0);
    }

    @Override
    public void perform() {
        int pyramidDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);
        this.requiredFlags = pyramidDepth;

        for(int i = 0 ; i < pyramidDepth; i++) {
            String imagePath = FilenameConstants.PYRAMID_DIR + "/";
            String imageName = FilenameConstants.PYRAMID_IMAGE_PREFIX + i;
            PatchExtractor extractor = new PatchExtractor(imagePath, imageName, i, this);
            extractor.start();
        }

        ProgressDialogHandler.getInstance().showDialog("Extracting patches", "Extracting image patches from pyramid images.");
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ProgressDialogHandler.getInstance().hideDialog();
    }

    public synchronized void reportFinished() {
        this.currentFlags++;

        if(this.currentFlags == this.requiredFlags) {
            this.semaphore.release();
        }
    }

    public class PatchExtractor extends Thread {

        private int index;
        private Mat inputMat;
        private PatchExtractCommander commander;

        public PatchExtractor(String imagePath, String imageName, int index, PatchExtractCommander commander) {
            this.index = index;
            this.inputMat = ImageReader.getInstance().imReadOpenCV(imagePath + imageName, ImageFileAttribute.FileType.JPEG);
            this.commander = commander;
        }

        @Override
        public void run() {
            for(int col = 0; col < this.inputMat.cols(); col+=80) {
                for(int row = 0; row < this.inputMat.rows(); row+=80) {

                    Point point = new Point(col, row);
                    Mat patchMat = new Mat();
                    Imgproc.getRectSubPix(this.inputMat, new Size(80,80), point, patchMat);

                    ImageWriter.getInstance().saveMatrixToImage(patchMat, PATCH_DIR + this.index, PATCH_PREFIX+col+"_"+row, ImageFileAttribute.FileType.JPEG);
                }
            }

            this.commander.reportFinished();
        }
    }
}
