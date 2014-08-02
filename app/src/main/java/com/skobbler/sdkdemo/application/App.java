package com.skobbler.sdkdemo.application;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKAnnotationText;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.positioner.SKPosition;
import com.skobbler.ngx.routing.SKExtendedRoutePosition;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.sdkdemo.R;
import com.skobbler.sdkdemo.model.DownloadPackage;
import com.skobbler.sdkdemo.util.RoutePointsHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Class that stores global application state and manipulating route annotations on map view.
 *
 * @author Scout
 * @author Yuriy Pigovsky
 * @version %I%, %G%
 */
public class App {
    /**
     * Relative path (will be concatenated to some root) to hold SKMap resources on device
     */
    public static final String SKMAPS_DIR = "/SKMaps/";

    /**
     * File path of an image, which is used to mark regular distance amount on a route
     */
    public static final String MARK_PNG = ".Common/logo.png";

    /**
     * Size of the image, which is used to mark regular distance amount on a route in pixels
     */
    public static final int MARK_PNG_SIZE = 32;
    /**
     * A TAG for debugger logging messages produced by methods of this class
     */
    private static final String TAG = App.class.getSimpleName();
    /**
     * A static field holding instance of this application object
     */
    private static App instance;

    /**
     * Current geographical position shown in the map view
     */
    private SKPosition currentPosition = new SKPosition(50.441782d, 30.488273d);

    private double distanceToPutMark;

    /**
     * Current endpoint, which user is about to specify on the map view
     */
    private RoutePointsHelper.EndpointType currentRouteEndpointType = RoutePointsHelper.EndpointType.No;


    /**
     * A geographic coordinate specifying start of a route to be calculated
     */
    private SKCoordinate routeStart;

    /**
     * A geographic coordinate specifying finish of a route to be calculated
     */
    private SKCoordinate routeFinish;

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
     * A private constructor producing this application singleton.
     * By default it specifies railway station, Kyiv as the starting point, and airport Boryspil as
     * the finish point to calculate route. And 1000 meter distance as default unit
     * to put marks on the route.
     */
    private App() {
        setCurrentRouteEndpointType(RoutePointsHelper.EndpointType.No);
        routeStart = new SKCoordinate(30.488273, 50.441782);
        routeFinish = new SKCoordinate(30.8939274, 50.33822199999999);

        setDistanceToPutMark(1000d);
    }

    /**
     * Gets this unique application object. It also creates this object If it was accessed
     * for the first time,
     *
     * @return this unique application object
     */
    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    /**
     * This method restores the image used for marking route from app resources when it is
     * unavailable due to former extraction of SKMap resources by an app with the same package name
     *
     * @param context a context to acquire app resources from
     */
    public void copyMarkImage(Context context) {
        File markImage = new File(mapResourcesDirPath + MARK_PNG);

        if (!markImage.exists()) {
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
            bm = Bitmap.createScaledBitmap(bm, MARK_PNG_SIZE, MARK_PNG_SIZE, false);
            try {
                FileOutputStream outStream = new FileOutputStream(markImage);
                bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds a current route endpoint (either start of finish) depending on value of
     * <code>getCurrentRouteEndpointType()</code> at a <code>point</code> tapped on the map view.
     * and shows it on the map view as an annotation.
     * Does nothing if <code>point</code> is null or <code>getCurrentRouteEndpointType()</code> has 'No' value.
     *
     * @param point a screen point tapped in the map view
     */
    public void addRouteEndPoint(SKScreenPoint point) {
        if (point == null || getCurrentRouteEndpointType() == RoutePointsHelper.EndpointType.No) {
            return;
        }

        double[] mercator = mapView.screenToMercator(point);

        double[] latlng = mapView.mercatorToGps(mercator[0], mercator[1]);


        SKCoordinate coordinate = new SKCoordinate(
                latlng[0], latlng[1]);

        addRouteEndPoint(coordinate);
    }

    /**
     * Adds a current route endpoint (either start of finish) depending on value of
     * <code>getCurrentRouteEndpointType()</code> at a geographic <code>coordinate</code>
     * and shows it on the map view as an annotation.
     * Does nothing if <code>coordinate</code> is null or <code>getCurrentRouteEndpointType()</code>
     * has 'No' value.
     *
     * @param coordinate a gps coordinate to be added as an endpoint
     */
    public void addRouteEndPoint(SKCoordinate coordinate) {
        if (coordinate == null || getCurrentRouteEndpointType() == RoutePointsHelper.EndpointType.No) {
            return;
        }

        if (getCurrentRouteEndpointType() == RoutePointsHelper.EndpointType.Start) {
            routeStart = coordinate;
        } else if (getCurrentRouteEndpointType() == RoutePointsHelper.EndpointType.Finish) {
            routeFinish = coordinate;
        }
        drawEndpointAnnotation();
    }

    /**
     * Draws either start or finish route endpoint depending on <code>endpointType</code> argument.
     * Appropriate field (<code>routeStart</code> or <code>routeFinish</code>) should be not null
     * specifying the gps coordinate where the annotation is about to appear
     *
     * @param endpointType either Start or Finish. This method does nothing for 'No' value
     */
    private void drawEndpointAnnotation(RoutePointsHelper.EndpointType endpointType) {
        if (endpointType == RoutePointsHelper.EndpointType.No) {
            return;
        }

        SKAnnotation annotation = new SKAnnotation();
        SKAnnotationText label = new SKAnnotationText();
        if (routeStart != null && endpointType == RoutePointsHelper.EndpointType.Start) {
            annotation.setUniqueID(0);
            label.setText(getString(R.string.start));
            annotation.setLocation(routeStart);
        } else if (routeFinish != null && endpointType == RoutePointsHelper.EndpointType.Finish) {
            annotation.setUniqueID(1);
            label.setText(getString(R.string.finish));
            annotation.setLocation(routeFinish);
        }
        annotation.setText(label);

        getMapView().addAnnotation(annotation);
    }

    /**
     * Draws either start or finish route endpoint depending on
     * <code>getCurrentRouteEndpointType()</code> value.
     * Appropriate field (<code>routeStart</code> or <code>routeFinish</code>) should be not null
     * specifying the gps coordinate where the annotation is about to appear
     * <code>getCurrentRouteEndpointType()</code> should be either Start or Finish.
     * This method does nothing for 'No' value
     */
    private void drawEndpointAnnotation() {
        drawEndpointAnnotation(getCurrentRouteEndpointType());
    }

    /**
     * Gets context associated with the map view
     *
     * @return this view context
     */
    public Context getContext() {
        return getMapView().getContext();
    }

    /**
     * Gets string resource from current context
     *
     * @param id identifier of the string resources
     * @return appropriate string from resources
     */
    public String getString(int id) {
        return getContext().getString(id);
    }

    /**
     * Removes all annotations which were in use by a former route and redraws both start and
     * finish route endpoint annotations.
     */
    public void updateRouteEndPointAnnotations() {
        getMapView().deleteAllAnnotationsAndCustomPOIs();
        drawEndpointAnnotation(RoutePointsHelper.EndpointType.Start);
        drawEndpointAnnotation(RoutePointsHelper.EndpointType.Finish);
    }

    /**
     * Launches a single route calculation using specified in <code>routeStart</code> and
     * <code>routeFinish</code> endpoints.
     */
    public void launchRouteCalculation() {
        // get a route object and populate it with the desired properties
        SKRouteSettings route = new SKRouteSettings();
        route.setExtendedPointsReturned(true);

        // set start and destination points
        route.setStartCoordinate(routeStart);
        route.setDestinationCoordinate(routeFinish);
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

    public String getMapResourcesDirPath() {
        return mapResourcesDirPath;
    }

    public void setupMapResourcesDirPath(Context context) {
        File externalDir = context.getExternalFilesDir(null);

        // determine path where map resources should be copied on the device
        mapResourcesDirPath = (externalDir != null ? externalDir : context.getFilesDir() ) + SKMAPS_DIR;
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

    public RoutePointsHelper.EndpointType getCurrentRouteEndpointType() {
        return currentRouteEndpointType;
    }

    public void setCurrentRouteEndpointType(RoutePointsHelper.EndpointType currentRouteEndpointType) {
        this.currentRouteEndpointType = currentRouteEndpointType;
    }

    /**
     * Moves the map view to the geographical position, specified by <code>currentPosition</code> field.
     * Does nothing if either <code>currentPosition</code> or <code>mapView</code> is null.
     */
    public void goTo() {
        if (currentPosition == null || mapView == null) {
            return;
        }

        mapView.reportNewGPSPosition(currentPosition);
        mapView.centerMapOnCurrentPositionSmooth(17, 500);
    }

    /**
     * Moves the map view to the geographical position, specified by <code>location</code> argument
     * and changes the <code>currentPosition</code> field appropriately.
     * Does nothing if either <code>location</code> argument or <code>mapView</code> field is null.
     *
     * @param location
     */
    public void goTo(SKCoordinate location) {
        if (location == null) {
            return;
        }
        currentPosition = new SKPosition(location.getLatitude(), location.getLongitude());
        goTo();
    }

    /**
     * An unit of distance in meters. This unit is used to put marks on a route every such distance.
     */
    public double getDistanceToPutMark() {
        return distanceToPutMark;
    }

    public void setDistanceToPutMark(double distanceToPutMark) {
        this.distanceToPutMark = distanceToPutMark;
    }

    public void drawMarksOnRegularDistances(List<SKExtendedRoutePosition> routePoints) {
        updateRouteEndPointAnnotations();
        for (SKAnnotation annotation : RoutePointsHelper.calculateMarksOnRegularDistances(routePoints, getDistanceToPutMark(), getMapResourcesDirPath() + MARK_PNG)) {
            getMapView().addAnnotation(annotation);
        }
    }

}
