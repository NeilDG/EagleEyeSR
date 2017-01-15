package neildg.com.eagleeyesr.ui.views;

import android.view.View;

/**
 * UI abstract container model
 * CONVENTION: Classes that have "Screen" suffix do not extend the View class of android.
 * Otherwise, it has the "View" suffix.
 * Created by NeilDG on 12/3/2016.
 */

public abstract class AScreen {

    protected View referenceView;

    public AScreen(View referenceView) {
        this.referenceView = referenceView;
    }

    //generic functions
    public void show() {
        this.referenceView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        this.referenceView.setVisibility(View.INVISIBLE);
    }

    public void setVisibility(int visibility) {
        this.referenceView.setVisibility(visibility);
    }

    public boolean isShown() {
        return this.referenceView.getVisibility() == View.VISIBLE;
    }

    public int getVisibility() {
        return this.referenceView.getVisibility();
    }

    public abstract void initialize();
}
