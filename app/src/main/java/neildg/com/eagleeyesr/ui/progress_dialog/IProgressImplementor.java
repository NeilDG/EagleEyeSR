package neildg.com.eagleeyesr.ui.progress_dialog;

/**
 * Created by NeilDG on 4/27/2017.
 */
public interface IProgressImplementor {
    void setup(String title, String message);

    void updateProgress(float progress);

    float getProgress();

    void show();
    void hide();
    boolean isShown();
}
