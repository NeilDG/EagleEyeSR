/**
 * 
 */
package neildg.com.megatronsr.config;

import android.util.Log;

import neildg.com.megatronsr.config.values.BaseConfig;
import neildg.com.megatronsr.config.values.MultipleImageConfig;


/**
 * Class that loads the specified config value
 * @author NeilDG
 *
 */
public class ConfigHandler {
	private final static String TAG = "SR_ConfigHandler";
	
	private static ConfigHandler sharedInstance = null;
	public static ConfigHandler getInstance() {
		if(sharedInstance == null) {
			sharedInstance = new ConfigHandler();
		}
		
		return sharedInstance;
	}
	
	private BaseConfig assignedConfig;
	
	private ConfigHandler() {
		//change the configuration here
		this.assignedConfig = new MultipleImageConfig();
		
		Log.d(TAG, "Initialized config. Image limit:  "+this.assignedConfig.getImageLimit()+ " Shutter delay:" +this.assignedConfig.getShutterDelay() + "Width: " +this.assignedConfig.getCameraWidth()
				+ " Height: " +this.assignedConfig.getCameraHeight());
	}
	
	public BaseConfig getCurrentConfig() {
		return this.assignedConfig;
	}
}
