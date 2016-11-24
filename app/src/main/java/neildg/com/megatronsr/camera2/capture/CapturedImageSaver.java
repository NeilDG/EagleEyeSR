package neildg.com.megatronsr.camera2.capture;

import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import neildg.com.megatronsr.io.FileImageWriter;
import neildg.com.megatronsr.io.ImageFileAttribute;

/**
 * Handles the saving of image upon capture
 * Created by NeilDG on 11/19/2016.
 */

public class CapturedImageSaver implements ImageReader.OnImageAvailableListener {

    private final static String TAG = "CapturedImageSaver";

    private File file;
    private String filePath;
    private String name;
    private ImageFileAttribute.FileType fileType;

    private int index = 0;

    public CapturedImageSaver(String filePath, String name, ImageFileAttribute.FileType fileType) {
        this.index = 0;
        this.filePath = filePath;
        this.name = name;
        this.fileType = fileType;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        this.file = new File(this.filePath + "/" + this.name + "_" +this.index + ImageFileAttribute.getFileExtension(this.fileType));
        Log.d(TAG, "On image available: " +this.file.getPath() + "Time = " + System.currentTimeMillis());
        Image image = null;
        try {
            image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            save(bytes);

            this.index++;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (image != null) {
                image.close();
                //reader.close();
            }
        }
    }

    private void save(byte[] bytes) throws IOException {
        OutputStream output = null;
        try {
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
