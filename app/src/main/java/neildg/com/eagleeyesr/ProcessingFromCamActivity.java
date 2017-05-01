package neildg.com.eagleeyesr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.io.DirectoryStorage;
import neildg.com.eagleeyesr.io.FileImageReader;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.platformtools.notifications.NotificationCenter;
import neildg.com.eagleeyesr.platformtools.notifications.Notifications;
import neildg.com.eagleeyesr.processing.process_observer.IProcessListener;
import neildg.com.eagleeyesr.processing.process_observer.SRProcessManager;
import neildg.com.eagleeyesr.threads.ReleaseSRProcessor;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;

public class ProcessingFromCamActivity extends AppCompatActivity implements IProcessListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing_from_cam);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ProgressDialogHandler.initialize(this);
        this.initializeButtons();

        ReleaseSRProcessor releaseSRProcessor = new ReleaseSRProcessor();
        releaseSRProcessor.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        ProgressDialogHandler.getInstance().setDefaultProgressImplementor();
        SRProcessManager.getInstance().setProcessListener(this, this);

        this.updateImageViewStatus();
    }

    @Override
    protected void onDestroy() {
        ProgressDialogHandler.destroy();
        super.onDestroy();
    }

    private void updateImageViewStatus() {
        Button imageViewBtn = (Button) this.findViewById(R.id.image_results_view_btn);
        imageViewBtn.setEnabled(FileImageReader.getInstance().doesImageExists(FilenameConstants.HR_SUPERRES, ImageFileAttribute.FileType.JPEG));
    }


    private void initializeButtons() {
        Button imageViewBtn = (Button) this.findViewById(R.id.image_results_view_btn);
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
        ProcessingFromCamActivity.this.updateImageViewStatus();

        ///automatically start image preview
        Intent previewIntent = new Intent(ProcessingFromCamActivity.this, ImageViewActivity.class);
        startActivity(previewIntent);
    }

    @Override
    public void onProducedInitialHR() {
        //start image preview intent for initial viewing
        Intent previewIntent = new Intent(this, ImageViewActivity.class);
        startActivity(previewIntent);
    }
}
