package neildg.com.megatronsr.processing;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.preprocessing.BitmapURIRepository;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by neil.dg on 3/10/16.
 */
public class LRWarpingOperator implements IOperator {
    private final static String TAG = "WarpingOperator";

    private MatOfKeyPoint refKeypoint;
    //private List<MatOfDMatch> goodMatchList;
    //private List<MatOfKeyPoint> keyPointList;

    private Mat referenceMat;

    private Mat warpedMat = new Mat();
    private Mat outputMat = new Mat();

    private List<Mat> warpedMatrixList = new ArrayList<Mat>();

    public LRWarpingOperator(MatOfKeyPoint refKeypoint) {
        this.refKeypoint = refKeypoint;

        this.referenceMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);
        this.outputMat = new Mat(this.referenceMat.size(), this.referenceMat.type());
        this.referenceMat.copyTo(this.outputMat);

        ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "holderimage", ImageFileAttribute.FileType.JPEG);
    }

    public void perform() {
        ProgressDialogHandler.getInstance().hideDialog();

        Log.d(TAG, "LR Homography warping");

        int numImages = BitmapURIRepository.getInstance().getNumImages();
        for (int i = 1; i < numImages; i++) {
            Mat comparingMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);

            this.warpedMat = new Mat();
            this.warpImage(comparingMat);

            ProgressDialogHandler.getInstance().showDialog("Image warping", "Warping image " + i + " to reference image.");

            this.warpedMatrixList.add(this.warpedMat);
            ImageWriter.getInstance().saveMatrixToImage(this.warpedMat, "warp_"+i, ImageFileAttribute.FileType.JPEG);

            ProgressDialogHandler.getInstance().hideDialog();
        }

        this.finalizeResult();
        //ImageWriter.getInstance().saveMatrixToImage(this.outputMat, FilenameConstants.HR_PROCESSED_STRING);
    }

    private void performTVDenoising(Mat inputMat) {
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGR2YUV);

        List<Mat> splittedYUVMat = new ArrayList<Mat>();
        Core.split(inputMat, splittedYUVMat);

        Mat denoisedMat = new Mat(inputMat.size(), CvType.CV_32FC1);

        List<Mat> lrObservations = new ArrayList<>();
        lrObservations.add(splittedYUVMat.get(0)); //only apply denoising to Y

        Photo.denoise_TVL1(lrObservations, denoisedMat, 60.0f, 30);

        Core.merge(splittedYUVMat, inputMat);
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_YUV2BGR);
    }

    public List<Mat> getWarpedMatrixList() {
        return this.warpedMatrixList;
    }

    private void finalizeResult() {
        this.refKeypoint.release(); this.refKeypoint = null;
        this.referenceMat.release(); this.referenceMat = null;
    }

    private void warpImage(Mat candidateMat) {
        MatOfPoint2f matOfPoint1 = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2 = new MatOfPoint2f();

        KeyPoint[] keyPoints1 = this.refKeypoint.toArray();
        List<Point> pointList1 = new ArrayList<Point>();

       for(int i = 0; i < keyPoints1.length; i++) {
            pointList1.add(keyPoints1[i].pt);
       }

        matOfPoint1.fromList(pointList1);

        MatOfByte status = new MatOfByte(); MatOfFloat error = new MatOfFloat();
        Video.calcOpticalFlowPyrLK(this.referenceMat,candidateMat,matOfPoint1, matOfPoint2,status,error);

        //((M0.type() == CV_32F || M0.type() == CV_64F) && M0.rows == 3 && M0.cols == 3)

        Log.d(TAG, "Homography pre info: matOfPoint1 ROWS: " + matOfPoint1.rows() + " matOfPoint1 COLS: " + matOfPoint1.cols());
        Log.d(TAG, "Homography pre info: matOfPoint2 ROWS: " + matOfPoint2.rows() + " matOfPoint2 COLS: " + matOfPoint2.cols());

        Mat homography = Calib3d.findHomography(matOfPoint1, matOfPoint2, Calib3d.RANSAC, 1);
        Log.d(TAG, "Homography info: ROWS: " + homography.rows() + " COLS: " + homography.cols());

        Imgproc.warpPerspective(candidateMat, this.warpedMat, homography, this.warpedMat.size(), Imgproc.INTER_CUBIC, Core.BORDER_TRANSPARENT, Scalar.all(0));
    }
}
