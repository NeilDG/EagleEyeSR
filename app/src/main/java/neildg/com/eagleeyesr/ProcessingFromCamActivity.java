package neildg.com.eagleeyesr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import neildg.com.eagleeyesr.processing.listeners.IProcessListener;
import neildg.com.eagleeyesr.threads.ReleaseSRProcessor;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;

public class ProcessingFromCamActivity extends AppCompatActivity implements IProcessListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing_from_cam);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ProgressDialogHandler.initialize(this);
        this.initializeButtons();

        ReleaseSRProcessor releaseSRProcessor = new ReleaseSRProcessor(this);
        releaseSRProcessor.start();

    }

    @Override
    protected void onDestroy() {
        ProgressDialogHandler.destroy();
        super.onDestroy();
    }


    private void initializeButtons() {
        Button imageViewBtn = (Button) this.findViewById(R.id.image_results_view_btn);
        imageViewBtn.setEnabled(false);

        imageViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(ProcessingFromCamActivity.this, ImageViewActivity.class);
                startActivity(previewIntent);
            }
        });
    }

    @Override
    public void onProcessCompleted() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button imageViewBtn = (Button) ProcessingFromCamActivity.this.findViewById(R.id.image_results_view_btn);
                imageViewBtn.setEnabled(true);
            }
        });
    }
}
