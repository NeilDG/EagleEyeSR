package neildg.com.eagleeyesr.io;

/**
 * Created by NeilDG on 4/21/2016.
 */
public class ImageFileAttribute {

    public final static String JPEG_EXT = ".jpg";
    public final static String PNG_EXT = ".png";

    public enum FileType {
        JPEG,
        PNG
    }

    public static String getFileExtension(FileType fileType) {
        if(fileType == FileType.JPEG) {
            return JPEG_EXT;
        }
        else if(fileType == FileType.PNG) {
            return PNG_EXT;
        }
        else {
            return JPEG_EXT;
        }
    }
}
