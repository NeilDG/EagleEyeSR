package neildg.com.megatronsr.model.single;

import org.opencv.core.Core;
import org.opencv.core.Mat;
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

    private String imagePath;
    private Mat patchMat = null;


    public ImagePatch(int col, int row, String imagePath) {
        this.col = 0;
        this.row = 0;
        this.imagePath = imagePath;
    }

    public Mat getPatchMat() {
        this.loadPatchMatIfNull();
        return this.patchMat;
    }

    private void loadPatchMatIfNull() {
      if(this.patchMat == null) {
          this.patchMat = ImageReader.getInstance().imReadOpenCV(this.imagePath, ImageFileAttribute.FileType.JPEG);
      }
    }

    public boolean isLoadedToMemory() {
        return (this.patchMat != null);
    }

    public void releaseMat() {
        if(this.patchMat != null) {
            this.patchMat.release();
        }
    }

    public static double measureSimilarity(ImagePatch patch1, ImagePatch patch2) {
        Mat resultMat = new Mat();
        Imgproc.matchTemplate(patch1.getPatchMat(), patch2.getPatchMat(), resultMat,Imgproc.TM_SQDIFF_NORMED);

        double value = Core.norm(resultMat, Core.NORM_L1);
        resultMat.release();

        return value;
    }

}
