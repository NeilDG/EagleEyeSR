package neildg.com.eagleeyesr.ui.views;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import neildg.com.eagleeyesr.R;
import neildg.com.eagleeyesr.ui.progress_dialog.IProgressImplementor;
import neildg.com.eagleeyesr.ui.progress_dialog.ProgressDialogHandler;

/**
 * Created by NeilDG on 4/27/2017.
 */

public class ImageProgressScreen extends AScreen implements IProgressImplementor {
    private final static String TAG = "ImageProgressScreen";

    private TextView textView;
    private RoundCornerProgressBar progressBar;

    public ImageProgressScreen(View view) {
        super(view);
    }

    @Override
    public void initialize() {
        this.textView = (TextView) this.referenceView.findViewById(R.id.text_view_message);
        this.progressBar = (RoundCornerProgressBar) this.referenceView.findViewById(R.id.image_view_progress_bar);
        this.progressBar.setMax(100);
    }

    @Override
    public void setup(String title, String message) {
        this.textView.setText(message);
    }

    @Override
    public void updateProgress(float progress) {
        Log.i(TAG, "Progress is: " +progress);
        if(progress < this.progressBar.getMax()) {
            this.progressBar.setProgress(progress);
        }
        else {
            this.progressBar.setProgress(this.progressBar.getMax());
        }
    }

    @Override
    public float getProgress() {
       return this.progressBar.getProgress();
    }

    @Override
    public String getMessage() {
        return this.textView.getText().toString();
    }
}
