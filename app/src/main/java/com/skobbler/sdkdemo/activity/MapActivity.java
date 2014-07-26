package com.skobbler.sdkdemo.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.skobbler.ngx.SKCategories.SKPOICategory;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKCircle;
import com.skobbler.ngx.map.SKCoordinateRegion;
import com.skobbler.ngx.map.SKMapCustomPOI;
import com.skobbler.ngx.map.SKMapPOI;
import com.skobbler.ngx.map.SKMapSettings.SKMapFollowerMode;
import com.skobbler.ngx.map.SKMapSurfaceListener;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapSurfaceView.SKAnimationType;
import com.skobbler.ngx.map.SKMapViewHolder;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKPolygon;
import com.skobbler.ngx.map.SKPolyline;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.map.realreach.SKRealReachListener;
import com.skobbler.ngx.map.realreach.SKRealReachSettings;
import com.skobbler.ngx.navigation.SKNavigationListener;
import com.skobbler.ngx.navigation.SKNavigationManager;
import com.skobbler.ngx.navigation.SKNavigationSettings;
import com.skobbler.ngx.navigation.SKNavigationState;
import com.skobbler.ngx.poitracker.SKDetectedPOI;
import com.skobbler.ngx.poitracker.SKPOITrackerListener;
import com.skobbler.ngx.poitracker.SKPOITrackerManager;
import com.skobbler.ngx.poitracker.SKTrackablePOI;
import com.skobbler.ngx.poitracker.SKTrackablePOIType;
import com.skobbler.ngx.positioner.SKCurrentPositionListener;
import com.skobbler.ngx.positioner.SKCurrentPositionProvider;
import com.skobbler.ngx.positioner.SKPosition;
import com.skobbler.ngx.routing.SKRouteListener;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.ngx.util.SKLogging;
import com.skobbler.ngx.versioning.SKMapUpdateListener;
import com.skobbler.ngx.versioning.SKVersioningManager;
import com.skobbler.sdkdemo.R;
import com.skobbler.sdkdemo.application.DemoApplication;
import com.skobbler.sdkdemo.util.AdvicePlayer;
import com.skobbler.sdkdemo.util.DemoUtils;
import com.skobbler.sdkdemo.view.CustomCalloutView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Activity displaying the map
 * 
 * 
 */
public class MapActivity extends Activity implements SKMapSurfaceListener, SKRouteListener, SKNavigationListener,
        SKRealReachListener, SKPOITrackerListener, SKCurrentPositionListener, SensorEventListener, SKMapUpdateListener,
        View.OnClickListener, View.OnTouchListener {
    
    
    private static final String TAG = "MapActivity";
    private ToggleButton[] toggleButtonMarkRoute;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (app.getRouteEndpointId()<0)
        {
            return false;
        }
        app.addRouteEndPoint(new SKScreenPoint(motionEvent.getX(), motionEvent.getY()));
        for(ToggleButton v : toggleButtonMarkRoute)
            v.setChecked(false);
        app.setRouteEndpointId(-1);
        return false;
    }

    private enum MapOption {
        MAP_DISPLAY, MAP_OVERLAYS, ALTERNATIVE_ROUTES, MAP_STYLES, REAL_REACH, TRACKS, ANNOTATIONS, ROUTING_AND_NAVIGATION, POI_TRACKING, HEAT_MAP
    }
    
    public static SKPOICategory[] heatMapCategories;
    
    /**
     * Current option selected
     */
    private MapOption currentMapOption = MapOption.MAP_DISPLAY;
    
    /**
     * Application context object
     */
    private DemoApplication app;
    
    /**
     * Surface view for displaying the map
     */
    private SKMapSurfaceView mapView;
    
    /**
     * Options menu
     */
    private View menu;
    
    /**
     * View for selecting alternative routes
     */
    private View altRoutesView;
    
    /**
     * View for selecting the map style
     */
    private LinearLayout mapStylesView;
    
    /**
     * Buttons for selecting alternative routes
     */
    private Button[] altRoutesButtons;
    
    /**
     * Bottom button
     */
    private Button bottomButton;
    
    /**
     * The current position button
     */
    private Button positionMeButton;
    
    /**
     * The heading button
     */
    private Button headingButton;
    
    /**
     * The map popup view
     */
    private CustomCalloutView mapPopup;
    
    /**
     * Ids for alternative routes
     */
    private List<Integer> routeIds = new ArrayList<Integer>();
    
    /**
     * Currently elected annotation
     */
    private SKAnnotation selectedAnnotation;
    
    /**
     * Tells if a navigation is ongoing
     */
    private boolean navigationInProgress;
    
    /**
     * POIs to be detected on route
     */
    private Map<Integer, SKTrackablePOI> trackablePOIs;
    
    /**
     * Trackable POIs that are currently rendered on the map
     */
    private Map<Integer, SKTrackablePOI> drawnTrackablePOIs;
    
    /**
     * Tracker manager object
     */
    private SKPOITrackerManager poiTrackingManager;
    
    /**
     * Current position provider
     */
    private SKCurrentPositionProvider currentPositionProvider;
    
    /**
     * Current position
     */
    private SKPosition currentPosition;
    
    /**
     * Tells if heading is currently active
     */
    private boolean headingOn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        app = DemoApplication.getInstance();
        
        currentPositionProvider = new SKCurrentPositionProvider(this);
        currentPositionProvider.setCurrentPositionListener(this);
        
        if (DemoUtils.hasGpsModule(this)) {
            currentPositionProvider.requestLocationUpdates(true, true, true);
        }
        
        SKMapViewHolder mapViewGroup = (SKMapViewHolder) findViewById(R.id.view_group_map);
        mapView = mapViewGroup.getMapSurfaceView();
        mapView.setMapSurfaceListener(this);
        mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NONE);
        mapPopup = (CustomCalloutView) findViewById(R.id.map_popup);
        applySettingsOnMapView();
        app.setMapView(mapView);
        poiTrackingManager = new SKPOITrackerManager(this);
        
        menu = findViewById(R.id.options_menu);
        altRoutesView = findViewById(R.id.alt_routes);
        altRoutesButtons =
                new Button[] { (Button) findViewById(R.id.alt_route_1), (Button) findViewById(R.id.alt_route_2),
                        (Button) findViewById(R.id.alt_route_3) };
        mapStylesView = (LinearLayout) findViewById(R.id.map_styles);
        bottomButton = (Button) findViewById(R.id.bottom_button);
        positionMeButton = (Button) findViewById(R.id.position_me_button);
        headingButton = (Button) findViewById(R.id.heading_button);
        
        SKVersioningManager.getInstance().setMapUpdateListener(this);


        toggleButtonMarkRoute = new ToggleButton[] {
                (ToggleButton)findViewById(R.id.buttonMarkRouteStart),
                (ToggleButton)findViewById(R.id.buttonMarkRouteFinish)
        };

        for(int id : new int[]{
                R.id.buttonMarkRouteStart,
                R.id.buttonMarkRouteFinish,
                R.id.buttonZoomIn,
                R.id.buttonZoomOut,
                R.id.buttonSearchByAddressActivity
        })
            findViewById(id).setOnClickListener(this);

        // set the route listener to be notified of route calculation
        // events
        SKRouteManager.getInstance().setRouteListener(this);

        mapView.setOnTouchListener(this);
    }
    
    /**
     * Customize the map view
     */
    private void applySettingsOnMapView() {
        mapView.getMapSettings().setMapRotationEnabled(true);
        mapView.getMapSettings().setMapZoomingEnabled(true);
        mapView.getMapSettings().setMapPanningEnabled(true);
        mapView.getMapSettings().setZoomWithAnchorEnabled(true);
        mapView.getMapSettings().setInertiaRotatingEnabled(true);
        mapView.getMapSettings().setInertiaZoomingEnabled(true);
        mapView.getMapSettings().setInertiaPanningEnabled(true);
    }
    
    @SuppressLint("UseSparseArrays")
    /**
     * Populate the collection of trackable POIs
     */
    private void initializeTrackablePOIs() {
        
        trackablePOIs = new HashMap<Integer, SKTrackablePOI>();
        
        trackablePOIs.put(64142, new SKTrackablePOI(64142, 0, 37.735610, -122.446434, -1, "Teresita Boulevard"));
        trackablePOIs.put(64143, new SKTrackablePOI(64143, 0, 37.732367, -122.442033, -1, "Congo Street"));
        trackablePOIs.put(64144, new SKTrackablePOI(64144, 0, 37.732237, -122.429190, -1, "John F Foran Freeway"));
        trackablePOIs.put(64145, new SKTrackablePOI(64145, 1, 37.738090, -122.401470, -1, "Revere Avenue"));
        trackablePOIs.put(64146, new SKTrackablePOI(64146, 0, 37.741128, -122.398562, -1, "McKinnon Ave"));
        trackablePOIs.put(64147, new SKTrackablePOI(64147, 1, 37.746154, -122.394077, -1, "Evans Ave"));
        trackablePOIs.put(64148, new SKTrackablePOI(64148, 0, 37.750057, -122.392287, -1, "Cesar Chavez Street"));
        trackablePOIs.put(64149, new SKTrackablePOI(64149, 1, 37.762823, -122.392957, -1, "18th Street"));
        trackablePOIs.put(64150, new SKTrackablePOI(64150, 0, 37.760242, -122.392495, 180, "20th Street"));
        trackablePOIs.put(64151, new SKTrackablePOI(64151, 0, 37.755157, -122.392196, 180, "23rd Street"));
        
        trackablePOIs.put(64152, new SKTrackablePOI(64152, 0, 37.773526, -122.452706, -1, "Shrader Street"));
        trackablePOIs.put(64153, new SKTrackablePOI(64153, 0, 37.786535, -122.444528, -1, "Pine Street"));
        trackablePOIs.put(64154, new SKTrackablePOI(64154, 1, 37.792242, -122.424426, -1, "Franklin Street"));
        trackablePOIs.put(64155, new SKTrackablePOI(64155, 0, 37.716146, -122.409480, -1, "Campbell Ave"));
        trackablePOIs.put(64156, new SKTrackablePOI(64156, 0, 37.719133, -122.388280, -1, "Fitzgerald Ave"));
        
        drawnTrackablePOIs = new HashMap<Integer, SKTrackablePOI>();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (headingOn) {
            startOrientationSensor();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (headingOn) {
            stopOrientationSensor();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentPositionProvider.stopLocationUpdates();
        SKMaps.getInstance().destroySKMaps();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    @Override
    public void onSurfaceCreated() {
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                View chessBackground = findViewById(R.id.chess_board_background);
                chessBackground.setVisibility(View.GONE);
                app.goTo();
            }
        });
        
        if (currentMapOption.equals(MapOption.TRACKS) && TrackElementsActivity.selectedTrackElement != null) {
            if (!TrackElementsActivity.routeCalculationRequested) {
                app.getMapView().drawTrackElement(TrackElementsActivity.selectedTrackElement);
                app.getMapView().fitTrackElementInView(TrackElementsActivity.selectedTrackElement, false);
            } else {
                SKRouteManager.getInstance().setRouteListener(this);
                SKRouteManager.getInstance().createRouteFromTrackElement(TrackElementsActivity.selectedTrackElement,
                        SKRouteSettings.SKROUTE_CAR_FASTEST, 31);
            }
        }
        
        if (currentMapOption == MapOption.HEAT_MAP && heatMapCategories != null) {
            mapView.showHeatMapsWithPoiType(heatMapCategories);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (menu.getVisibility() == View.VISIBLE) {
                    menu.setVisibility(View.GONE);
                } else if (menu.getVisibility() == View.GONE) {
                    clearMap();
                    menu.setVisibility(View.VISIBLE);
                    menu.bringToFront();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && menu.getVisibility() == View.VISIBLE) {
            menu.setVisibility(View.GONE);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    
    public void onClick(View v) {
        
        switch (v.getId()) {
            case R.id.buttonSearchByAddressActivity:
                startActivity(new Intent(this, SearchByAddressActivity.class));
                break;
            case R.id.buttonZoomOut:
                mapView.setZoom(mapView.getZoomLevel()*.9f);
                break;
            case R.id.buttonZoomIn:
                mapView.setZoom(mapView.getZoomLevel()*1.1f);
                break;
            case R.id.buttonMarkRouteStart:
            case R.id.buttonMarkRouteFinish:
                ToggleButton tb = (ToggleButton)v;
                app.setRouteEndpointId(-1);
                if (tb.isChecked())
                {
                    app.setRouteEndpointId(v.getId() == R.id.buttonMarkRouteStart ? 0 : 1);
                    toggleButtonMarkRoute[1-app.getRouteEndpointId()].setChecked(false);
                }
                break;
            case R.id.alt_route_1:
                selectAlternativeRoute(0);
                break;
            case R.id.alt_route_2:
                selectAlternativeRoute(1);
                break;
            case R.id.alt_route_3:
                selectAlternativeRoute(2);
                break;
            case R.id.map_style_day:
                selectMapStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "daystyle/", "daystyle.json"));
                break;
            case R.id.map_style_night:
                selectMapStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "nightstyle/", "nightstyle.json"));
                break;
            case R.id.map_style_outdoor:
                selectMapStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "outdoorstyle/", "outdoorstyle.json"));
                break;
            case R.id.map_style_grayscale:
                selectMapStyle(new SKMapViewStyle(app.getMapResourcesDirPath() + "grayscalestyle/", "grayscalestyle.json"));
                break;
            case R.id.bottom_button:
                if (currentMapOption == MapOption.ROUTING_AND_NAVIGATION) {
                    if (bottomButton.getText().equals(getResources().getString(R.string.calculate_route))) {
                        app.launchRouteCalculation();
                    } else if (bottomButton.getText().equals(getResources().getString(R.string.start_navigation))) {
                        bottomButton.setText(getResources().getString(R.string.stop_navigation));
                        launchNavigation();
                    } else if (bottomButton.getText().equals(getResources().getString(R.string.stop_navigation))) {
                        bottomButton.setText(getResources().getString(R.string.calculate_route));
                        stopNavigation();
                    }
                }
                break;
            case R.id.position_me_button:
                if (headingOn) {
                    setHeading(false);
                }
                if (currentPosition != null) {
                    mapView.centerMapOnCurrentPositionSmooth(17, 500);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_position_available), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.heading_button:
                app.goTo();
                if (currentPosition != null) {
                    setHeading(true);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_position_available), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                break;
        }
    }
    
    public void onMenuOptionClick(View v) {
        switch (v.getId()) {
            case R.id.option_map_display:
                currentMapOption = MapOption.MAP_DISPLAY;
                break;
            case R.id.option_overlays:
                currentMapOption = MapOption.MAP_OVERLAYS;
                drawShapes();
                mapView.setZoom(14);
                mapView.centerMapOnPosition(new SKCoordinate(-122.4200, 37.7765));
                break;
            case R.id.option_alt_routes:
                currentMapOption = MapOption.ALTERNATIVE_ROUTES;
                altRoutesView.setVisibility(View.VISIBLE);
                launchAlternativeRouteCalculation();
                break;
            case R.id.option_map_styles:
                currentMapOption = MapOption.MAP_STYLES;
                mapStylesView.setVisibility(View.VISIBLE);
                selectStyleButton();
                break;
            case R.id.option_map_creator:
                currentMapOption = MapOption.MAP_DISPLAY;
                mapView.applySettingsFromFile(app.getMapCreatorFilePath());
                break;
            case R.id.option_tracks:
                currentMapOption = MapOption.TRACKS;
                startActivity(new Intent(this, TrackElementsActivity.class));
                break;
            case R.id.option_real_reach:
                currentMapOption = MapOption.REAL_REACH;
                showRealReach();
                break;
            case R.id.option_map_xml_and_downloads:
                if (DemoUtils.isInternetAvailable(this)) {
                    startActivity(new Intent(MapActivity.this, MapPackagesListActivity.class));
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.option_reverse_geocoding:
                startActivity(new Intent(this, ReverseGeocodingActivity.class));
                break;
            case R.id.option_address_search:
                startActivity(new Intent(this, OfflineAddressSearchActivity.class));
                break;

            case R.id.option_nearby_search:
                startActivity(new Intent(this, NearbySearchActivity.class));
                break;
            case R.id.option_annotations:
                currentMapOption = MapOption.ANNOTATIONS;
                prepareAnnotations();
                break;
            case R.id.option_category_search:
                startActivity(new Intent(this, CategorySearchResultsActivity.class));
                break;
            case R.id.option_routing_and_navigation:
                currentMapOption = MapOption.ROUTING_AND_NAVIGATION;
                bottomButton.setVisibility(View.VISIBLE);
                bottomButton.setText(getResources().getString(R.string.calculate_route));
                break;
            case R.id.option_poi_tracking:
                currentMapOption = MapOption.POI_TRACKING;
                if (trackablePOIs == null) {
                    initializeTrackablePOIs();
                }
                app.launchRouteCalculation();
                break;
            case R.id.option_heat_map:
                currentMapOption = MapOption.HEAT_MAP;
                startActivity(new Intent(this, POICategoriesListActivity.class));
                break;
            default:
                break;
        }
        if (currentMapOption != MapOption.MAP_DISPLAY) {
            positionMeButton.setVisibility(View.GONE);
            headingButton.setVisibility(View.GONE);
        }
        menu.setVisibility(View.GONE);
    }
    

    /**
     * Launches the calculation of three alternative routes
     */
    private void launchAlternativeRouteCalculation() {
        SKRouteSettings route = new SKRouteSettings();
        route.setStartCoordinate(new SKCoordinate(-122.392284, 37.787189));
        route.setDestinationCoordinate(new SKCoordinate(-122.484378, 37.856300));
        // number of alternative routes specified here
        route.setNoOfRoutes(3);
        route.setRouteMode(SKRouteSettings.SKROUTE_CAR_FASTEST);
        route.setRouteExposed(true);
        SKRouteManager.getInstance().setRouteListener(this);
        SKRouteManager.getInstance().calculateRoute(route);
    }
    
    /**
     * Initiate real reach calculation
     */
    private void showRealReach() {
        // set listener for real reach calculation events
        mapView.setRealReachListener(this);
        // get object that can be used to specify real reach calculation
        // properties
        SKRealReachSettings realReachSettings = new SKRealReachSettings();
        // set center position for real reach
        SKCoordinate realReachCenter = new SKCoordinate(13.387165, 52.516929);
        realReachSettings.setLatitude(realReachCenter.getLatitude());
        realReachSettings.setLongitude(realReachCenter.getLongitude());
        // set measurement unit for real reach
        realReachSettings.setMeasurementUnit(SKRealReachSettings.UNIT_SECOND);
        // set the range value (in the unit previously specified)
        realReachSettings.setRange(15 * 60);
        // set the transport mode
        realReachSettings.setTransportMode(SKRealReachSettings.VEHICLE_TYPE_BICYCLE);
        // initiate real reach
        mapView.displayRealReachWithSettings(realReachSettings);
    }
    
    /**
     * Draws annotations on map
     */
    private void prepareAnnotations() {
        
        // get the annotation object
        SKAnnotation annotation1 = new SKAnnotation();
        // set unique id used for rendering the annotation
        annotation1.setUniqueID(10);
        // set annotation location
        annotation1.setLocation(new SKCoordinate(-122.4200, 37.7765));
        // set minimum zoom level at which the annotation should be visible
        annotation1.setMininumZoomLevel(5);
        // set the annotation's type
        annotation1.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
        // render annotation on map
        mapView.addAnnotation(annotation1);
        
        SKAnnotation annotation2 = new SKAnnotation();
        annotation2.setUniqueID(11);
        annotation2.setLocation(new SKCoordinate(-122.410338, 37.769193));
        annotation2.setMininumZoomLevel(5);
        annotation2.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_GREEN);
        mapView.addAnnotation(annotation2);
        
        SKAnnotation annotation3 = new SKAnnotation();
        annotation3.setUniqueID(12);
        annotation3.setLocation(new SKCoordinate(-122.430337, 37.779776));
        annotation3.setMininumZoomLevel(5);
        annotation3.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_BLUE);
        mapView.addAnnotation(annotation3);
        
        selectedAnnotation = annotation1;
        // set map zoom level
        mapView.setZoom(14);
        // center map on a position
        mapView.centerMapOnPosition(new SKCoordinate(-122.4200, 37.7765));
        updatePopupPosition();
    }
    
    /**
     * Changes the map popup's position so that it is placed above the
     * annotation currently selected
     */
    private void updatePopupPosition() {
        // get the annotation's screen coordinates
        SKScreenPoint screenPoint = mapView.coordinateToPoint(selectedAnnotation.getLocation());
        // change the popup's (screen) position
        mapPopup.changePosition(screenPoint.getX(), screenPoint.getY(), 65);
    }
    
    /**
     * Draws shapes on map
     */
    private void drawShapes() {
        
        // get a polygon shape object
        SKPolygon polygon = new SKPolygon();
        // set the polygon's nodes
        List<SKCoordinate> nodes = new ArrayList<SKCoordinate>();
        nodes.add(new SKCoordinate(-122.4342, 37.7765));
        nodes.add(new SKCoordinate(-122.4141, 37.7765));
        nodes.add(new SKCoordinate(-122.4342, 37.7620));
        polygon.setNodes(nodes);
        // set the outline size
        polygon.setOutlineSize(3);
        // set colors used to render the polygon
        polygon.setOutlineColor(new float[] { 1f, 0f, 0f, 1f });
        polygon.setColor(new float[] { 1f, 0f, 0f, 0.2f });
        // render the polygon on the map
        mapView.addPolygon(polygon);
        
        // get a circle mask shape object
        SKCircle circleMask = new SKCircle();
        // set the shape's mask scale
        circleMask.setMaskedObjectScale(1.3f);
        // set the colors
        circleMask.setColor(new float[] { 1f, 1f, 0.5f, 0.67f });
        circleMask.setOutlineColor(new float[] { 0f, 0f, 0f, 1f });
        circleMask.setOutlineSize(3);
        // set circle center and radius
        circleMask.setCircleCenter(new SKCoordinate(-122.4200, 37.7665));
        circleMask.setRadius(300);
        // set outline properties
        circleMask.setOutlineDottedPixelsSkip(6);
        circleMask.setOutlineDottedPixelsSolid(10);
        // set the number of points for rendering the circle
        circleMask.setNumberOfPoints(150);
        // render the circle mask
        mapView.addCircle(circleMask);
        
        
        // get a polyline object
        SKPolyline polyline = new SKPolyline();
        // set the nodes on the polyline
        nodes = new ArrayList<SKCoordinate>();
        nodes.add(new SKCoordinate(-122.4342, 37.7898));
        nodes.add(new SKCoordinate(-122.4141, 37.7898));
        nodes.add(new SKCoordinate(-122.4342, 37.7753));
        polyline.setNodes(nodes);
        // set polyline color
        polyline.setColor(new float[] { 0f, 0f, 1f, 1f });
        // set properties for the outline
        polyline.setOutlineColor(new float[] { 0f, 0f, 1f, 1f });
        polyline.setOutlineSize(4);
        polyline.setOutlineDottedPixelsSolid(3);
        polyline.setOutlineDottedPixelsSkip(3);
        mapView.addPolyline(polyline);
    }
    
    private void selectMapStyle(SKMapViewStyle newStyle) {
        mapView.getMapSettings().setMapStyle(newStyle);
        selectStyleButton();
    }
    
    /**
     * Selects the style button for the current map style
     */
    private void selectStyleButton() {
        for (int i = 0; i < mapStylesView.getChildCount(); i++) {
            mapStylesView.getChildAt(i).setSelected(false);
        }
        SKMapViewStyle mapStyle = mapView.getMapSettings().getMapStyle();
        if (mapStyle == null || mapStyle.getStyleFileName().equals("daystyle.json")) {
            findViewById(R.id.map_style_day).setSelected(true);
        } else if (mapStyle.getStyleFileName().equals("nightstyle.json")) {
            findViewById(R.id.map_style_night).setSelected(true);
        } else if (mapStyle.getStyleFileName().equals("outdoorstyle.json")) {
            findViewById(R.id.map_style_outdoor).setSelected(true);
        } else if (mapStyle.getStyleFileName().equals("grayscalestyle.json")) {
            findViewById(R.id.map_style_grayscale).setSelected(true);
        }
    }
    
    /**
     * Clears the map
     */
    private void clearMap() {
        setHeading(false);
        switch (currentMapOption) {
            case MAP_DISPLAY:
                break;
            case MAP_OVERLAYS:
                // clear all map overlays (shapes)
                mapView.clearAllOverlays();
                break;
            case ALTERNATIVE_ROUTES:
                hideAlternativeRoutesButtons();
                // clear the alternative routes
                SKRouteManager.getInstance().clearRouteAlternatives();
                // clear the selected route
                SKRouteManager.getInstance().clearCurrentRoute();
                routeIds.clear();
            case MAP_STYLES:
                mapStylesView.setVisibility(View.GONE);
                break;
            case TRACKS:
                TrackElementsActivity.selectedTrackElement = null;
                break;
            case REAL_REACH:
                // removes real reach from the map
                mapView.clearRealReachDisplay();
                break;
            case ANNOTATIONS:
                mapPopup.setVisibility(View.GONE);
                // removes the annotations and custom POIs currently rendered
                mapView.deleteAllAnnotationsAndCustomPOIs();
            case ROUTING_AND_NAVIGATION:
                bottomButton.setVisibility(View.GONE);
                if (navigationInProgress) {
                    // stop navigation if ongoing
                    stopNavigation();
                }
                break;
            case POI_TRACKING:
                if (navigationInProgress) {
                    // stop the navigation
                    stopNavigation();
                }
                // remove the detected POIs from the map
                mapView.deleteAllAnnotationsAndCustomPOIs();
                // stop the POI tracker
                poiTrackingManager.stopPOITracker();
                break;
            case HEAT_MAP:
                heatMapCategories = null;
                mapView.clearHeatMapsDisplay();
                break;
            default:
                break;
        }
        currentMapOption = MapOption.MAP_DISPLAY;
        positionMeButton.setVisibility(View.VISIBLE);
        headingButton.setVisibility(View.VISIBLE);
    }
    
    private void deselectAlternativeRoutesButtons() {
        for (Button b : altRoutesButtons) {
            b.setSelected(false);
        }
    }
    
    private void hideAlternativeRoutesButtons() {
        deselectAlternativeRoutesButtons();
        altRoutesView.setVisibility(View.GONE);
        for (Button b : altRoutesButtons) {
            b.setText("distance\ntime");
        }
    }
    
    private void selectAlternativeRoute(int routeIndex) {
        deselectAlternativeRoutesButtons();
        altRoutesButtons[routeIndex].setSelected(true);
        SKRouteManager.getInstance().zoomToRoute(1, 1, 110, 8, 8, 8);
        SKRouteManager.getInstance().setCurrentRouteByUniqueId(routeIds.get(routeIndex));
    }
    
    /**
     * Launches a navigation on the current route
     */
    private void launchNavigation() {
        // get navigation settings object
        SKNavigationSettings navigationSettings = new SKNavigationSettings();
        // set the desired navigation settings
        navigationSettings.setNavigationType(SKNavigationSettings.NAVIGATION_TYPE_SIMULATION);
        navigationSettings.setPositionerVerticalAlignment(-0.25f);
        navigationSettings.setShowRealGPSPositions(false);
        // get the navigation manager object
        SKNavigationManager navigationManager = SKNavigationManager.getInstance();
        navigationManager.setMapView(mapView);
        // set listener for navigation events
        navigationManager.setNavigationListener(this);
        // start navigating using the settings
        navigationManager.startNavigation(navigationSettings);
        navigationInProgress = true;
    }
    
    /**
     * Stops the navigation
     */
    private void stopNavigation() {
        navigationInProgress = false;
        SKNavigationManager.getInstance().stopNavigation();
    }
    
    // route computation callbacks ...
    @Override
    public void onAllRoutesCompleted() {
        
    }


    /*
    @Override
    public void onRouteCalculationCompleted(final int statusMessage, final int routeDistance, final int routeEta,
            final boolean thisRouteIsComplete, final int id) {
        if (statusMessage != SKRouteListener.ROUTE_SUCCESS) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    Toast.makeText(MapActivity.this, getResources().getString(R.string.route_calculation_failed),
                            Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        if (currentMapOption == MapOption.ALTERNATIVE_ROUTES) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    int routeIndex = routeIds.size();
                    routeIds.add(id);
                    altRoutesButtons[routeIndex].setText(DemoUtils.formatDistance(routeDistance) + "\n"
                            + DemoUtils.formatTime(routeEta));
                    if (routeIndex == 0) {
                        // select 1st alternative by default
                        selectAlternativeRoute(0);
                    }
                }
            });
        } else if (currentMapOption == MapOption.ROUTING_AND_NAVIGATION || currentMapOption == MapOption.POI_TRACKING) {
            // select the current route (on which navigation will run)
            SKRouteManager.getInstance().setCurrentRouteByUniqueId(id);
            // zoom to the current route
            SKRouteManager.getInstance().zoomToRoute(1, 1, 8, 8, 8, 8);

            List<SKCoordinate> routePoints = SKRouteManager.getInstance().getRoutePointsForRoute(id);
            setMarksOnRegularDistances(routePoints);


            if (currentMapOption == MapOption.ROUTING_AND_NAVIGATION) {
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        bottomButton.setText(getResources().getString(R.string.start_navigation));
                    }
                });
            } else if (currentMapOption == MapOption.POI_TRACKING) {
                // start the POI tracker
                poiTrackingManager.startPOITrackerWithRadius(10000, 0.5);
                // set warning rules for trackable POIs
                poiTrackingManager.addWarningRulesforPoiType(SKTrackablePOIType.SPEEDCAM);
                // launch navigation
                launchNavigation();
            }
        } else if (currentMapOption == MapOption.TRACKS) {
            // launch navigation on route obtained from GPX track
            launchNavigation();
        }
    }


    private void setMarksOnRegularDistances(List<SKCoordinate> routePoints) {
        if (routePoints == null)
            return;

        int annotationId = 2;

        System.err.println("The route has "+routePoints.size()+" points");
        for(int i=1; i<routePoints.size(); i+=10)
        {
            SKAnnotation annotation = new SKAnnotation();

            SKCoordinate location = routePoints.get(i);
            annotation.setLocation(new SKCoordinate(location.getLongitude(),location.getLatitude()));
            SKAnnotationText annTxt = new SKAnnotationText();
            String annString = String.format("%3d", i);
            annTxt.setText(annString);
            System.err.printf("Add annotation %s, lat: %f, long: %f\n",
                    annString,
                    location.getLatitude(),
                    location.getLongitude()
                    );
            annotation.setText(annTxt);
            annotation.setUniqueID(annotationId++);
            mapView.addAnnotation(annotation);
        }
    }
    */

    @Override
    public void onRouteCalculationCompleted(final int statusMessage, final int routeDistance, final int routeEta, final boolean thisRouteIsComplete, final int id)
    {
        if (statusMessage != SKRouteListener.ROUTE_SUCCESS)
            return;

        SKRouteManager.getInstance().setCurrentRouteByUniqueId(id);

        List<SKCoordinate> routePoints = SKRouteManager.getInstance().getRoutePointsForRoute(id);

        System.err.printf("The route has %d points\n",routePoints.size());

        for(int i=0; i<routePoints.size(); i++)
        {
            SKCoordinate location = routePoints.get(i);

            System.err.printf("lat: %f, long: %f\n",
                    location.getLatitude(),
                    location.getLongitude());
        }
    }


    @Override
    public void onReceivedPOIs(SKTrackablePOIType type, List<SKDetectedPOI> detectedPois) {
        updateMapWithLatestDetectedPOIs(detectedPois);
    }
    
    /**
     * Updates the map when trackable POIs are detected such that only the
     * currently detected POIs are rendered on the map
     * @param detectedPois
     */
    private void updateMapWithLatestDetectedPOIs(List<SKDetectedPOI> detectedPois) {
        List<Integer> detectedIdsList = new ArrayList<Integer>();
        for (SKDetectedPOI detectedPoi : detectedPois) {
            detectedIdsList.add(detectedPoi.getPoiID());
        }
        for (int detectedPoiId : detectedIdsList) {
            if (detectedPoiId == -1) {
                continue;
            }
            if (drawnTrackablePOIs.get(detectedPoiId) == null) {
                drawnTrackablePOIs.put(detectedPoiId, trackablePOIs.get(detectedPoiId));
                drawDetectedPOI(detectedPoiId);
            }
        }
        for (int drawnPoiId : new ArrayList<Integer>(drawnTrackablePOIs.keySet())) {
            if (!detectedIdsList.contains(drawnPoiId)) {
                drawnTrackablePOIs.remove(drawnPoiId);
                mapView.deleteAnnotation(drawnPoiId);
            }
        }
    }
    
    /**
     * Draws a detected trackable POI as an annotation on the map
     * @param poiId
     */
    private void drawDetectedPOI(int poiId) {
        SKAnnotation annotation = new SKAnnotation();
        annotation.setUniqueID(poiId);
        SKTrackablePOI poi = trackablePOIs.get(poiId);
        annotation.setLocation(new SKCoordinate(poi.getLongitude(), poi.getLatitude()));
        annotation.setMininumZoomLevel(5);
        annotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_STATIC_MATCHED_SPEED_CAM_2D);
        mapView.addAnnotation(annotation);
    }
    
    @Override
    public void onUpdatePOIsInRadius(double latitude, double longitude, int radius) {
        // set the POIs to be tracked by the POI tracker
        poiTrackingManager.setTrackedPOIs(SKTrackablePOIType.SPEEDCAM,
                new ArrayList<SKTrackablePOI>(trackablePOIs.values()));
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        mapView.reportNewHeading(event.values[0]);
    }
    
    /**
     * Enables/disables heading mode
     * @param enabled
     */
    private void setHeading(boolean enabled) {
        if (enabled) {
            headingOn = true;
            mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.POSITION_PLUS_HEADING);
            startOrientationSensor();
        } else {
            headingOn = false;
            mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NONE);
            stopOrientationSensor();
        }
    }
    
    /**
     * Activates the orientation sensor
     */
    private void startOrientationSensor() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_UI);
    }
    
    /**
     * Deactivates the orientation sensor
     */
    private void stopOrientationSensor() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }
    
    @Override
    public void onCurrentPositionUpdate(SKPosition currentPosition) {
        this.currentPosition = currentPosition;
        mapView.reportNewGPSPosition(this.currentPosition);
    }
    
    @Override
    public void onOnlineRouteComputationHanging(int status) {
        
    }
    
    @Override
    public void onServerLikeRouteCalculationCompleted(int status) {
        
    }
    
    // map interaction callbacks ...
    @Override
    public void onActionPan() {
        if (headingOn) {
            setHeading(false);
        }
    }
    
    @Override
    public void onActionZoom() {
        
    }
    
    
    @Override
    public void onAnnotationSelected(SKAnnotation annotation) {
        selectedAnnotation = annotation;
        // show the popup at the proper position when selecting an annotation
        mapPopup.setVisibility(View.VISIBLE);
        updatePopupPosition();
    }
    
    @Override
    public void onCustomPOISelected(SKMapCustomPOI customPoi) {
        
    }
    
    
    @Override
    public void onDoubleTap(SKScreenPoint point) {
        // zoom in on a position when double tapping
        mapView.zoomInAt(point);
    }
    
    @Override
    public void onInternetConnectionNeeded() {
        
    }
    
    @Override
    public void onLongPress(SKScreenPoint point) {
        
    }
    
    @Override
    public void onMapActionDown(SKScreenPoint point) {
        
    }
    
    @Override
    public void onMapActionUp(SKScreenPoint point) {
        
    }
    
    @Override
    public void onMapPOISelected(SKMapPOI mapPOI) {
        
    }
    
    @Override
    public void onMapRegionChanged(SKCoordinateRegion mapRegion) {
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                if (selectedAnnotation != null) {
                    // update the popup position when the area shown on the map
                    // changes
                    updatePopupPosition();
                }
            }
        });
    }
    
    @Override
    public void onRotateMap() {
        
    }
    
    @Override
    public void onScreenOrientationChanged() {
        
    }



    @Override
    public void onSingleTap(SKScreenPoint point) {
        //selectedAnnotation = null;
        //mapPopup.setVisibility(View.GONE);

    }
    
    
    @Override
    public void onCompassSelected() {
        
    }
    
    @Override
    public void onInternationalisationCalled(int result) {
        
    }
    
    @Override
    public void onDestinationReached() {
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                Toast.makeText(MapActivity.this, "Destination reached", Toast.LENGTH_SHORT).show();
                // clear the map when reaching destination
                clearMap();
            }
        });
    }
    
    @Override
    public void onFreeDriveUpdated(String countryCode, String streetName, int streetType, double currentSpeed,
            double speedLimit) {
        
    }
    
    @Override
    public void onReRoutingStarted() {
        
    }
    
    @Override
    public void onSignalNewAdvice(String[] audioFiles, boolean specialSoundFile) {
        // a new navigation advice was received
        SKLogging.writeLog(TAG, "navigation advice: " + Arrays.asList(audioFiles), Log.DEBUG);
        AdvicePlayer.getInstance().playAdvice(audioFiles);
    }
    
    @Override
    public void onSpeedExceeded(String[] adviceList, boolean speedExceeded) {
        
    }
    
    @Override
    public void onUpdateNavigationState(SKNavigationState navigationState) {}
    
    
    @Override
    public void onVisualAdviceChanged(boolean firstVisualAdviceChanged, boolean secondVisualAdviceChanged,
            SKNavigationState navigationState) {}
    
    @Override
    public void onRealReachCalculationCompleted(int xMin, int xMax, int yMin, int yMax) {
        // fit the reachable area on the screen when real reach calculataion
        // ends
        mapView.fitRealReachInView(xMin, xMax, yMin, yMax, false, 0);
    }
    
    
    @Override
    public void onPOIClusterSelected(SKPOICluster poiCluster) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onTunnelEvent(boolean tunnelEntered) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onAnimationsFinished(SKAnimationType animationType, boolean inertial) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onMapRegionChangeEnded(SKCoordinateRegion mapRegion) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onMapRegionChangeStarted(SKCoordinateRegion mapRegion) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onOffportRequestCompleted(int requestId) {
        // TODO Auto-generated method stub
        
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
