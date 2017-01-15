package neildg.com.eagleeyesr.processing.multiple.alignment;

import org.opencv.core.Mat;
import org.opencv.photo.AlignMTB;
import org.opencv.photo.Photo;

import java.util.Arrays;
import java.util.List;

import neildg.com.eagleeyesr.io.FileImageWriter;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.model.AttributeHolder;
import neildg.com.eagleeyesr.model.AttributeNames;
import neildg.com.eagleeyesr.processing.IOperator;

/**
 * Performs MTB median alignment. Converts the images into median threshold bitmaps (1 for above median luminance threshold, 0 otherwise). It is aligned by BIT operations.
 * Created by NeilDG on 12/17/2016.
 */

public class MedianAlignmentOperator implements IOperator {
    private final static String TAG = "ExposureAlignmentOperator";

    private Mat[] imageSequenceList;
    private String[] resultNames;
    public MedianAlignmentOperator(Mat[] imageSequenceList, String[] resultNames) {
        this.imageSequenceList = imageSequenceList;
        this.resultNames = resultNames;
    }

    @Override
    public void perform() {
        AlignMTB mtbAligner = Photo.createAlignMTB();

        List<Mat> processMatList = Arrays.asList(this.imageSequenceList);
        mtbAligner.process(processMatList, processMatList);

        //FileImageWriter.getInstance().saveMatrixToImage(processMatList.get(0), FilenameConstants.INPUT_PREFIX_STRING + this.inputIndex, ImageFileAttribute.FileType.JPEG);

        for(int i = 1; i < processMatList.size(); i++) {
            //FileImageWriter.getInstance().saveMatrixToImage(processMatList.get(i), FilenameConstants.MEDIAN_ALIGNMENT_PREFIX + (i-1), ImageFileAttribute.FileType.JPEG);
            FileImageWriter.getInstance().saveMatrixToImage(processMatList.get(i), resultNames[i - 1], ImageFileAttribute.FileType.JPEG);
        }

        AttributeHolder.getSharedInstance().putValue(AttributeNames.WARPED_IMAGES_LENGTH_KEY, processMatList.size() - 1);
    }
}
