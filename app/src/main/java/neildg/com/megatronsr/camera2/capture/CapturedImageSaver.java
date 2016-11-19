package neildg.com.megatronsr.camera2.capture;

import android.media.Image;
import android.media.ImageReader;

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

    private File file;

    public CapturedImageSaver(String filePath, String name, ImageFileAttribute.FileType fileType) {
        this.file = new File(filePath + "/" + name + ImageFileAttribute.getFileExtension(fileType));
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            save(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (image != null) {
                image.close();
                reader.close();
            }
        }
    }

    private void save(byte[] bytes) throws IOException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);
        } finally {
            if (null != output) {
                output.close();
            }
        }
    }
}
