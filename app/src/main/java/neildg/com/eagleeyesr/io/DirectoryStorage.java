package neildg.com.eagleeyesr.io;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by neil.dg on 3/16/16.
 */
public class DirectoryStorage {
    private final static String TAG = "DirectoryStorage";

    public final static String ALBUM_NAME_PREFIX = "/SR";
    public final static String DEBUG_FILE_PREFIX = "/DEBUG";

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
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + FileImageWriter.ALBUM_NAME_PREFIX + albumNumber);

        return file.isDirectory() && file.exists();
    }

    private void identifyDir() {
        //identify directory index first
        /*while(this.isAlbumDirExisting(this.startingAlbum)) {
            this.startingAlbum++;
        }
        //create path
        this.refreshProposedPath(); */


        this.refreshProposedPath();
        if(this.isAlbumDirExisting(this.startingAlbum)) {
            FileImageWriter.recreateDirectory(this.proposedPath);
        }
    }

    public void createDirectory() {

        this.identifyDir();

        File filePath = new File(this.proposedPath);
        filePath.mkdirs();

        Log.d(TAG, "Image storage is set to: " + proposedPath);
    }

    public void refreshProposedPath() {
        this.proposedPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + ALBUM_NAME_PREFIX + this.startingAlbum;
    }

    public String getProposedPath() {
        return this.proposedPath;
    }

    public String getDebugFilePath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + DEBUG_FILE_PREFIX;
    }

}
