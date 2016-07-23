package neildg.com.megatronsr.model.multiple;

import org.opencv.core.Mat;

/**
 * Displacement value class used by optical flow.
 * Created by NeilDG on 7/23/2016.
 */
public class DisplacementValue {
    private Mat xPoints;
    private Mat yPoints;

    public DisplacementValue(Mat xPoints, Mat yPoints) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
    }

    public Mat getXPoints() {
        return this.xPoints;
    }

    public Mat getYPoints() {
        return this.yPoints;
    }
}