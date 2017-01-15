package net.sourceforge.opencamera.external_bridge;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NeilDG on 1/7/2017.
 */

public class ImageSaveBroadcaster implements IBroadcaster {
    private final static String TAG = "EventBroadcaster";

    private static ImageSaveBroadcaster sharedInstance = null;
    public static ImageSaveBroadcaster getSharedInstance() {
        if(sharedInstance == null) {
            sharedInstance = new ImageSaveBroadcaster();
        }

        return sharedInstance;
    }

    private List<IEvent> eventList = new ArrayList<>();

    private ImageSaveBroadcaster() {

    }

    public void addEvent(IEvent event) {
        this.eventList.add(event);
    }

    public void removeEvent(IEvent event) {
        this.eventList.remove(event);
    }

    @Override
    public void broadcastEvent() {
        for(int i = 0; i < this.eventList.size(); i++) {
            this.eventList.get(i).onReceivedEvent();
        }
    }
}
