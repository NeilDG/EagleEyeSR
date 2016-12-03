package neildg.com.megatronsr.ui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import neildg.com.megatronsr.R;
import neildg.com.megatronsr.io.FileImageReader;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.model.multiple.ProcessingQueue;
import neildg.com.megatronsr.platformtools.notifications.NotificationCenter;
import neildg.com.megatronsr.platformtools.notifications.NotificationListener;
import neildg.com.megatronsr.platformtools.notifications.Notifications;
import neildg.com.megatronsr.platformtools.notifications.Parameters;

/**
 * UI container and model for the processing queue view.
 * CONVENTION: Classes that have "Screen" suffix do not extend the View class of android.
 * Otherwise, it has the "View" suffix.
 * Created by NeilDG on 12/3/2016.
 */

public class ProcessingQueueScreen extends AViewStubScreen implements NotificationListener {
    private final static String TAG = "ProcessingQueueScreen";

    private LayoutInflater inflater;
    private ViewGroup parentElementView;
    private ProgressBar processingBar;
    private Activity activity;

    private List<ImageDetailElement> imageElementList = new ArrayList<>();

    public ProcessingQueueScreen(ViewStub viewStub, boolean makeVisible, LayoutInflater  inflater, ProgressBar progressBar, Activity holdingActivity) {
        super(viewStub, makeVisible);
        this.inflater = inflater;
        this.activity = holdingActivity;
        this.parentElementView = (ViewGroup) this.referenceView.findViewById(R.id.queue_container);
        this.processingBar = progressBar;
        this.processingBar.setVisibility(View.INVISIBLE);

        NotificationCenter.getInstance().removeObserver(Notifications.ON_IMAGE_ENQUEUED, this); //remove first before adding to avoid duplication
        NotificationCenter.getInstance().addObserver(Notifications.ON_IMAGE_ENQUEUED, this);
    }

    @Override
    public void initialize() {
        ImageButton processingCloseBtn = (ImageButton) this.referenceView.findViewById(R.id.processing_close_btn);
        processingCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingQueueScreen.this.referenceView.setVisibility(View.INVISIBLE);
            }
        });

        this.processingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });
    }

    public void addImageToProcess(String fileName) {
        ImageDetailElement imageDetailElement = new ImageDetailElement(this.parentElementView, this.inflater);
        imageDetailElement.setup(fileName);

        this.imageElementList.add(imageDetailElement);

        //show processing bar if an image element was added.
        if(this.imageElementList.size() == 1) {
            this.processingBar.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "Image element added for processing of " +fileName);
    }

    public void removeImageElement(String fileName) {

        int indexToRemove = 0;
        for(int i = 0; i < this.imageElementList.size(); i++) {
            ImageDetailElement imageDetailElement = this.imageElementList.get(i);
            if(imageDetailElement.getImageName() == fileName) {
                imageDetailElement.destroy();
                indexToRemove = i;
                break;
            }
        }

        this.imageElementList.remove(indexToRemove);

        //hide processing bar if no more input
        if(this.imageElementList.size() == 0) {
            this.processingBar.setVisibility(View.INVISIBLE);
        }

    }

    public void clearElements() {
        for(int i = 0; i < this.imageElementList.size(); i++) {
            this.imageElementList.get(i).destroy();
        }

        this.imageElementList.clear();
    }

    @Override
    public void onNotify(String notificationString, Parameters params) {
        if(notificationString == Notifications.ON_IMAGE_ENQUEUED) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String imageEnqueued = ProcessingQueue.getInstance().peekImageName();
                    ProcessingQueueScreen.this.addImageToProcess(imageEnqueued);
                }
            });
        }
    }

    public class ImageDetailElement {

        private LayoutInflater inflater;

        private LinearLayout imageDetailView;
        private ViewGroup parentContainer;
        private String imageName;

        public ImageDetailElement(ViewGroup parentContainer, LayoutInflater inflater) {
            this.inflater = inflater;
            this.parentContainer = parentContainer;
        }

        public void setup(String inputImageName) {
            this.imageName = inputImageName;
            this.imageDetailView = (LinearLayout) this.inflater.inflate(R.layout.element_image_detail_container, this.parentContainer);

            ImageView imageThumbnail = (ImageView) this.imageDetailView.findViewById(R.id.image_thumbnail);
            Bitmap thumbnailBmp = FileImageReader.getInstance().loadBitmapThumbnail(inputImageName, ImageFileAttribute.FileType.JPEG, 70, 70);
            imageThumbnail.setImageBitmap(thumbnailBmp);
        }

        public void destroy() {
            this.parentContainer.removeView(this.imageDetailView);
        }

        public String getImageName() {
            return this.imageName;
        }


    }
}
