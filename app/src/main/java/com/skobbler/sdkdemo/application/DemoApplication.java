package com.skobbler.sdkdemo.application;


import android.util.Log;

import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKAnnotationText;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.positioner.SKPosition;
import com.skobbler.ngx.routing.SKExtendedRoutePosition;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.ngx.util.SKComputingDistance;
import com.skobbler.sdkdemo.model.DownloadPackage;

import java.util.List;
import java.util.Map;


/**
 * Class that stores global application state
 */
public class DemoApplication {

    private static final String TAG = DemoApplication.class.getSimpleName();
    private SKPosition currentPosition = new SKPosition(50.441782d,30.488273d);

    private double distanceToPutMark;

    private DemoApplication()
    {
        setRouteEndpointId(-1);
        setRouteEndpoints(new SKCoordinate[]
                        {
                                new SKCoordinate( 30.488273, 50.441782),
                                new SKCoordinate( 30.8939274, 50.33822199999999)
                        }
        );

        distanceToPutMark = 1000d;
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


        SKCoordinate coordinate = new SKCoordinate(
                latlng[0], latlng[1]);

        addRouteEndPoint(coordinate);

        /*if (getRouteEndpointId()==1)
            launchRouteCalculation();*/
    }

    public void addRouteEndPoint(SKCoordinate coordinate) {
        SKAnnotation annotation = new SKAnnotation();
        getRouteEndpoints()[getRouteEndpointId()] = coordinate;

        annotation.setLocation(coordinate);
        SKAnnotationText annTxt = new SKAnnotationText();
        annTxt.setText(getRouteEndpointId() == 0 ? "Start": "Finish");
        annotation.setText(annTxt);
        annotation.setUniqueID(getRouteEndpointId());
        mapView.addAnnotation(annotation);
    }

    public void addRouteEndPointAnnotations()
    {
        getMapView().deleteAllAnnotationsAndCustomPOIs();
        for(int i=0; i<routeEndpoints.length; i++) {
            setRouteEndpointId(i);
            addRouteEndPoint(routeEndpoints[i]);
        }
        setRouteEndpointId(-1);
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
        route.setExtendedPointsReturned(true);

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

    public void setMarksOnRegularDistances(List<SKExtendedRoutePosition> routePoints)
    {
        addRouteEndPointAnnotations();
        Log.d(TAG, "test routePoints");
        if (routePoints == null) {
            Log.d(TAG, "routePoints is null");
            return;
        }


        int annotationId = 2;

        double[] distanceToPoints = computeDistanceToPoints(routePoints);

        int k=1;
        double previousToCurrent = distanceToPoints[k];

        for(int i=0; i<(int)Math.floor(distanceToPoints[distanceToPoints.length-1]/distanceToPutMark); ++i){
            double markPosition = i * distanceToPutMark;
            while (distanceToPoints[k]< markPosition) {
                k++;
                previousToCurrent =distanceToPoints[k] - distanceToPoints[k-1];
            }
            double previousToMark = markPosition - distanceToPoints[k-1];
            double alpha = previousToMark/previousToCurrent;

            SKCoordinate markLocation = new SKCoordinate(
                    routePoints.get(k-1).getLongitude() *(1d-alpha) + routePoints.get(k).getLongitude() * alpha,
                    routePoints.get(k-1).getLatitude() *(1d-alpha) + routePoints.get(k).getLatitude() * alpha
                    );

            SKAnnotation annotation = new SKAnnotation();
            // The image should be a power of 2. _( 32x32, 64x64, etc)
            annotation.setImagePath(getMapResourcesDirPath()+".Common/logo.png");

            annotation.setImageSize(10);
            annotation.setLocation(markLocation);
            SKAnnotationText annTxt = new SKAnnotationText();
            String annString = String.format("%d", i);
            annTxt.setText(annString);
            String msg = String.format("The route at %d km has a point with latitude %f and longitude %f",
                    i,
                    markLocation.getLatitude(),
                    markLocation.getLongitude()
            );
            Log.d(TAG, msg);
            //System.out.println(msg);
            //System.err.println(msg);
            annotation.setText(annTxt);
            annotation.setUniqueID(annotationId++);
            mapView.addAnnotation(annotation);

        }
    }

    static public double[] computeDistanceToPoints(List<SKExtendedRoutePosition> routePoints) {
        SKExtendedRoutePosition previousPoint = routePoints.get(0);
        SKExtendedRoutePosition currentPoint;


        double routeLength = 0d;
        double[] distanceToPoints = new double[routePoints.size()];

        for(int i=1; i<routePoints.size(); i++, previousPoint=currentPoint) {
            currentPoint = routePoints.get(i);

            double distanceFromPreviousToCurrent = SKComputingDistance.distanceBetween(
                    previousPoint.getLongitude(), previousPoint.getLatitude(),
                    currentPoint.getLongitude(), currentPoint.getLatitude()
            );

            routeLength += distanceFromPreviousToCurrent;
            distanceToPoints[i]=routeLength;
        }
        return distanceToPoints;
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
