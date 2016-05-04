package neildg.com.megatronsr.processing.multiple;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.MetricsLogger;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 4/17/2016.
 */
public class TVDenoiseHROperator implements IOperator {

    private final static String TAG = "TVDenoiseHROperator";

    private Mat hrMat;
    private Mat groundTruthMat;

    public TVDenoiseHROperator() {

    }

    @Override
    public void perform() {
        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();

        Mat lrMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);
        Imgproc.cvtColor(lrMat, lrMat, Imgproc.COLOR_BGR2GRAY);

        this.groundTruthMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.GROUND_TRUTH_PREFIX_STRING, ImageFileAttribute.FileType.JPEG);
        Imgproc.cvtColor(this.groundTruthMat, this.groundTruthMat, Imgproc.COLOR_BGR2GRAY);
        ImageWriter.getInstance().saveMatrixToImage(this.groundTruthMat, FilenameConstants.GROUND_TRUTH_PREFIX_STRING, ImageFileAttribute.FileType.JPEG);

        this.hrMat = Mat.ones(lrMat.rows() * ParameterConfig.getScalingFactor(), lrMat.cols() * ParameterConfig.getScalingFactor(), lrMat.type());

        Mat nearestMat = new Mat();
        Imgproc.resize(lrMat, nearestMat, nearestMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_NEAREST);
        ImageWriter.getInstance().saveMatrixToImage(nearestMat, "nearest", ImageFileAttribute.FileType.JPEG);
        MetricsLogger.getSharedInstance().takeMetrics("GroundTruthVSNearest",this.groundTruthMat, "GroundTruth",nearestMat, "Nearest Neighbor", "Ground Truth vs. Nearest-neighbor");
        nearestMat.release();

        Mat cubicMat = new Mat();
        Imgproc.resize(lrMat, cubicMat, cubicMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        ImageWriter.getInstance().saveMatrixToImage(cubicMat, "bicubic", ImageFileAttribute.FileType.JPEG);
        MetricsLogger.getSharedInstance().takeMetrics("GroundTruthVSBicubic",this.groundTruthMat, "GroundTruth",cubicMat, "Bicubic", "Ground Truth vs. Bicubic");
        cubicMat.release();


        List<Mat> lrObservations = new ArrayList<Mat>();
        lrObservations.add(lrMat);

        for(int i = 1; i < numImages; i++) {
            ProgressDialogHandler.getInstance().showDialog("Observing LR set","Loading LR image " +i);
            Mat mat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
            lrObservations.add(mat);
        }

        for(int i = 0; i < lrObservations.size(); i++) {
            Log.d(TAG, "LR Mat "+i+" Size: " +lrObservations.get(i).elemSize()+ " Rows: " +lrObservations.get(i).rows()+ " Cols: " +lrObservations.get(i).cols()+ " Type: " +lrObservations.get(i).type()+ " Channels: "+lrObservations.get(i).channels());
        }

        ProgressDialogHandler.getInstance().showDialog("TV Denoising","Denoising and creating HR image through observations.");
        Mat denoisedMat = new Mat(lrMat.size(), lrMat.type());

        Photo.denoise_TVL1(lrObservations, denoisedMat,30,100);
        Imgproc.resize(denoisedMat, this.hrMat, this.hrMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        ImageWriter.getInstance().saveMatrixToImage(denoisedMat, "denoised_lr", ImageFileAttribute.FileType.JPEG);
        ImageWriter.getInstance().saveMatrixToImage(this.hrMat, "denoised_HR", ImageFileAttribute.FileType.JPEG);

        MetricsLogger.getSharedInstance().takeMetrics("GroundTruthVsDenoisedHR", this.groundTruthMat, "GroundTruth", this.hrMat, "TVL1 Denoised HR", "Ground Truth vs TVL1 Denoised HR");

        MetricsLogger.getSharedInstance().debugPSNRTable();
        MetricsLogger.getSharedInstance().logResultsToJSON(FilenameConstants.METRICS_NAME_STRING);

        ProgressDialogHandler.getInstance().hideDialog();

        denoisedMat.release();
        this.hrMat.release();
        for(Mat mat : lrObservations) {
            mat.release();
        }
        lrObservations.clear();
    }
}
