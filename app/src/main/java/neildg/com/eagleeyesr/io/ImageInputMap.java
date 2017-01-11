package neildg.com.eagleeyesr.io;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the image input map from the bitmap uri repository.
 * Created by NeilDG on 12/28/2016.
 */

public class ImageInputMap {
    private final static String TAG = "ImageInputMap";
    private static ImageInputMap sharedInstance = new ImageInputMap();

    private List<String> imagePath = new ArrayList<>();

    private ImageInputMap() {

    }

    public static void setImagePath(List<Uri> imageURIList) {
        sharedInstance.imagePath.clear();
        for(int i = 0; i < imageURIList.size(); i++) {
            sharedInstance.imagePath.add(imageURIList.get(i).getPath());
        }
    }

    public static String getInputImage(int index) {
        Log.d(TAG, "Selected  path: " +sharedInstance.imagePath.get(index));
        return sharedInstance.imagePath.get(index);
    }

}
