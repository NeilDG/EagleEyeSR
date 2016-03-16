package neildg.com.megatronsr.metrics;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neil.dg on 3/16/16.
 */
public class ImageMetrics {

    public static double getPSNR(Mat I1, Mat I2) {
        Mat s1 = new Mat();
        Core.absdiff(I1, I2, s1);       // |I1 - I2|

        s1.convertTo(s1, CvType.CV_32F);  // cannot make a square on 8 bits
        s1 = s1.mul(s1);           // |I1 - I2|^2

        Scalar s = Core.sumElems(s1);         // sum elements per channel

        double sse = s.val[0] + s.val[1] + s.val[2]; // sum channels

        if (sse <= 1e-10) // for small values return zero
            return 0;
        else
        {
            double  mse =sse /(double)(I1.channels() * I1.total());
            double psnr = 10.0*Math.log10((255*255)/mse);
            return psnr;
        }
    }
}
