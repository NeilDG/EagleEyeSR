package neildg.com.eagleeyesr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import neildg.com.eagleeyesr.constants.BuildMode;
import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.BitmapURIRepository;
import neildg.com.eagleeyesr.io.DirectoryStorage;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.platformtools.notifications.NotificationCenter;
import neildg.com.eagleeyesr.platformtools.notifications.Notifications;
import neildg.com.eagleeyesr.processing.process_observer.IProcessListener;
import neildg.com.eagleeyesr.processing.process_observer.SRProcessManager;
import neildg.com.eagleeyesr.threads.DebugSRProcessor;
import neildg.com.eagleeyesr.threads.ReleaseSRProcessor;
import neildg.com.eagleeyesr.threads.SingleImageSRProcessor;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;
import neildg.com.eagleeyesr.ui.views.OptionsScreen;

/*
 * Activity to use for release mode
 * By: NeilDG
 */
public class ProcessingActivityRelease extends AppCompatActivity implements IProcessListener {

    private OptionsScreen optionsScreen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing_release);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ProgressDialogHandler.initialize(this);

        this.initializeOverlayViews();
        this.initializeButtons();

    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView numImagesText = (TextView) this.findViewById(R.id.num_images_txt);
        numImagesText.setText(Integer.toString(BitmapURIRepository.getInstance().getNumImagesSelected()));

        ProgressDialogHandler.getInstance().setDefaultProgressImplementor();
        SRProcessManager.getInstance().setProcessListener(this, this);

        this.updateImageViewStatus();
    }

    @Override
    protected void onDestroy() {
        ProgressDialogHandler.destroy();
        super.onDestroy();
    }

    private void initializeButtons() {
        Button srButton = (Button) this.findViewById(R.id.perform_sr_btn);
        srButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingActivityRelease.this.executeSRProcessor();
            }
        });

        Button imageViewBtn = (Button) this.findViewById(R.id.image_results_view_btn);
        imageViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(ProcessingActivityRelease.this, ImageViewActivity.class);
                startActivity(previewIntent);
            }
        });

        Button settinsBtn = (Button) this.findViewById(R.id.settings_btn);
        settinsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingActivityRelease.this.optionsScreen.show();
            }
        });

    }

    private void updateImageViewStatus() {
        Button imageViewBtn = (Button) this.findViewById(R.id.image_results_view_btn);
        imageViewBtn.setEnabled(FileImageReader.getInstance().doesImageExists(FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG));
    }

    private void initializeOverlayViews() {
        //create container for options view
        this.optionsScreen = new OptionsScreen(this.findViewById(R.id.options_overlay_layout));
        this.optionsScreen.initialize();
        this.optionsScreen.hide();
    }

    @Override
    public void onBackPressed() {
        if(this.optionsScreen != null && this.optionsScreen.isShown()) {
            this.optionsScreen.hide();
        }
        else {
            super.onBackPressed();
        }
    }


    private void executeSRProcessor() {
        if(ParameterConfig.getCurrentTechnique() == ParameterConfig.SRTechnique.MULTIPLE) {

            if(BuildMode.DEVELOPMENT_BUILD == true) {
                DebugSRProcessor debugSRProcessor = new DebugSRProcessor(this);
                debugSRProcessor.start();
            }
            else {
                ReleaseSRProcessor releaseSRProcessor = new ReleaseSRProcessor();
                releaseSRProcessor.start();
            }

        }
        else {
            new SingleImageSRProcessor().start();
        }
    }

    @Override
    public void onProcessCompleted() {
       this.updateImageViewStatus();
        ///automatically start image preview
        Intent previewIntent = new Intent(ProcessingActivityRelease.this, ImageViewActivity.class);
        startActivity(previewIntent);
    }

    @Override
    public void onProducedInitialHR() {
        //start image preview intent for initial viewing
        Intent previewIntent = new Intent(ProcessingActivityRelease.this, ImageViewActivity.class);
        startActivity(previewIntent);
    }
}
