package neildg.com.megatronsr.processing.multiple;

import android.util.Log;

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
import neildg.com.megatronsr.io.BitmapURIRepository;
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

    private Mat referenceMat;
    private MatOfKeyPoint refKeypoint;
    private Mat referenceDescriptor;

    private List<MatOfKeyPoint> lrKeypointsList = new ArrayList<MatOfKeyPoint>();
    private List<Mat> lrDescriptorList = new ArrayList<Mat>();
    private List<MatOfDMatch> dMatchesList = new ArrayList<MatOfDMatch>();

    private FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
    private DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
    private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

    public FeatureMatchingOperator() {
        this.referenceMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + "0", ImageFileAttribute.FileType.JPEG);
    }

    public Mat getReferenceMat() {return this.referenceMat;}
    public MatOfKeyPoint getRefKeypoint() {
        return this.refKeypoint;
    }

    public List<MatOfDMatch> getdMatchesList() {
        return this.dMatchesList;
    }

    public List<MatOfKeyPoint> getLrKeypointsList() {
        return this.lrKeypointsList;
    }

    public void perform() {
        ProgressDialogHandler.getInstance().hideDialog();
        ProgressDialogHandler.getInstance().showDialog("Finding features in first image", "Finding features in first image. Succeeding LR images will be matched to it.");

        this.detectFeaturesInReference();

        ProgressDialogHandler.getInstance().hideDialog();


        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();

        for(int i = 1; i < numImages; i++) {
            Mat comparingMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING +i, ImageFileAttribute.FileType.JPEG);

            ProgressDialogHandler.getInstance().showDialog("Finding feature in image " + i, "Finding features in image " + i);
            this.detectFeaturesInLR(comparingMat);

            //ProgressDialogHandler.getInstance().hideDialog();
        }

        ProgressDialogHandler.getInstance().hideDialog();

        for(int i = 1; i < numImages; i++) {
            Mat comparingMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING +i, ImageFileAttribute.FileType.JPEG);

            ProgressDialogHandler.getInstance().showDialog("Matching features for image " +i, "Matching features in image " +i+ " to reference image.");
            this.matchFeaturesToReference(this.lrDescriptorList.get(i - 1));

            Mat matchesShower = new Mat();
            Features2d.drawMatches(this.referenceMat, this.refKeypoint, comparingMat, this.lrKeypointsList.get(i-1), this.dMatchesList.get(i-1), matchesShower);
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

    private void detectFeaturesInLR(Mat imgMat) {
        Mat lrDescriptor = new Mat();
        MatOfKeyPoint refKeypoint = new MatOfKeyPoint();

        featureDetector.detect(imgMat,refKeypoint);
        this.descriptorExtractor.compute(imgMat, refKeypoint, lrDescriptor);

        this.lrKeypointsList.add(refKeypoint);
        this.lrDescriptorList.add(lrDescriptor);
    }

    private void matchFeaturesToReference(Mat comparingDescriptor) {
        MatOfDMatch initialMatch = new MatOfDMatch();
        this.matcher.match(this.referenceDescriptor, comparingDescriptor, initialMatch);

        //only select good matches
        DMatch[] dMatchList = initialMatch.toArray();
        List<DMatch> goodMatchesList = new ArrayList<DMatch>();
        for(int i = 0; i < dMatchList.length; i++) {
            Log.d(TAG, "dMatch distance: " +dMatchList[i].distance);
            /*if(dMatchList[i].distance < 25.0f)*/ {
                goodMatchesList.add(dMatchList[i]);
            }
        }

        initialMatch.release();

        //filter matches to only show good ones
        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromArray(goodMatchesList.toArray(new DMatch[goodMatchesList.size()]));

        this.dMatchesList.add(goodMatches);
    }


}
