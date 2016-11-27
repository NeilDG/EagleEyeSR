package neildg.com.megatronsr.processing.multiple.fusion;

import android.util.Log;

import org.opencv.core.Mat;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 6/18/2016.
 */
public class MotionFusionOperator implements IOperator {
    private final static String TAG = "MotionFusionOperator";

    private Mat[] zeroFilledMatSequences;

    private Mat outputMat;

    /*
     * Accepts zero filled mat sequences that should include the reference mat
     */
    public MotionFusionOperator(Mat[] zeroFilledMatSequences) {
        this.zeroFilledMatSequences = zeroFilledMatSequences;

        this.outputMat = FileImageReader.getInstance().imReadOpenCV(FilenameConstants.HR_CUBIC + 0, ImageFileAttribute.FileType.JPEG);
    }

    @Override
    public void perform() {
        for(int i = 0; i < this.zeroFilledMatSequences.length; i++) {
            ProgressDialogHandler.getInstance().showDialog("Fusing", "Fusing image " +i);
            Mat mat = this.zeroFilledMatSequences[i];
            Mat mask = ImageOperator.produceMask(mat);

            Log.d(TAG, "Mat size: " +mat.size().toString()+ " OUtput mat size: " +this.outputMat.size().toString()+ " Mask size: " +mask.size().toString());
            mat.copyTo(this.outputMat, mask);
            //Core.add(mat, this.outputMat, this.outputMat, mask);

            mat.release();
            mask.release();

            FileImageWriter.getInstance().saveMatrixToImage(this.outputMat, "fusion", "step_"+i, ImageFileAttribute.FileType.JPEG);
        }

        FileImageWriter.getInstance().saveMatrixToImage(this.outputMat, "result", ImageFileAttribute.FileType.JPEG);

        //deallocate memory
        this.outputMat.release();
        this.zeroFilledMatSequences = null;

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
