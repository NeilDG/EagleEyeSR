package neildg.com.megatronsr.constants;

/**
 * Created by NeilDG on 3/5/2016.
 */
public class ParameterConfig {

    private static ParameterConfig sharedInstance = null;

    private int scalingFactor = 1;

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
}
