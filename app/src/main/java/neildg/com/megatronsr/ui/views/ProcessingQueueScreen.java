package neildg.com.megatronsr.ui.views;

import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import neildg.com.megatronsr.CameraActivity;
import neildg.com.megatronsr.R;

/**
 * UI container and model for the processing queue view.
 * CONVENTION: Classes that have "Screen" suffix do not extend the View class of android.
 * Otherwise, it has the "View" suffix.
 * Created by NeilDG on 12/3/2016.
 */

public class ProcessingQueueScreen extends AViewStubScreen {
    private final static String TAG = "ProcessingQueueScreen";

    private View processingQueueView;

    public ProcessingQueueScreen(ViewStub viewStub, boolean makeVisible) {
        super(viewStub, makeVisible);
    }

    @Override
    public void initialize() {
        ImageButton processingCloseBtn = (ImageButton) this.referenceView.findViewById(R.id.processing_close_btn);
        processingCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingQueueScreen.this.referenceView.setVisibility(View.INVISIBLE);
            }
        });
    }
}
