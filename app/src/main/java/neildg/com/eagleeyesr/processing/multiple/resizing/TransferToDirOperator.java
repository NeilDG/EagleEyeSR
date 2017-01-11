package neildg.com.eagleeyesr.processing.multiple.resizing;

import android.graphics.Bitmap;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.io.BitmapURIRepository;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.processing.IOperator;

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
        //imageWriter.recreateDirectory();
        BitmapURIRepository bitmapURIRepository = BitmapURIRepository.getInstance();

        for(int i = 0; i < numImagesSelected; i++) {
            Bitmap downsampledBitmap = bitmapURIRepository.getDownsampledBitmap(i, 1);
            imageWriter.saveBitmapImage(downsampledBitmap, FilenameConstants.INPUT_PREFIX_STRING +i, ImageFileAttribute.FileType.JPEG);
            downsampledBitmap.recycle();
        }
    }
}
