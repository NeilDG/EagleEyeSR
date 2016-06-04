package neildg.com.megatronsr.processing.multiple.fusion;

import org.opencv.core.Mat;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.multiple.workers.ZeroFillWorker;
import neildg.com.megatronsr.processing.imagetools.ImageOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/23/2016.
 */
public class ZeroFillFusionOperator implements IOperator {
    private final static String TAG = "ZeroFillFusionOperator";

    private List<Mat> warpedMatrixList = null;

    //private Mat groundTruthMat;
    private Mat outputMat;

    private Semaphore zeroFillSem;

    public  ZeroFillFusionOperator(List<Mat> warpedMatrixList) {
        this.warpedMatrixList = warpedMatrixList;
    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Upsampling warped images", "Performing zero-fill upsample to warped images");

        //this.groundTruthMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.GROUND_TRUTH_PREFIX_STRING, ImageFileAttribute.FileType.JPEG);
        this.outputMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.INITIAL_HR_PREFIX_STRING + 0, ImageFileAttribute.FileType.JPEG);

        List<ZeroFillWorker>  workerList = new LinkedList<>();
        List<Mat> hrWarpedList = new LinkedList<>();

        this.zeroFillSem = new Semaphore(0);
        this.zeroFillUpsize(workerList);

        try {
            this.zeroFillSem.acquire(this.warpedMatrixList.size());

            for(int i = 0; i < workerList.size(); i++) {
                hrWarpedList.add(workerList.get(i).getHrMat());
            }

            workerList.clear();

            //clear original matrix list
            for(int  i = 0; i < this.warpedMatrixList.size(); i++) {
                this.warpedMatrixList.get(i).release();
            }
            this.warpedMatrixList.clear();

            this.fuseImages(hrWarpedList);

        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        ProgressDialogHandler.getInstance().hideDialog();
    }

    private void zeroFillUpsize(List<ZeroFillWorker> workerList) {
        int scalingFactor = ParameterConfig.getScalingFactor();

        for(int i = 0; i < this.warpedMatrixList.size(); i++) {
            ZeroFillWorker zeroFillWorker = new ZeroFillWorker(this.warpedMatrixList.get(i), scalingFactor, 0, 0, this.zeroFillSem);
            zeroFillWorker.start();
            workerList.add(zeroFillWorker);
        }
    }

    private void fuseImages(List<Mat> hrWarpedMatrixList) {
        double threshold = Double.MAX_VALUE;

        List<Mat> trimmedHRList = new LinkedList<>();

        for(int i = 0; i < hrWarpedMatrixList.size(); i++) {
            ProgressDialogHandler.getInstance().showDialog("Fusing images", "Fusing image " +i);
            Mat baseWarpMat = hrWarpedMatrixList.get(i);

            double newRMSE = ImageOperator.measureRMSENoise(baseWarpMat);
            if(newRMSE <= threshold) {
                threshold = newRMSE;
                trimmedHRList.add(baseWarpMat);
            }
            else {
                baseWarpMat.release();
            }
        }

        ProgressDialogHandler.getInstance().showDialog("Fusing images", "Finalizing");
        this.outputMat = ImageOperator.blendImages(trimmedHRList);
        ImageWriter.getInstance().saveMatrixToImage(this.outputMat, FilenameConstants.HR_PROCESSED_STRING, ImageFileAttribute.FileType.JPEG);
        ProgressDialogHandler.getInstance().hideDialog();
    }


}
