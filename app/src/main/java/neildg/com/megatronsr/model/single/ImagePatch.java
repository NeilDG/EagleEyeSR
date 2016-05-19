package neildg.com.megatronsr.model.single;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;

/**
 * Represents an image patch
 * Created by neil.dg on 5/5/16.
 */
public class ImagePatch {
    private final static String TAG = "ImagePatch";

    private int col = 0;
    private int row = 0;
    private int patchSize = 0;
    private String imageName;

    private Mat parentMat = null;
    private Mat patchMat = null;


    public ImagePatch(int col, int row, int patchSize, String imageName, Mat parentMat) {
        this.col = 0;
        this.row = 0;
        this.imageName = imageName;
        this.patchSize = patchSize;
        this.parentMat = parentMat;
    }

    public String getImageName() {
        return this.imageName;
    }

    public Mat getPatchMat() {
        this.loadPatchMatIfNull();
        return this.patchMat;
    }

    public void loadPatchMatIfNull() {
      /*if(this.patchMat == null) {
          this.patchMat = ImageReader.getInstance().imReadOpenCV(this.imagePath, ImageFileAttribute.FileType.JPEG);
          Log.d(TAG, "Patch "+this.imagePath+ " loaded! Size: " +this.patchMat.elemSize()+ " Cols: " +this.patchMat.cols()+ " Rows: " +this.patchMat.rows());
      }*/
        if(this.patchMat == null) {
            this.patchMat = new Mat();
            Point point = new Point(this.col, this.row);
            Imgproc.getRectSubPix(this.parentMat, new Size(this.patchSize, this.patchSize), point, this.patchMat);
        }
    }

    public boolean isLoadedToMemory() {
        return (this.patchMat != null);
    }

    public void releaseMat() {
        if(this.patchMat != null) {
            this.patchMat.release();
            this.patchMat = null;
        }
    }

}
