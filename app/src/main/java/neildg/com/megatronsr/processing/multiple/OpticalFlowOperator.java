package neildg.com.megatronsr.processing.multiple;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.multiple.fusion.ZeroFillFusionOperator;
import neildg.com.megatronsr.processing.multiple.workers.ZeroFillWorker;
import neildg.com.megatronsr.processing.operators.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Upsamples the warped images and then computes pixel displacements by optical flow
 * Created by NeilDG on 5/25/2016.
 */
public class OpticalFlowOperator implements IOperator {
    private final static String TAG = "OpticalFlowOperator";

    private List<MatOfDMatch> goodMatchList;
    private List<MatOfKeyPoint> keyPointList;

    private List<Mat> warpedMatrixList;
    private MatOfKeyPoint refKeyPoint;

    private Semaphore zeroFillSem;

    public OpticalFlowOperator(List<Mat> warpedMatrixList, MatOfKeyPoint refKeypoint) {
        this.warpedMatrixList = warpedMatrixList;
        this.refKeyPoint = refKeypoint;
    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Optical flow","Calculating optical flow of images");
        List<ZeroFillWorker> workerList = new LinkedList<>();
        this.zeroFillSem = new Semaphore(0);
        this.zeroFillWarps(workerList);

        try {
            this.zeroFillSem.acquire(this.warpedMatrixList.size());

            /*this.storeZeroFilledMatrices(workerList);
            Mat referenceMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);
            referenceMat = ImageOperator.performZeroFill(referenceMat, ParameterConfig.getScalingFactor(), 0, 0);
            MatOfKeyPoint refKeyPoint = this.redetectKeypoints(referenceMat);*/

            Mat referenceMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);
            MatOfKeyPoint refKeyPoint = this.redetectKeypoints(referenceMat);
            this.performOpticalFlow(referenceMat, refKeyPoint);

            ProgressDialogHandler.getInstance().hideDialog();
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void zeroFillWarps(List<ZeroFillWorker> workerList) {
        int scalingFactor = ParameterConfig.getScalingFactor();

        for(int i = 0; i < this.warpedMatrixList.size(); i++) {
            ZeroFillWorker zeroFillWorker = new ZeroFillWorker(this.warpedMatrixList.get(i), scalingFactor, 0, 0, this.zeroFillSem);
            zeroFillWorker.start();
            workerList.add(zeroFillWorker);
        }
    }

    private MatOfKeyPoint redetectKeypoints(Mat referenceMat) {
        ProgressDialogHandler.getInstance().showDialog("Keypoint detection","Identifying keypoints in reference image.");

        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        Mat referenceDescriptor = new Mat();
        MatOfKeyPoint refKeypoint = new MatOfKeyPoint();

        featureDetector.detect(referenceMat, refKeypoint);
        descriptorExtractor.compute(referenceMat, refKeypoint, referenceDescriptor);

        Mat debugMat = new Mat();
        Features2d.drawKeypoints(referenceMat, refKeypoint, debugMat);
        ImageWriter.getInstance().saveMatrixToImage(debugMat, FilenameConstants.OPTICAL_FLOW_DIR, "keypoints", ImageFileAttribute.FileType.JPEG);
        return refKeypoint;
    }

    private void storeZeroFilledMatrices(List<ZeroFillWorker> workerList) {

        ProgressDialogHandler.getInstance().showDialog("Optical flow", "Saving zero-filled HR warped images");
            for (int i = 0; i < this.warpedMatrixList.size(); i++) {
                Mat hrMat = workerList.get(i).getHrMat();
                hrMat.copyTo(this.warpedMatrixList.get(i));
                hrMat.release();

                ImageWriter.getInstance().saveMatrixToImage(this.warpedMatrixList.get(i), FilenameConstants.OPTICAL_FLOW_DIR,
                        FilenameConstants.OPTICAL_FLOW_IMAGE_ORIG+i, ImageFileAttribute.FileType.JPEG);
            }

            workerList.clear();

    }

    private void performOpticalFlow(Mat referenceMat, MatOfKeyPoint refKeypoint) {
        ProgressDialogHandler.getInstance().showDialog("Optical flow", "Readjusting warped images");

        MatOfByte status = new MatOfByte(); MatOfFloat error = new MatOfFloat();

        /*MatOfPoint2f matOfPoint1 = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2 = new MatOfPoint2f();

        KeyPoint[] keyPoints1 = refKeypoint.toArray();

        List<Point> pointList1 = new LinkedList<>();
        for(int i = 0; i < keyPoints1.length; i++) {
            pointList1.add(keyPoints1[i].pt);
        }
        matOfPoint1.fromList(pointList1);

        for(int i = 0; i < this.warpedMatrixList.size(); i++) {
            Mat hrMat = this.warpedMatrixList.get(i);
            Video.calcOpticalFlowPyrLK(referenceMat, hrMat, matOfPoint1, matOfPoint2, status, error);

            Mat newPoints = new Mat(hrMat.size(), CvType.CV_16SC2);
            Mat emptyMat = new Mat();
            //Mat yPoints = new Mat(matOfPoint2.size(), CvType.CV_32FC1);
            for(int row = 0; row <  matOfPoint2.rows(); row++) {
                newPoints.put(row, 0, matOfPoint2.get(row, 0));
                //yPoints.put(row, 0, matOfPoint2.get(row, 0)[1]);
                //Log.d(TAG, "xPoint: " +xPoints.get(row,0)[0] + " yPoint: " +xPoints.get(row,0)[1]+ " Length:" + matOfPoint2.rows());
            }

            Imgproc.remap(hrMat, hrMat, newPoints, emptyMat, Imgproc.INTER_CUBIC);

            ImageWriter.getInstance().saveMatrixToImage(this.warpedMatrixList.get(i), FilenameConstants.OPTICAL_FLOW_DIR,
                    FilenameConstants.OPTICAL_FLOW_IMAGE_PREFIX + i, ImageFileAttribute.FileType.JPEG);
        }*/

        /*MatOfPoint2f matOfPoint1 = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2 = new MatOfPoint2f();

        KeyPoint[] keyPoints1 = refKeypoint.toArray();

        List<Point> pointList1 = new LinkedList<>();
        for(int i = 0; i < keyPoints1.length; i++) {
            pointList1.add(keyPoints1[i].pt);
        }
        matOfPoint1.fromList(pointList1);*/

        Imgproc.cvtColor(referenceMat, referenceMat, Imgproc.COLOR_RGB2GRAY);
        for(int i = 0; i < this.warpedMatrixList.size(); i++) {
            Mat hrMat = this.warpedMatrixList.get(i);
            Mat flowMat = new Mat();
            ImageWriter.getInstance().saveMatrixToImage(hrMat, FilenameConstants.OPTICAL_FLOW_DIR,
                    FilenameConstants.OPTICAL_FLOW_IMAGE_ORIG+i, ImageFileAttribute.FileType.JPEG);

            Imgproc.cvtColor(hrMat, hrMat, Imgproc.COLOR_RGB2GRAY);
            Log.d(TAG, "Checking channels " +referenceMat.channels()+ " Next mat: " +hrMat.channels() + " Size: " +referenceMat.size().toString()+ " Next mat size: " +hrMat.size().toString());

            Video.calcOpticalFlowFarneback(referenceMat, hrMat, flowMat, 0.5, 3, 15, 3, 5, 1.2, Video.MOTION_HOMOGRAPHY);

            //test remap
            Mat xPoints = new Mat(hrMat.size(), CvType.CV_32FC1);
            Mat yPoints = new Mat(hrMat.size(), CvType.CV_32FC1);
            for(int y = 0; y < hrMat.rows(); y++) {
                for(int x = 0; x < hrMat.cols(); x++) {
                    //Log.d(TAG, "flowMat x: " +x+ " y: " +y+ " channels: "+flowMat.channels()+ " value: " +flowMat.get(y,x)[0]+" "+flowMat.get(y,x)[1]);
                    Point pt = new Point(x + flowMat.get(y,x)[0], y + flowMat.get(y,x)[1]);
                    xPoints.put(y,x, pt.x);
                    yPoints.put(y,x, pt.y);
                }
            }

            ProgressDialogHandler.getInstance().showDialog("Optical flow", "Remapping warped image " +i);
            Imgproc.remap(hrMat, hrMat, xPoints, yPoints, Imgproc.INTER_CUBIC);
            ImageWriter.getInstance().saveMatrixToImage(this.warpedMatrixList.get(i), FilenameConstants.OPTICAL_FLOW_DIR,
                    FilenameConstants.OPTICAL_FLOW_IMAGE_PREFIX + i, ImageFileAttribute.FileType.JPEG);
        }
    }

    private MatOfPoint2f obtainPixelPoints(Mat referenceMat, int scaling) {

        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        int space = scaling - 1;
        for(int row = 0; row < referenceMat.rows(); row += space) {
            for(int col = 0; col < referenceMat.cols(); col += space) {
                Point pt = new Point(row, col);
                matOfPoint2f.put(row, 0, pt.x, pt.y);
            }
        }

        return matOfPoint2f;
    }

    private void debugPrint(MatOfPoint2f point2f) {
        for (int y = 0; y < point2f.rows(); y++) {
            for (int x = 0; x < point2f.cols(); x++) {
                Log.d(TAG, "MatofPoint2 values: " + point2f.get(y, x)[0] + "   "+point2f.get(y,x)[1]);
                // pointMap.put(y,x, matOfPoint2.get(x,y));
            }
        }
    }
}
