package neildg.com.megatronsr.processing.multiple.resizing;

import android.graphics.Bitmap;

import org.opencv.core.Mat;
import org.opencv.photo.Photo;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageReader;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.processing.IOperator;

/**
 * Created by NeilDG on 3/5/2016.
 */
public class DownsamplingOperator implements IOperator {
    private static String TAG = "DownsamplingOperator";

    private int downsampleFactor = 2;
    private int numImagesSelected = 1;

    public DownsamplingOperator(int downsampleFactor, int numImagesSelected) {
        this.downsampleFactor = downsampleFactor;
        this.numImagesSelected = numImagesSelected;
    }

    public void perform() {
        ImageWriter imageWriter = ImageWriter.getInstance();
        BitmapURIRepository bitmapURIRepository = BitmapURIRepository.getInstance();

        //get bitmap ground-truth and transfer to debugging folder
        for(int i = 0; i < numImagesSelected; i++) {
            Bitmap bitmap = bitmapURIRepository.getOriginalBitmap(i);
            imageWriter.saveBitmapImage(bitmap, FilenameConstants.GROUND_TRUTH_PREFIX_STRING+i, ImageFileAttribute.FileType.JPEG);
            bitmap.recycle();
        }

        //TODO: Test. Denoise ground-truth image.
        /*Mat groundTruthMat = ImageReader.getInstance().imReadOpenCV(FilenameConstants.GROUND_TRUTH_PREFIX_STRING, ImageFileAttribute.FileType.JPEG);
        Photo.fastNlMeansDenoisingColored(groundTruthMat, groundTruthMat, 3, 3, 7, 21);
        ImageWriter.getInstance().saveMatrixToImage(groundTruthMat, FilenameConstants.GROUND_TRUTH_PREFIX_STRING + "_denoise", ImageFileAttribute.FileType.JPEG);*/

        for(int i = 0; i < numImagesSelected; i++) {
            Bitmap downsampledBitmap = bitmapURIRepository.getDownsampledBitmap(i, this.downsampleFactor);
            imageWriter.saveBitmapImage(downsampledBitmap, FilenameConstants.DOWNSAMPLE_PREFIX_STRING+i, ImageFileAttribute.FileType.JPEG);
            downsampledBitmap.recycle();
        }
    }
}
