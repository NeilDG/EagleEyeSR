package neildg.com.eagleeyesr.processing.multiple.old_fusion;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.model.single_gaussian.LoadedImagePatch;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ColorSpaceOperator;
import neildg.com.eagleeyesr.processing.imagetools.ImageMeasures;
import neildg.com.eagleeyesr.processing.imagetools.ImageOperator;

/**
 * Merges multiple image by patch comparison. Which patch is the best among the regions?
 * Created by NeilDG on 6/19/2016.
 */
public class MultiplePatchFusionOperator implements IOperator {

    private static String TAG = "MPFusionOperator";

    private Mat originMat;
    private Mat[] warpedMatList;

    private Mat[] yuvOriginMat;


    public MultiplePatchFusionOperator(Mat originMat, Mat[] warpedMatList) {

        //TODO: uncomment for RGB processing
        //this.originMat = originMat;
        //this.warpedMatList = warpedMatList;

        //convert mat into its Y channels first
        this.yuvOriginMat = ColorSpaceOperator.convertRGBToYUV(originMat);
        this.originMat = this.yuvOriginMat[ColorSpaceOperator.Y_CHANNEL];

        this.warpedMatList = new Mat[warpedMatList.length];
        for(int i = 0 ; i < warpedMatList.length; i++) {
            Mat[] warpedMatYUV = ColorSpaceOperator.convertRGBToYUV(warpedMatList[i]);
            this.warpedMatList[i] = warpedMatYUV[ColorSpaceOperator.Y_CHANNEL];
        }
    }

    @Override
    public void perform() {
        int patchSize = 4;

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
                        ImageOperator.replacePatchOnROI(this.originMat, 1, originPatch, imagePatch);
                    }

                    imagePatch.release();
                }

                originPatch.release();
            }
        }

        FileImageWriter.getInstance().saveMatrixToImage(this.originMat, "initial_output", ImageFileAttribute.FileType.JPEG);

        Mat processedYMat = Mat.ones(this.originMat.rows() * ParameterConfig.getScalingFactor(), this.originMat.cols() * ParameterConfig.getScalingFactor(), this.originMat.type());
        Imgproc.resize(this.originMat, processedYMat, processedYMat.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        FileImageWriter.getInstance().saveMatrixToImage(processedYMat, "initial_result", ImageFileAttribute.FileType.JPEG);

        this.mergeResults(processedYMat);
        processedYMat.release();

    }

    /*
     * Merges origin Mat (Y) to bicubic interpolated (UV) mat
     */
    private void mergeResults(Mat processedYMat) {
        this.yuvOriginMat[ColorSpaceOperator.Y_CHANNEL] = processedYMat;

        Mat scaledU = Mat.ones(this.yuvOriginMat[ColorSpaceOperator.U_CHANNEL].rows() * ParameterConfig.getScalingFactor(), this.yuvOriginMat[ColorSpaceOperator.U_CHANNEL].cols() * ParameterConfig.getScalingFactor(), this.yuvOriginMat[ColorSpaceOperator.U_CHANNEL].type());
        Imgproc.resize(this.yuvOriginMat[ColorSpaceOperator.U_CHANNEL], scaledU, scaledU.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        this.yuvOriginMat[ColorSpaceOperator.U_CHANNEL] = scaledU;

        Mat scaledV =  Mat.ones(this.yuvOriginMat[ColorSpaceOperator.V_CHANNEL].rows() * ParameterConfig.getScalingFactor(), this.yuvOriginMat[ColorSpaceOperator.V_CHANNEL].cols() * ParameterConfig.getScalingFactor(), this.yuvOriginMat[ColorSpaceOperator.V_CHANNEL].type());
        Imgproc.resize(this.yuvOriginMat[ColorSpaceOperator.V_CHANNEL], scaledV, scaledV.size(), ParameterConfig.getScalingFactor(), ParameterConfig.getScalingFactor(), Imgproc.INTER_CUBIC);
        this.yuvOriginMat[ColorSpaceOperator.V_CHANNEL] = scaledV;

        Mat outputMat = new Mat();

        Log.d(TAG, "Y size: " +this.yuvOriginMat[ColorSpaceOperator.Y_CHANNEL].size() +
                " U size: " +this.yuvOriginMat[ColorSpaceOperator.U_CHANNEL].size() +
                " V size: " +this.yuvOriginMat[ColorSpaceOperator.V_CHANNEL].size());

        Core.merge(Arrays.asList(this.yuvOriginMat), outputMat);
        Imgproc.cvtColor(outputMat, outputMat, Imgproc.COLOR_YUV2BGR);
        FileImageWriter.getInstance().saveMatrixToImage(outputMat, "result", ImageFileAttribute.FileType.JPEG);
    }
}
