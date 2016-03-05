package neildg.com.megatronsr.processing;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConstants;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 3/5/2016.
 */
public class ShiftAddFusionOperator {
    private static String TAG = "ShiftAddFusionOperator";

    private Mat referenceMat;
    private Mat hrMat;

    public ShiftAddFusionOperator() {

    }

    public void perform() {
        this.referenceMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + "0.jpg");

        this.hrMat = Mat.ones(referenceMat.rows() * ParameterConstants.SCALING_FACTOR, referenceMat.cols() * ParameterConstants.SCALING_FACTOR, this.referenceMat.type());

        this.copyMatToHR(this.referenceMat, 0, 0);
        //this.bruteForcePutLRMat();
        this.assembleHRMatWithFeatures();

        //temp save hrImage
        ImageWriter.getInstance().saveMatrixToImage(this.hrMat, FilenameConstants.HR_PROCESSED_STRING);
        this.hrMat.release();
        this.hrMat = null;
    }

    /*
    Inserts the referenceMat in the HR matrix
     */
    private void copyMatToHR(Mat fromMat, int xOffset, int yOffset) {
        int pixelSpace = ParameterConstants.SCALING_FACTOR;

        for(int row = 0; row < fromMat.rows(); row++) {
            for(int col = 0; col < fromMat.cols(); col++) {
                double[] lrPixelData = fromMat.get(row, col);

                int resultRow = (row * pixelSpace) + yOffset;
                int resultCol = (col * pixelSpace) + xOffset;

                if(resultRow < this.hrMat.rows() && resultCol < this.hrMat.cols()) {
                    this.hrMat.put(resultRow, resultCol, lrPixelData);
                }

            }
        }

    }

    private void bruteForcePutLRMat() {
        int pixelSpace = ParameterConstants.SCALING_FACTOR;

       /* for(int i = 1; i < BitmapURIRepository.getInstance().getNumImages(); i++) {
            Mat lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i + ".jpg");
            this.copyMatToHR(lrMat, i,0);

            lrMat.release();
            lrMat = null;
        }*/


        Mat lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 1 + ".jpg");
        this.copyMatToHR(lrMat,1,0);

        lrMat.release();


        lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 2 + ".jpg");
        this.copyMatToHR(lrMat,0,1);
        lrMat.release();

        lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 3 + ".jpg");
        lrMat.release();
        lrMat = null;
    }

    private void assembleHRMatWithInpaint() {
        int pixelSpace = ParameterConstants.SCALING_FACTOR;

        Mat lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 1 + ".jpg");
        this.copyMatToHR(lrMat, 1, 0);

        ProgressDialogHandler.getInstance().hideDialog();
        ProgressDialogHandler.getInstance().showDialog("Detecting features", "Detecting features using FAST method.");
        MatOfKeyPoint matOfKeyPoint = new MatOfKeyPoint();
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.FAST);
        featureDetector.detect(this.hrMat, matOfKeyPoint);

        Mat refKeypoints = Mat.zeros(this.hrMat.rows(), this.hrMat.cols(), this.hrMat.type());
        this.referenceMat.copyTo(refKeypoints);
        Features2d.drawKeypoints(this.hrMat, matOfKeyPoint, refKeypoints);

        ImageWriter.getInstance().saveMatrixToImage(refKeypoints, "keypoints");

        Imgproc.cvtColor(refKeypoints, refKeypoints, Imgproc.COLOR_BGR2GRAY);
        refKeypoints.convertTo(refKeypoints, CvType.CV_8UC1);
        Photo.inpaint(this.hrMat, refKeypoints, this.hrMat, 4, Photo.INPAINT_TELEA);

        lrMat.release();
    }

    private void assembleHRMatWithFeatures() {
        int pixelSpace = ParameterConstants.SCALING_FACTOR;

        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();
        Mat lrMat1 = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 0 + ".jpg");

        ProgressDialogHandler.getInstance().hideDialog();
        ProgressDialogHandler.getInstance().showDialog("Detecting features", "Detecting features using FAST method.");
        MatOfKeyPoint matOfKeyPoint1 = new MatOfKeyPoint();
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        featureDetector.detect(lrMat1, matOfKeyPoint1);

        Mat refKeypoints = Mat.zeros(lrMat1.rows(), lrMat1.cols(), lrMat1.type());
        lrMat1.copyTo(refKeypoints);
        Features2d.drawKeypoints(lrMat1, matOfKeyPoint1, refKeypoints);

        ImageWriter.getInstance().saveMatrixToImage(refKeypoints, "keypoints_1");

        refKeypoints.release();

        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        descriptorExtractor.compute(lrMat1, matOfKeyPoint1, descriptors1);

        Mat lrMat2 = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 1 + ".jpg");
        featureDetector = FeatureDetector.create(FeatureDetector.FAST);
        MatOfKeyPoint matOfKeyPoint2 = new MatOfKeyPoint();
        featureDetector.detect(lrMat2, matOfKeyPoint2);

        lrMat2.copyTo(refKeypoints);
        Features2d.drawKeypoints(lrMat2, matOfKeyPoint2, refKeypoints);

        ImageWriter.getInstance().saveMatrixToImage(refKeypoints, "keypoints_2");
        refKeypoints.release();

        descriptorExtractor.compute(lrMat2, matOfKeyPoint2, descriptors2);


        MatOfDMatch dMatches = new MatOfDMatch();
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        matcher.match(descriptors1, descriptors2, dMatches);

        DMatch[] dMatchList = dMatches.toArray();
        List<DMatch> goodMatchesList = new ArrayList<DMatch>();
        for(int i = 0; i < dMatchList.length; i++) {
            Log.d(TAG, "Distance is: " + dMatchList[i].distance);
            if(dMatchList[i].distance < 40.0f) {
                goodMatchesList.add(dMatchList[i]);
            }
        }

        //filter matches to only show good ones
        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromArray(goodMatchesList.toArray(new DMatch[goodMatchesList.size()]));

        Mat matchesShower = new Mat();
        Features2d.drawMatches(lrMat1, matOfKeyPoint1, lrMat2, matOfKeyPoint2, goodMatches, matchesShower);
        ImageWriter.getInstance().saveMatrixToImage(matchesShower, "matches");
    }
}
