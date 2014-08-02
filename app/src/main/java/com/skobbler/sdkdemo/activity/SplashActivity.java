package com.skobbler.sdkdemo.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.navigation.SKAdvisorSettings;
import com.skobbler.ngx.util.SKLogging;
import com.skobbler.ngx.versioning.SKMapUpdateListener;
import com.skobbler.ngx.versioning.SKVersioningManager;
import com.skobbler.sdkdemo.R;
import com.skobbler.sdkdemo.application.App;
import com.skobbler.sdkdemo.util.DemoUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Activity that installs required resources (from assets/MapResources.zip) to
 * the device
 * 
 */
public class SplashActivity extends Activity implements SKPrepareMapTextureListener, SKMapUpdateListener {


    private static final String TAG = SplashActivity.class.getSimpleName();
    public static final String SKMAPS_DIR = "/SKMaps/";
    /**
     * Path to the MapResources directory
     */
    private String mapResourcesDirPath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        SKLogging.enableLogs(true);
        File externalDir = getExternalFilesDir(null);
        
        // determine path where map resources should be copied on the device
        mapResourcesDirPath = (externalDir != null ? externalDir : getFilesDir() ) + SKMAPS_DIR;

        App.getInstance().setMapResourcesDirPath(mapResourcesDirPath);

        
        if (!new File(mapResourcesDirPath).exists()) {
            // if map resources are not already present copy them to
            // mapResourcesDirPath in the following thread
            new SKPrepareMapTextureThread(this, mapResourcesDirPath, "SKMaps.zip", this).start();
            // copy some other resource needed
            copyOtherResources();
            prepareMapCreatorFile();
            App.getInstance().copyMarkImage(this);
        } else {
            // map resources have already been copied - start the map activity
            Toast.makeText(SplashActivity.this, "Map resources copied in a previous run", Toast.LENGTH_SHORT).show();
            prepareMapCreatorFile();
            App.getInstance().copyMarkImage(this);
            initializeLibrary();
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            finish();
                            startActivity(new Intent(SplashActivity.this, MapActivity.class));
                        }
                    });
                }
            }, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onMapTexturesPrepared(boolean prepared) {
        initializeLibrary();
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                Toast.makeText(SplashActivity.this, "Map resources were copied", Toast.LENGTH_SHORT).show();
                

                finish();
                startActivity(new Intent(SplashActivity.this, MapActivity.class));
            }
        });
    }
    
    /**
     * Copy some additional resources from assets
     */
    private void copyOtherResources() {
        new Thread() {
            
            public void run() {
                try {
                    String tracksPath = mapResourcesDirPath + "GPXTracks";
                    File tracksDir = new File(tracksPath);
                    if (!tracksDir.exists()) {
                        boolean mkdirsOk = tracksDir.mkdirs();
                        if (!mkdirsOk)
                            Log.d(TAG, "Error making dirs");
                    }
                    DemoUtils.copyAssetsToFolder(getAssets(), "GPXTracks", mapResourcesDirPath + "GPXTracks");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    
    /**
     * Copies the map creator file from assets to a storage.
     */
    private void prepareMapCreatorFile() {
        final App app = App.getInstance();
        final Thread prepareGPXFileThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    
                    final String mapCreatorFolderPath = mapResourcesDirPath + "MapCreator";
                    final File mapCreatorFolder = new File(mapCreatorFolderPath);
                    // create the folder where you want to copy the json file
                    if (!mapCreatorFolder.exists()) {
                        boolean mkdirsOk = mapCreatorFolder.mkdirs();
                        if (!mkdirsOk)
                            Log.d(TAG, "Error creating mapCreator folder");
                    }
                    app.setMapCreatorFilePath(mapCreatorFolderPath + "/mapcreatorFile.json");
                    DemoUtils.copyAsset(getAssets(), "MapCreator", mapCreatorFolderPath, "mapcreatorFile.json");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        });
        prepareGPXFileThread.start();
    }

    /**
     * Initializes the SKMaps framework
     */
    private void initializeLibrary() {
        final App app = App.getInstance();
        // get object holding map initialization settings
        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();
        // set path to map resources and initial map style
        initMapSettings.setMapResourcesPaths(app.getMapResourcesDirPath(),
                new SKMapViewStyle(app.getMapResourcesDirPath() + "daystyle/", "daystyle.json"));
        
        final SKAdvisorSettings advisorSettings = initMapSettings.getAdvisorSettings();
        advisorSettings.setLanguage("en");
        advisorSettings.setAdvisorVoice("en");
        advisorSettings.setPlayInitialAdvice(true);
        advisorSettings.setPlayAfterTurnInformalAdvice(true);
        advisorSettings.setPlayInitialVoiceNoRouteAdvice(true);
        initMapSettings.setAdvisorSettings(advisorSettings);
       
        // EXAMPLE OF ADDING PREINSTALLED MAPS
        // initMapSettings.setPreinstalledMapsPath(app.getMapResourcesDirPath()
        // + "/PreinstalledMaps");
        // initMapSettings.setConnectivityMode(SKMaps.CONNECTIVITY_MODE_OFFLINE);
        
        // Example of setting light maps
        // initMapSettings.setMapDetailLevel(SKMapsInitSettings.SK_MAP_DETAIL_LIGHT);
         // initialize map using the settings object
        SKVersioningManager.getInstance().setMapUpdateListener(this);
        SKMaps.getInstance().initializeSKMaps(this, initMapSettings, getString(R.string.API_KEY));
    }

    @Override
    public void onMapVersionSet() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onNewVersionDetected(int newVersion) {
        // TODO Auto-generated method stub
        Log.e("","new version "+newVersion);
    }

    @Override
    public void onNoNewVersionDetected() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onVersionFileDownloadTimeout() {
        // TODO Auto-generated method stub
        
    }
}
