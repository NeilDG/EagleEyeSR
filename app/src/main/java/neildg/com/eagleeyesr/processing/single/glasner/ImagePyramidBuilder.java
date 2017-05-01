package neildg.com.eagleeyesr.processing.single.glasner;

import android.graphics.Bitmap;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.io.BitmapURIRepository;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.model.AttributeHolder;
import neildg.com.eagleeyesr.model.AttributeNames;
import neildg.com.eagleeyesr.processing.IOperator;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;

/**
 * Created by NeilDG on 5/4/2016.
 */
public class ImagePyramidBuilder implements IOperator {
    private final static String TAG = "ImagePyramidBuilder";

    public ImagePyramidBuilder() {

    }

    @Override
    public void perform() {
        ProgressDialogHandler.getInstance().showProcessDialog("Creating Image Pyramid", "Building downscale image pyramid");
        Bitmap originalBitmap = BitmapURIRepository.getInstance().getOriginalBitmap(0);
        FileImageWriter.getInstance().saveBitmapImage(originalBitmap, FilenameConstants.PYRAMID_DIR, FilenameConstants.PYRAMID_IMAGE_PREFIX + "0", ImageFileAttribute.FileType.JPEG);

        int maxDownscale = (int) AttributeHolder.getSharedInstance().getValue(AttributeNames.MAX_PYRAMID_DEPTH_KEY, 0);
        for(int i = 1; i <= maxDownscale; i++) {
            Bitmap bitmap = BitmapURIRepository.getInstance().getDownsampledBitmap(0, (int) Math.pow(2, i));
            FileImageWriter.getInstance().saveBitmapImage(bitmap, FilenameConstants.PYRAMID_DIR, FilenameConstants.PYRAMID_IMAGE_PREFIX + i, ImageFileAttribute.FileType.JPEG);
        }

        ProgressDialogHandler.getInstance().hideProcessDialog();
    }
}
