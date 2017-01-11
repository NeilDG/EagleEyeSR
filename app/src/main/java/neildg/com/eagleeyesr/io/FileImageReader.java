/**
 * 
 */
package neildg.com.eagleeyesr.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Reads images from external dir
 * @author NeilDG
 *
 */
public class FileImageReader {
	private final static String TAG = "SR_ImageReader";
	
	private static FileImageReader sharedInstance = null;
	public static FileImageReader getInstance() {
		return sharedInstance;
	}
	
	private Context context;
	
	private FileImageReader(Context context) {
		this.context = context;
	}
	
	public static void initialize(Context context) {
		sharedInstance = new FileImageReader(context);
	}
	
	public static void destroy() {
		sharedInstance = null;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	/**
	 * Loads the specified image and returns its byte data
	 */
	public byte[] getBytesFromFile(String fileName, ImageFileAttribute.FileType fileType) {
		File file = new File(FileImageWriter.getInstance().getFilePath() + "/" +fileName + ImageFileAttribute.getFileExtension(fileType));
		
		try {
			if(file.exists()) {
				FileInputStream inputStream = new FileInputStream(file);
				
				byte[] readBytes = new byte[(int) file.length()];
				inputStream.read(readBytes);
				inputStream.close();
				
				return readBytes;
			}
			else {
				Log.e(TAG, fileName + " does not exist in " +file.getAbsolutePath()+ " !");
				return null;
			}
		} catch(IOException e) {
			Log.e(TAG, "Error reading file " +e.getMessage());
			return null;
		}
	}

	/**
	 * Reads an image from file and returns its matrix form represented by openCV
	 * @param fileName
	 * @return
	 */
	public Mat imReadOpenCV(String fileName, ImageFileAttribute.FileType fileType) {
		String completeFilePath = FileImageWriter.getInstance().getFilePath() + "/" + fileName + ImageFileAttribute.getFileExtension(fileType);

		Log.d(TAG, "Filepath for imread: " + completeFilePath);
		return Imgcodecs.imread(completeFilePath);
	}

	public Mat imReadFullPath(String fullPath) {
		Log.d(TAG, "Filepath for imread: " + fullPath);
		return Imgcodecs.imread(fullPath);
	}

	public Mat imReadColor(String fileName, ImageFileAttribute.FileType fileType) {
		String completeFilePath = FileImageWriter.getInstance().getFilePath() + "/" + fileName + ImageFileAttribute.getFileExtension(fileType);

		Log.d(TAG, "Filepath for imread: " + completeFilePath);
		return Imgcodecs.imread(completeFilePath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
	}

	public boolean doesImageExists(String fileName, ImageFileAttribute.FileType fileType) {
		File file = new File(FileImageWriter.getInstance().getFilePath() + "/" +fileName + ImageFileAttribute.getFileExtension(fileType));
		return file.exists();
	}

	public Bitmap loadBitmapFromFile(String fileName, ImageFileAttribute.FileType fileType) {
		String completeFilePath = FileImageWriter.getInstance().getFilePath() + "/" + fileName + ImageFileAttribute.getFileExtension(fileType);
		Log.d(TAG, "Filepath for loading bitmap: " +completeFilePath);
		return BitmapFactory.decodeFile(completeFilePath);
	}

	public Bitmap loadBitmapThumbnail(String fileName, ImageFileAttribute.FileType fileType, int width, int height) {
		Bitmap resized = ThumbnailUtils.extractThumbnail(this.loadBitmapFromFile(fileName, fileType), width, height);
		return resized;
	}

	public String getDecodedFilePath(String fileName, ImageFileAttribute.FileType fileType) {
		String completeFilePath = FileImageWriter.getInstance().getFilePath() + "/" + fileName + ImageFileAttribute.getFileExtension(fileType);
		return completeFilePath;
	}
}
