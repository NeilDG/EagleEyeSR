package neildg.com.megatronsr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.model.multiple.ProcessingQueue;
import neildg.com.megatronsr.processing.listeners.IProcessListener;
import neildg.com.megatronsr.threads.ReleaseSRProcessor;
import neildg.com.megatronsr.threads.SingleImageSRProcessor;
import neildg.com.megatronsr.ui.ProgressDialogHandler;
import neildg.com.megatronsr.ui.views.OptionsScreen;

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

        ProgressDialogHandler.initialize(this);

        this.initializeOverlayViews();
        this.initializeButtons();

    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView numImagesText = (TextView) this.findViewById(R.id.num_images_txt);
        numImagesText.setText(Integer.toString(BitmapURIRepository.getInstance().getNumImagesSelected()));

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

        //temporarily disable results button
        Button imageViewBtn = (Button) this.findViewById(R.id.image_results_view_btn);
        imageViewBtn.setEnabled(false);

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
            ReleaseSRProcessor releaseSRProcessor = new ReleaseSRProcessor(this);
            releaseSRProcessor.start();
        }
        else {
            new SingleImageSRProcessor().start();
        }
    }

    @Override
    public void onProcessCompleted() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button imageViewBtn = (Button) ProcessingActivityRelease.this.findViewById(R.id.image_results_view_btn);
                imageViewBtn.setEnabled(true);
            }
        });
    }
}
