package neildg.com.megatronsr.processing;

import android.graphics.Bitmap;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.io.BitmapURIRepository;

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
        Bitmap bitmap = bitmapURIRepository.getOriginalBitmap(0);
        imageWriter.saveBitmapImage(bitmap, FilenameConstants.GROUND_TRUTH_PREFIX_STRING, ImageFileAttribute.FileType.JPEG);

        for(int i = 0; i < numImagesSelected; i++) {
            Bitmap downsampledBitmap = bitmapURIRepository.getDownsampledBitmap(i, this.downsampleFactor);
            imageWriter.saveBitmapImage(downsampledBitmap, FilenameConstants.DOWNSAMPLE_PREFIX_STRING+i, ImageFileAttribute.FileType.JPEG);
            downsampledBitmap.recycle();
        }
    }
}
