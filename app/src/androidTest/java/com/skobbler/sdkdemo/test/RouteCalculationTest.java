package com.skobbler.sdkdemo.test;

import android.test.AndroidTestCase;
import android.util.Log;

import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.routing.SKExtendedRoutePosition;
import com.skobbler.ngx.routing.SKRouteListener;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.sdkdemo.R;
import com.skobbler.sdkdemo.application.App;
import com.skobbler.sdkdemo.util.RoutePointsHelper;

import java.util.List;

public class RouteCalculationTest extends AndroidTestCase implements SKRouteListener {

    private static final String TAG = RouteCalculationTest.class.getSimpleName();
    private boolean initialized = false;
    private Object routeCalculationFinished = new Object();
    private volatile boolean routeCalculated = false;

    private int statusMessage;
    private List<SKExtendedRoutePosition> routePointsForRoute;

    public void setUp() throws Exception {
        super.setUp();

        if (initialized) {
            return;
        }

        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();

        // Setup map resource directory using context
        App.getInstance().setupMapResourcesDirPath(getContext());

        // set path to map resources and initial map style
        initMapSettings.setMapResourcesPaths(App.getInstance().getMapResourcesDirPath(),
                new SKMapViewStyle(App.getInstance().getMapResourcesDirPath() + "daystyle/", "daystyle.json"));

        SKMaps.getInstance().initializeSKMaps(getContext(), initMapSettings, getContext().getString(R.string.API_KEY));

        // set the route listener to be notified of route calculation
        // events
        SKRouteManager.getInstance().setRouteListener(this);

        App.getInstance().launchRouteCalculation();
    }

    public void tearDown() throws Exception {

    }

    public void testWasRouteCalculatedSuccessfully() {
        waitForRouteCalculationFinish();
        assertEquals("Is route calculated successfully?", ROUTE_SUCCESS, this.statusMessage);
        Log.d(TAG,
                String.format("The rote was calculated successfully and consist of %d points\n",
                        routePointsForRoute.size())
        );
    }

    public void testIsRoute37kmLength() {
        waitForRouteCalculationFinish();
        SKAnnotation[] annotations = RoutePointsHelper.calculateMarksOnRegularDistances(routePointsForRoute, 1000d, null);

        assertEquals("Is route from the railway station in Kyiv to the Boryspil airport has length of 37 km?",
                37, annotations.length);
    }

    public void waitForRouteCalculationFinish() {
        while (!routeCalculated) {
            try {
                synchronized (routeCalculationFinished) {
                    routeCalculationFinished.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRouteCalculationCompleted(int statusMessage,
                                            int routeDistance,
                                            int routeEta,
                                            boolean thisRouteIsComplete,
                                            int id) {
        this.statusMessage = statusMessage;
        this.routePointsForRoute = SKRouteManager.getInstance().getExtendedRoutePointsForRoute(id);
        this.routeCalculated = true;
        synchronized (routeCalculationFinished) {
            routeCalculationFinished.notify();
        }
    }

    @Override
    public void onAllRoutesCompleted() {

    }

    @Override
    public void onServerLikeRouteCalculationCompleted(int i) {

    }

    @Override
    public void onOnlineRouteComputationHanging(int i) {

    }
}