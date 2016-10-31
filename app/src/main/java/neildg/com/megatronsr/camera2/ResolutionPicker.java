package neildg.com.megatronsr.camera2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;

/**
 * Stores available resolution upon request
 * Created by NeilDG on 10/23/2016.
 */

public class ResolutionPicker {
    private final static String TAG = "ResolutionPicker";

    private static ResolutionPicker sharedInstance = null;
    public static  ResolutionPicker getSharedInstance() {
        return sharedInstance;
    }

    private Size[] availableCameraSizes;
    private Size[] availableThumbnailSizes;

    private ResolutionPicker() {

    }

    private void setAvailableCameraSizes(Size[] availableCameraSizes, Size[] availableThumbnailSizes) {
        this.availableCameraSizes = availableCameraSizes;
        this.availableThumbnailSizes = availableThumbnailSizes;
    }

    public Size[] getAvailableCameraSizes() {
        return this.availableCameraSizes;
    }

    public int getAvailableSizeCount() {
        return this.availableCameraSizes.length;
    }

    public Size getLastAvailableSize() {
        return this.availableCameraSizes[this.availableCameraSizes.length - 1];
    }

    public Size getFirstAvailableSize() {
        return this.availableCameraSizes[0];
    }

    public Size getAvailableSizeAt(int index) {
        return this.availableCameraSizes[index];
    }

    public Size getLastAvailableThumbnailSize() {
        return this.availableThumbnailSizes[this.availableThumbnailSizes.length - 1];
    }

    /*
     * Updates available camera sizes. Also creates a new singleton class.
     */
    public static void updateCameraSettings(Context context, String cameraID) {
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);


            sharedInstance = null;
            sharedInstance = new ResolutionPicker();

            sharedInstance.setAvailableCameraSizes(map.getOutputSizes(ImageFormat.JPEG), characteristics.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES));
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
