package neildg.com.megatronsr.constants;

/**
 * Created by NeilDG on 3/5/2016.
 */
public class ParameterConfig {

    private static ParameterConfig sharedInstance = null;

    public enum SRTechnique {
        SINGLE,
        MULTIPLE
    }

    private int scalingFactor = 1;
    private SRTechnique currentTechnique = SRTechnique.MULTIPLE;

    private ParameterConfig() {}

    private static void initialize() {
        if(sharedInstance == null) {
            sharedInstance = new ParameterConfig();
        }
    }

    public static void setScalingFactor(int scale) {
        initialize();
        sharedInstance.scalingFactor = scale;
    }

    public static int getScalingFactor() {
        initialize();
        return sharedInstance.scalingFactor;
    }

    public static void setTechnique(SRTechnique technique) {
        initialize();
        sharedInstance.currentTechnique = technique;
    }

    public static SRTechnique getCurrentTechnique() {
        initialize();
        return sharedInstance.currentTechnique;
    }
}
