package com.skobbler.sdkdemo.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.search.SKSearchResult;
import com.skobbler.ngx.search.SKSearchResultParent;
import com.skobbler.sdkdemo.R;
import com.skobbler.sdkdemo.application.DemoApplication;


/**
 * Activity where offline reverse geocoding is performed
 */
public class ReverseGeocodingActivity extends Activity {
    
    private DemoApplication application;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reverse_geocoding);
        application = DemoApplication.getInstance();
    }
    
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reverse_geocode_button:
                SKCoordinate position = getPosition();
                if (position != null) {
                    // run reverse geocoding and obtain a search result
                    SKSearchResult result = application.getMapView().reverseGeocodePosition(position);
                    // display the search result name
                    String text = result != null ? result.getName() : "NULL";
                    if (result != null && result.getParentsList() != null) {
                        String separator = ", ";
                        for (SKSearchResultParent parent : result.getParentsList()) {
                            text += separator + parent.getParentName();
                        }
                    }
                    
                    ((TextView) findViewById(R.id.reverse_geocoding_result)).setText(text);
                } else {
                    Toast.makeText(this, "Invalid latitude or longitude was provided", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    
    /**
     * Gets the position from the data in the latitude and longitude fields
     * @return the position, null if invalid latitude/longitude was provided
     */
    private SKCoordinate getPosition() {
        try {
            String latString = ((TextView) findViewById(R.id.latitude_field)).getText().toString();
            String longString = ((TextView) findViewById(R.id.longitude_field)).getText().toString();
            double latitude = Double.parseDouble(latString);
            double longitude = Double.parseDouble(longString);
            if (latitude > 90 || latitude < -90) {
                return null;
            }
            if (longitude > 180 || longitude < -180) {
                return null;
            }
            return new SKCoordinate(longitude, latitude);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }
}