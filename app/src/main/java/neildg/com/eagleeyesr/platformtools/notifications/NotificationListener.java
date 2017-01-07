
package neildg.com.eagleeyesr.platformtools.notifications;

/**
 * Add this to the one who will listen to a certain notification
 * @author user
 *
 */
public interface NotificationListener {

	public abstract void onNotify(String notificationString, Parameters params);
}
