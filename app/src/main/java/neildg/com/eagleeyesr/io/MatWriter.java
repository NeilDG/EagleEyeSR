package neildg.com.eagleeyesr.io;

import org.opencv.core.Mat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Debugging utility class for mat writing on file.
 * Created by NeilDG on 6/2/2016.
 */
public class MatWriter {
    private final static String TAG = "MatWriter";

    public static void writeMat(Mat mat, String directory, String fileName) {
        File dirFile = new File(DirectoryStorage.getSharedInstance().getProposedPath() + "/" + directory);
        if(dirFile.mkdirs() == false) {
            dirFile.mkdir();
        }

        File file = new File(dirFile.getPath(), fileName + ".txt");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

            for(int row = 0; row < mat.rows(); row++) {
                String values = "";
                for(int col = 0; col < mat.cols(); col++) {
                    values += getFormattedVal(mat, row, col) + " || ";
                }

                writer.write(values);
                writer.newLine();
            }

            writer.flush();
            writer.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeMat(Mat mat, String fileName) {
        File dirFile = new File(DirectoryStorage.getSharedInstance().getProposedPath() + "/");
        if(dirFile.mkdirs() == false) {
            dirFile.mkdir();
        }

        File file = new File(dirFile.getPath(), fileName + ".txt");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

            for(int row = 0; row < mat.rows(); row++) {
                String values = "";
                for(int col = 0; col < mat.cols(); col++) {
                    values += getFormattedVal(mat, row, col) + " || ";
                }

                writer.write(values);
                writer.newLine();
            }

            writer.flush();
            writer.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFormattedVal(Mat mat, int row, int col) {
        double[] values = mat.get(row, col);

        String formatted = "";
        for(int i = 0;i < values.length; i++) {
            formatted += values[i] + "&";
        }

        return formatted;
    }
}
