package com.skobbler.sdkdemo.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.content.res.AssetManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.common.io.ByteStreams;


public class DemoUtils {
    
    /**
     * Gets formatted time from a given number of seconds
     * @param timeInSec
     * @return
     */
    public static String formatTime(int timeInSec) {
        StringBuilder builder = new StringBuilder();
        int hours = timeInSec / 3600;
        int minutes = (timeInSec - hours * 3600) / 60;
        int seconds = timeInSec - hours * 3600 - minutes * 60;
        builder.insert(0, seconds + "s");
        if (minutes > 0 || hours > 0) {
            builder.insert(0, minutes + "m ");
        }
        if (hours > 0) {
            builder.insert(0, hours + "h ");
        }
        return builder.toString();
    }
    
    /**
     * Formats a given distance value (given in meters)
     * @param distInMeters
     * @return
     */
    public static String formatDistance(int distInMeters) {
        if (distInMeters < 1000) {
            return distInMeters + "m";
        } else {
            return ((float) distInMeters / 1000) + "km";
        }
    }
    
    /**
     * Copies files from assets to destination folder
     * @param assetManager
     * @param sourceFolder
     * @param destination
     * @throws IOException
     */
    public static void copyAssetsToFolder(AssetManager assetManager, String sourceFolder, String destinationFolder)
            throws IOException {
        final String[] assets = assetManager.list(sourceFolder);
        
        final File destFolderFile = new File(destinationFolder);
        if (!destFolderFile.exists()) {
            destFolderFile.mkdirs();
        }
        copyAsset(assetManager, sourceFolder, destinationFolder, assets);
    }
    
    /**
     * Copies files from assets to destination folder
     * @param assetManager
     * @param sourceFolder
     * @param destination
     * @param assetsNames
     * @throws IOException
     */
    public static void copyAsset(AssetManager assetManager, String sourceFolder, String destinationFolder,
            String... assetsNames) throws IOException {
        
        for (String assetName : assetsNames) {
            OutputStream destinationStream = new FileOutputStream(new File(destinationFolder + "/" + assetName));
            String[] files = assetManager.list(sourceFolder + "/" + assetName);
            if (files == null || files.length == 0) {
                
                InputStream asset = assetManager.open(sourceFolder + "/" + assetName);
                try {
                    ByteStreams.copy(asset, destinationStream);
                } finally {
                    asset.close();
                    destinationStream.close();
                }
            }
        }
    }
    
    /**
     * Copies files from assets to destination folder.
     * @param assetManager
     * @param assetName the asset that needs to be copied
     * @param destinationFolder path to folder where you want to store the asset
     * archive
     * @throws IOException
     */
    public static void copyAsset(AssetManager assetManager, String assetName, String destinationFolder)
            throws IOException {
        
        OutputStream destinationStream = new FileOutputStream(new File(destinationFolder + "/" + assetName));
        InputStream asset = assetManager.open(assetName);
        try {
            ByteStreams.copy(asset, destinationStream);
        } finally {
            asset.close();
            destinationStream.close();
        }
    }
    
    /**
     * Tells if internet is currently available on the device
     * @param currentContext
     * @return
     */
    public static boolean isInternetAvailable(Context currentContext) {
        ConnectivityManager conectivityManager =
                (ConnectivityManager) currentContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                if (networkInfo.isConnected()) {
                    return true;
                }
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (networkInfo.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if the current device has a GPS module (hardware)
     * @return true if the current device has GPS
     */
    public static boolean hasGpsModule(final Context context) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        for (final String provider : locationManager.getAllProviders()) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                return true;
            }
        }
        return false;
    }
}