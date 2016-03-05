package neildg.com.megatronsr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import neildg.com.megatronsr.preprocessing.BitmapURIRepository;
import neildg.com.megatronsr.processing.SRProcessor;
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
        numImagesText.setText(Integer.toString(BitmapURIRepository.getInstance().getNumImages()));
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
                SRProcessor.getSharedInstance().start();
            }
        });
    }

}
