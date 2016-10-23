package neildg.com.megatronsr.camera2;

import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Handles the rendering of the camera surface
 * Created by NeilDG on 10/23/2016.
 */

public class CameraTextureView implements TextureView.SurfaceTextureListener {
    private final static String TAG = "CameraSurfaceView";

    public static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private TextureView textureView;
    private ICameraTextureViewListener textureViewListener;

    public CameraTextureView(TextureView textureView, ICameraTextureViewListener textureViewListener) {
        this.textureView = textureView;
        this.textureView.setSurfaceTextureListener(this);
        this.textureViewListener = textureViewListener;

    }

    public TextureView getTextureView() {
        return this.textureView;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        //send success event to custom listener
        this.textureViewListener.onCameraTextureViewAvailable(this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Transform you image captured size according to the surface width and height
        Log.d(TAG, "On texture size changed: " +width+ "X" +height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void updateAspectRatio(Size size) {
        Log.d(TAG, "Image dimensions proposed: " +size.getWidth() + " X " +size.getHeight());
        this.textureView.setLayoutParams(new RelativeLayout.LayoutParams(size.getWidth(), size.getHeight()));
    }

    public void updateToOptimalSize(Size[] sizes) {
        Size optimalSize = getOptimalPreviewSize(sizes, this.textureView.getWidth(), this.textureView.getHeight());
        //Size optimalSize = new Size(1000, 1000);
        this.updateAspectRatio(optimalSize);
    }

    public static Size getOptimalPreviewSize(Size[] sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }
}
