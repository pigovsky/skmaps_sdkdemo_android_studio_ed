package com.skobbler.sdkdemo.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.skobbler.sdkdemo.R;


/**
 * Activity in which nearby search parameters are introduced
 * 
 * 
 */
public class NearbySearchActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_search);
    }
    
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_button:
                if (validateCoordinates()) {
                    int radius = Integer.parseInt(((TextView) findViewById(R.id.radius_field)).getText().toString());
                    Intent intent = new Intent(this, NearbySearchResultsActivity.class);
                    intent.putExtra("radius", radius);
                    intent.putExtra("latitude",
                            Double.parseDouble(((TextView) findViewById(R.id.latitude_field)).getText().toString()));
                    intent.putExtra("longitude",
                            Double.parseDouble(((TextView) findViewById(R.id.longitude_field)).getText().toString()));
                    intent.putExtra("searchTopic", ((TextView) findViewById(R.id.search_topic_field)).getText()
                            .toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Invalid latitude or longitude was provided", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    
    private boolean validateCoordinates() {
        try {
            String latString = ((TextView) findViewById(R.id.latitude_field)).getText().toString();
            String longString = ((TextView) findViewById(R.id.longitude_field)).getText().toString();
            double latitude = Double.parseDouble(latString);
            double longitude = Double.parseDouble(longString);
            if (latitude > 90 || latitude < -90) {
                return false;
            }
            if (longitude > 180 || longitude < -180) {
                return false;
            }
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
