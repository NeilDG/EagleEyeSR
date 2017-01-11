/**
 * 
 */
package neildg.com.eagleeyesr.platformtools.notifications;

import java.util.HashMap;

/**
 * Represents parameters required by the handled notification event.
 * This is now applicable for NotificationCenter V2.0
 * @author NeilDG
 *
 */
public class Parameters {

	protected HashMap<String, Object> objData = new HashMap<String, Object>();
	protected HashMap<String, Integer> intData = new HashMap<String, Integer>();
	protected HashMap<String, Boolean> boolData = new  HashMap<String, Boolean>();
	protected HashMap<String, Float> floatData = new HashMap<String, Float>();
	protected HashMap<String, Double> doubleData = new HashMap<String, Double>();
	protected HashMap<String, String> stringData = new HashMap<String, String>();
	
	public void putExtra(String key, int data){
		this.intData.put(key, data);
	}
	
	public void putExtra(String key, boolean data) {
		this.boolData.put(key, data);
	}
	
	public void putExtra(String key, float data) {
		this.floatData.put(key, data);
	}
	
	public void putExtra(String key, double data) {
		this.doubleData.put(key, data);
	}
	
	public void putExtra(String key, String data) {
		this.stringData.put(key, data);
	}
	
	public void putExtra(String key, Object data) {
		this.objData.put(key, data);
	}
	
	public Object getObjectExtra(String key, Object defaultValue) {
		if(this.objData.containsKey(key)) {
			return this.objData.get(key);
		}
		else {
			return defaultValue;
		}
	}
	
	public int getIntExtra(String key, int defaultValue) {
		if(this.intData.containsKey(key)) {
			return this.intData.get(key);
		}
		else {
			return defaultValue;
		}
	}
	
	public boolean getBooleanExtra(String key, boolean defaultValue) {
		if(this.boolData.containsKey(key)) {
			return this.boolData.get(key);
		}
		else {
			return defaultValue;
		}
	}
	
	public float getFloatExtra(String key, float defaultValue) {
		if(this.floatData.containsKey(key)) {
			return this.floatData.get(key);
		}
		else {
			return defaultValue;
		}
	}
	
	public double getDoubleExtra(String key, double defaultValue) {
		if(this.doubleData.containsKey(key)) {
			return this.doubleData.get(key);
		}
		else {
			return defaultValue;
		}
	}
	
	public String getStringExtra(String key, String defaultValue) {
		if(this.stringData.containsKey(key)) {
			return this.stringData.get(key);
		}
		else {
			return defaultValue;
		}
	}
	
}
