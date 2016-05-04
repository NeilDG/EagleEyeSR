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

        String imageName = FilenameConstants.PYRAMID_DIR + "/" + FilenameConstants.PYRAMID_IMAGE_PREFIX + 0;
        PatchExtractor extractor = new PatchExtractor(imageName, this);
        extractor.start();

        /*for(int i = 0 ; i < pyramidDepth; i++) {
            String imageName = FilenameConstants.PYRAMID_DIR + "/" + FilenameConstants.PYRAMID_IMAGE_PREFIX + i;
            PatchExtractor extractor = new PatchExtractor(imageName, this);
            extractor.start();
        }

        ProgressDialogHandler.getInstance().showDialog("Extracting patches", "Extracting image patches from pyramid images.");
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ProgressDialogHandler.getInstance().hideDialog();*/
    }

    public synchronized void reportFinished() {
        this.currentFlags++;

        if(this.currentFlags == this.requiredFlags) {
            this.semaphore.release();
        }
    }

    public class PatchExtractor extends Thread {

        private String imageName;
        private Mat inputMat;
        private PatchExtractCommander commander;

        public PatchExtractor(String imageName, PatchExtractCommander commander) {
            this.imageName = imageName;
            this.inputMat = ImageReader.getInstance().imReadOpenCV(imageName, ImageFileAttribute.FileType.JPEG);
            this.commander = commander;
        }

        @Override
        public void run() {
            ProgressDialogHandler.getInstance().showDialog("Extracting patches", "Extracting image patches from pyramid images.");
            for(int col = 0; col < this.inputMat.cols(); col+=80) {
                for(int row = 0; row < this.inputMat.rows(); row+=80) {
                    Point point = new Point(col, row);
                    Mat patchMat = new Mat();
                    Imgproc.getRectSubPix(this.inputMat, new Size(80,80), point, patchMat);

                    ImageWriter.getInstance().saveMatrixToImage(patchMat, this.imageName +"_"+FilenameConstants.PATCH_PREFIX+"_"+col+"_"+row, ImageFileAttribute.FileType.JPEG);
                }
            }

            ProgressDialogHandler.getInstance().hideDialog();
            this.commander.reportFinished();
        }
    }
}
