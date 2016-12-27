package neildg.com.megatronsr.ui.views;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import neildg.com.megatronsr.R;
import neildg.com.megatronsr.constants.ParameterConfig;
import neildg.com.megatronsr.processing.multiple.alignment.WarpingConstants;
import neildg.com.megatronsr.processing.multiple.fusion.FusionConstants;
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

        RadioGroup warpChoiceGroup = (RadioGroup) this.referenceView.findViewById(R.id.warp_choice_radiogroup);
        warpChoiceGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.best_alignment_btn) {
                    ParameterConfig.setPrefs(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.BEST_ALIGNMENT);
                }
                else if(checkedId == R.id.exposure_align_btn) {
                    ParameterConfig.setPrefs(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.MEDIAN_ALIGNMENT);
                }
                else if(checkedId == R.id.perspective_warp_btn) {
                    ParameterConfig.setPrefs(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.PERSPECTIVE_WARP);
                }
                Log.d(TAG, ParameterConfig.WARP_CHOICE_KEY + " set to " +ParameterConfig.getPrefsInt(ParameterConfig.WARP_CHOICE_KEY, WarpingConstants.AFFINE_WARP));
            }
        });

        RadioGroup srChoiceGroup = (RadioGroup) this.referenceView.findViewById(R.id.sr_choice_radiogroup);
        srChoiceGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.sr_choice_fast_btn) {
                    ParameterConfig.setPrefs(ParameterConfig.SR_CHOICE_KEY, FusionConstants.FAST_MODE);
                }
                else if(checkedId == R.id.sr_choice_full_btn) {
                    ParameterConfig.setPrefs(ParameterConfig.SR_CHOICE_KEY, FusionConstants.FULL_SR_MODE);
                }
                Log.d(TAG, ParameterConfig.SR_CHOICE_KEY + " set to " +ParameterConfig.getPrefsInt(ParameterConfig.SR_CHOICE_KEY, FusionConstants.FULL_SR_MODE));
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
    }

    /*
         * Set some default settings here
         */
    private void setDefaults() {
        ToggleButton debugBtn = (ToggleButton) this.referenceView.findViewById(R.id.debug_option_btn);
        debugBtn.setChecked(true); //enable debug mode by default

        ToggleButton denoiseBtn = (ToggleButton) this.referenceView.findViewById(R.id.denoise_option_btn);
        denoiseBtn.setChecked(false); //disable denoising mode by default.

        RadioGroup warpChoiceGroup = (RadioGroup) this.referenceView.findViewById(R.id.warp_choice_radiogroup);
        RadioButton warpChoiceBtn = (RadioButton) warpChoiceGroup.findViewById(R.id.best_alignment_btn);
        warpChoiceBtn.setChecked(true);

        RadioGroup srChoiceGroup = (RadioGroup) this.referenceView.findViewById(R.id.sr_choice_radiogroup);
        RadioButton srChoiceBtn = (RadioButton) srChoiceGroup.findViewById(R.id.sr_choice_fast_btn);
        srChoiceBtn.setChecked(true);
    }
}
