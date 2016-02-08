
package neildg.com.megatronsr.platformtools.utils.notifications;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

/**
 * Represents the notification center just like Cocos2D and iOS
 * @author user
 *
 */
public class NotificationCenter {

	private static NotificationCenter sharedInstance = null;
	
	private final static String TAG = "NotificationCenter";
	
	private HashMap<String, ArrayList<NotificationListener>> notificationMap;
	private NotificationCenter() {
		this.notificationMap = new HashMap<String, ArrayList<NotificationListener>>();
	}
	
	public void addObserver(String notificationString, NotificationListener listener) {
		
		//if there is already an existing key, put listener to array list
		if(this.notificationMap.containsKey(notificationString)) {
			ArrayList<NotificationListener> listenerList = this.notificationMap.get(notificationString);
			listenerList.add(listener);
		}
		//create new arraylist
		else {
			ArrayList<NotificationListener> listenerList = new ArrayList<NotificationListener>();
			listenerList.add(listener);
			this.notificationMap.put(notificationString, listenerList);
		}
	}
	
	public void removeObserver(String notificationString, NotificationListener listener) {
		if(this.notificationMap.containsKey(notificationString)) {
			ArrayList<NotificationListener> listenerList = this.notificationMap.get(notificationString);
			if(listenerList.remove(listener)) {
				Log.e(TAG, "Removed observer " +listener);
			}
			else {
				Log.e(TAG, "Listener not found. Doing nothing");
			}
		}
	}
	
	public void clearObservers() {
		this.notificationMap.clear();
	}
	
	public void postNotification(String notificationString, Parameters parameters) {
		ArrayList<NotificationListener> listenerList = this.notificationMap.get(notificationString);
		
		if(listenerList != null) {
			for(NotificationListener listener : listenerList) {
				listener.onNotify(notificationString, parameters);
			}
		}
	}
	
	public void postNotification(String notificationString) {
		ArrayList<NotificationListener> listenerList = this.notificationMap.get(notificationString);
		
		if(listenerList != null) {
			for(NotificationListener listener : listenerList) {
				listener.onNotify(notificationString, null);
			}
		}
	}
	
	public static NotificationCenter getInstance() {
		if(sharedInstance == null) {
			sharedInstance = new NotificationCenter();
		}
		
		return sharedInstance;
	}
}
