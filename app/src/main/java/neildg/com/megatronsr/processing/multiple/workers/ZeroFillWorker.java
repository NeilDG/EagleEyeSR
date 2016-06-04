package neildg.com.megatronsr.processing.multiple.workers;

import org.opencv.core.Mat;

import java.util.concurrent.Semaphore;

import neildg.com.megatronsr.processing.imagetools.ImageOperator;

public class ZeroFillWorker extends Thread {

    private Mat inputMat;
    private Mat hrMat;

    private int scaling = 1;
    private int offsetX = 0;
    private int offsetY = 0;

    private Semaphore semaphore;

    public ZeroFillWorker(Mat inputMat, int scaling, int offsetX, int offsetY, Semaphore semaphore) {
        this.inputMat = inputMat;
        this.scaling = scaling;
        this.offsetX = offsetX;
        this.offsetY = offsetY;

        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        //this.hrMat = ImageOperator.performInterpolation(this.inputMat, this.scaling, Imgproc.INTER_CUBIC);
        this.hrMat = ImageOperator.performZeroFill(this.inputMat, this.scaling, 0, 0);
        this.semaphore.release();
    }

    public Mat getHrMat() {
        return this.hrMat;
    }
}