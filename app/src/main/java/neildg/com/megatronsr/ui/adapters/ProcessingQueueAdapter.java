package neildg.com.megatronsr.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import neildg.com.megatronsr.R;
import neildg.com.megatronsr.ui.elements.ImageDetailElement;

/**
 * Created by NeilDG on 12/3/2016.
 */

public class ProcessingQueueAdapter extends ArrayAdapter<ImageDetailElement> {
    public ProcessingQueueAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ProcessingQueueAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public ProcessingQueueAdapter(Context context, int resource, ImageDetailElement[] objects) {
        super(context, resource, objects);
    }

    public ProcessingQueueAdapter(Context context, int resource, int textViewResourceId, ImageDetailElement[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public ProcessingQueueAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
    }

    public ProcessingQueueAdapter(Context context, int resource, int textViewResourceId, List objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ImageDetailElement detailElement = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.element_image_detail_container, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.image_thumbnail);
        imageView.setImageBitmap(detailElement.getThumbnail());

        TextView textView = (TextView) convertView.findViewById(R.id.pipeline_stage_txt);
        textView.setText(detailElement.getPipelineStage());
        return convertView;
    }

}
