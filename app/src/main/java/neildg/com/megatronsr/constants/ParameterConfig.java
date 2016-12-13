package neildg.com.megatronsr.constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by NeilDG on 3/5/2016.
 */
public class ParameterConfig {

    private final static String TAG = "ParameterConfig";
    private static ParameterConfig sharedInstance = null;

    public enum SRTechnique {
        SINGLE,
        MULTIPLE
    }

    private static String PARAMETER_PREFS = "parameter_config";
    private static String SCALE_KEY = "scale";

    public final static String DEBUGGING_FLAG_KEY = "DEBUGGING_FLAG_KEY";
    public final static String DENOISE_FLAG_KEY = "DENOISE_FLAG_KEY";
    public final static String FEATURE_MINIMUM_DISTANCE_KEY = "FEATURE_MINIMUM_DISTANCE_KEY";
    public final static String FUSION_THRESHOLD_KEY = "FUSION_THRESHOLD_KEY";
    public final static String WARP_CHOICE_KEY = "WARP_CHOICE_KEY";

    public final static int MAX_FUSION_THRESHOLD = 200;

    private SRTechnique currentTechnique = SRTechnique.MULTIPLE;

    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editorPrefs;

    private ParameterConfig(Context appContext) {
        this.sharedPrefs = appContext.getSharedPreferences(PARAMETER_PREFS, Context.MODE_PRIVATE);
        this.editorPrefs = this.sharedPrefs.edit();
    }

    public static void initialize(Context appContext) {
        if(sharedInstance == null) {
            sharedInstance = new ParameterConfig(appContext);
        }
    }

    public static void setScalingFactor(int scale) {
        sharedInstance.editorPrefs.putInt(SCALE_KEY, scale);
        sharedInstance.editorPrefs.commit();

        Log.d(TAG, "Scaling set to in prefs: " +getScalingFactor());
        //sharedInstance.scalingFactor = scale;
    }

    public static int getScalingFactor() {
        return sharedInstance.sharedPrefs.getInt(SCALE_KEY, 1);
    }

    public static void setTechnique(SRTechnique technique) {
        sharedInstance.currentTechnique = technique;
    }

    public static SRTechnique getCurrentTechnique() {
        return sharedInstance.currentTechnique;
    }

    public static void setPrefs(String key, boolean value) {
        sharedInstance.editorPrefs.putBoolean(key, value);
        sharedInstance.editorPrefs.commit();
    }

    public static void setPrefs(String key, int value) {
        sharedInstance.editorPrefs.putInt(key, value);
        sharedInstance.editorPrefs.commit();
    }

    public static void setPrefs(String key, float value) {
        sharedInstance.editorPrefs.putFloat(key, value);
        sharedInstance.editorPrefs.commit();
    }

    public static boolean getPrefsBoolean(String key, boolean defaultValue) {
        return sharedInstance.sharedPrefs.getBoolean(key, defaultValue);
    }

    public static int getPrefsInt(String key, int defaultValue) {
        return sharedInstance.sharedPrefs.getInt(key, defaultValue);
    }

    public static float getPrefsFloat(String key, float defaultValue) {
        return sharedInstance.sharedPrefs.getFloat(key, defaultValue);
    }
}
