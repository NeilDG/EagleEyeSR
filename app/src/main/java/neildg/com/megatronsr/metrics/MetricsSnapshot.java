package neildg.com.megatronsr.metrics;

import android.util.Log;

/**
 * Created by neil.dg on 3/16/16.
 */
public class MetricsSnapshot {
    private final static String TAG ="MetricsSnapshot";

    private String mat1Name;
    private String mat2Name;

    private double psnr;

    private String description;

    public MetricsSnapshot(String mat1Name, String mat2Name, double psnr, String description) {
        this.mat1Name = mat1Name;
        this.mat2Name = mat2Name;
        this.psnr = psnr;
        this.description = description;
    }

    public String getMat1Name() {
        return this.mat1Name;
    }

    public String getMat2Name() {
        return this.mat2Name;
    }

    public double getPSNR() {
        return this.psnr;
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
}
