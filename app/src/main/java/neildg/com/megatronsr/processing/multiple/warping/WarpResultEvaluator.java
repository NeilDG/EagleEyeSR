package neildg.com.megatronsr.processing.multiple.warping;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.MatMemory;

/**
 * Operator that verifies the quality of warped images by measuring its norm against the first reference  LR image.
 * If it's above the threshold, it will be filtered out.
 * Created by NeilDG on 12/12/2016.
 */

public class WarpResultEvaluator implements IOperator {
    private final static String TAG = "WarpResultEvaluator";

    private Mat referenceMat;
    private Mat[] warpedMatList;

    String referenceImageName;
    String[] warpedMatNames;

    public WarpResultEvaluator(String referenceImageName, String[] warpedMatNames) {
        this.referenceImageName = referenceImageName;
        this.warpedMatNames = warpedMatNames;
    }

    @Override
    public void perform() {
        this.referenceMat = FileImageReader.getInstance().imReadOpenCV(referenceImageName, ImageFileAttribute.FileType.JPEG);
        this.warpedMatList = new Mat[this.warpedMatNames.length];

        for(int i = 0; i < this.warpedMatNames.length; i++) {
            this.warpedMatList[i] = FileImageReader.getInstance().imReadOpenCV(this.warpedMatNames[i], ImageFileAttribute.FileType.JPEG);

            double distanceNorm = Core.norm(this.warpedMatList[i], this.referenceMat, Core.NORM_L1);

            Mat differenceMat = new Mat();
            Core.subtract(this.warpedMatList[i], this.referenceMat, differenceMat);
            Scalar sumElemScalar = Core.sumElems(differenceMat);

            double totalDifference = 0.0;
            for(int channels = 0; channels < differenceMat.channels(); channels++) {
                totalDifference += sumElemScalar.val[i];
            }

            Log.d(TAG, "Norm distance of "+this.warpedMatNames[i]+ " to reference mat: " +distanceNorm+ " Total difference: " +totalDifference);
        }

        this.referenceMat.release();
        MatMemory.releaseAll(this.warpedMatList, false);
    }
}
