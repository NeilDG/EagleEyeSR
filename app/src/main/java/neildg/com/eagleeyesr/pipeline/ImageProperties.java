package neildg.com.eagleeyesr.pipeline;

import neildg.com.eagleeyesr.platformtools.notifications.Parameters;

/**
 * Image properties file that serve as storage of attributes needed for assessing conditions of images by image workers.
 * Created by NeilDG on 12/10/2016.
 */

public class ImageProperties extends Parameters {
    private final static String TAG = "ImageProperties";

    public ImageProperties() {

    }

    public void clearAll() {
        this.objData.clear();
        this.intData.clear();
        this.boolData.clear();
        this.floatData.clear();
        this.doubleData.clear();
        this.stringData.clear();
    }

}
