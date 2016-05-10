package neildg.com.megatronsr.model.single;

/**
 * Patch attributes are stored here for convenience on retrieving and formulating of image patch matrices.
 * Created by NeilDG on 5/10/2016.
 */
public class PatchAttribute {
    private final static String TAG = "PatchAttribute";

    private int pyramidDepth = 0;
    private int col = 0;
    private int row = 0;
    private String imageName;
    private String imagePath;

    public PatchAttribute(int pyramidDepth, int col, int row, String imageName, String imagePath) {
        this.pyramidDepth = pyramidDepth;
        this.col = col;
        this.row = row;
        this.imageName = imageName;
        this.imagePath = imagePath;
    }


    public int getPyramidDepth() {
        return pyramidDepth;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImagePath() {
        return imagePath;
    }
}
