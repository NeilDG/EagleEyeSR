/**
 * 
 */
package neildg.com.megatronsr.config.values;


/**
 * Implemention of config for multiple image processing
 * @author NeilDG
 *
 */
public class MultipleImageConfig extends BaseConfig {
	
	@Override
	protected void configure() {
		this.imageLimit = 10;
		this.shutterDelay = 100;
		
		this.defaultWidth = DefaultConfigValues.DEFAULT_CAMERA_WIDTH;
		this.defaultHeight = DefaultConfigValues.DEFAULT_CAMERA_HEIGHT;
	}
	
}
