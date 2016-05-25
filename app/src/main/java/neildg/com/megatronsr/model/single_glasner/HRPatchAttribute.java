package neildg.com.megatronsr.model.single_glasner;

import org.opencv.core.Mat;

/**
 * Created by NeilDG on 5/19/2016.
 */
public class HRPatchAttribute {
    private final static String TAG = "HRPatchAttribute";

    private Mat patchMat;
    private String imageName;

    private int colStart = 0;
    private int rowStart = 0;
    private int colEnd = 0;
    private int rowEnd = 0;

    public HRPatchAttribute(int colStart, int rowStart, int colEnd, int rowEnd, String imageName, Mat patchMat) {
        this.imageName = imageName;
        this.patchMat = patchMat;

        this.colStart = colStart;
        this.rowStart = rowStart;
        this.colEnd = colEnd;
        this.rowEnd = rowEnd;
    }

    public Mat getPatchMat() {
        return this.patchMat;
    }

    public void releaseMat() {
        this.patchMat.release();
        this.patchMat = null;
    }

    public void assignPatchMat(Mat patchMat) {
        this.patchMat = patchMat;
    }

    public int getColStart() {
        return this.colStart;
    }

    public int getRowStart() {
        return this.rowStart;
    }

    public int getColEnd() {
        return this.colEnd;
    }
    public int getRowEnd() {
        return this.rowEnd;
    }
}
