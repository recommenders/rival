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
    public static final File dataFolder = new File("data");
    public static String url;
    public static String folder;
    public DataDownloader(String url, String folder){
        this.url = url;
        this.folder = folder;
    }

    public static void main(String[] args) {
        String url = "http://files.grouplens.org/datasets/movielens/ml-100k.zip";
        String folder = "data/ml-10k";
        DataDownloader dd = new DataDownloader(url, folder);
        dd.downloadAndUnzip();
    }

    public void download(){
        URL dataURL = null;
        String fileName = folder + "/" + url.substring(url.lastIndexOf("/")+1);
        if (new File(fileName).exists())
            return;
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

    public void downloadAndUnzip(){
        URL dataURL = null;
        String fileName = folder + "/" + url.substring(url.lastIndexOf("/")+1);
        if (new File(fileName).exists())
            return;
        try {
            dataURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        File compressedData = new File(fileName);
        try {
            FileUtils.copyURLToFile(dataURL, compressedData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ZipFile zipFile = new ZipFile(compressedData);
            zipFile.extractAll(dataFolder.getCanonicalPath());
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//        FileUtils.deleteQuietly(compressedData);
//            FileUtils.deleteDirectory(dataFolder);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }



}
