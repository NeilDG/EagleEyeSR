package neildg.com.eagleeyesr.model.single_gaussian;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Similar to an image patch implementation but this time, the mat is a submat of an original mat
 * Created by NeilDG on 5/23/2016.
 */
public class LoadedImagePatch{
    private final static String TAG = "LoadedImagePatch";

    private Mat patchMat;

    private int colStart;
    private int rowStart;
    private int rowEnd;
    private int colEnd;

    /*
     * Submat is extracted from the parent mat
     */
    public LoadedImagePatch(Mat parentMat, int patchSize, int colStart, int rowStart) {
        Point point = new Point(colStart, rowStart);
        Size size = new Size(patchSize, patchSize);

        this.patchMat = Mat.zeros(size,parentMat.type());

        Imgproc.getRectSubPix(parentMat, size, point, this.patchMat);

        this.colStart = colStart;
        this.rowStart = rowStart;
        this.rowEnd = this.rowStart + patchSize;
        this.colEnd = this.colStart + patchSize;
    }

    public int getColStart() {
        return this.colStart;
    }

    public int getRowEnd() {
        return this.rowEnd;
    }

    public int getRowStart() {
        return this.rowStart;
    }

    public int getColEnd() {
        return this.colEnd;
    }

    public Mat getPatchMat() {
        return this.patchMat;
    }

    public void release() {
        patchMat.release();
        patchMat = null;
    }

}
