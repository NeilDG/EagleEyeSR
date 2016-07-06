package neildg.com.megatronsr.processing.multiple;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
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
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.multiple.ProcessedImageRepo;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by neil.dg on 3/10/16.
 */
public class LRWarpingOperator {
    private final static String TAG = "WarpingOperator";

    private MatOfKeyPoint refKeypoint;
    private MatOfDMatch[] goodMatchList;
    private MatOfKeyPoint[] keyPointList;
    private Mat[] imagesToWarpList;

    public LRWarpingOperator(MatOfKeyPoint refKeypoint, Mat[] imagesToWarpList, MatOfDMatch[] goodMatchList, MatOfKeyPoint[] keyPointList) {
        this.goodMatchList = goodMatchList;
        this.keyPointList = keyPointList;
        this.refKeypoint = refKeypoint;
        this.imagesToWarpList = imagesToWarpList;
    }

    public void perform() {
        ProgressDialogHandler.getInstance().hideDialog();

        /*int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();
        for (int i = 1; i < numImages; i++) {
            Mat comparingMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);

            ProgressDialogHandler.getInstance().showDialog("Image warping", "Warping image " + i + " to reference image.");

            Mat warpedMat = this.warpImage(this.goodMatchList.get(i - 1), this.keyPointList.get(i - 1), comparingMat);
            ProcessedImageRepo.getSharedInstance().storeWarpedMat(warpedMat);

            ImageWriter.getInstance().saveMatrixToImage(warpedMat, "warp_" +i, ImageFileAttribute.FileType.JPEG);

            ProgressDialogHandler.getInstance().hideDialog();
        }*/

        for(int i = 0; i < this.imagesToWarpList.length; i++) {
            ProgressDialogHandler.getInstance().showDialog("Image warping", "Warping image " + (i+1) + " to reference image.");

            Mat warpedMat = this.warpImage(this.goodMatchList[i], this.keyPointList[i], this.imagesToWarpList[i]);
            ProcessedImageRepo.getSharedInstance().storeWarpedMat(warpedMat);

            ImageWriter.getInstance().saveMatrixToImage(warpedMat, "warp_" +i, ImageFileAttribute.FileType.JPEG);

            this.imagesToWarpList[i].release();
            ProgressDialogHandler.getInstance().hideDialog();
        }

        this.imagesToWarpList = null;

        this.finalizeResult();
    }

    private void finalizeResult() {
        this.refKeypoint.release(); this.refKeypoint = null;
        for(MatOfDMatch dMatch: this.goodMatchList) {
            dMatch.release();
        }
        this.goodMatchList = null;

        for(MatOfKeyPoint keyPoint: this.keyPointList) {
            keyPoint.release();
        }
        this.keyPointList = null;
    }

    private Mat warpImage(MatOfDMatch goodMatch, MatOfKeyPoint candidateKeypoint, Mat candidateMat) {
        MatOfPoint2f matOfPoint1 = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2 = new MatOfPoint2f();

        KeyPoint[] keyPoints1 = this.refKeypoint.toArray();
        KeyPoint[] keyPoints2 = candidateKeypoint.toArray();

        List<Point> pointList1 = new ArrayList<>();
        List<Point> pointList2 = new ArrayList<>();

        DMatch[] dMatchArray = goodMatch.toArray();

        for(int i = 0; i < dMatchArray.length; i++) {
            Log.d(TAG, "DMATCHES" + dMatchArray[i].toString());

            pointList1.add(keyPoints1[dMatchArray[i].queryIdx].pt);
            pointList2.add(keyPoints2[dMatchArray[i].trainIdx].pt);
        }

        matOfPoint1.fromList(pointList1); matOfPoint2.fromList(pointList2);

        //MatOfByte status = new MatOfByte(); MatOfFloat error = new MatOfFloat();
        //Video.calcOpticalFlowPyrLK(this.referenceMat, candidateMat, matOfPoint1, matOfPoint2, status, error);

        //((M0.type() == CV_32F || M0.type() == CV_64F) && M0.rows == 3 && M0.cols == 3)

        Log.d(TAG, "Homography pre info: matOfPoint1 ROWS: " + matOfPoint1.rows() + " matOfPoint1 COLS: " + matOfPoint1.cols());
        Log.d(TAG, "Homography pre info: matOfPoint2 ROWS: " + matOfPoint2.rows() + " matOfPoint2 COLS: " + matOfPoint2.cols());

        Mat homography = Calib3d.findHomography(matOfPoint2, matOfPoint1, Calib3d.RANSAC, 1);

        Log.d(TAG, "Homography info: ROWS: " + homography.rows() + " COLS: " + homography.cols());

        Mat warpedMat = new Mat();
        Imgproc.warpPerspective(candidateMat, warpedMat, homography, warpedMat.size(), Imgproc.INTER_NEAREST, Core.BORDER_TRANSPARENT, Scalar.all(0));

        return warpedMat;
    }
}
