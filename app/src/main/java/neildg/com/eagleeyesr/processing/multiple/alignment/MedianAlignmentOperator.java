package neildg.com.eagleeyesr.processing.multiple.alignment;

import org.opencv.core.Mat;
import org.opencv.photo.AlignMTB;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.model.AttributeHolder;
import neildg.com.eagleeyesr.model.AttributeNames;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.threads.FlaggingThread;

/**
 * Performs MTB median alignment. Converts the images into median threshold bitmaps (1 for above median luminance threshold, 0 otherwise). It is aligned by BIT operations.
 * Created by NeilDG on 12/17/2016.
 */

public class MedianAlignmentOperator implements IOperator {
    private final static String TAG = "ExposureAlignmentOperator";

    private Mat[] imageSequenceList;
    private String[] resultNames;
    public MedianAlignmentOperator(Mat[] imageSequenceList, String[] resultNames) {
        this.imageSequenceList = imageSequenceList;
        this.resultNames = resultNames;
    }

    @Override
    public void perform() {
        AlignMTB mtbAligner = Photo.createAlignMTB();

        List<Mat> processMatList = Arrays.asList(this.imageSequenceList);
        mtbAligner.process(processMatList, processMatList);

        for(int i = 1; i < processMatList.size(); i++) {
            FileImageWriter.getInstance().saveMatrixToImage(processMatList.get(i), resultNames[i - 1], ImageFileAttribute.FileType.JPEG);
        }

        AttributeHolder.getSharedInstance().putValue(AttributeNames.WARPED_IMAGES_LENGTH_KEY, processMatList.size() - 1);

        /*int length = this.imageSequenceList.length - 1;
        MedianAlignWorker[] mtbWorkers = new MedianAlignWorker[length];
        Semaphore mtbSem = new Semaphore(length);

        for(int i = 0; i < mtbWorkers.length; i++) {
            mtbWorkers[i] = new MedianAlignWorker(mtbSem, this.imageSequenceList[0], this.imageSequenceList[i + 1]);
            mtbWorkers[i].startWork();
        }

        try {
            mtbSem.acquire(length);

            for(int i = 0; i < mtbWorkers.length; i++) {
                FileImageWriter.getInstance().saveMatrixToImage(mtbWorkers[i].getAlignedMat(), resultNames[i], ImageFileAttribute.FileType.JPEG);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AttributeHolder.getSharedInstance().putValue(AttributeNames.WARPED_IMAGES_LENGTH_KEY, length);*/
    }

    private class MedianAlignWorker extends FlaggingThread {

        private Mat referenceMat;
        private Mat comparingMat;

        private Mat resultMat;

        public MedianAlignWorker(Semaphore semaphore, Mat referenceMat, Mat comparingMat) {
            super(semaphore);

            this.referenceMat = referenceMat;
            this.comparingMat = comparingMat;
        }

        @Override
        public void run() {
            AlignMTB mtbAligner = Photo.createAlignMTB();

            List<Mat> processMatList = new ArrayList<>();
            processMatList.add(this.referenceMat);
            processMatList.add(this.comparingMat);

            mtbAligner.process(processMatList, processMatList);

            this.resultMat = processMatList.get(1);

            this.finishWork();
        }

        public Mat getAlignedMat() {
            return this.resultMat;
        }
    }
}
