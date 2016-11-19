package neildg.com.megatronsr.processing.multiple.resizing;

import android.graphics.Bitmap;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.processing.IOperator;

/**
 * Similar to downsampling operator but transfers files instead
 * Created by NeilDG on 9/10/2016.
 */
public class TransferToDirOperator implements IOperator {
    private final static String TAG = "TransferToDirOperator";

    private int numImagesSelected;

    public TransferToDirOperator(int numImagesSelected) {
        this.numImagesSelected = numImagesSelected;
    }

    @Override
    public void perform() {
        FileImageWriter imageWriter = FileImageWriter.getInstance();
        BitmapURIRepository bitmapURIRepository = BitmapURIRepository.getInstance();

        for(int i = 0; i < numImagesSelected; i++) {
            Bitmap downsampledBitmap = bitmapURIRepository.getDownsampledBitmap(i, 1);
            imageWriter.saveBitmapImage(downsampledBitmap, FilenameConstants.INPUT_PREFIX_STRING +i, ImageFileAttribute.FileType.JPEG);
            downsampledBitmap.recycle();
        }
    }
}
