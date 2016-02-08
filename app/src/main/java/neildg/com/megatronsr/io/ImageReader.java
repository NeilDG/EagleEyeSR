/**
 * 
 */
package neildg.com.megatronsr.io;

import android.content.Context;
import android.os.Environment;
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
public class ImageReader {
	private final static String TAG = "SR_ImageReader";
	
	private static ImageReader sharedInstance = null;
	public static ImageReader getInstance() {
		/*if(sharedInstance == null) {
			sharedInstance = new ImageReader();
		}*/
		
		return sharedInstance;
	}
	
	private Context context;
	
	private ImageReader(Context context) {
		this.context = context;
	}
	
	public static void initialize(Context context) {
		sharedInstance = new ImageReader(context);
	}
	
	public static void destroy() {
		
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public boolean isAlbumDirExisting(int albumNumber) {
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + ImageWriter.ALBUM_NAME_PREFIX + albumNumber);
		
		return file.isDirectory() && file.exists();
	}
	
	/**
	 * Loads the specified image and returns its byte data
	 */
	public byte[] getBytesFromFile(String fileName) {
		File file = new File(ImageWriter.getInstance().getFilePath() + "/" +fileName);
		
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
	public Mat imReadOpenCV(String fileName) {
		String completeFilePath = ImageWriter.getInstance().getFilePath() + "/" +fileName;
		
		Log.d(TAG, "Filepath for imread: " +completeFilePath);
		return Imgcodecs.imread(completeFilePath);
	}
}
