package neildg.com.eagleeyesr.ui;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import neildg.com.eagleeyesr.R;

/**
 * Created by NeilDG on 11/27/2016.
 */

public class ProcessingDialog extends Dialog {

    private final static String TAG = "ProcessingDialog";

    private TextView textView;
    private RoundCornerProgressBar progressBar;

    public ProcessingDialog(Context context) {
        super(context);
        this.setContentView(R.layout.dialog_sr_progress);

        this.textView = (TextView) this.findViewById(R.id.text_view_message);
        this.progressBar = (RoundCornerProgressBar) this.findViewById(R.id.progress_bar);
        this.progressBar.setMax(100);
    }

    public void setup(String title, String message) {
        this.setTitle(title);
        this.textView.setText(message);
    }

    public void updateProgress(float progress) {
        if(progress < this.progressBar.getMax()) {
            this.progressBar.setProgress(progress);
        }
        else {
            this.progressBar.setProgress(this.progressBar.getMax());
        }
    }

    public float getProgress() {
        return this.progressBar.getProgress();
    }
}
