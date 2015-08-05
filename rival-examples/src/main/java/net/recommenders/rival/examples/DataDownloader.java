package net.recommenders.rival.examples;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

/**
 * Class used to download a file and unzips it.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class DataDownloader {

    /**
     * The URL.
     */
    private String url;
    /**
     * The folder where the file will be unzipped it.
     */
    private String folder;

    /**
     * Default constructor.
     *
     * @param url the URL from where the file will be downloaded
     * @param folder the folder where the file will be uncompressed
     */
    public DataDownloader(String url, String folder) {
        this.url = url;
        this.folder = folder;
    }

    /**
     * Main method
     *
     * @param args argument (not used)
     */
    public static void main(String[] args) {
        String url = "http://files.grouplens.org/datasets/movielens/ml-100k.zip";
        String folder = "data3/ml-100k";
        DataDownloader dd = new DataDownloader(url, folder);
        dd.downloadAndUnzip();
    }

    /**
     * Downloads the file from the provided url.
     */
    public void download() {
        URL dataURL = null;
        String fileName = folder + "/" + url.substring(url.lastIndexOf("/") + 1);
        if (new File(fileName).exists()) {
            return;
        }
        try {
            dataURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        File downloadedData = new File(fileName);
        try {
            FileUtils.copyURLToFile(dataURL, downloadedData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Downloads the file from the provided url and uncompresses it to the given
     * folder.
     */
    public void downloadAndUnzip() {
        URL dataURL = null;
        String fileName = folder + "/" + url.substring(url.lastIndexOf("/") + 1);
        File compressedData = new File(fileName);
        if (!new File(fileName).exists()) {
            try {
                dataURL = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                FileUtils.copyURLToFile(dataURL, compressedData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ZipFile zipFile = new ZipFile(compressedData);
            File dataFolder = new File(folder);
            zipFile.extractAll(dataFolder.getCanonicalPath());
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
