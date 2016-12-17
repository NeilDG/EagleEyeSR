package neildg.com.megatronsr.processing.multiple.alignment;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.video.Video;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.model.multiple.DisplacementValue;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ColorSpaceOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Creates zero-filled images by optical flow
 * Created by NeilDG on 5/25/2016.
 */
public class OpticalFlowZeroFillOperator implements IOperator {
    private final static String TAG = "OpticalFlowOperator";

    private Mat originMat;
    private Mat[] matSequences;
    private DisplacementValue[] displacementValues;

    private Semaphore signalFlag = new Semaphore(0);

    /*
     * Origin mat - Original upsampled origin mat for reference in optical flow
     * Mat sequences - List of succeeding LR frames for flow calculation.
     */
    public OpticalFlowZeroFillOperator(Mat originMat, Mat[] matSequences) {
       this.originMat = originMat;
       this.matSequences = matSequences;
        this.displacementValues = new DisplacementValue[this.matSequences.length];
    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showProcessDialog("Optical flow","Calculating optical flow of images");

        List<OpticalFlowWorker>  flowWorkerList = new LinkedList<>();
        for(int i = 0; i < this.matSequences.length; i++) {
            OpticalFlowWorker flowWorker = new OpticalFlowWorker(this.originMat, this.matSequences[i], i + 1, this.signalFlag);
            flowWorker.start();

            flowWorkerList.add(flowWorker);
        }

        try {
            this.signalFlag.acquire(this.matSequences.length);

            //zero filled displaced mat are stored in process image repo
            for(int i = 0; i < flowWorkerList.size(); i++) {
                //Mat mat = flowWorkerList.get(i).getZeroFilledMat();
                //ProcessedImageRepo.getSharedInstance().storeZeroFilledMat(mat);

                this.displacementValues[i] = flowWorkerList.get(i).getDisplacementValue();
            }

            flowWorkerList.clear();
            ProgressDialogHandler.getInstance().hideProcessDialog();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public DisplacementValue[] getDisplacementValues() {
        return this.displacementValues;
    }

    private class OpticalFlowWorker extends Thread {

        private Mat comparingMat;
        private Semaphore signalFlag;
        private int index;

        private Mat mat1; //grayscale of origin mat
        private Mat mat2;  //grayscale of comparing mat
        //private Mat zeroFilledMat;
        private DisplacementValue displacementValue;

        public OpticalFlowWorker(Mat originMat, Mat comparingMat, int index, Semaphore signalFlag) {
            this.comparingMat = comparingMat;
            this.index = index;
            this.signalFlag = signalFlag;

            this.mat1 = ColorSpaceOperator.rgbToGray(originMat);
            this.mat2 = ColorSpaceOperator.rgbToGray(comparingMat);
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
                    //Log.d(TAG, "flowMat col: " +col+ " row: " +row+ " channels: "+flowMat.channels()+ " value: " +flowMat.get(row,col)[0]+" "+flowMat.get(row,col)[1]);
                    Point pt = new Point(col + flowMat.get(row,col)[0], row + flowMat.get(row,col)[1]);
                    xPoints.put(row,col, pt.x);
                    yPoints.put(row,col, pt.y);
                }
            }
            //perform zero-filling based from motion translation
            int scaling = ParameterConfig.getScalingFactor();
            this.displacementValue = new DisplacementValue(xPoints, yPoints);
            //this.zeroFilledMat = ImageOperator.performZeroFill(this.comparingMat, scaling, xPoints, yPoints);
            //ImageWriter.getInstance().saveMatrixToImage(this.zeroFilledMat, "ZeroFill", "zero_fill_"+this.index, ImageFileAttribute.FileType.JPEG);

            //clear memory allocations
            this.mat1.release();
            this.mat2.release();

            this.signalFlag.release(); //release flag of sem
        }

        /*public Mat getZeroFilledMat() {
            return this.zeroFilledMat;
        }*/
        public DisplacementValue getDisplacementValue() {return this.displacementValue;}
    }
}
