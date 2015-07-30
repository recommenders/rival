package net.recommenders.rival.examples;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class DataDownloader {

    private String url;
    private String folder;

    public DataDownloader(String url, String folder) {
        this.url = url;
        this.folder = folder;
    }

    public static void main(String[] args) {
        String url = "http://files.grouplens.org/datasets/movielens/ml-100k.zip";
        String folder = "data3/ml-100k";
        DataDownloader dd = new DataDownloader(url, folder);
        dd.downloadAndUnzip();
    }

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
