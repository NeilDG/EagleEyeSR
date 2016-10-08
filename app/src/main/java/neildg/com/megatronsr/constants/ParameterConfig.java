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

    //private int scalingFactor = 1;

    private static String PARAMETER_PREFS = "parameter_config";
    private static String SCALE_KEY = "scale";
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
}
