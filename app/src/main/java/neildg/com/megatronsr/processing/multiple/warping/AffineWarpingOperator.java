package neildg.com.megatronsr.processing.multiple.warping;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Affine warping counterpart of perspective warping
 * Created by NeilDG on 7/30/2016.
 */
public class AffineWarpingOperator implements IOperator {
    private final static String TAG = "AffineWarpingOperator";

    private Mat referenceMat;
    private Mat[] inputMatList;
    private Mat[] warpedMatList;

    public AffineWarpingOperator(Mat referenceMat, Mat[] inputMatList) {
        this.referenceMat = referenceMat;
        this.inputMatList = inputMatList;

        this.warpedMatList = new Mat[this.inputMatList.length];
    }

    public Mat[] getWarpedMatList() {
        return this.warpedMatList;
    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Affine warping", "Performing affine warping on images.");
        for(int i = 0; i < inputMatList.length; i++) {
            Mat affineMat = Video.estimateRigidTransform(this.inputMatList[i], this.referenceMat, true);
            this.warpedMatList[i] = new Mat();

            //perform affine warp if valid affine mat is found
            if(affineMat.rows() == 2 && affineMat.cols() == 3) {
                Imgproc.warpAffine(this.inputMatList[i], this.warpedMatList[i], affineMat, this.inputMatList[i].size(), Imgproc.INTER_NEAREST, Core.BORDER_REPLICATE, Scalar.all(0));
            }
            else {
                //copy if no affine mat is found
                this.inputMatList[i].copyTo(this.warpedMatList[i]);
            }


            ImageWriter.getInstance().saveMatrixToImage(this.warpedMatList[i], "affine_warp_"+i, ImageFileAttribute.FileType.JPEG);
        }
        ProgressDialogHandler.getInstance().hideDialog();

    }
}
