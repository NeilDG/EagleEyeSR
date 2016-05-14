package neildg.com.megatronsr.io;

import android.util.JsonWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import neildg.com.megatronsr.metrics.MetricsSnapshot;
import neildg.com.megatronsr.model.single.PatchAttribute;

/**
 * Created by NeilDG on 5/14/2016.
 */
public class JSONSaver {
    private final static String TAG = "JSONSaver";

    public static void writeSimilarPatches(String fileName, HashMap<PatchAttribute, PatchAttribute> pairwiseTable) {
        File jsonFile = new File(DirectoryStorage.getSharedInstance().getProposedPath(), fileName + ".json");
        try {
            FileOutputStream out = new FileOutputStream(jsonFile);
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out));
            jsonWriter.setIndent("  ");


            //begin writing
            JSONObject jsonObject = mapToJson(pairwiseTable);
            jsonWriter.beginArray();
            jsonWriter.value(jsonObject.toString());
            jsonWriter.endArray();

            jsonWriter.flush();
            jsonWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject mapToJson(HashMap<PatchAttribute, PatchAttribute> data)
    {
        JSONObject object = new JSONObject();

        for (HashMap.Entry<PatchAttribute, PatchAttribute>  entry : data.entrySet())
        {
            /*
             * Deviate from the original by checking that keys are non-null and
             * of the proper type. (We still defer validating the values).
             */
            PatchAttribute lrPatchAttrib = entry.getKey();
            PatchAttribute hrPatchAttrib = entry.getValue();

            String key = lrPatchAttrib.getImageName();

            if (key == null)
            {
                throw new NullPointerException("key == null");
            }
            try
            {
                object.put(lrPatchAttrib.getImageName(), hrPatchAttrib.getImageName());
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        return object;
    }
}
