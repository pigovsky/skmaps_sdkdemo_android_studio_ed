package com.skobbler.sdkdemo.application;


import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKAnnotationText;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.positioner.SKPosition;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.sdkdemo.model.DownloadPackage;

import java.util.Map;


/**
 * Class that stores global application state
 */
public class DemoApplication {

    private SKPosition currentPosition;

    private DemoApplication()
    {
        setRouteEndpointId(-1);
        setRouteEndpoints(new SKCoordinate[2]);
    }

    private static DemoApplication _instance;

    public static DemoApplication getInstance()
    {
        if(_instance==null)
            _instance = new DemoApplication();
        return _instance;
    }

    private int routeEndpointId = -1;
    private SKCoordinate[] routeEndpoints;


    public void addRouteEndPoint(SKScreenPoint point)
    {
        if (getRouteEndpointId() <0)
            return;

        double[] mercator = mapView.screenToMercator(point);

        double[] latlng = mapView.mercatorToGps(mercator[0], mercator[1]);

        SKAnnotation annotation = new SKAnnotation();
        SKCoordinate coordinate = new SKCoordinate(
                latlng[0], latlng[1]);

        getRouteEndpoints()[getRouteEndpointId()] = coordinate;

        annotation.setLocation(coordinate);
        SKAnnotationText annTxt = new SKAnnotationText();
        annTxt.setText(getRouteEndpointId() == 0 ? "Start": "Finish");
        annotation.setText(annTxt);
        annotation.setUniqueID(getRouteEndpointId());
        mapView.addAnnotation(annotation);
    }

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

    /**
     * Launches a single route calculation
     */
    public void launchRouteCalculation() {
        // get a route object and populate it with the desired properties
        SKRouteSettings route = new SKRouteSettings();
        // set start and destination points
        route.setStartCoordinate(getRouteEndpoints()[0]);
        route.setDestinationCoordinate(getRouteEndpoints()[1]);
        // set the number of routes to be calculated
        route.setNoOfRoutes(1);
        // set the route mode
        route.setRouteMode(SKRouteSettings.SKROUTE_CAR_FASTEST);
        // set whether the route should be shown on the map after it's computed
        route.setRouteExposed(true);

        // pass the route to the calculation routine
        SKRouteManager.getInstance().calculateRoute(route);
    }


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


    public int getRouteEndpointId() {
        return routeEndpointId;
    }

    public void setRouteEndpointId(int routeEndpointId) {
        this.routeEndpointId = routeEndpointId;
    }

    public SKCoordinate[] getRouteEndpoints() {
        return routeEndpoints;
    }

    public void setRouteEndpoints(SKCoordinate[] routeEndpoints) {
        this.routeEndpoints = routeEndpoints;
    }

    public void goTo()
    {
        if (currentPosition == null || mapView == null)
            return;
        System.err.println(currentPosition);
        mapView.reportNewGPSPosition(currentPosition);
        mapView.centerMapOnCurrentPositionSmooth(17, 500);
    }

    public void goTo(SKCoordinate location) {
        currentPosition = new SKPosition(location.getLatitude(), location.getLongitude());
        goTo();
    }
}
