/**
 * 
 */
package neildg.com.megatronsr.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Writes images to external directory
 * @author NeilDG
 *
 */
public class FileImageWriter {
	private final static String TAG = "SR_ImageWriter";
	
	private static FileImageWriter sharedInstance = null;
	public static FileImageWriter getInstance() {
		return sharedInstance;
	}
	
	public final static String ALBUM_NAME_PREFIX = "/SR";

	private Context context;
	private String proposedPath;
	
	private FileImageWriter(Context context) {
		this.context = context;
		this.proposedPath = DirectoryStorage.getSharedInstance().getProposedPath();
	}
	
	public static void initialize(Context context) {
		
		if(sharedInstance == null) {
			sharedInstance = new FileImageWriter(context);
			
			//also initialize image reader
			FileImageReader.initialize(context);
		}
	}
	
	public static void destroy() {
		//also destroy image reader
		sharedInstance = null;
		FileImageReader.destroy();
	}

	/**
	 * Saves a given image for viewing or later reuse in processing.
	 * Images saved are automatically appended with a JPEG extension.
	 * @param imageData
	 */
	public void saveImage(byte[] imageData, String fileName, ImageFileAttribute.FileType fileType) {
		try {
			if(imageData != null) {
				File processedImageFile = new File(this.proposedPath, fileName + ImageFileAttribute.getFileExtension(fileType));
				FileOutputStream fos = new FileOutputStream(processedImageFile);
				fos.write(imageData);
				fos.close();
			}
		}
		catch(IOException e) {
			Log.e(TAG, "Error writing image: " +e.getMessage());
		}
	}

	public void saveBitmapImage(Bitmap bitmap, String fileName, ImageFileAttribute.FileType fileType) {

        try {
            File processedImageFile = new File(this.proposedPath, fileName + ImageFileAttribute.getFileExtension(fileType));
            FileOutputStream out = new FileOutputStream(processedImageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            // NOTE: PNG is a lossless format, the compression factor (100) is ignored
			out.close();
            Log.d(TAG, "Saved: " +processedImageFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void saveBitmapImage(Bitmap bitmap, String directory, String fileName, ImageFileAttribute.FileType fileType) {
		try {
			File dirFile = new File(this.proposedPath + "/" + directory);
			dirFile.mkdirs();

			File processedImageFile = new File(dirFile.getPath(), fileName + ImageFileAttribute.getFileExtension(fileType));

			FileOutputStream out = new FileOutputStream(processedImageFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			// NOTE: PNG is a lossless format, the compression factor (100) is ignored
			out.close();
			Log.d(TAG, "Saved: " +processedImageFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void saveMatrixToImage(Mat mat, String fileName, ImageFileAttribute.FileType fileType) {
		File imageFile = new File(this.proposedPath, fileName + ImageFileAttribute.getFileExtension(fileType));
		Imgcodecs.imwrite(imageFile.getAbsolutePath(), mat);

		Log.d(TAG, "Saved " + imageFile.getAbsolutePath());
	}

	public synchronized void saveMatrixToImage(Mat mat, String directory, String fileName, ImageFileAttribute.FileType fileType) {
		File dirFile = new File(this.proposedPath + "/" + directory);
		if(dirFile.mkdirs() == false) {
			dirFile.mkdir();
		}

		File imageFile = new File(dirFile.getPath(), fileName + ImageFileAttribute.getFileExtension(fileType));
		Imgcodecs.imwrite(imageFile.getAbsolutePath(), mat);

		//Log.d(TAG, "Saved " + imageFile.getAbsolutePath());
	}

	public synchronized void deleteImage(String fileName, ImageFileAttribute.FileType fileType) {
		File imageFile = new File(this.proposedPath, fileName + ImageFileAttribute.getFileExtension(fileType));
		imageFile.delete();
	}

	public synchronized void deleteImage(String fileName, String directory, ImageFileAttribute.FileType fileType) {
		File dirFile = new File(this.proposedPath + "/" + directory);
		File imageFile = new File(dirFile.getPath(), fileName + ImageFileAttribute.getFileExtension(fileType));
		imageFile.delete();
	}
	
	public String getFilePath() {
		return this.proposedPath;
	}
}
