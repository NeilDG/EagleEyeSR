package neildg.com.eagleeyesr.ui.progress_dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import neildg.com.eagleeyesr.R;
import neildg.com.eagleeyesr.ui.progress_dialog.IProgressImplementor;

/**
 * Created by NeilDG on 11/27/2016.
 */

public class ProcessingDialog extends Dialog implements IProgressImplementor {

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

    @Override
    public void setup(String title, String message) {
        this.setTitle(title);
        this.textView.setText(message);
    }

    @Override
    public void updateProgress(float progress) {
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
    public void show() {
        super.show();
        this.setCancelable(false);
    }

    @Override
    public boolean isShown() {
        return this.isShowing();
    }

    @Override
    public void hide() {
        this.dismiss();
    }
}
