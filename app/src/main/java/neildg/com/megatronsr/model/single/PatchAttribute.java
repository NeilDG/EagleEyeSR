package neildg.com.megatronsr.model.single;

/**
 * Patch attributes are stored here for convenience on retrieving and formulating of image patch matrices.
 * Created by NeilDG on 5/10/2016.
 */
public class PatchAttribute {
    private final static String TAG = "PatchAttribute";

    private int pyramidDepth = 0;
    private int colStart = 0;
    private int rowStart = 0;
    private int colEnd = 0;
    private int rowEnd = 0;
    private String imageName;
    private String imagePath;

    public PatchAttribute(int pyramidDepth, int colStart, int rowStart, int colEnd, int rowEnd, String imageName, String imagePath) {
        this.pyramidDepth = pyramidDepth;
        this.colStart = colStart;
        this.rowStart = rowStart;
        this.colEnd = colEnd;
        this.rowEnd = rowEnd;
        this.imageName = imageName;
        this.imagePath = imagePath;
    }


    public int getPyramidDepth() {
        return pyramidDepth;
    }

    public int getColStart() {
        return this.colStart;
    }

    public int getRowStart() {
        return this.rowStart;
    }

    public int getColEnd() {
        return this.colEnd;
    }
    public int getRowEnd() {
        return this.rowEnd;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImagePath() {
        return imagePath;
    }
}
