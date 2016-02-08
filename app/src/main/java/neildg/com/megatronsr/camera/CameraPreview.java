package neildg.com.megatronsr.camera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private final static String TAG = "CameraPreview";
	
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private DrawingView drawingView;

    private AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            if (arg0){
                mCamera.cancelAutoFocus();      
            }
        }
    };
    
    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    public void assignCamera(Camera camera) {
    	this.mCamera = camera;
    	
    	// Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    public void assignDrawingView(DrawingView view) {
    	this.drawingView = view;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            OldCameraManager.getInstance().grantCapturePermission();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    
    public void updateCameraSource(Camera camera) {
    	this.mCamera = camera;
    	
    	try {
    		this.mCamera.stopPreview();
    	}
    	catch(Exception e) {
    		
    	}
    	
    	try {
    		this.mCamera.setPreviewDisplay(this.mHolder);
    		this.mCamera.startPreview();
    		OldCameraManager.getInstance().grantCapturePermission();
    	} catch(Exception e) {
    		Log.e(TAG, "Error starting camera preview: " +e.getMessage());
    	}
    }
    
    /*
     * Function for focus upon touch on view
     */
    public void focusOn(Rect focusRect) {
    	try {
    		
            List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(focusRect, 1000);
            focusList.add(focusArea);
      
            Camera.Parameters param = mCamera.getParameters();
            param.setFocusAreas(focusList);
            param.setMeteringAreas(focusList);
            mCamera.setParameters(param);
      
            mCamera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Unable to autofocus");
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(event.getAction() == MotionEvent.ACTION_DOWN) {
    		float x = event.getX();
            float y = event.getY();
      
            Rect touchRect = new Rect(
                (int)(x - 100), 
                (int)(y - 100), 
                (int)(x + 100), 
                (int)(y + 100));
            

            final Rect targetFocusRect = new Rect(
                touchRect.left * 2000/this.getWidth() - 1000,
                touchRect.top * 2000/this.getHeight() - 1000,
                touchRect.right * 2000/this.getWidth() - 1000,
                touchRect.bottom * 2000/this.getHeight() - 1000);
      
            this.focusOn(targetFocusRect);
            this.drawingView.setHaveTouch(true, touchRect);
            this.drawingView.invalidate();
      
            // Remove the square indicator after 1000 msec
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
        
                @Override
                public void run() {
                    drawingView.setHaveTouch(false, new Rect(0,0,0,0));
                    drawingView.invalidate();
                }
            }, 1000);
    	}
    	
    	return true;
    }
    
}
