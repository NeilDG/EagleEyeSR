package neildg.com.eagleeyesr.processing.multiple.alignment;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.model.AttributeHolder;
import neildg.com.eagleeyesr.model.AttributeNames;
import neildg.com.eagleeyesr.processing.IOperator;

/**
 * Affine warping counterpart of perspective warping
 * Created by NeilDG on 7/30/2016.
 */
public class AffineWarpingOperator implements IOperator {
    private final static String TAG = "AffineWarpingOperator";

    private Mat referenceMat;

    private Mat[] candidateMatList;
    private Mat[] imagesToWarpList;
    private Mat[] warpedMatList;

    /*
     * The target reference mat, the matrices to be used for feature matching and identifying affine transformation from reference mat,
     * the actual images to apply affine transformation. Candidate mat list and images to warp list should be of equal length and 1:1 correspondence.
     */
    public AffineWarpingOperator(Mat referenceMat, Mat[] candidateMatList, Mat[] imagesToWarpList) {
        this.referenceMat = referenceMat;
        this.candidateMatList = candidateMatList;
        this.imagesToWarpList = imagesToWarpList;

        this.warpedMatList = new Mat[this.candidateMatList.length];
    }

    public Mat[] getWarpedMatList() {
        return this.warpedMatList;
    }

    @Override
    public void perform() {
        for(int i = 0; i < candidateMatList.length; i++) {
            Log.e(TAG, "input size: " +this.candidateMatList[i].size()+ " Reference mat size: " +this.referenceMat.size());
            Mat affineMat = Video.estimateRigidTransform(this.referenceMat, this.candidateMatList[i], true);
            this.warpedMatList[i] = new Mat();

            //perform affine warp if valid affine mat is found
            if(affineMat.rows() == 2 && affineMat.cols() == 3) {
                this.warpedMatList[i].release();
                this.warpedMatList[i] = performAffineToTarget(affineMat, this.imagesToWarpList[i]);
            }
            else {
                //copy if no affine mat is found
                this.imagesToWarpList[i].copyTo(this.warpedMatList[i]);
            }

            FileImageWriter.getInstance().saveMatrixToImage(this.warpedMatList[i], FilenameConstants.AFFINE_WARP_PREFIX + i, ImageFileAttribute.FileType.JPEG);
        }

        AttributeHolder.getSharedInstance().putValue(AttributeNames.WARPED_IMAGES_LENGTH_KEY, this.warpedMatList.length);
    }

    /*
     * Performs affine transformation to target mat, using a specified affine transformation. WARNING. This operation cannot be performed in-place (using the same target mat as destination).
     */
    public static Mat performAffineToTarget(Mat affineMat, Mat targetMat) {
        Mat transformedMat = new Mat();
        Imgproc.warpAffine(targetMat, transformedMat, affineMat, targetMat.size(), Imgproc.INTER_NEAREST, Core.BORDER_REPLICATE, Scalar.all(0));

        return transformedMat;
    }
}
