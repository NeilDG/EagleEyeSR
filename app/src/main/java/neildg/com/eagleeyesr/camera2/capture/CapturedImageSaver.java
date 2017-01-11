package neildg.com.eagleeyesr.camera2.capture;

import android.annotation.TargetApi;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import neildg.com.eagleeyesr.constants.FilenameConstants;
import neildg.com.eagleeyesr.constants.ParameterConfig;
import neildg.com.eagleeyesr.io.ImageFileAttribute;
import neildg.com.eagleeyesr.pipeline.ProcessingQueue;
import neildg.com.eagleeyesr.ui.ProgressDialogHandler;

/**
 * Handles the saving of image upon capture
 * Created by NeilDG on 11/19/2016.
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public class CapturedImageSaver implements ImageReader.OnImageAvailableListener {

    private final static String TAG = "CapturedImageSaver";

    private File file;
    private String filePath;
    private ImageFileAttribute.FileType fileType;

    public CapturedImageSaver(String filePath, ImageFileAttribute.FileType fileType) {
        this.filePath = filePath;
        this.fileType = fileType;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        String formattedString = FilenameConstants.INPUT_PREFIX_STRING + ProcessingQueue.getInstance().getCounter();
        this.file = new File(this.filePath + "/" + formattedString+ ImageFileAttribute.getFileExtension(this.fileType));
        long timeStart = ParameterConfig.getPrefsLong("capture_start", 0);
        Log.d(TAG, "On image available: " +this.file.getPath());
        Image image = null;
        try {
            image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            long timeEnd = System.currentTimeMillis();
            Log.d(TAG, "Time spent before image save: " +((timeEnd - timeStart) / 1000.0f)+"s");

            save(bytes);
            ProcessingQueue.getInstance().enqueueImageName(formattedString);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (image != null) {
                image.close();
                //reader.close();
                long timeEnd = System.currentTimeMillis();
                Log.d(TAG, "Time spent after image saved: " +((timeEnd - timeStart) / 1000.0f)+"s");
            }
        }
    }

    private void save(byte[] bytes) throws IOException {
        OutputStream output = null;
        try {
            ProgressDialogHandler.getInstance().updateProgress(20.0f);
            output = new FileOutputStream(this.file);
            output.write(bytes);

            Log.d(TAG, "Picture saved: " +this.file.getPath());

        } finally {
            if (null != output) {
                output.close();
            }
        }
    }
}
