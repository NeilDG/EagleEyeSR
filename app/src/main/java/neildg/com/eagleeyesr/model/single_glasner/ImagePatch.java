package neildg.com.eagleeyesr.model.single_glasner;

import android.util.Log;

import org.opencv.core.Mat;

import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageReader;

/**
 * Represents an image patch
 * Created by neil.dg on 5/5/16.
 */
public class ImagePatch {
    private final static String TAG = "ImagePatch";

    private String imageName;
    private String imagePath;
    private Mat patchMat = null;


    public ImagePatch(String imageName, String imagePath) {
        this.imageName = imageName;
        this.imagePath = imagePath;
    }

    public String getImageName() {
        return this.imageName;
    }

    public Mat getPatchMat() {
        this.loadPatchMatIfNull();
        return this.patchMat;
    }

    public void loadPatchMatIfNull() {
      if(this.patchMat == null) {
          this.patchMat = FileImageReader.getInstance().imReadOpenCV(this.imagePath, ImageFileAttribute.FileType.JPEG);
          Log.d(TAG, "Patch "+this.imagePath+ " loaded! Size: " +this.patchMat.elemSize()+ " Cols: " +this.patchMat.cols()+ " Rows: " +this.patchMat.rows());
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
