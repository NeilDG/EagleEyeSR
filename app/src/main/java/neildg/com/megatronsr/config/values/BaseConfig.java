/**
 * 
 */
package neildg.com.megatronsr.config.values;


/**
 * The base config class. Use this to create customized config class via inheritance.
 * @author NeilDG
 *
 */
public abstract class BaseConfig {
	private final static String TAG = "CameraEnhance_BaseConfig";
	
	protected long shutterDelay = DefaultConfigValues.SHUTTER_DELAY;	//delay between capture represented in milliseconds
	protected int imageLimit = DefaultConfigValues.NUM_IMAGES_TO_CAPTURE;	//number of images to capture
	protected int defaultWidth = DefaultConfigValues.DEFAULT_CAMERA_WIDTH;	//default camera width
	protected int defaultHeight = DefaultConfigValues.DEFAULT_CAMERA_HEIGHT; //default camera height
	
	public BaseConfig() {
		this.configure();
	}
	
	protected abstract void configure();
	
	public long getShutterDelay() {
		return this.shutterDelay = 0;
	}
	
	public int getImageLimit() {
		return this.imageLimit;
	}
	
	public int getCameraWidth() {
		return this.defaultWidth;
	}
	
	public int getCameraHeight() {
		return this.defaultHeight;
	}
}
