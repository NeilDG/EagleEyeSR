package neildg.com.eagleeyesr.constants;

/**
 * Filename constants
 * Created by NeilDG on 3/5/2016.
 */
public class FilenameConstants {
    public final static  String GROUND_TRUTH_PREFIX_STRING = "groundtruth_";
    public final static String INPUT_PREFIX_STRING = "input_";
    public final static String INPUT_PREFIX_SHARPEN_STRING = "sharpen_";

    public final static String HR_NEAREST = "nearest";
    public final static String HR_CUBIC = "cubic";
    public final static String HR_LINEAR = "linear";
    public final static String HR_SUPERRES = "result";
    public final static String HR_ITERATION_PREFIX_STRING = "hr_initial_";

    public final static String WARP_PREFIX = "warp_";
    public final static String AFFINE_WARP_PREFIX = "affine_warp_";
    public final static String MEDIAN_ALIGNMENT_PREFIX = "median_align_";

    public final static String EDGE_DIRECTORY_PREFIX = "YangEdges";
    public final static String IMAGE_EDGE_PREFIX = "image_edge_";


    public final static String MATCHES_PREFIX_STRING = "refimage_matchto_";
    public final static String KEYPOINTS_STRING = "refimage_keypoint";

    public final static String METRICS_NAME_STRING = "psnr_metrics";

    //public final static String OPTICAL_FLOW_DIR = "optical_flow";
    //public final static String OPTICAL_FLOW_IMAGE_ORIG = "default_";
    //public final static String OPTICAL_FLOW_IMAGE_PREFIX = "flow_";

    //for single image SR
    public final static String PYRAMID_DIR = "pyramid";
    public final static String PYRAMID_IMAGE_PREFIX = "image_pyr_";
    public final static String RESULTS_DIR = "results";
    public final static String RESULTS_CUBIC = "prehr_cubic";
    public final static String RESULTS_GLASNER = "hr_glasner";
    public final static String RESULTS_GLASNER_SHARPEN = "hr_glasner_sharpen";

    //for gaussian single image
    public final static String INPUT_GAUSSIAN_DIR = "input";
    public final static String INPUT_FILE_NAME = "original";
    public final static String INPUT_BLUR_FILENAME = "blurred";

    //for debugging
    public final static String DEBUG_DIR = "debugging";
    public final static String MAT_VALUE_PREFIX = "mat_values_";

}
