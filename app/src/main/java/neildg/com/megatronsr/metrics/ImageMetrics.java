package neildg.com.megatronsr.metrics;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neil.dg on 3/16/16.
 */
public class ImageMetrics {

    public static double getPSNR(Mat I1, Mat I2) {
            double mse = getMSE(I1, I2);
            double psnr = 10.0*Math.log10((255*255)/mse);
            return psnr;
    }

    public static double getMSE(Mat I1, Mat I2) {
        Mat s1 = new Mat();
        Core.absdiff(I1, I2, s1);       // |I1 - I2|

        s1.convertTo(s1, CvType.CV_32F);  // cannot make a square on 8 bits
        s1 = s1.mul(s1);           // |I1 - I2|^2

        Scalar s = Core.sumElems(s1);         // sum elements per channel
        s1.release();

        double sse = s.val[0] + s.val[1] + s.val[2]; // sum channels

        if (sse <= 1e-10) // for small values return zero
            return 0;
        else
        {
            double  mse =sse /(double)(I1.channels() * I1.total());
            return mse;
        }
    }

    public static double getRMSE(Mat I1, Mat I2) {
        double mse = getMSE(I1,I2);
        return  Math.sqrt(mse);
    }

    public static Scalar getSSIM(Mat i1, Mat i2) {
        final double C1 = 6.5025, C2 = 58.5225;

        Mat I1 = new Mat(); Mat I2 = new Mat();
        i1.convertTo(I1, CvType.CV_32F);
        i2.convertTo(I2, CvType.CV_32F);

        Mat I1_2 = I1.mul(I1);
        Mat I2_2 = I2.mul(I2);
        Mat I1_I2 = I1.mul(I2);

        /*
        Mat mu1, mu2;   //
        GaussianBlur(I1, mu1, Size(11, 11), 1.5);
        GaussianBlur(I2, mu2, Size(11, 11), 1.5);

        Mat mu1_2   =   mu1.mul(mu1);
        Mat mu2_2   =   mu2.mul(mu2);
        Mat mu1_mu2 =   mu1.mul(mu2);

        Mat sigma1_2, sigma2_2, sigma12;

        GaussianBlur(I1_2, sigma1_2, Size(11, 11), 1.5);
        sigma1_2 -= mu1_2;

        GaussianBlur(I2_2, sigma2_2, Size(11, 11), 1.5);
        sigma2_2 -= mu2_2;

        GaussianBlur(I1_I2, sigma12, Size(11, 11), 1.5);
        sigma12 -= mu1_mu2;
         */

        Mat mu1 = new Mat(); Mat mu2 = new Mat();
        Imgproc.GaussianBlur(I1, mu1, new Size(11,11), 1.5);
        Imgproc.GaussianBlur(I2, mu2, new Size(11,11), 1.5);

        I1.release(); I2.release();

        Mat mu1_2   =   mu1.mul(mu1);
        Mat mu2_2   =   mu2.mul(mu2);
        Mat mu1_mu2 =   mu1.mul(mu2);

        Mat sigma1_2 = new Mat(); Mat sigma2_2 = new Mat(); Mat sigma12 = new Mat();

        Imgproc.GaussianBlur(I1_2, sigma1_2, new Size(11, 11), 1.5);
        Core.subtract(sigma1_2, mu1_2, sigma1_2);

        Imgproc.GaussianBlur(I2_2, sigma2_2, new Size(11, 11), 1.5);
        Core.subtract(sigma2_2, mu2_2, sigma2_2);

        Imgproc.GaussianBlur(I1_I2, sigma12, new Size(11, 11), 1.5);
        Core.subtract(sigma12, mu1_mu2, sigma12);

        /*
 Mat t1, t2, t3;

 t1 = 2 * mu1_mu2 + C1;
 t2 = 2 * sigma12 + C2;
 t3 = t1.mul(t2);              // t3 = ((2*mu1_mu2 + C1).*(2*sigma12 + C2))

 t1 = mu1_2 + mu2_2 + C1;
 t2 = sigma1_2 + sigma2_2 + C2;
 t1 = t1.mul(t2);               // t1 =((mu1_2 + mu2_2 + C1).*(sigma1_2 + sigma2_2 + C2))

 Mat ssim_map;
 divide(t3, t1, ssim_map);      // ssim_map =  t3./t1;

 Scalar mssim = mean( ssim_map ); // mssim = average of ssim map
 return mssim;
         */

        Mat t1 = new Mat(); Mat t2 = new Mat(); Mat t3 = new Mat();
        Scalar twoScalar = Scalar.all(2);
        Scalar c1Scalar = Scalar.all(C1); Scalar c2Scalar = Scalar.all(C2);

        Core.multiply(mu1_mu2, twoScalar, t1);
        Core.add(t1, c1Scalar, t1);
        Core.multiply(sigma12, twoScalar, t2);
        Core.add(t2, c2Scalar, t2);
        t3 = t1.mul(t2);

        Core.add(mu1_2, mu2_2, t1); Core.add(t1,c1Scalar,t1);
        Core.add(sigma1_2, sigma2_2, t2); Core.add(t2, c2Scalar, t2);
        t1 = t1.mul(t2);

        mu1_2.release(); mu2_2.release(); sigma1_2.release(); sigma2_2.release();

        Mat ssim_map = new Mat();
        Core.divide(t3,t1,ssim_map);

        Scalar mssim = Scalar.all(0); mssim = Core.mean(ssim_map);

        t3.release(); t1.release(); ssim_map.release();
        return mssim;

    }

}
