package neildg.com.megatronsr.ui;

import android.app.Dialog;
import android.content.Context;
import android.util.Size;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import neildg.com.megatronsr.R;

/**
 * Created by NeilDG on 10/23/2016.
 */

public class ResolutionPickerDialog extends Dialog {

    public ResolutionPickerDialog(Context context) {
        super(context);
        this.setContentView(R.layout.resolution_size_picker);
        this.setTitle("Choose resolution size");
    }

    public void setup(Size[] availableSizes) {
        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.radio_group_container);

        for(int i = availableSizes.length - 1; i >= 0; i--) {
            RadioButton radioButton = new RadioButton(this.getContext());
            double computedMP = (availableSizes[i].getWidth() * availableSizes[i].getHeight()) / 1000000.0;
            long computedMPRound = Math.round(computedMP);
            radioButton.setText(availableSizes[i].getWidth() + " X " +availableSizes[i].getHeight()+ " (" +computedMPRound+ " MP)");
            radioGroup.addView(radioButton);
        }
    }
}
