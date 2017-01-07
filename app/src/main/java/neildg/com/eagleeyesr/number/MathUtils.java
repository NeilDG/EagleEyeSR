package neildg.com.eagleeyesr.number;

/**
 * Created by NeilDG on 5/13/2016.
 */
public class MathUtils {

    public static int clamp(int value, int minValue, int maxValue) {
        if(value > minValue && value < maxValue) {
            return value;
        }
        else if(value == minValue) {
            return minValue;
        }
        else {
            return maxValue;
        }
    }
}
