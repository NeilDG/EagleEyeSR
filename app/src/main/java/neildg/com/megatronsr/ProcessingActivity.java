package neildg.com.megatronsr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.io.BitmapURIRepository;
import neildg.com.megatronsr.processing.ITest;
import neildg.com.megatronsr.processing.debugging.ImageWarpingTest;
import neildg.com.megatronsr.processing.debugging.MatSavingTest;
import neildg.com.megatronsr.processing.debugging.ZeroFillingTest;
import neildg.com.megatronsr.processing.imagetools.OpticalFlowTest;
import neildg.com.megatronsr.threads.DebuggingProcessor;
import neildg.com.megatronsr.threads.MultipleImageSRProcessor;
import neildg.com.megatronsr.threads.SingleImageSRProcessor;
import neildg.com.megatronsr.ui.ProgressDialogHandler;

/*
    Processing activity for SR. Just a simple debugging activity
 */
public class ProcessingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        this.initializeButtons();

        ProgressDialogHandler.initialize(this);
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
               ProcessingActivity.this.executeSRProcessor();
            }
        });

        Button debugSaveBtn = (Button) this.findViewById(R.id.save_mat_btn);
        debugSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatSavingTest matSavingTest = new MatSavingTest();
                ProcessingActivity.this.executeDebugAction(matSavingTest);
            }
        });

        Button debugZeroFillBtn = (Button) this.findViewById(R.id.zero_fill_btn);
        debugZeroFillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZeroFillingTest zeroFillingTest = new ZeroFillingTest();
                ProcessingActivity.this.executeDebugAction(zeroFillingTest);
            }
        });

        Button debugWarpBtn = (Button) this.findViewById(R.id.warp_btn);
        debugWarpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageWarpingTest warpingTest = new ImageWarpingTest();
                ProcessingActivity.this.executeDebugAction(warpingTest);
            }
        });

        Button debugFlowBtn = (Button) this.findViewById(R.id.optical_flow_btn);
        debugFlowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpticalFlowTest opticalFlowTest = new OpticalFlowTest();
                ProcessingActivity.this.executeDebugAction(opticalFlowTest);
            }
        });
    }

    private void executeSRProcessor() {
        if(ParameterConfig.getCurrentTechnique() == ParameterConfig.SRTechnique.MULTIPLE) {
            new MultipleImageSRProcessor().start();
        }
        else {
            new SingleImageSRProcessor().start();
        }
    }

    private void executeDebugAction(ITest test) {
        new DebuggingProcessor(test).start();
    }

}
