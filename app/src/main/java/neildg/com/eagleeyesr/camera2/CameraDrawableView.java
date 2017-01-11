package neildg.com.eagleeyesr.camera2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import neildg.com.eagleeyesr.R;
import neildg.com.eagleeyesr.subroutine.CoroutineCreator;
import neildg.com.eagleeyesr.subroutine.ICoroutine;

/**
 * Overlay view for drawing textures on screen such as auto-focus region.
 * Created by NeilDG on 11/1/2016.
 */

public class CameraDrawableView extends TextureView implements TextureView.SurfaceTextureListener {
    private final static String TAG = "CameraDrawableView";

    private final static int FOCUS_OFFSET_X = 70;
    private final static int FOCUS_OFFSET_Y = 70;

    private boolean surfaceReady = false;

    private Bitmap focusBitmap;

    public CameraDrawableView(Context context) {
        super(context);
        this.setSurfaceTextureListener(this);
    }

    public CameraDrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setSurfaceTextureListener(this);
    }

    public CameraDrawableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setSurfaceTextureListener(this);
    }

    public CameraDrawableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surfaceReady = true;
        Log.d(TAG,"Surface is ready!");

        this.setOpaque(false);
        this.focusBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.horizon_indicator_aim);

        Canvas canvas = this.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.unlockCanvasAndPost(canvas);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        this.surfaceReady = false;
        if(this.focusBitmap != null) {
            this.focusBitmap.recycle();
            this.focusBitmap = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void drawFocusRegion(int x, int y, long duration) {

        this.invalidate();

        if(!this.surfaceReady || !this.isAvailable()) {
            return;
        }

        Canvas canvas = this.lockCanvas();

        if (canvas == null) {
            return;
        }

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(this.focusBitmap, x - FOCUS_OFFSET_X , y - FOCUS_OFFSET_Y, null);

        this.unlockCanvasAndPost(canvas);

        DelayHideFocus delayHideFocus = new DelayHideFocus(this);
        CoroutineCreator.getInstance().startCoroutine("DelayHideFocus", delayHideFocus, duration);

    }

    private class DelayHideFocus implements ICoroutine {

        private TextureView view;
        private Canvas canvas;

        public DelayHideFocus(TextureView view) {
            this.view = view;
            this.canvas = this.view.lockCanvas();
        }

        @Override
        public void perform() {

        }

        @Override
        public void onCoroutineStarted() {

        }

        @Override
        public void onCoroutineEnded() {
            this.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            this.view.unlockCanvasAndPost(this.canvas);
        }
    }
}
