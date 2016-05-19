package neildg.com.megatronsr.processing.single;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Semaphore;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.model.single.PatchAttributeTable;
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
        PatchAttributeTable.initialize();
    }

    @Override
    public void perform() {
        int pyramidDepth = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);
        this.requiredFlags = pyramidDepth;

        for(int i = 0 ; i < pyramidDepth; i++) {
            String imageDir = FilenameConstants.PYRAMID_DIR + "/";
            String imagePrefix = FilenameConstants.PYRAMID_IMAGE_PREFIX + i;
            PatchExtractor extractor = new PatchExtractor(imageDir, imagePrefix, i, this);
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

        private String imagePrefix;
        private String fullImagePath;

        public PatchExtractor(String imageDir, String imagePrefix, int index, PatchExtractCommander commander) {
            this.index = index;
            this.imagePrefix = imagePrefix;
            this.fullImagePath = imageDir + this.imagePrefix;
            this.inputMat = ImageReader.getInstance().imReadOpenCV(this.fullImagePath, ImageFileAttribute.FileType.JPEG);
            this.commander = commander;

        }

        @Override
        public void run() {
            int patchSize = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.PATCH_SIZE_KEY, 0);
            for(int col = 0; col < this.inputMat.cols(); col+=patchSize) {
                for(int row = 0; row < this.inputMat.rows(); row+=patchSize) {

                    Point point = new Point(col, row);
                    Mat patchMat = new Mat();
                    Imgproc.getRectSubPix(this.inputMat, new Size(patchSize,patchSize), point, patchMat);

                    String patchDir = PATCH_DIR + this.index;
                    String patchImageName = PATCH_PREFIX +col+"_"+row;
                    String patchImagePath =  patchDir + "/" +patchImageName;
                    ImageWriter.getInstance().saveMatrixToImage(patchMat, patchDir,patchImageName, ImageFileAttribute.FileType.JPEG);
                    PatchAttributeTable.getInstance().addPatchAttribute(this.index, col, row, col + patchSize, row + patchSize, patchImageName, patchImagePath);

                    patchMat.release();
                }
            }

            this.commander.reportFinished();
        }
    }
}
