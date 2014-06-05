package net.recommenders.rival.examples.movielens100k;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class DataDownloader {
    public static final File dataFolder = new File("data");
    public static String url;
    public DataDownloader(String url){
        this.url = url;
    }

    public static void main(String[] args) {
        String url = "http://files.grouplens.org/datasets/movielens/ml-100k.zip";
        DataDownloader dd = new DataDownloader(url);
        dd.downloadAndUnzip();
    }

    public void downloadAndUnzip(){
        URL dataURL = null;
        String fileName = dataFolder + "/" + url.substring(url.lastIndexOf("/")+1);
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
