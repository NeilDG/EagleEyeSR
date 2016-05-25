package neildg.com.megatronsr.processing.single.glasner;

import android.graphics.Bitmap;

import java.io.File;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.io.ImageWriter;
import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/4/2016.
 */
public class ImagePyramidBuilder implements IOperator {
    private final static String TAG = "ImagePyramidBuilder";

    public ImagePyramidBuilder() {

    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showDialog("Creating Image Pyramid", "Building downscale image pyramid");
        Bitmap originalBitmap = BitmapURIRepository.getInstance().getOriginalBitmap(0);
        ImageWriter.getInstance().saveBitmapImage(originalBitmap, FilenameConstants.PYRAMID_DIR, FilenameConstants.PYRAMID_IMAGE_PREFIX + "0", ImageFileAttribute.FileType.JPEG);

        int maxDownscale = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);
        for(int i = 1; i <= maxDownscale; i++) {
            Bitmap bitmap = BitmapURIRepository.getInstance().getDownsampledBitmap(0, (int) Math.pow(2, i));
            ImageWriter.getInstance().saveBitmapImage(bitmap, FilenameConstants.PYRAMID_DIR, FilenameConstants.PYRAMID_IMAGE_PREFIX + i, ImageFileAttribute.FileType.JPEG);
        }

        ProgressDialogHandler.getInstance().hideDialog();
    }
}
