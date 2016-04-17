package neildg.com.megatronsr.processing;

import android.graphics.Bitmap;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.preprocessing.BitmapURIRepository;

/**
 * Created by NeilDG on 3/5/2016.
 */
public class DownsamplingOperator {
    private static String TAG = "DownsamplingOperator";

    private int downsampleFactor = 2;

    public DownsamplingOperator(int downsampleFactor) {
        this.downsampleFactor = downsampleFactor;
    }

    public void perform() {
        ImageWriter imageWriter = ImageWriter.getInstance();
        BitmapURIRepository bitmapURIRepository = BitmapURIRepository.getInstance();

        //get bitmap ground-truth and transfer to debugging folder
        Bitmap bitmap = bitmapURIRepository.getDownsampledBitmap(0, 1);
        imageWriter.saveBitmapImage(bitmap, FilenameConstants.GROUND_TRUTH_PREFIX_STRING);

        for(int i = 0; i < BitmapURIRepository.getInstance().getNumImages(); i++) {
            Bitmap downsampledBitmap = bitmapURIRepository.getDownsampledBitmap(i, this.downsampleFactor);
            imageWriter.saveBitmapImage(downsampledBitmap, FilenameConstants.DOWNSAMPLE_PREFIX_STRING+i);
            downsampledBitmap.recycle();
        }
    }
}
