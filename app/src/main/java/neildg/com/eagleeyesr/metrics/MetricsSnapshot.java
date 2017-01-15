package neildg.com.eagleeyesr.metrics;

import android.util.Log;

import org.opencv.core.Scalar;

/**
 * Created by neil.dg on 3/16/16.
 */
public class MetricsSnapshot {
    private final static String TAG ="MetricsSnapshot";

    private String mat1Name;
    private String mat2Name;

    private double rmse;
    private double psnr;
    private Scalar ssim;

    private String description;

    public MetricsSnapshot(String mat1Name, String mat2Name, double rmse, double psnr, String description) {
        this.mat1Name = mat1Name;
        this.mat2Name = mat2Name;
        this.rmse = rmse;
        this.psnr = psnr;

        this.description = description;
    }

    public void setSSIM(Scalar ssim) {
        this.ssim = ssim;
    }

    public String getMat1Name() {
        return this.mat1Name;
    }

    public String getMat2Name() {
        return this.mat2Name;
    }

    public double getRMSE() {return this.rmse;}
    public double getPSNR() {
        return this.psnr;
    }

    public Scalar getSSIM() {
        return ssim;
    }

    public String getDescription() {
        return this.description;
    }

    public void print() {
        Log.d(TAG, this.summarize());
    }

    public String summarize() {
        return (this.mat1Name + " || " +this.mat2Name+ "  == PSNR: " +this.psnr + "  | Description: " +description);
    }

    public String getSSIMInfo() {
        if(this.ssim != null) {
            return ("SSIM || B: "+this.ssim.val[0]+ " G: " +this.ssim.val[1]+ " R: " +this.ssim.val[2]);
        }
        else {
            return ("SSIM not computed.");
        }
    }
}
