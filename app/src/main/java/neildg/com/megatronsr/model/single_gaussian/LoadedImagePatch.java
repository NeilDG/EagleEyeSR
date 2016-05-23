package neildg.com.megatronsr.model.single_gaussian;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.model.single_glasner.ImagePatch;

/**
 * Similar to an image patch implementation but this time, the mat is a submat of an original mat
 * Created by NeilDG on 5/23/2016.
 */
public class LoadedImagePatch{
    private final static String TAG = "LoadedImagePatch";

    private Mat patchMat;

    private int rowStart;
    private int colStart;
    private int rowEnd;
    private int colEnd;

    /*
     * Submat is extracted from the parent mat
     */
    public LoadedImagePatch(Mat parentMat, Size patchSize, int rowStart, int colStart) {
        this.patchMat = new Mat();
        Point point = new Point(rowStart, colStart);
        Imgproc.getRectSubPix(parentMat, patchSize, point, this.patchMat);

        this.rowStart = rowStart;
        this.colStart = colStart;
        this.rowEnd = this.rowStart + (int) patchSize.width;
        this.colEnd = this.colEnd + (int) patchSize.height;
    }

    public int getRowStart() {
        return this.rowStart;
    }

    public int getRowEnd() {
        return this.rowEnd;
    }

    public int getColStart() {
        return this.colStart;
    }

    public int getColEnd() {
        return this.colEnd;
    }

    public Mat getPatchMat() {
        return this.patchMat;
    }

}
