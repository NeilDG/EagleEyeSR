package neildg.com.megatronsr.processing.multiple.fusion;

import org.opencv.core.Mat;

import java.util.List;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.multiple.ProcessedImageRepo;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 6/18/2016.
 */
public class MotionFusionOperator implements IOperator {
    private final static String TAG = "MotionFusionOperator";

    private List<Mat> zeroFilledMatSequences;

    private Mat outputMat;

    /*
     * Accepts zero filled mat sequences that should include the reference mat
     */
    public MotionFusionOperator(List<Mat> zeroFilledMatSequences) {
        this.zeroFilledMatSequences = zeroFilledMatSequences;

        int scaling = ParameterConfig.getScalingFactor();
        Mat referenceMat = this.zeroFilledMatSequences.get(0);
        this.outputMat = new Mat(referenceMat.rows() * scaling, referenceMat.cols() * scaling, referenceMat.type());
    }

    @Override
    public void perform() {
        for(int i = 0; i < this.zeroFilledMatSequences.size(); i++) {
            ProgressDialogHandler.getInstance().showDialog("Fusing", "Fusing image " +i);
            Mat mat = this.zeroFilledMatSequences.get(i);
            Mat mask = ImageOperator.produceMask(mat);

            mat.copyTo(this.outputMat,mask);

            mat.release();
            mask.release();

            ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "fusion", "step_"+i, ImageFileAttribute.FileType.JPEG);
        }

        ImageWriter.getInstance().saveMatrixToImage(this.outputMat, "result", ImageFileAttribute.FileType.JPEG);

        //deallocate memory
        this.outputMat.release();
        this.zeroFilledMatSequences.clear();

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
