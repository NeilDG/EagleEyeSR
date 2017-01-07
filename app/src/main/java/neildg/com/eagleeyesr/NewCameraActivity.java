package neildg.com.eagleeyesr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import net.sourceforge.opencamera.OpenCameraActivity;

import neildg.com.eagleeyesr.ui.views.OptionsScreen;
import neildg.com.eagleeyesr.ui.views.ProcessingQueueScreen;

/**
 * Extends the OpenCamera activity for addition of UIs and some references to SR code
 * Created by NeilDG on 1/7/2017.
 */

public class NewCameraActivity extends OpenCameraActivity {

    //overlay views
    private ProcessingQueueScreen processingQueueScreen;
    private OptionsScreen optionsScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.inflateCustomLayouts();
        this.initializeButtons();
        this.filterUneededViews();
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
}
