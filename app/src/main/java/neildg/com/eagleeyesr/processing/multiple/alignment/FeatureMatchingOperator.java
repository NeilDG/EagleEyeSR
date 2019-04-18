package neildg.com.eagleeyesr.processing.multiple.alignment;

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
import java.util.concurrent.Semaphore;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.processing.imagetools.MatMemory;
import neildg.com.eagleeyesr.threads.FlaggingThread;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;

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

    /*
     * Input is the list of images to compare. First image will be the reference image and succeeding images will be matched against it.
     */
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

        /*this.detectFeaturesInReference();
        for(int i = 0; i < this.comparingMatList.length; i++) {
            this.detectFeatures(this.comparingMatList[i], i);
        }

        for(int i = 0; i < comparingMatList.length; i++) {
            //Mat comparingMat = this.comparingMatList[i];
            this.matchFeaturesToReference(this.lrDescriptorList[i],i);

        }
        MatMemory.releaseAll(this.lrDescriptorList, false);*/

        ////MULTI-THREADED FEATURE MATCHING
        this.detectFeaturesInReference();
        FeatureMatcher[] featureMatchers = new FeatureMatcher[this.comparingMatList.length];
        Semaphore featureSem = new Semaphore(this.comparingMatList.length);

        //perform multithreaded feature matching
        for(int i = 0; i < featureMatchers.length; i++) {
            featureMatchers[i] = new FeatureMatcher(featureSem, this.referenceDescriptor, this.comparingMatList[i]);
            featureMatchers[i].startWork();
        }

        try {
            featureSem.acquire(this.comparingMatList.length);

            for(int i = 0; i < featureMatchers.length; i++) {

                this.dMatchesList[i] = featureMatchers[i].getMatches();
                this.lrKeypointsList[i] = featureMatchers[i].getLRKeypoint();

                Mat matchesShower = new Mat();
                Features2d.drawMatches(this.referenceMat, this.refKeypoint, this.comparingMatList[i], this.lrKeypointsList[i], this.dMatchesList[i], matchesShower);
                FileImageWriter.getInstance().saveMatrixToImage(matchesShower, FilenameConstants.MATCHES_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
                matchesShower.release();
            }

            featureSem.release(this.comparingMatList.length);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void detectFeaturesInReference() {
        //find features in reference LR image
        this.referenceDescriptor = new Mat();
        this.refKeypoint = new MatOfKeyPoint();

        featureDetector.detect(this.referenceMat, this.refKeypoint);
        descriptorExtractor.compute(this.referenceMat, this.refKeypoint, this.referenceDescriptor);
    }

    /*private void detectFeatures(Mat imgMat, int index) {
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

        float minDistance = ParameterConfig.getPrefsFloat(ParameterConfig.FEATURE_MINIMUM_DISTANCE_KEY, 999.0f);
        //only select good matches
        DMatch[] dMatchList = initialMatch.toArray();
        List<DMatch> goodMatchesList = new ArrayList<DMatch>();
        for(int i = 0; i < dMatchList.length; i++) {
            Log.d(TAG, "dMatch distance: " +dMatchList[i].distance);
            if(dMatchList[i].distance < minDistance) {
                goodMatchesList.add(dMatchList[i]);
            }
        }

        initialMatch.release();

        //filter matches to only show good ones
        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromArray(goodMatchesList.toArray(new DMatch[goodMatchesList.size()]));

        this.dMatchesList[index] = goodMatches;
    }*/

    private class FeatureMatcher extends FlaggingThread {

        private Mat refDescriptor;
        private Mat comparingMat;
        private FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        private DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        private MatOfKeyPoint keypoint;
        private Mat descriptor;
        private MatOfDMatch matches;

        public FeatureMatcher(Semaphore semaphore, Mat referenceDescriptor, Mat comparingMat) {
            super(semaphore);

            this.refDescriptor = referenceDescriptor;
            this.comparingMat = comparingMat;
        }

        @Override
        public void run() {

            //detect features in comparing mat
            this.descriptor = new Mat();
            this.keypoint = new MatOfKeyPoint();

            this.featureDetector.detect(this.comparingMat,this.keypoint);
            this.descriptorExtractor.compute(this.comparingMat, this.keypoint, this.descriptor);
            this.matches = this.matchFeaturesToReference();

            this.finishWork();
        }

        private MatOfDMatch matchFeaturesToReference() {
            MatOfDMatch initialMatch = new MatOfDMatch();
            Log.d(TAG, "Reference descriptor type: "+ CvType.typeToString(this.refDescriptor.type()) + " Comparing descriptor type: "+CvType.typeToString(this.descriptor.type()));
            Log.d(TAG, "Reference size: " +this.refDescriptor.size().toString()+ " Comparing descriptor size: " +this.descriptor.size().toString());
            this.matcher.match(this.refDescriptor, this.descriptor, initialMatch);

            float minDistance = ParameterConfig.getPrefsFloat(ParameterConfig.FEATURE_MINIMUM_DISTANCE_KEY, 999.0f);
            //only select good matches
            DMatch[] dMatchList = initialMatch.toArray();
            List<DMatch> goodMatchesList = new ArrayList<DMatch>();
            for(int i = 0; i < dMatchList.length; i++) {
                Log.d(TAG, "dMatch distance: " +dMatchList[i].distance);
                if(dMatchList[i].distance < minDistance) {
                    goodMatchesList.add(dMatchList[i]);
                }
            }

            initialMatch.release();

            //filter matches to only show good ones
            MatOfDMatch goodMatches = new MatOfDMatch();
            goodMatches.fromArray(goodMatchesList.toArray(new DMatch[goodMatchesList.size()]));

            return goodMatches;
        }

        public MatOfDMatch getMatches() {
            return this.matches;
        }

        public MatOfKeyPoint getLRKeypoint() {
            return this.keypoint;
        }
    }

}
