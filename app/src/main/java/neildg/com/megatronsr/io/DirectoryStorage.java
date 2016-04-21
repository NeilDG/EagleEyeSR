package neildg.com.megatronsr.io;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by neil.dg on 3/16/16.
 */
public class DirectoryStorage {
    private final static String TAG = "DirectoryStorage";

    public final static String ALBUM_NAME_PREFIX = "/SR";

    private static DirectoryStorage sharedInstance = null;

    public static DirectoryStorage getSharedInstance() {
        if(sharedInstance == null) {
            sharedInstance = new DirectoryStorage();
        }

        return sharedInstance;
    }

    private int startingAlbum = 0;
    private String proposedPath;

    private DirectoryStorage() {

    }

    public boolean isAlbumDirExisting(int albumNumber) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + ImageWriter.ALBUM_NAME_PREFIX + albumNumber);

        return file.isDirectory() && file.exists();
    }

    private void identifyDir() {
        //identify directory index first
        while(this.isAlbumDirExisting(this.startingAlbum)) {
            this.startingAlbum++;
        }

        //create path
        this.proposedPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + ALBUM_NAME_PREFIX + this.startingAlbum;
    }

    public void createDirectory() {

        this.identifyDir();

        File filePath = new File(this.proposedPath);
        filePath.mkdirs();

        Log.d(TAG, "Image storage is set to: " + proposedPath);
    }

    public String getProposedPath() {
        return this.proposedPath;
    }

}
