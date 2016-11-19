package neildg.com.megatronsr.processing.multiple.refinement;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.photo.Photo;

import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ColorSpaceOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Class that handles denoising operations
 * Created by NeilDG on 7/10/2016.
 */
public class DenoisingOperator implements IOperator{
    private final static String TAG = "DenoisingOperator";

    private Mat[] matList;
    private Mat[] outputMatList;
    public DenoisingOperator(Mat[] matList) {
        this.matList = matList;
        this.outputMatList = new Mat[this.matList.length];
    }

    @Override
    public void perform() {
        for(int i = 0; i < this.matList.length; i++) {
            ProgressDialogHandler.getInstance().showDialog("Denoising", "Denoising image " +i);

            //perform denoising on energy channel only
            Mat[] yuvMat = ColorSpaceOperator.convertRGBToYUV(this.matList[i]);
            Mat denoisedMat = new Mat();
            MatOfFloat h = new MatOfFloat(3.0f);
            Photo.fastNlMeansDenoising(yuvMat[ColorSpaceOperator.Y_CHANNEL], denoisedMat, h, 7, 21, Core.NORM_L1);

            FileImageWriter.getInstance().saveMatrixToImage(yuvMat[ColorSpaceOperator.Y_CHANNEL], "noise_" +i, ImageFileAttribute.FileType.JPEG);
            FileImageWriter.getInstance().saveMatrixToImage(denoisedMat, "denoise_" +i, ImageFileAttribute.FileType.JPEG);

            //merge channel then convert back to RGB
            yuvMat[ColorSpaceOperator.Y_CHANNEL].release();
            yuvMat[ColorSpaceOperator.Y_CHANNEL] = denoisedMat;

            this.outputMatList[i] = ColorSpaceOperator.convertYUVtoRGB(yuvMat);

            /*Mat denoisedMat = new Mat();
            Photo.fastNlMeansDenoisingColored(this.matList[i], denoisedMat, 3, 0, 7, 21);
            this.outputMatList[i] = denoisedMat;
            ImageWriter.getInstance().saveMatrixToImage(this.matList[i], "noise_" +i, ImageFileAttribute.FileType.JPEG);
            ImageWriter.getInstance().saveMatrixToImage(denoisedMat, "denoise_" +i, ImageFileAttribute.FileType.JPEG);*/

            ProgressDialogHandler.getInstance().hideDialog();
        }
    }

    public Mat[] getResult() {
        return this.outputMatList;
    }
}
