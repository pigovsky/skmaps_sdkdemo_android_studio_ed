package com.skobbler.sdkdemo.util;

import android.os.AsyncTask;

import com.skobbler.ngx.SKCoordinate;

public class SearchTask extends
        AsyncTask<String, Void, SKCoordinate> {

    private ISKCoordinateFound foundListener;

    public SearchTask(ISKCoordinateFound foundListener)
    {
        this.foundListener = foundListener;
    }

    @Override
    protected SKCoordinate doInBackground(String... params) {
        return GeocoderHelper.fetchLocationFromAddressUsingGoogleMap(params[0]);
    }

    @Override
    protected void onPostExecute(SKCoordinate location) {
        foundListener.coordinateFound(location);
    }
}