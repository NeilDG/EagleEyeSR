package neildg.com.megatronsr.processing.multiple.alignment;

import org.opencv.core.Mat;
import org.opencv.photo.AlignMTB;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import neildg.com.megatronsr.constants.FilenameConstants;
import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.io.ImageFileAttribute;
import neildg.com.megatronsr.model.AttributeHolder;
import neildg.com.megatronsr.model.AttributeNames;
import neildg.com.megatronsr.processing.IOperator;
import neildg.com.megatronsr.processing.imagetools.MatMemory;

/**
 * Performs MTB exposure alignment.
 * Created by NeilDG on 12/17/2016.
 */

public class ExposureAlignmentOperator implements IOperator {
    private final static String TAG = "ExposureAlignmentOperator";

    private Mat[] imageSequenceList;
    private int inputIndex;
    public ExposureAlignmentOperator(Mat[] imageSequenceList, int inputIndex) {
        this.imageSequenceList = imageSequenceList;
        this.inputIndex = inputIndex;
    }

    @Override
    public void perform() {
        AlignMTB mtbAligner = Photo.createAlignMTB();

        List<Mat> processMatList = Arrays.asList(this.imageSequenceList);
        mtbAligner.process(processMatList, processMatList);

        FileImageWriter.getInstance().saveMatrixToImage(processMatList.get(0), FilenameConstants.INPUT_PREFIX_STRING + this.inputIndex, ImageFileAttribute.FileType.JPEG);

        for(int i = 1; i < processMatList.size(); i++) {
            FileImageWriter.getInstance().saveMatrixToImage(processMatList.get(i), FilenameConstants.WARP_PREFIX + (i-1), ImageFileAttribute.FileType.JPEG);
        }

        AttributeHolder.getSharedInstance().putValue(AttributeNames.WARPED_IMAGES_LENGTH_KEY, processMatList.size() - 1);

    }
}
