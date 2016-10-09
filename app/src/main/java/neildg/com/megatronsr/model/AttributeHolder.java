package neildg.com.megatronsr.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.w3c.dom.Attr;

import java.util.HashMap;
import java.util.Objects;

import neildg.com.megatronsr.constants.ParameterConfig;

/**
 * Created by NeilDG on 5/4/2016.
 */
public class AttributeHolder {
    private final static String TAG = "AttributeHolder";
    private static AttributeHolder sharedInstance = null;
    public static AttributeHolder getSharedInstance() {
        return sharedInstance;
    }

    private static String ATTRIBUTE_PREFS = "attribute_holder_prefs";
    //private HashMap<String, Object> valueTable = new HashMap<String, Object>();

    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editorPrefs;


    public static void initialize(Context appContext) {
        if(sharedInstance == null) {
            sharedInstance = new AttributeHolder(appContext);
        }
    }

    private AttributeHolder(Context appContext) {
        this.sharedPrefs = appContext.getSharedPreferences(ATTRIBUTE_PREFS, Context.MODE_PRIVATE);
        this.editorPrefs = this.sharedPrefs.edit();

    }

    public void putValue(String key, int value) {
        /*if(this.valueTable.containsKey(key)) {
            Log.e(TAG, key + " already exists in table!");
        }
        else {
            this.valueTable.put(key, value);
        }*/
        this.editorPrefs.putInt(key, value);
        this.editorPrefs.commit();
        Log.d(TAG, "Value added: " +this.getValue(key, -1));
    }

    public void putValue(String key, float value) {
        /*if(this.valueTable.containsKey(key)) {
            Log.e(TAG, key + " already exists in table!");
        }
        else {
            this.valueTable.put(key, value);
        }*/
        this.editorPrefs.putFloat(key, value);
        this.editorPrefs.commit();
        Log.d(TAG, "Value added: " +this.getValueFloat(key, -1.0f));
    }

    /*public Object getValue(String key, Object defaultValue) {
        if(this.valueTable.containsKey(key)) {
            return this.valueTable.get(key);
        }
        else {
            return defaultValue;
        }
        return this.sharedPrefs.getString(key, "");
    }*/

    public int getValue(String key, int defaultValue) {
        return this.sharedPrefs.getInt(key, defaultValue);
    }

    public float getValueFloat(String key, float defaultValue) {
       return this.sharedPrefs.getFloat(key, defaultValue);
    }


    public void reset() {
       // this.valueTable.clear();
       this.editorPrefs.clear();
    }
}
