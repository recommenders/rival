/*
 * Copyright 2015 recommenders.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
     * @param theUrl the URL from where the file will be downloaded
     * @param theFolder the folder where the file will be uncompressed
     */
    public DataDownloader(final String theUrl, final String theFolder) {
        this.url = theUrl;
        this.folder = theFolder;
    }

    /**
     * Main method.
     *
     * @param args argument (not used)
     */
    public static void main(final String[] args) {
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
            assert dataURL != null;
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
                assert dataURL != null;
                FileUtils.copyURLToFile(dataURL, compressedData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ZipFile zipFile = new ZipFile(compressedData);
            File dataFolder = new File(folder);
            zipFile.extractAll(dataFolder.getCanonicalPath());
        } catch (ZipException | IOException e) {
            e.printStackTrace();
        }
    }
}
