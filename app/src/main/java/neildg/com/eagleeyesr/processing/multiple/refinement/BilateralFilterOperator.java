package neildg.com.eagleeyesr.processing.multiple.refinement;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.processing.imagetools.ColorSpaceOperator;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;

/**Class that performs bilateral filtering for images as alternative to denoising.
 * Created by NeilDG on 4/26/2017.
 */

public class BilateralFilterOperator implements IOperator {
    private final static String TAG = "BilateralFilterOperator";

    private Mat[] matList;
    private Mat[] outputMatList;

    public BilateralFilterOperator(Mat[] matList) {
        this.matList = matList;
        this.outputMatList = new Mat[this.matList.length];
    }

    @Override
    public void perform() {
        for(int i = 0; i < this.matList.length; i++) {
            ProgressDialogHandler.getInstance().showProcessDialog("Denoising", "Performing bilateral filtering for image " +i, ProgressDialogHandler.getInstance().getProgress());

            //perform denoising on energy channel only
            Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(this.matList[i]);
            Mat bilateralMat = new Mat();
            MatOfFloat h = new MatOfFloat(6.0f);
            Imgproc.bilateralFilter(yuvMat[ColorSpaceOperator.Y_CHANNEL], bilateralMat, 15, 160, 160);

            FileImageWriter.getInstance().saveMatrixToImage(yuvMat[ColorSpaceOperator.Y_CHANNEL], "noise_" +i, ImageFileAttribute.FileType.JPEG);
            FileImageWriter.getInstance().saveMatrixToImage(bilateralMat, "denoise_" +i, ImageFileAttribute.FileType.JPEG);

            //merge channel then convert back to RGB
            yuvMat[ColorSpaceOperator.Y_CHANNEL].release();
            yuvMat[ColorSpaceOperator.Y_CHANNEL] = bilateralMat;

            this.outputMatList[i] = ColorSpaceOperator.convertYUVtoRGB(yuvMat);

            ProgressDialogHandler.getInstance().hideUserDialog();
        }
    }

    public Mat[] getResult() {
        return this.outputMatList;
    }
}
