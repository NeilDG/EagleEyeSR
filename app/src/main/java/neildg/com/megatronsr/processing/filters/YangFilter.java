package neildg.com.megatronsr.processing.filters;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.Arrays;

import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Implements the yang filter edge features on a set of images
 * Created by NeilDG on 7/17/2016.
 */
public class YangFilter implements IOperator {
    private static String TAG = "YangFilter";

    private Mat[] inputMatList;

    private Mat f1Kernel;
    private Mat f2Kernel;
    private Mat f3Kernel;
    private Mat f4Kernel;

    public YangFilter(Mat[] inputMatList) {
        this.inputMatList = inputMatList;

        Integer[] f1 = new Integer[]{-1, 0, 1};
        Integer[] f2 = new Integer[]{-1, 0, 1}; //transpose
        Integer[] f3 = new Integer[]{-1, 0, 2, 0, -1};
        Integer[] f4 = new Integer[]{-1, 0, 2, 0, -1}; ///transpose

        this.f1Kernel = Converters.vector_int_to_Mat(Arrays.asList(f1));
        this.f2Kernel =  Converters.vector_int_to_Mat(Arrays.asList(f2));
        Core.transpose(this.f2Kernel, this.f2Kernel);

        this.f3Kernel = Converters.vector_int_to_Mat(Arrays.asList(f3));
        this.f4Kernel =  Converters.vector_int_to_Mat(Arrays.asList(f4));
        Core.transpose(this.f4Kernel, this.f4Kernel);
    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Feature detection", "Extracting edge features");

        for(int i = 0; i < this.inputMatList.length; i++) {
            Mat inputf1 = new Mat(); Mat inputf2 = new Mat(); Mat inputf3 = new Mat(); Mat inputf4 = new Mat();
            Imgproc.filter2D(this.inputMatList[i], inputf1, this.inputMatList[i].depth(), this.f1Kernel);
            ImageWriter.getInstance().saveMatrixToImage(inputf1, "YangEdges", "image_f1_"+i, ImageFileAttribute.FileType.JPEG);

            Imgproc.filter2D(this.inputMatList[i], inputf2, this.inputMatList[i].depth(), this.f2Kernel);
            ImageWriter.getInstance().saveMatrixToImage(inputf2, "YangEdges", "image_f2_"+i, ImageFileAttribute.FileType.JPEG);

            Imgproc.filter2D(this.inputMatList[i], inputf3, this.inputMatList[i].depth(), this.f3Kernel);
            ImageWriter.getInstance().saveMatrixToImage(inputf3, "YangEdges", "image_f3_"+i, ImageFileAttribute.FileType.JPEG);

            Imgproc.filter2D(this.inputMatList[i], inputf4, this.inputMatList[i].depth(), this.f4Kernel);
            ImageWriter.getInstance().saveMatrixToImage(inputf4, "YangEdges", "image_f4_"+i, ImageFileAttribute.FileType.JPEG);

            inputf1.release();
            inputf2.release();
            inputf3.release();
            inputf4.release();
        }
        ProgressDialogHandler.getInstance().hideDialog();
    }
}
