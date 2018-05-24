package neildg.com.eagleeyesr.io;

import android.net.Uri;
import android.util.Log;

import java.io.File;
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
    private boolean hasUsedUri = false;

    private ImageInputMap() {

    }

    public static void setImagePathFromUri(List<Uri> imageURIList) {
        sharedInstance.imagePath.clear();
        for(int i = 0; i < imageURIList.size(); i++) {
            sharedInstance.imagePath.add(imageURIList.get(i).getPath());
        }

        sharedInstance.hasUsedUri = true;
    }

    public static void setImagePath(String[] imageList) {
        sharedInstance.imagePath.clear();
        for(int i = 0; i < imageList.length; i++) {
            sharedInstance.imagePath.add(imageList[i]);
        }

        sharedInstance.hasUsedUri = false;
    }

    /*
     * Deletes captured images from the camera module to conserve space.
     */
    public static void deletePlaceholderImages() {

        if(sharedInstance.hasUsedUri == true) {
            return;
        }

        for(int i = 0; i < sharedInstance.imagePath.size(); i++) {
            File dirFile = new File(sharedInstance.imagePath.get(i));
            FileImageWriter.deleteRecursive(dirFile);
            Log.i(TAG, "File " +dirFile.getAbsolutePath()+ " deleted.");
        }
    }

    public static String getInputImage(int index) {
        Log.d(TAG, "Selected  path: " +sharedInstance.imagePath.get(index));
        return sharedInstance.imagePath.get(index);
    }

    public static int numImages() {
        return sharedInstance.imagePath.size();
    }

}
