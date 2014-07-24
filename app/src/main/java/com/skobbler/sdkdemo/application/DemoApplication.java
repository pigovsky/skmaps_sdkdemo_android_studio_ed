package com.skobbler.sdkdemo.application;


import java.util.Map;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.sdkdemo.model.DownloadPackage;
import android.app.Application;


/**
 * Class that stores global application state
 */
public class DemoApplication extends Application {
    
    /**
     * Path to the map resources directory on the device
     */
    private String mapResourcesDirPath;
    
    /**
     * Packages obtained from parsing Maps.xml
     */
    private Map<String, DownloadPackage> packageMap;
    
    /**
     * Map view object
     */
    private SKMapSurfaceView mapView;
    
    /**
     * Absolute path to the file used for mapCreator - mapcreatorFile.json
     */
    private String mapCreatorFilePath;
    
    public Map<String, DownloadPackage> getPackageMap() {
        return packageMap;
    }
    
    public void setPackageMap(Map<String, DownloadPackage> packageMap) {
        this.packageMap = packageMap;
    }
    
    public void setMapResourcesDirPath(String mapResourcesDirPath) {
        this.mapResourcesDirPath = mapResourcesDirPath;
    }
    
    public String getMapResourcesDirPath() {
        return mapResourcesDirPath;
    }
    
    public SKMapSurfaceView getMapView() {
        return mapView;
    }
    
    public void setMapView(SKMapSurfaceView mapView) {
        this.mapView = mapView;
    }
    
    
    public String getMapCreatorFilePath() {
        return mapCreatorFilePath;
    }
    
    
    public void setMapCreatorFilePath(String mapCreatorFilePath) {
        this.mapCreatorFilePath = mapCreatorFilePath;
    }


}
