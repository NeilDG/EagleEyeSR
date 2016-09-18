package neildg.com.megatronsr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.threads.ReleaseSRProcessor;
import neildg.com.megatronsr.threads.SingleImageSRProcessor;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/*
 * Activity to use for release mode
 * By: NeilDG
 */
public class ProcessingActivityRelease extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing_release);

        ProgressDialogHandler.initialize(this);

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
        Button nearestBtn = (Button) this.findViewById(R.id.nearest_btn);
        nearestBtn.setEnabled(false);

        Button bicubicBtn = (Button) this.findViewById(R.id.bicubic_btn);
        bicubicBtn.setEnabled(false);

        Button srViewBtn = (Button) this.findViewById(R.id.sr_view_btn);
        srViewBtn.setEnabled(false);
    }


    private void executeSRProcessor() {
        if(ParameterConfig.getCurrentTechnique() == ParameterConfig.SRTechnique.MULTIPLE) {
            ReleaseSRProcessor releaseSRProcessor = new ReleaseSRProcessor();
            releaseSRProcessor.start();
        }
        else {
            new SingleImageSRProcessor().start();
        }
    }
}
