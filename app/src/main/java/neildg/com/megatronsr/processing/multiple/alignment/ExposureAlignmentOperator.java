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
import neildg.com.megatronsr.processing.IOperator;

/**
 * Performs MTB exposure alignment.
 * Created by NeilDG on 12/17/2016.
 */

public class ExposureAlignmentOperator implements IOperator {
    private final static String TAG = "ExposureAlignmentOperator";

    private Mat[] imageSequenceList;
    public ExposureAlignmentOperator(Mat[] imageSequenceList) {
        this.imageSequenceList = imageSequenceList;
    }

    @Override
    public void perform() {
        AlignMTB mtbAligner = Photo.createAlignMTB();

        List<Mat> processMatList = Arrays.asList(this.imageSequenceList);
        mtbAligner.process(processMatList, processMatList);

        for(int i = 0; i < processMatList.size(); i++) {
            FileImageWriter.getInstance().saveMatrixToImage(processMatList.get(i), FilenameConstants.WARP_PREFIX + i, ImageFileAttribute.FileType.JPEG);
        }
    }
}
