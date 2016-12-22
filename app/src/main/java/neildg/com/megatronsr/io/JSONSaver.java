package neildg.com.megatronsr.io;

import android.util.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import neildg.com.megatronsr.model.single_glasner.PatchAttribute;
import neildg.com.megatronsr.model.single_glasner.PatchRelation;
import neildg.com.megatronsr.model.single_glasner.PatchRelationTable;
import neildg.com.megatronsr.processing.multiple.alignment.WarpingConstants;

/**
 * Created by NeilDG on 5/14/2016.
 */
public class JSONSaver {
    private final static String TAG = "JSONSaver";

    public static void writeSimilarPatches(String fileName, HashMap<PatchAttribute, PatchRelationTable.PatchRelationList> pairwiseTable) {
        File jsonFile = new File(DirectoryStorage.getSharedInstance().getProposedPath(), fileName + ".json");
        try {
            FileOutputStream out = new FileOutputStream(jsonFile);
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out));
            jsonWriter.setIndent("  ");


            //begin writing
            /*JSONObject jsonObject = mapToJson(pairwiseTable);
            jsonWriter.beginArray();
            jsonWriter.value(jsonObject.toString());
            jsonWriter.endArray();*/

            jsonWriter.beginArray();
            for(HashMap.Entry<PatchAttribute, PatchRelationTable.PatchRelationList> entry: pairwiseTable.entrySet()) {
                PatchAttribute lrPatchAttrib = entry.getKey();
                PatchRelationTable.PatchRelationList patchRelationList = entry.getValue();

                jsonWriter.beginObject();
                jsonWriter.name("lr_patch_key").value(lrPatchAttrib.getImageName());

                for(int i = 0; i < patchRelationList.getCount(); i++) {
                    PatchRelation patchRelation = patchRelationList.getPatchRelationAt(i);
                    PatchAttribute lrAttrib = patchRelation.getLrAttrib();
                    PatchAttribute hrAttrib = patchRelation.getHrAttrib();

                    jsonWriter.name(""+i).beginObject();
                    jsonWriter.name("lr").value(lrAttrib.getImageName());
                    jsonWriter.name("hr").value(hrAttrib.getImageName());
                    jsonWriter.name("similarity").value(patchRelation.getSimilarity());
                    jsonWriter.endObject();
                }

                jsonWriter.endObject();
            }
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


    /*
     * Writes the edge consistency measure in a file for further comparison.
     */
    public static void debugWriteEdgeConsistencyMeasure(int warpMethod, int[] warpedResults, int[] warpResultDifferences, String[] warpedMatNames) {
        String fileName = "no_warp_specified";
        if(warpMethod == WarpingConstants.AFFINE_WARP) {
            fileName = "affine_warp_result";
        }
        else if(warpMethod == WarpingConstants.PERSPECTIVE_WARP) {
            fileName = "perspective_warp_result";
        }
        else if(warpMethod == WarpingConstants.MEDIAN_ALIGNMENT) {
            fileName = "exposure_alignment_result";
        }

        File jsonFile = new File(DirectoryStorage.getSharedInstance().getProposedPath(), fileName + ".json");
        try {
            FileOutputStream out = new FileOutputStream(jsonFile);
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out));
            jsonWriter.setIndent("  ");

            jsonWriter.beginArray();
            for(int i = 0; i < warpedMatNames.length; i++) {
                jsonWriter.beginObject();

                jsonWriter.name(warpedMatNames[i]).value(warpedResults[i]);

                jsonWriter.endObject();
            }

            for(int i = 0; i < warpedMatNames.length; i++) {
                jsonWriter.beginObject();

                jsonWriter.name(warpedMatNames[i] + " difference from reference").value(warpResultDifferences[i]);

                jsonWriter.endObject();
            }

            jsonWriter.endArray();
            jsonWriter.flush();
            jsonWriter.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {

        }
    }
}
