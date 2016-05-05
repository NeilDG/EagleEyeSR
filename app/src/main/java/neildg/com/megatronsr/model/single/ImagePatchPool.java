package neildg.com.megatronsr.model.single;

/**
 * Created by neil.dg on 5/5/16.
 */
public class ImagePatchPool {
    private final static String TAG = "ImagePatchPool";
    private static ImagePatchPool sharedInstance = new ImagePatchPool();

    public static ImagePatchPool getInstance() {
        return sharedInstance;
    }

    public static void initialize() {
        sharedInstance = new ImagePatchPool();
    }

    public static void destroy() {
        sharedInstance = null;
    }

    public static final int MAX_LOADED_PATCHES = 200;
    
    private int loadedPatches = 0;

    private ImagePatchPool() {

    }
}
