package neildg.com.eagleeyesr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import net.sourceforge.opencamera.OpenCameraActivity;
import net.sourceforge.opencamera.external_bridge.IEvent;
import net.sourceforge.opencamera.external_bridge.ImageSaveBroadcaster;

import neildg.com.eagleeyesr.camera2.CameraUserSettings;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.pipeline.ProcessingQueue;
import neildg.com.eagleeyesr.platformtools.notifications.NotificationCenter;
import neildg.com.eagleeyesr.platformtools.notifications.NotificationListener;
import neildg.com.eagleeyesr.platformtools.notifications.Notifications;
import neildg.com.eagleeyesr.platformtools.notifications.Parameters;
import neildg.com.eagleeyesr.threads.CaptureSRProcessor;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;
import neildg.com.eagleeyesr.ui.views.OptionsScreen;
import neildg.com.eagleeyesr.ui.views.ProcessingQueueScreen;

/**
 * Extends the OpenCamera activity for addition of UIs and some references to SR code
 * Created by NeilDG on 1/7/2017.
 */

public class NewCameraActivity extends OpenCameraActivity implements IEvent {
    private final static String TAG = "NewCameraActivity";
    //overlay views
    private ProcessingQueueScreen processingQueueScreen;
    private OptionsScreen optionsScreen;

    private CaptureSRProcessor srProcessor = new CaptureSRProcessor(); //thread that performs super-resolution.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.inflateCustomLayouts();
        this.initializeButtons();
        this.filterUneededViews();

        ProcessingQueue.initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        this.srProcessor.startBackgroundThread();

        ProgressDialogHandler.initialize(this);
        ImageSaveBroadcaster.getSharedInstance().addEvent(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ProgressDialogHandler.destroy();
        ImageSaveBroadcaster.getSharedInstance().removeEvent(this);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        CameraUserSettings.destroy();
        this.srProcessor.stopBackgroundThread();
        ProcessingQueue.destroy();
    }

    private void inflateCustomLayouts() {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View optionsView = inflater.inflate(R.layout.overlay_options_layout, null);
        ViewGroup parentView = (ViewGroup) this.findViewById(R.id.opencamera_overlay_frame);
        parentView.addView(optionsView);

        View processingQueueView = inflater.inflate(R.layout.processing_queue_view, null);
        parentView.addView(processingQueueView);

        this.optionsScreen = new OptionsScreen(optionsView);
        this.optionsScreen.initialize();
        this.optionsScreen.hide();

        //create container for processing queue view
        ProgressBar processingQueueBar = (ProgressBar) this.findViewById(R.id.processing_bar);
        this.processingQueueScreen = new ProcessingQueueScreen(processingQueueView, processingQueueBar, this);
        this.processingQueueScreen.initialize();
        this.processingQueueScreen.hide();
    }

    private void initializeButtons() {
        ImageButton modeBtn = (ImageButton) this.findViewById(R.id.settings);
        modeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View optionsOverlayView = NewCameraActivity.this.findViewById(R.id.options_overlay_layout);
                optionsOverlayView.setVisibility(View.VISIBLE);
            }
        });

        ImageButton imagePreviewBtn = (ImageButton) this.findViewById(R.id.gallery);
        imagePreviewBtn.setVisibility(View.GONE); //show the image thumbnail when the HD image is available
    }

    private void filterUneededViews() {
        View switchVideoButton = this.findViewById(R.id.switch_video);
        switchVideoButton.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(this.optionsScreen != null && this.optionsScreen.isShown()) {
            this.optionsScreen.hide();
        }
        else if(this.processingQueueScreen != null && this.processingQueueScreen.isShown()) {
            this.processingQueueScreen.hide();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void clickedGallery(View view) {
        Intent previewIntent = new Intent(NewCameraActivity.this,ImageViewActivity.class);
        startActivity(previewIntent);
    }

    @Override
    public void onReceivedEvent() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ImageButton imageButton = (ImageButton) NewCameraActivity.this.findViewById(R.id.btn_image_preview);
                Bitmap thumbnailBmp = FileImageReader.getInstance().loadBitmapThumbnail(ProcessingQueue.getInstance().getLatestImageName(), ImageFileAttribute.FileType.JPEG, 300, 300);
                imageButton.setImageBitmap(thumbnailBmp);
                imageButton.setEnabled(true);
            }
        });

        NotificationCenter.getInstance().postNotification(Notifications.ON_IMAGE_ENQUEUED);
        NotificationCenter.getInstance().postNotification(Notifications.ON_SR_AWAKE);
    }
}
