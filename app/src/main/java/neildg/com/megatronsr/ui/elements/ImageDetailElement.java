package neildg.com.megatronsr.ui.elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.ImageFileAttribute;

/**
 * An image detail element model to be used by the queue adapter.
 * Created by NeilDG on 12/3/2016.
 */

public class ImageDetailElement{
    private final static String TAG = "ImageDetailElement";

    private Bitmap thumbnailBmp;
    private String imageName;

    public ImageDetailElement() {

    }

    public void setup(String inputImageName) {
        this.imageName = inputImageName;

        this.thumbnailBmp = FileImageReader.getInstance().loadBitmapThumbnail(this.imageName, ImageFileAttribute.FileType.JPEG, 70, 70);
        Log.d(TAG, "Queue: Thumbnail bmp details: " +this.imageName);

    }

    public void destroy() {
       if(this.thumbnailBmp != null) {
           this.thumbnailBmp.recycle();
           this.thumbnailBmp = null;
       }
    }

    public String getImageName() {
        return this.imageName;
    }
    public Bitmap getThumbnail() {
        return this.thumbnailBmp;
    }


}
