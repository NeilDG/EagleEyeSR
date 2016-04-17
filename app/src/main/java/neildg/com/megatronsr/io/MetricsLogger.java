package neildg.com.megatronsr.io;

import android.util.JsonWriter;
import android.util.Log;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Hashtable;

import neildg.com.megatronsr.metrics.ImageMetrics;
import neildg.com.megatronsr.metrics.MetricsSnapshot;

/**
 * Created by neil.dg on 3/16/16.
 */
public class MetricsLogger {
    private final static String TAG = "MetricsLogger";

    private static MetricsLogger sharedInstance = null;
    public static MetricsLogger getSharedInstance() {
        if(sharedInstance == null) {
            sharedInstance = new MetricsLogger();
        }

        return sharedInstance;
    }

    private Hashtable<String, MetricsSnapshot> psnrTable = new Hashtable<String, MetricsSnapshot>();

    private MetricsLogger() {

    }

    public void takeMetrics(String key, Mat m1, String m1Name, Mat m2, String m2Name, String description) {
        if(this.psnrTable.containsKey(key)) {
            Log.e(TAG, key + " is already in the psnr table.");
        }
        else {
            MetricsSnapshot snapshot = new MetricsSnapshot(m1Name, m2Name, ImageMetrics.getRMSE(m1,m2), ImageMetrics.getPSNR(m1,m2), description);
            this.psnrTable.put(key, snapshot);
        }
    }

    public void debugPSNRTable() {
        for(MetricsSnapshot snapshot : this.psnrTable.values()) {
            snapshot.print();
        }
    }

    public void logResultsToJSON(String fileName) {

        File jsonFile = new File(DirectoryStorage.getSharedInstance().getProposedPath(), fileName + ".json");
        try {
            FileOutputStream out = new FileOutputStream(jsonFile);
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out));
            jsonWriter.setIndent("  ");

            this.writeTable(jsonWriter);
            jsonWriter.flush();
            jsonWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    private void writeTable(JsonWriter writer) throws IOException {

        writer.beginArray();
        for(String key: this.psnrTable.keySet()) {
            writer.beginObject();

            writer.name(key).value(key);

            MetricsSnapshot snapshot = this.psnrTable.get(key);
            writer.name("description").value(snapshot.getDescription());
            writer.name("mat1").value(snapshot.getMat1Name());
            writer.name("mat2").value(snapshot.getMat2Name());
            writer.name("rmse").value(snapshot.getRMSE());
            writer.name("psnr").value(snapshot.getPSNR());
            writer.name("ssim").value(snapshot.getSSIMInfo());
            writer.endObject();
        }

        writer.endArray();
    }

    public void clear() {
        this.psnrTable.clear();
    }
}
