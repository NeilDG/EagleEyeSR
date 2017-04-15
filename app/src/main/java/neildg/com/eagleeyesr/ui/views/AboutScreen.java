package neildg.com.eagleeyesr.ui.views;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import neildg.com.eagleeyesr.R;
import neildg.com.eagleeyesr.platformtools.core_application.ApplicationCore;

/**
 * Created by NeilDG on 4/15/2017.
 */

public class AboutScreen extends AScreen {
    private final static String TAG = "AboutScreen";

    public AboutScreen(View view) {
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

        Button articleBtn = (Button) this.referenceView.findViewById(R.id.article_link_btn);
        articleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://jivp.eurasipjournals.springeropen.com/articles/10.1186/s13640-016-0156-z"));
                ApplicationCore.getInstance().getMainActivity().startActivity(browserIntent);
            }
        });
    }
}
