package com.skobbler.sdkdemo.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skobbler.ngx.packages.SKPackageManager;
import com.skobbler.sdkdemo.R;
import com.skobbler.sdkdemo.application.App;
import com.skobbler.sdkdemo.model.DownloadPackage;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


/**
 * Activity in which map package downloads are performed
 * 
 */
public class DownloadActivity extends Activity {
    
    private static final int NO_BYTES_INTO_ONE_MB = 1048576;
    
    /**
     * Path at which download packages are temporarily stored
     */
    private static String packagesPath;
    
    private App application;
    
    private ProgressBar progressBar;
    
    private Button startDownloadButton;
    
    private TextView downloadPercentage;
    
    /**
     * Selected map package to be downloaded
     */
    private DownloadPackage dowloadPackage;
    
    /**
     * Index of the current download resource
     */
    private int downloadResourceIndex;
    
    /**
     * URLs to download resources
     */
    private List<String> downloadResourceUrls;
    
    /**
     * Download resources extensions
     */
    private List<String> downloadResourceExtensions;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = App.getInstance();
        setContentView(R.layout.activity_download);
        packagesPath = application.getMapResourcesDirPath() + "/Maps/downloads/";
        progressBar = (ProgressBar) findViewById(R.id.download_progress_bar);
        startDownloadButton = (Button) findViewById(R.id.download_button);
        downloadPercentage = (TextView) findViewById(R.id.download_percentage_text);
        dowloadPackage = application.getPackageMap().get(getIntent().getStringExtra("packageCode"));
        startDownloadButton.setText(getResources().getString(R.string.label_download) + " " + dowloadPackage.getName());
        prepareDownloadResources();
    }
    
    /**
     * Prepares a list of download resources for the selected package to be
     * downloaded
     */
    private void prepareDownloadResources() {
        downloadResourceUrls = new ArrayList<String>();
        downloadResourceExtensions = new ArrayList<String>();
        downloadResourceIndex = 0;
        
        // the resources to be downloaded for the selected package will be:
        // - the .skm file (the map)
        // - the textures file (.txg)
        // - the name-browser files (.ngi, .ngi.dat) necessary for offline
        // searches
        
        downloadResourceUrls.add(SKPackageManager.getInstance().getURLInfoForPackageWithCode(dowloadPackage.getCode())
                .getMapURL());
        downloadResourceExtensions.add(".skm");
        
        downloadResourceUrls.add(SKPackageManager.getInstance().getURLInfoForPackageWithCode(dowloadPackage.getCode())
                .getTexturesURL());
        downloadResourceExtensions.add(".txg");
        
        String nbFilesZipUrl =
                SKPackageManager.getInstance().getURLInfoForPackageWithCode(dowloadPackage.getCode())
                        .getNameBrowserFilesURL();
        downloadResourceUrls.add(nbFilesZipUrl.replaceFirst("\\.zip", ".ngi"));
        downloadResourceExtensions.add(".ngi");
        downloadResourceUrls.add(nbFilesZipUrl.replaceFirst("\\.zip", ".ngi.dat"));
        downloadResourceExtensions.add(".ngi.dat");
    }
    
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download_button:
                startDownloadButton.setEnabled(false);
                downloadResource(downloadResourceUrls.get(0), downloadResourceExtensions.get(0));
                break;
            default:
                break;
        }
    }
    
    /**
     * Downloads a resource from the server
     * @param url URL to the download resource
     * @param extension extension of the resource
     */
    private void downloadResource(final String url, final String extension) {
        // thread download a remote resource
        Thread downloadThread = new Thread() {
            
            private long lastProgressUpdateTime = System.currentTimeMillis();
            
            @Override
            public void run() {
                super.run();
                // get the request used to download the resource at the URL
                HttpGet request = new HttpGet(url);
                DefaultHttpClient httpClient = new DefaultHttpClient();
                try {
                    // execute the request
                    HttpResponse response = httpClient.execute(request);
                    InputStream responseStream = response.getEntity().getContent();
                    if (!new File(packagesPath).exists()) {
                        new File(packagesPath).mkdirs();
                    }
                    // local file at temporary path where the resource is
                    // downloaded
                    RandomAccessFile localFile =
                            new RandomAccessFile(packagesPath + dowloadPackage.getCode() + extension, "rw");
                    
                    // download the resource to the temporary path
                    long bytesRead = localFile.length();
                    localFile.seek(bytesRead);
                    byte[] data = new byte[NO_BYTES_INTO_ONE_MB];
                    while (true) {
                        final int actual = responseStream != null ? responseStream.read(data, 0, data.length) : 0;
                        if (actual > 0) {
                            bytesRead += actual;
                            localFile.write(data, 0, actual);
                            if (downloadResourceExtensions.get(downloadResourceIndex).equals(".skm")) {
                                // notify the UI about progress (in case of the
                                // SKM download resource)
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastProgressUpdateTime > 100) {
                                    updateDownloadProgress(bytesRead, dowloadPackage.getSize());
                                    lastProgressUpdateTime = currentTime;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    localFile.close();
                    // notify that the download was finished
                    updateOnFinishDownload();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        // start downloading in the thread
        downloadThread.start();
    }
    
    /**
     * Update the progress bar to show the progress of the SKM resource download
     * @param downloadedSize size downloaded so far
     * @param totalSize total size of the resource
     */
    private void updateDownloadProgress(final long downloadedSize, final long totalSize) {
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                int progress = (int) (progressBar.getMax() * ((float) downloadedSize / totalSize));
                progressBar.setProgress(progress);
                downloadPercentage.setText(((float) progress / 10) + "%");
            }
        });
    }
    
    /**
     * Update when a resource download was completed
     */
    private void updateOnFinishDownload() {
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                progressBar.setProgress(progressBar.getMax());
                if (downloadResourceExtensions.get(downloadResourceIndex).equals(".skm")) {
                    downloadPercentage.setText("100%");
                }
                if (downloadResourceIndex >= downloadResourceUrls.size() - 1) {
                    // if the download of the last resource was completed -
                    // install the package
                    SKPackageManager.getInstance().addOfflinePackage(packagesPath, dowloadPackage.getCode());
                    // at this point the downloaded package should be available
                    // offline
                    Toast.makeText(DownloadActivity.this.getApplicationContext(),
                            "Map of " + dowloadPackage + " is now available offline", Toast.LENGTH_SHORT).show();
                } else {
                    // start downloading the next queued resource from the
                    // package
                    downloadResourceIndex++;
                    downloadResource(downloadResourceUrls.get(downloadResourceIndex),
                            downloadResourceExtensions.get(downloadResourceIndex));
                }
            }
        });
    }
}