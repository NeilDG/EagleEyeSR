package neildg.com.megatronsr.threads;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.MatWriter;
import neildg.com.megatronsr.processing.multiple.DownsamplingOperator;
import neildg.com.megatronsr.processing.multiple.FeatureMatchingOperator;
import neildg.com.megatronsr.processing.multiple.LRWarpingOperator;
import neildg.com.megatronsr.processing.operators.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 6/2/2016.
 */
public class DebuggingProcessor extends Thread {
    private final static String TAG = "";

    public enum DebugType {
        DEBUG_SAVE_MAT,
        ZERO_FILLING,
        WARP_IMAGES,
    }

    private DebugType debugType;

    public DebuggingProcessor(DebugType debugType) {
        this.debugType = debugType;
    }

    @Override
    public void run() {
        ProgressDialogHandler.getInstance().showDialog("Debug mode", "Performing debug operation. Please wait...");

        if(this.debugType == DebugType.DEBUG_SAVE_MAT) {
            this.performDebugSaving();
        }
        else if(this.debugType == DebugType.ZERO_FILLING) {
            this.performZeroFilling();
        }
        else if(this.debugType == DebugType.WARP_IMAGES) {
            this.performImageWarping();
        }

        ProgressDialogHandler.getInstance().hideDialog();
    }

    private void performDebugSaving() {
        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();
        for (int i = 0; i < numImages; i++) {
            Mat imageMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            MatWriter.writeMat(imageMat, FilenameConstants.DEBUG_DIR, FilenameConstants.MAT_VALUE_PREFIX + i);
        }
    }

    private void performZeroFilling() {
        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();
        for (int i = 0; i < numImages; i++) {
            Mat imageMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            imageMat = ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 0, 0);

            MatWriter.writeMat(imageMat, FilenameConstants.DEBUG_DIR, "zero_fill_val_" + i);
            ImageWriter.getInstance().saveMatrixToImage(imageMat, FilenameConstants.DEBUG_DIR, "zero_fill_" + i, ImageFileAttribute.FileType.JPEG);

            imageMat.release();
        }
    }

    /*
     * Performs zero-filling first before image warping!
     */
    private void performImageWarping() {
        DownsamplingOperator downsamplingOperator = new DownsamplingOperator(ParameterConfig.getScalingFactor(), BitmapURIRepository.getInstance().getNumImagesSelected());
        downsamplingOperator.perform();

        int numImages = BitmapURIRepository.getInstance().getNumImagesSelected();

        List<Mat> testMatList = new LinkedList<>();
        for (int i = 0; i < numImages; i++) {
            Mat imageMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            imageMat = ImageOperator.performZeroFill(imageMat, ParameterConfig.getScalingFactor(), 0, 0);

            ImageWriter.getInstance().saveMatrixToImage(imageMat, FilenameConstants.DOWNSAMPLE_PREFIX_STRING + i, ImageFileAttribute.FileType.JPEG);
            testMatList.add(imageMat);
        }

        FeatureMatchingOperator matchingOperator = new FeatureMatchingOperator();
        matchingOperator.perform();

        LRWarpingOperator warpingOperator = new LRWarpingOperator(matchingOperator.getRefKeypoint(), matchingOperator.getdMatchesList(), matchingOperator.getLrKeypointsList());
        warpingOperator.perform();

        List<Mat> warpedMatrixList = warpingOperator.getWarpedMatrixList();
        Mat testOutputMat = new Mat();
        for(int i = 0; i < warpedMatrixList.size(); i++) {
            Mat warpedMat = warpedMatrixList.get(i);
            Mat maskMat = Mat.zeros(warpedMat.size(), warpedMat.type());

            warpedMat.copyTo(maskMat);
            Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_RGB2GRAY);
            maskMat.convertTo(maskMat, CvType.CV_8UC1);
            Imgproc.threshold(maskMat, maskMat, 1, 255, Imgproc.THRESH_BINARY);

            warpedMat.copyTo(testOutputMat, maskMat);

            warpedMat.release();
            maskMat.release();
        }

        warpedMatrixList.clear();
       ImageWriter.getInstance().saveMatrixToImage(testOutputMat, "test", ImageFileAttribute.FileType.JPEG);
    }

    /*
 Inserts the referenceMat in the HR matrix
  */
    private void copyMatToHR(Mat fromMat, Mat hrMat, int xOffset, int yOffset) {
        int pixelSpace = ParameterConfig.getScalingFactor();

        for(int row = 0; row < fromMat.rows(); row++) {
            for(int col = 0; col < fromMat.cols(); col++) {
                double[] lrPixelData = fromMat.get(row, col);

                int resultRow = (row * pixelSpace) + yOffset;
                int resultCol = (col * pixelSpace) + xOffset;

                if(resultRow < hrMat.rows() && resultCol < hrMat.cols()) {
                    hrMat.put(resultRow, resultCol, lrPixelData);
                }

            }
        }

    }
}
