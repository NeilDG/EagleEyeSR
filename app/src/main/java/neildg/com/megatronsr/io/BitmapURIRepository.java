package neildg.com.megatronsr.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import neildg.com.megatronsr.platformtools.core_application.ApplicationCore;

/**
 * Created by NeilDG on 3/5/2016.
 */
public class BitmapURIRepository {
    private final static String TAG = "BitmapURIRepository";
    private static BitmapURIRepository ourInstance = new BitmapURIRepository();

    public static BitmapURIRepository getInstance() {
        return ourInstance;
    }

    private List<Uri> selectedUriList;
    private Context context;

    private BitmapURIRepository() {
        this.context = ApplicationCore.getInstance().getAppContext();
    }

    public void setImageURIList(List<Uri> selectedUriList) {
        this.selectedUriList = selectedUriList;
    }

    public int getNumImagesSelected() {
        return this.selectedUriList.size();
    }

    public Bitmap getOriginalBitmap(int index) {
        return this.getDownsampledBitmap(index, 1);
    }

    public Bitmap getDownsampledBitmap(int index, int downsampleFactor) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options outDimens = getBitmapDimensions(this.selectedUriList.get(index));
            int sampleSize = calculateSampleSize(outDimens.outWidth, outDimens.outHeight, outDimens.outWidth / downsampleFactor, outDimens.outHeight / downsampleFactor);
            bitmap = this.downscaleBitmap(this.selectedUriList.get(index), sampleSize);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private BitmapFactory.Options getBitmapDimensions(Uri uri) throws FileNotFoundException, IOException {
        BitmapFactory.Options outDimens = new BitmapFactory.Options();
        outDimens.inJustDecodeBounds = true; // the decoder will return null (no bitmap)

        InputStream is= this.context.getContentResolver().openInputStream(uri);
        // if Options requested only the size will be returned
        BitmapFactory.decodeStream(is, null, outDimens);
        is.close();

        return outDimens;
    }

    private int calculateSampleSize(int width, int height, int targetWidth, int targetHeight) {
        int inSampleSize = 1;

        if (height > targetHeight || width > targetWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) targetHeight);
            final int widthRatio = Math.round((float) width / (float) targetWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    private Bitmap downscaleBitmap(Uri uri, int sampleSize) throws FileNotFoundException, IOException {
        Bitmap resizedBitmap;
        BitmapFactory.Options outBitmap = new BitmapFactory.Options();
        outBitmap.inJustDecodeBounds = false; // the decoder will return a bitmap
        outBitmap.inSampleSize = sampleSize;

        InputStream is = this.context.getContentResolver().openInputStream(uri);
        resizedBitmap = BitmapFactory.decodeStream(is, null, outBitmap);
        is.close();

        return resizedBitmap;
    }


}
