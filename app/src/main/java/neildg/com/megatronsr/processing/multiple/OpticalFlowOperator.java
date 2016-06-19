package neildg.com.megatronsr.processing.multiple;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.video.Video;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.model.multiple.ProcessedImageRepo;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Creates zero-filled images by optical flow
 * Created by NeilDG on 5/25/2016.
 */
public class OpticalFlowOperator implements IOperator {
    private final static String TAG = "OpticalFlowOperator";

    private Mat originMat;
    private List<Mat> matSequences = new LinkedList<>();

    private Semaphore signalFlag = new Semaphore(0);

    public OpticalFlowOperator() {
       int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();

        this.originMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);

        for(int i = 1; i < numImages; i++) {
            Mat mat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            this.matSequences.add(mat);
        }
    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Optical flow","Calculating optical flow of images");

        List<OpticalFlowWorker>  flowWorkerList = new LinkedList<>();
        for(int i = 0; i < this.matSequences.size(); i++) {
            OpticalFlowWorker flowWorker = new OpticalFlowWorker(this.originMat, this.matSequences.get(i), i + 1, this.signalFlag);
            flowWorker.start();

            flowWorkerList.add(flowWorker);
        }

        try {
            this.signalFlag.acquire(this.matSequences.size());

            this.originMat = ImageOperator.performZeroFill(this.originMat, ParameterConfig.getScalingFactor(), 0, 0);
            ProcessedImageRepo.getSharedInstance().storeZeroFilledMat(this.originMat);

            for(int i = 0; i < flowWorkerList.size(); i++) {
                Mat mat = flowWorkerList.get(i).getZeroFilledMat();
                ProcessedImageRepo.getSharedInstance().storeZeroFilledMat(mat);
            }

            flowWorkerList.clear();
            ProgressDialogHandler.getInstance().hideDialog();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private class OpticalFlowWorker extends Thread {

        private Mat comparingMat;
        private Semaphore signalFlag;
        private int index;

        private Mat mat1; //grayscale of origin mat
        private Mat mat2;  //grayscale of comparing mat
        private Mat zeroFilledMat;

        public OpticalFlowWorker(Mat originMat, Mat comparingMat, int index, Semaphore signalFlag) {
            this.comparingMat = comparingMat;
            this.index = index;
            this.signalFlag = signalFlag;

            this.mat1 = ImageOperator.rgbToGray(originMat);
            this.mat2 = ImageOperator.rgbToGray(comparingMat);
        }

        @Override
        public void run() {
            Mat xPoints = new Mat(this.comparingMat.size(), CvType.CV_32FC1);
            Mat yPoints = new Mat(this.comparingMat.size(), CvType.CV_32FC1);

            //compute motion translation by optical flow
            Mat flowMat = new Mat();
            Video.calcOpticalFlowFarneback(this.mat1, this.mat2, flowMat, 0.25, 5, 15, 3, 5, 1.5, Video.MOTION_TRANSLATION);
            //MatWriter.writeMat(flowMat, "flow_"+this.index);

            for(int row = 0; row < this.comparingMat.rows(); row++) {
                for(int col = 0; col < this.comparingMat.cols(); col++) {
                    //Log.d(TAG, "flowMat x: " +x+ " row: " +row+ " channels: "+flowMat.channels()+ " value: " +flowMat.get(row,x)[0]+" "+flowMat.get(row,x)[1]);
                    Point pt = new Point(col + flowMat.get(row,col)[0], row + flowMat.get(row,col)[1]);
                    xPoints.put(row,col, pt.x);
                    yPoints.put(row,col, pt.y);
                }
            }

            //perform zero-filling based from motion translation
            int scaling = ParameterConfig.getScalingFactor();
            this.zeroFilledMat = ImageOperator.performZeroFill(this.comparingMat, scaling, xPoints, yPoints);
            //ImageWriter.getInstance().saveMatrixToImage(this.zeroFilledMat, "ZeroFill", "zero_fill_"+this.index, ImageFileAttribute.FileType.JPEG);

            //clear memory allocations
            this.comparingMat.release();
            this.mat1.release();
            this.mat2.release();

            this.signalFlag.release(); //release flag of sem
        }

        public Mat getZeroFilledMat() {
            return this.zeroFilledMat;
        }
    }


}
