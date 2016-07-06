package neildg.com.megatronsr.processing.multiple;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Compare LR reference mat and match features to LR2...LRN.
 * Created by NeilDG on 3/6/2016.
 */
public class FeatureMatchingOperator {
    private final static String TAG = "FeatureMatchingOperator";

    private MatOfKeyPoint refKeypoint;
    private Mat referenceDescriptor;

    private MatOfKeyPoint[] lrKeypointsList;
    private Mat[] lrDescriptorList;
    private MatOfDMatch[] dMatchesList;

    private FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
    private DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
    private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

    private Mat referenceMat;
    private Mat[] comparingMatList;

    public FeatureMatchingOperator(Mat referenceMat, Mat[] comparingMatList) {
        this.referenceMat = referenceMat;
        this.comparingMatList = comparingMatList;

        this.lrKeypointsList = new MatOfKeyPoint[this.comparingMatList.length];
        this.lrDescriptorList = new Mat[this.comparingMatList.length];
        this.dMatchesList = new MatOfDMatch[this.comparingMatList.length];
    }

    public MatOfKeyPoint getRefKeypoint() {
        return this.refKeypoint;
    }
    public MatOfDMatch[] getdMatchesList() {
        return this.dMatchesList;
    }
    public MatOfKeyPoint[] getLrKeypointsList() {
        return this.lrKeypointsList;
    }

    public void perform() {
        ProgressDialogHandler.getInstance().hideDialog();
        ProgressDialogHandler.getInstance().showDialog("Finding features", "Detecting features on images.");

        this.detectFeaturesInReference();
        for(int i = 0; i < this.comparingMatList.length; i++) {
            this.detectFeatures(this.comparingMatList[i], i);
        }

        ProgressDialogHandler.getInstance().hideDialog();

        for(int i = 0; i < comparingMatList.length; i++) {
            Mat comparingMat = this.comparingMatList[i];

            ProgressDialogHandler.getInstance().showDialog("Matching features for image " +i, "Matching features in image " +i+ " to reference image.");
            this.matchFeaturesToReference(this.lrDescriptorList[i],i);

            Mat matchesShower = new Mat();
            Features2d.drawMatches(this.referenceMat, this.refKeypoint, comparingMat, this.lrKeypointsList[i], this.dMatchesList[i], matchesShower);
            ImageWriter.getInstance().saveMatrixToImage(matchesShower, FilenameConstants.MATCHES_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);

            ProgressDialogHandler.getInstance().hideDialog();

            matchesShower.release();
        }
    }

    private void detectFeaturesInReference() {
        //find features in reference LR image
        this.referenceDescriptor = new Mat();
        this.refKeypoint = new MatOfKeyPoint();

        featureDetector.detect(this.referenceMat, this.refKeypoint);
        descriptorExtractor.compute(this.referenceMat, this.refKeypoint, this.referenceDescriptor);
    }

    private void detectFeatures(Mat imgMat, int index) {
        Mat lrDescriptor = new Mat();
        MatOfKeyPoint refKeypoint = new MatOfKeyPoint();

        featureDetector.detect(imgMat,refKeypoint);
        this.descriptorExtractor.compute(imgMat, refKeypoint, lrDescriptor);

        this.lrKeypointsList[index] = refKeypoint;
        this.lrDescriptorList[index] = lrDescriptor;
    }

    private void matchFeaturesToReference(Mat comparingDescriptor, int index) {
        MatOfDMatch initialMatch = new MatOfDMatch();
        Log.d(TAG, "Reference descriptor type: "+ CvType.typeToString(this.referenceDescriptor.type()) + " Comparing descriptor type: "+CvType.typeToString(comparingDescriptor.type()));
        Log.d(TAG, "Reference size: " +this.referenceDescriptor.size().toString()+ " Comparing descriptor size: " +comparingDescriptor.size().toString());
        this.matcher.match(this.referenceDescriptor, comparingDescriptor, initialMatch);

        //only select good matches
        DMatch[] dMatchList = initialMatch.toArray();
        List<DMatch> goodMatchesList = new ArrayList<DMatch>();
        for(int i = 0; i < dMatchList.length; i++) {
            Log.d(TAG, "dMatch distance: " +dMatchList[i].distance);
            /*if(dMatchList[i].distance < 5.0f)*/ {
                goodMatchesList.add(dMatchList[i]);
            }
        }

        initialMatch.release();

        //filter matches to only show good ones
        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromArray(goodMatchesList.toArray(new DMatch[goodMatchesList.size()]));

        this.dMatchesList[index] = goodMatches;
    }


}
