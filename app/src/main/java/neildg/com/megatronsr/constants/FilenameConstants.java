package neildg.com.megatronsr.constants;

/**
 * Created by NeilDG on 3/5/2016.
 */
public class FilenameConstants {
    public final static  String GROUND_TRUTH_PREFIX_STRING = "groundtruth";
    public final static String DOWNSAMPLE_PREFIX_STRING = "downsample_";
    public final static String HR_PROCESSED_STRING = "result";

    public final static String INITIAL_HR_NEAREST = "prehr_nearest";
    public final static String INITIAL_HR_PREFIX_STRING = "prehr_intercubic_";
    public final static String INITIAL_HR_ZERO_FILLED_STRING = "prehr_zerofill";

    public final static String MATCHES_PREFIX_STRING = "refimage_matchto_";
    public final static String KEYPOINTS_STRING = "refimage_keypoint";

    public final static String METRICS_NAME_STRING = "psnr_metrics";

    //for single image SR
    public final static String PYRAMID_DIR = "pyramid";
    public final static String PYRAMID_IMAGE_PREFIX = "image_pyr_";
    public final static String RESULTS_DIR = "results";
    public final static String RESULTS_CUBIC = "prehr_cubic";
    public final static String RESULTS_GLASNER = "hr_glasner";
    public final static String RESULTS_GLASNER_SHARPEN = "hr_glasner_sharpen";

}
