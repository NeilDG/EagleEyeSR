package neildg.com.megatronsr.model;

import android.util.Log;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by NeilDG on 5/4/2016.
 */
public class AttributeHolder {
    private final static String TAG = "AttributeHolder";
    private static AttributeHolder sharedInstance = null;
    public static AttributeHolder getSharedInstance() {
        if(sharedInstance == null) {
            sharedInstance = new AttributeHolder();
        }

        return sharedInstance;
    }

    private HashMap<String, Object> valueTable = new HashMap<String, Object>();

    public void putValue(String key, Object value) {
        if(this.valueTable.containsKey(key)) {
            Log.e(TAG, key + "already exists in table!");
        }
        else {
            this.valueTable.put(key, value);
        }
    }

    public Object getValue(String key, Object defaultValue) {
        if(this.valueTable.containsKey(key)) {
            return this.valueTable.get(key);
        }
        else {
            return defaultValue;
        }
    }
}
