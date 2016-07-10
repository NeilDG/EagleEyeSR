package neildg.com.megatronsr.processing.multiple.refinement;

import org.opencv.core.Mat;
import org.opencv.photo.Photo;

import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
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

            Mat denoisedMat = new Mat();
            Photo.fastNlMeansDenoising(this.matList[i], denoisedMat, 3, 7, 21);
            this.outputMatList[i] = denoisedMat;
            ImageWriter.getInstance().saveMatrixToImage(this.matList[i], "noise_" +i, ImageFileAttribute.FileType.JPEG);
            ImageWriter.getInstance().saveMatrixToImage(denoisedMat, "denoise_" +i, ImageFileAttribute.FileType.JPEG);

            ProgressDialogHandler.getInstance().hideDialog();
        }
    }

    public Mat[] getResult() {
        return this.outputMatList;
    }
}
