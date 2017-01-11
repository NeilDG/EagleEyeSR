package neildg.com.eagleeyesr.ui.views;

import android.view.View;
import android.widget.Button;

import neildg.com.eagleeyesr.R;

/**
 * Created by NeilDG on 1/7/2017.
 */

public class InfoScreen extends AScreen {
    private final static String TAG = "InfoScreen";

    public InfoScreen(View view) {
        super(view);
    }

    @Override
    public void initialize() {
        Button closeBtn = (Button) this.referenceView.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        this.referenceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }
}
