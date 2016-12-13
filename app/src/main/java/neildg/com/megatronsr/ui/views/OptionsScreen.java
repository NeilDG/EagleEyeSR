package neildg.com.megatronsr.ui.views;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import neildg.com.megatronsr.R;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.processing.multiple.warping.WarpingConstants;
import neildg.com.megatronsr.ui.ResolutionPickerDialog;

/**
 * Contains reference to the options view
 * Created by NeilDG on 12/3/2016.
 */

public class OptionsScreen extends AScreen {
    private final static String TAG ="OptionsScreen";

    public OptionsScreen(View view) {
        super(view);
    }

    @Override
    public void initialize() {

        Button closeBtn = (Button) this.referenceView.findViewById(R.id.btn_overlay_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        ToggleButton debugBtn = (ToggleButton) this.referenceView.findViewById(R.id.debug_option_btn);
        debugBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ParameterConfig.setPrefs(ParameterConfig.DEBUGGING_FLAG_KEY, isChecked);
                Log.d(TAG, ParameterConfig.DEBUGGING_FLAG_KEY + " set to " +ParameterConfig.getPrefsBoolean(ParameterConfig.DEBUGGING_FLAG_KEY, false));
            }
        });

        ToggleButton denoiseBtn = (ToggleButton) this.referenceView.findViewById(R.id.denoise_option_btn);
        denoiseBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ParameterConfig.setPrefs(ParameterConfig.DENOISE_FLAG_KEY, isChecked);
                Log.d(TAG, ParameterConfig.DENOISE_FLAG_KEY + " set to " +ParameterConfig.getPrefsBoolean(ParameterConfig.DENOISE_FLAG_KEY, false));
            }
        });

        SeekBar thresholdBar = (SeekBar) this.referenceView.findViewById(R.id.fusion_seekbar);
        final EditText thresholdEditText = (EditText) this.referenceView.findViewById(R.id.fusion_text_value);
        thresholdBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thresholdEditText.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        RadioGroup warpChoiceGroup = (RadioGroup) this.referenceView.findViewById(R.id.warp_choice_radiogroup);
        warpChoiceGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.affine_radio_btn) {
                    ParameterConfig.setPrefs(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.AFFINE_WARP);
                }
                else if(checkedId == R.id.perspective_radio_btn) {
                    ParameterConfig.setPrefs(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.PERSPECTIVE_WARP);
                }
                Log.d(TAG, ParameterConfig.WARP_CHOICE_KEY + " set to " +ParameterConfig.getPrefsInt(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.AFFINE_WARP));
            }
        });

        this.setDefaults();
    }

    public void setupResolutionButton(final ResolutionPickerDialog dialog) {
       /*Button resolutionBtn = (Button) this.referenceView.findViewById(R.id.btn_image_resolution);
        resolutionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });*/
    }

    @Override
    public void hide() {
        super.hide();

        //update parameter config
        /*EditText minDistText = (EditText) this.referenceView.findViewById(R.id.feature_dist_text);
        String valueString = minDistText.getText().toString();

        try {
            float newValue = Float.parseFloat(valueString);
            ParameterConfig.setPrefs(ParameterConfig.FEATURE_MINIMUM_DISTANCE_KEY, newValue);

            Log.d(TAG, "New min distance threshold set: " +ParameterConfig.getPrefsFloat(ParameterConfig.FEATURE_MINIMUM_DISTANCE_KEY, 999.0f));
        } catch(NumberFormatException e) {
            Log.e(TAG, "Error in parsing min distance text. Not a valid value. Value: " +valueString);
        }*/

        EditText thresholdEditText = (EditText) this.referenceView.findViewById(R.id.fusion_text_value);
        String thresholdString = thresholdEditText.getText().toString();

        try {
            int newValue = Integer.parseInt(thresholdString);
            ParameterConfig.setPrefs(ParameterConfig.FUSION_THRESHOLD_KEY, newValue);

            Log.d(TAG, "New fusion min threshold set: " +ParameterConfig.getPrefsInt(ParameterConfig.FUSION_THRESHOLD_KEY, ParameterConfig.MAX_FUSION_THRESHOLD));
        } catch(NumberFormatException e) {
            Log.e(TAG, "Error in parsing min fusion threshold text. Not a valid value. Value: " +thresholdString);
        }
    }

    /*
         * Set some default settings here
         */
    private void setDefaults() {
        ToggleButton debugBtn = (ToggleButton) this.referenceView.findViewById(R.id.debug_option_btn);
        debugBtn.setChecked(true); //enable debug mode by default

        ToggleButton denoiseBtn = (ToggleButton) this.referenceView.findViewById(R.id.denoise_option_btn);
        denoiseBtn.setChecked(false); //disable denoising mode by default.

        SeekBar thresholdBar = (SeekBar) this.referenceView.findViewById(R.id.fusion_seekbar);
        thresholdBar.setProgress(0); thresholdBar.setMax(ParameterConfig.MAX_FUSION_THRESHOLD);

        RadioGroup warpChoiceGroup = (RadioGroup) this.referenceView.findViewById(R.id.warp_choice_radiogroup);
        RadioButton warpChoiceBtn = (RadioButton) warpChoiceGroup.findViewById(R.id.affine_radio_btn);
        warpChoiceBtn.setChecked(true);
    }
}
