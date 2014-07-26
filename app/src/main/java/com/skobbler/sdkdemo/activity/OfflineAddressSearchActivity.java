package com.skobbler.sdkdemo.activity;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.packages.SKPackage;
import com.skobbler.ngx.packages.SKPackageManager;
import com.skobbler.ngx.search.SKMultiStepSearchSettings;
import com.skobbler.ngx.search.SKSearchListener;
import com.skobbler.ngx.search.SKSearchManager;
import com.skobbler.ngx.search.SKSearchManager.SKListLevel;
import com.skobbler.ngx.search.SKSearchResult;
import com.skobbler.sdkdemo.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Activity where offline address searches are performed and results are listed
 * 
 * 
 */
public class OfflineAddressSearchActivity extends Activity implements SKSearchListener {
    
    /**
     * The current list level (see)
     */
    private short currentListLevel;
    
    /**
     * Top level packages available offline (countries and US states)
     */
    private List<SKPackage> packages;
    
    private ListView listView;
    
    private TextView operationInProgressLabel;
    
    private ResultsListAdapter adapter;
    
    /**
     * Offline address search results grouped by level
     */
    private Map<Short, List<SKSearchResult>> resultsPerLevel = new HashMap<Short, List<SKSearchResult>>();
    
    /**
     * Current top level package code
     */
    private String currentCountryCode;
    
    /**
     * Search manager object
     */
    private SKSearchManager searchManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        
        operationInProgressLabel = (TextView) findViewById(R.id.label_operation_in_progress);
        listView = (ListView) findViewById(R.id.list_view);
        operationInProgressLabel.setText(getResources().getString(R.string.searching));
        
        packages = Arrays.asList(SKPackageManager.getInstance().getInstalledPackages());
        searchManager = new SKSearchManager(this);

        /*
        if (packages.isEmpty()) {
            Toast.makeText(this, "No offline map packages are available", Toast.LENGTH_SHORT).show();
        }*/
        
        initializeList();
    }
    
    /**
     * Initializes list with top level packages
     */
    private void initializeList() {
        adapter = new ResultsListAdapter();
        listView.setAdapter(adapter);
        operationInProgressLabel.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                if (currentListLevel == 0) {
                    currentCountryCode = packages.get(position).getName();
                    changeLevel((short) (currentListLevel + 1), -1, currentCountryCode);
                } else if (currentListLevel < 3) {
                    changeLevel((short) (currentListLevel + 1), resultsPerLevel.get(currentListLevel).get(position)
                            .getId(), currentCountryCode);
                }
            }
        });
    }
    
    /**
     * Changes the list level and executes the corresponding action for the list
     * level change
     * @param newLevel the new level
     * @param parentId the parent id for which to execute offline address search
     * @param countryCode the current code to use in offline address search
     */
    private void changeLevel(short newLevel, long parentId, String countryCode) {
        if (newLevel == 0 || newLevel < currentListLevel) {
            // for new list level 0 or smaller than previous one just change the
            // level and update the adapter
            operationInProgressLabel.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            currentListLevel = newLevel;
            adapter.notifyDataSetChanged();
        } else if (newLevel > currentListLevel && newLevel > 0) {
            // for new list level greater than previous one execute an offline
            // address search
            operationInProgressLabel.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            // get a search object
            SKMultiStepSearchSettings searchObject = new SKMultiStepSearchSettings();
            // set the maximum number of results to be returned
            searchObject.setMaxSearchResultsNumber(25);
            // set the country code
            searchObject.setOfflinePackageCode(currentCountryCode);
            // set the search term
            searchObject.setSearchTerm("");
            // set the id of the parent node in which to search
            searchObject.setParentIndex(parentId);
            // set the list level
            searchObject.setListLevel(SKListLevel.forInt(newLevel + 1));
            // change the list level to the new one
            currentListLevel = newLevel;
            // initiate the search
            searchManager.multistepSearch(searchObject);
        }
    }
    
    @Override
    public void onReceivedSearchResults(final List<SKSearchResult> results) {
        // put in the map at the corresponding level the received results
        resultsPerLevel.put(currentListLevel, results);
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                operationInProgressLabel.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                if (results.size() > 0) {
                    // received results - update adapter to show the results
                    adapter.notifyDataSetChanged();
                } else {
                    // zero results - no change
                    currentListLevel--;
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        if (currentListLevel == 0) {
            super.onBackPressed();
        } else {
            // if not top level - decrement the current list level and show
            // results for the new level
            changeLevel((short) (currentListLevel - 1), -1, currentCountryCode);
        }
    }
    
    private class ResultsListAdapter extends BaseAdapter {
        
        @Override
        public int getCount() {
            if (currentListLevel > 0) {
                return resultsPerLevel.get(currentListLevel).size();
            } else {
                return packages.size();
            }
        }
        
        @Override
        public Object getItem(int position) {
            if (currentListLevel > 0) {
                return resultsPerLevel.get(currentListLevel).get(position);
            } else {
                return packages.get(position);
            }
        }
        
        @Override
        public long getItemId(int position) {
            return 0;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.layout_search_list_item, null);
            } else {
                view = convertView;
            }
            if (currentListLevel > 0) {
                // for offline address search results show the result name and
                // position
                ((TextView) view.findViewById(R.id.title)).setText(resultsPerLevel.get(currentListLevel).get(position)
                        .getName());
                SKCoordinate location = resultsPerLevel.get(currentListLevel).get(position).getLocation();
                ((TextView) view.findViewById(R.id.subtitle)).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.subtitle)).setText("location: (" + location.getLatitude() + ", "
                        + location.getLongitude() + ")");
            } else {
                ((TextView) view.findViewById(R.id.title)).setText(packages.get(position).getName());
                ((TextView) view.findViewById(R.id.subtitle)).setVisibility(View.GONE);
            }
            return view;
        }
    }
}
