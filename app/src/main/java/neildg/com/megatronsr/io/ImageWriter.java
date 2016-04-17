/**
 * 
 */
package neildg.com.megatronsr.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import neildg.com.megatronsr.camera.OldCameraManager;
import neildg.com.megatronsr.capture.ImageSequencesHolder;
import neildg.com.megatronsr.platformtools.utils.notifications.NotificationListener;
import neildg.com.megatronsr.platformtools.utils.notifications.Parameters;

/**
 * Writes images to external directory
 * @author NeilDG
 *
 */
public class ImageWriter implements NotificationListener {
	private final static String TAG = "SR_ImageWriter";
	
	private static ImageWriter sharedInstance = null;
	public static ImageWriter getInstance() {
		/*if(sharedInstance == null) {
			sharedInstance = new ImageWriter();
		}*/
		
		return sharedInstance;
	}
	
	public final static String ALBUM_NAME_PREFIX = "/MegatronSR";
	public final static String ORIGINAL_IMAGE_NAME = "original.jpg";
	public final static String PROCESSED_IMAGE_NAME = "processed.jpg";
	
	private Context context;
	private String proposedPath;
	
	private ImageWriter(Context context) {
		this.context = context;
		DirectoryStorage.getSharedInstance().createDirectory();

		this.proposedPath = DirectoryStorage.getSharedInstance().getProposedPath();
	}
	
	public static void initialize(Context context) {
		
		if(sharedInstance == null) {
			sharedInstance = new ImageWriter(context);
			
			//also initialize image reader
			ImageReader.initialize(context);
		}
	}
	
	public static void destroy() {
		//also destroy image reader
		ImageReader.destroy();
	}
	
	/*
	 * Starts writing to the specified directory
	 */
	public void startWriting() {
		
		//save original image
		try {
			byte[] imageData = ImageSequencesHolder.getInstance().getOriginalImageData();
			
			if(imageData != null) {
				File originalImageFile = new File(this.proposedPath, ORIGINAL_IMAGE_NAME);
				FileOutputStream fos = new FileOutputStream(originalImageFile);
				
				fos.write(imageData);
				fos.close();
			}
			
		}
		catch(IOException e) {
			Log.e(TAG, "Error writing original image: " +e.getMessage());
		}
		
		//save image sequences
		try {

			Camera.Parameters parameters = OldCameraManager.getInstance().requestCamera().getParameters();
			Size size = parameters.getPreviewSize(); 
			
			for(int i = 0; i < ImageSequencesHolder.getInstance().getImageToProcessSize(); i++) {
				byte[] imageData = ImageSequencesHolder.getInstance().getImageDataAt(i);
				
				if(imageData != null) {
			       
			        YuvImage image = new YuvImage(imageData, parameters.getPreviewFormat(), 
			                size.width, size.height, null); 
					File originalImageFile = new File(this.proposedPath, (i) + ".jpg");
					FileOutputStream fos = new FileOutputStream(originalImageFile);
					
					image.compressToJpeg( 
			                new Rect(0, 0, image.getWidth(), image.getHeight()), 90, 
			                fos); 
					fos.close();
				}
			}
			
		}
		catch(IOException e) {
			Log.e(TAG, "Error writing original image: " +e.getMessage());
		}
	}
	
	public void saveProcessedImage(byte[] imageData) {
		//save processed image
		try {
			if(imageData != null) {
				File processedImageFile = new File(this.proposedPath, PROCESSED_IMAGE_NAME);
				FileOutputStream fos = new FileOutputStream(processedImageFile);
				fos.write(imageData);
				fos.close();
			}
			
		}
		catch(IOException e) {
			Log.e(TAG, "Error writing original image: " +e.getMessage());
		}
	}
	
	/**
	 * Saves a given image for viewing or later reuse in processing.
	 * Images saved are automatically appended with a JPEG extension.
	 * @param imageData
	 */
	public void saveSpecifiedImage(byte[] imageData, String fileName) {
		try {
			if(imageData != null) {
				File processedImageFile = new File(this.proposedPath, fileName + ".jpg");
				FileOutputStream fos = new FileOutputStream(processedImageFile);
				fos.write(imageData);
				fos.close();
			}
		}
		catch(IOException e) {
			Log.e(TAG, "Error writing image: " +e.getMessage());
		}
	}

	public void saveBitmapImage(Bitmap bitmap, String fileName) {

        try {
            File processedImageFile = new File(this.proposedPath, fileName + ".jpg");
            FileOutputStream out = new FileOutputStream(processedImageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            // NOTE: PNG is a lossless format, the compression factor (100) is ignored
			out.close();
            Log.d(TAG, "Saved: " +processedImageFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void saveMatrixToImage(Mat mat, String fileName) {
		File imageFile = new File(this.proposedPath, fileName + ".jpg");
		Imgcodecs.imwrite(imageFile.getAbsolutePath(), mat);

		Log.d(TAG, "Saved " + imageFile.getAbsolutePath());
	}


	@Override
	public void onNotify(String notificationString, Parameters params) {
		
	}
	
	public String getFilePath() {
		return this.proposedPath;
	}
}
