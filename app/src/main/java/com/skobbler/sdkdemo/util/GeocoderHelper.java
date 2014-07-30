package com.skobbler.sdkdemo.util;

/**
 * Created by Pascal (http://stackoverflow.com/questions/9272918/service-not-available-in-geocoder). Thanks for him.
 * Apr 6 '13 at 16:16
 */

import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.skobbler.ngx.SKCoordinate;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class GeocoderHelper
{
    private static final AndroidHttpClient ANDROID_HTTP_CLIENT = AndroidHttpClient.newInstance(GeocoderHelper.class.getName());

    public static String fetchCityNameUsingGoogleMap(SKCoordinate location)
    {
        String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLatitude() + ","
                + location.getLongitude() + "&sensor=false&language=uk";

        try
        {
            JSONObject googleMapResponse = new JSONObject(ANDROID_HTTP_CLIENT.execute(new HttpGet(googleMapUrl),
                    new BasicResponseHandler()));

            JSONArray results = (JSONArray) googleMapResponse.get("results");

            JSONObject result = results.getJSONObject(0);

            Log.println(Log.INFO, "JSON:",result.toString());

            String address = result.get("formatted_address").toString();

            Log.println(Log.INFO, "JSON:", address);
            return address;
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
        }
        return null;
    }

    public static SKCoordinate fetchLocationFromAddressUsingGoogleMap(String address)
    {

        try
        {
            String googleMapUrl =
                    String.format("http://maps.googleapis.com/maps/api/geocode/json?address=%s",
                            URLEncoder.encode(address, "utf-8"));
            JSONObject googleMapResponse = new JSONObject(ANDROID_HTTP_CLIENT.execute(new HttpGet(googleMapUrl),
                    new BasicResponseHandler()));

            JSONArray results = (JSONArray) googleMapResponse.get("results");

            JSONObject result = results.getJSONObject(0);

            Log.println(Log.INFO, "JSON:",result.toString());

            JSONObject locationJSON = result.getJSONObject("geometry").getJSONObject("location");

            SKCoordinate location = new SKCoordinate();
            location.setLatitude(Double.parseDouble(locationJSON.getString("lat")));
            location.setLongitude(Double.parseDouble(locationJSON.getString("lng")));

            return location;
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
        }
        return null;
    }
}