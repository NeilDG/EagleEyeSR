package neildg.com.megatronsr.ui.views;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import neildg.com.megatronsr.R;
import neildg.com.megatronsr.pipeline.ProcessingQueue;
import neildg.com.megatronsr.pipeline.PipelineManager;
import neildg.com.megatronsr.platformtools.notifications.NotificationCenter;
import neildg.com.megatronsr.platformtools.notifications.NotificationListener;
import neildg.com.megatronsr.platformtools.notifications.Notifications;
import neildg.com.megatronsr.platformtools.notifications.Parameters;
import neildg.com.megatronsr.ui.adapters.ProcessingQueueAdapter;
import neildg.com.megatronsr.ui.elements.ImageDetailElement;

/**
 * UI container and model for the processing queue view.
 * CONVENTION: Classes that have "Screen" suffix do not extend the View class of android.
 * Otherwise, it has the "View" suffix.
 * Created by NeilDG on 12/3/2016.
 */

public class ProcessingQueueScreen extends AViewStubScreen implements NotificationListener {
    private final static String TAG = "ProcessingQueueScreen";

    private ProgressBar processingBar;
    private Activity activity;

    private ProcessingQueueAdapter arrayAdapter;
    private ListView listView;

    public ProcessingQueueScreen(ViewStub viewStub, boolean makeVisible, ProgressBar progressBar, Activity holdingActivity) {
        super(viewStub, makeVisible);
        this.activity = holdingActivity;
        this.processingBar = progressBar;
        this.processingBar.setVisibility(View.INVISIBLE);

        this.arrayAdapter = new ProcessingQueueAdapter(this.activity,0);
        this.listView = (ListView) this.referenceView.findViewById(R.id.queue_container);
        this.listView.setAdapter(this.arrayAdapter);

        NotificationCenter.getInstance().removeObserver(Notifications.ON_IMAGE_ENQUEUED, this); //remove first before adding to avoid duplication
        NotificationCenter.getInstance().addObserver(Notifications.ON_IMAGE_ENQUEUED, this);

        NotificationCenter.getInstance().removeObserver(Notifications.ON_IMAGE_EXITED_PIPELINE, this); //remove first before adding to avoid duplication
        NotificationCenter.getInstance().addObserver(Notifications.ON_IMAGE_EXITED_PIPELINE, this);

        NotificationCenter.getInstance().removeObserver(Notifications.ON_IMAGE_STAGE_UPDATED, this); //remove first before adding to avoid duplication
        NotificationCenter.getInstance().addObserver(Notifications.ON_IMAGE_STAGE_UPDATED, this);
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
        ImageDetailElement imageDetailElement = new ImageDetailElement();
        imageDetailElement.setup(fileName);
        imageDetailElement.updatePipelineStage(PipelineManager.PROCESSING_QUEUE_STAGE);

        this.arrayAdapter.add(imageDetailElement);

        //show processing bar if an image element was added.
        if(this.arrayAdapter.getCount() == 1) {
            this.processingBar.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "Image element added for processing of " +fileName);
    }

    public void updateImageStage(String imageName, String pipelineStage) {
        for(int i = 0; i < this.arrayAdapter.getCount(); i++) {
            ImageDetailElement imageDetailElement = this.arrayAdapter.getItem(i);
            if(imageDetailElement.getImageName() == imageName) {
                imageDetailElement.updatePipelineStage(pipelineStage);
                break;
            }
        }

        this.arrayAdapter.notifyDataSetChanged();
    }

    public void removeImageElement(String fileName) {

        for(int i = 0; i < this.arrayAdapter.getCount(); i++) {
            ImageDetailElement imageDetailElement = this.arrayAdapter.getItem(i);
            if(imageDetailElement.getImageName() == fileName) {
                imageDetailElement.destroy();
                this.arrayAdapter.remove(imageDetailElement);
                Log.d(TAG, "Image element " +fileName+ " removed.");
                break;
            }
        }

        //hide processing bar if no more input
        if(this.arrayAdapter.getCount() == 0) {
            this.processingBar.setVisibility(View.INVISIBLE);
        }


    }

    public void clearElements() {
        this.arrayAdapter.clear();
    }

    @Override
    public void onNotify(String notificationString, final Parameters params) {
        if(notificationString == Notifications.ON_IMAGE_ENQUEUED) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String imageEnqueued = ProcessingQueue.getInstance().getLatestImageName();
                    ProcessingQueueScreen.this.addImageToProcess(imageEnqueued);
                }
            });
        }
        else if(notificationString == Notifications.ON_IMAGE_EXITED_PIPELINE) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProcessingQueueScreen.this.removeImageElement(params.getStringExtra(PipelineManager.IMAGE_NAME_KEY, ""));
                }
            });
        }
        else if(notificationString == Notifications.ON_IMAGE_STAGE_UPDATED) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String imageName = params.getStringExtra(PipelineManager.IMAGE_NAME_KEY, "");
                    String pipelineStage = params.getStringExtra(PipelineManager.PIPELINE_STAGE_KEY, PipelineManager.PROCESSING_QUEUE_STAGE);
                    ProcessingQueueScreen.this.updateImageStage(imageName,pipelineStage);
                }
            });
        }
    }


}
