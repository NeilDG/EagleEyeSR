package neildg.com.megatronsr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.single_gaussian.LoadedImagePatch;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageMeasures;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Merges multiple image by patch comparison. Which patch is the best among the regions?
 * Created by NeilDG on 6/19/2016.
 */
public class MultiplePatchFusionOperator implements IOperator {

    private static String TAG = "MultiplePatchFusionOperator";

    private Mat originMat;
    private Mat[] warpedMatList;

    public MultiplePatchFusionOperator(Mat originMat, Mat[] warpedMatList) {
        this.originMat = originMat;
        this.warpedMatList = warpedMatList;
    }

    @Override
    public void perform() {
        int patchSize = 4;

        ProgressDialogHandler.getInstance().showDialog("Forming HR", "Replacing image patches");

        for(int row = 0; row < this.originMat.rows(); row += patchSize) {
            for(int col = 0; col < this.originMat.cols(); col += patchSize) {
                LoadedImagePatch  originPatch = new LoadedImagePatch(this.originMat, patchSize, col, row);

                double rmse = Double.MAX_VALUE;
                for(int i = 0; i < this.warpedMatList.length; i++) {
                    
                    LoadedImagePatch imagePatch = new LoadedImagePatch(this.warpedMatList[i], patchSize, col, row);
                    double newRMSE = ImageMeasures.measureRMSENoise(imagePatch.getPatchMat());
                    double matSimilarity = ImageMeasures.measureMATSimilarity(originPatch.getPatchMat(), imagePatch.getPatchMat());

                    if(matSimilarity <= 0.001 && newRMSE < rmse) {
                        //Log.d(TAG, "New RMSE is "+newRMSE+ " Old: " +rmse+ " Performing patch replacement.");
                        rmse = newRMSE;
                        ImageOperator.replacePatchOnROI(this.originMat, originPatch, imagePatch);
                    }

                    imagePatch.release();
                }

                originPatch.release();
            }
        }

        ProgressDialogHandler.getInstance().showDialog("Forming HR", "Finalizing");

        ImageWriter.getInstance().saveMatrixToImage(this.originMat, "initial_output", ImageFileAttribute.FileType.JPEG);
        Mat outputMat = Mat.ones(this.originMat.rows() * ParameterConfig.getScalingFactor(), this.originMat.cols() * ParameterConfig.getScalingFactor(), this.originMat.type());
        Imgproc.resize(this.originMat, outputMat, outputMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);

        ImageWriter.getInstance().saveMatrixToImage(outputMat, "initial_result", ImageFileAttribute.FileType.JPEG);

        Imgproc.medianBlur(outputMat, outputMat, 3);
        ImageWriter.getInstance().saveMatrixToImage(outputMat, "result", ImageFileAttribute.FileType.JPEG);

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
