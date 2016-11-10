package neildg.com.megatronsr.subroutine;

/**
 * Created by NeilDG on 11/1/2016.
 */

public interface ICoroutine {
    void perform();
    void onCoroutineStarted();
    void onCoroutineEnded();
}
