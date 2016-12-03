package neildg.com.megatronsr.ui.views;

import android.view.View;
import android.view.ViewStub;

/**
 * A class that accepts a viewstub reference, inflates it, and set it as the reference view.
 * Created by NeilDG on 12/3/2016.
 */

public abstract class AViewStubScreen extends AScreen {

    public AViewStubScreen(ViewStub viewStub, boolean makeVisible) {
        super(viewStub.inflate());
        if(makeVisible) {
            this.referenceView.setVisibility(View.VISIBLE);
        }
        else {
            this.referenceView.setVisibility(View.INVISIBLE);
        }
    }
}
