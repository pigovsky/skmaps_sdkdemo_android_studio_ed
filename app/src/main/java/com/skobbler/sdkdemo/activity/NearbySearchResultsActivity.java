package com.skobbler.sdkdemo.activity;


import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.search.SKNearbySearchSettings;
import com.skobbler.ngx.search.SKSearchListener;
import com.skobbler.ngx.search.SKSearchManager;
import com.skobbler.ngx.search.SKSearchResult;
import com.skobbler.ngx.search.SKSearchStatus;
import com.skobbler.sdkdemo.R;


/**
 * Activity in which a nearby search with some user provided parameters is
 * performed
 * 
 * 
 */
public class NearbySearchResultsActivity extends Activity implements SKSearchListener {
    
    /**
     * Search manager object
     */
    private SKSearchManager searchManager;
    
    private ListView listView;
    
    private ResultsListAdapter adapter;
    
    /**
     * List of pairs containing the search results names and categories
     */
    private List<Pair<String, String>> items = new ArrayList<Pair<String, String>>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        
        ((TextView) findViewById(R.id.label_operation_in_progress)).setText(getResources()
                .getString(R.string.searching));
        listView = (ListView) findViewById(R.id.list_view);
        
        // get the search manager and set the search result listener
        searchManager = new SKSearchManager(this);
        // get a nearby search object
        SKNearbySearchSettings nearbySearchObject = new SKNearbySearchSettings();
        // set the position around which to do the search and the search radius
        nearbySearchObject.setLocation(new SKCoordinate(getIntent().getDoubleExtra("longitude", 0), getIntent()
                .getDoubleExtra("latitude", 0)));
        nearbySearchObject.setRadius(getIntent().getIntExtra("radius", 0));
        // set the search topic
        nearbySearchObject.setSearchTerm(getIntent().getStringExtra("searchTopic"));
        // initiate the nearby search
        SKSearchStatus status = searchManager.nearbySearch(nearbySearchObject);
        if (status != SKSearchStatus.SK_SEARCH_NO_ERROR) {
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onReceivedSearchResults(final List<SKSearchResult> results) {
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                findViewById(R.id.label_operation_in_progress).setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                // populate the pair list when receiving search results
                for (SKSearchResult result : results) {
                    items.add(new Pair<String, String>(result.getName(), Integer.toString(result.getCategory()
                            .getValue())));
                }
                adapter = new ResultsListAdapter();
                listView.setAdapter(adapter);
            }
        });
    }
    
    private class ResultsListAdapter extends BaseAdapter {
        
        @Override
        public int getCount() {
            return items.size();
        }
        
        @Override
        public Object getItem(int position) {
            return items.get(position);
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
            ((TextView) view.findViewById(R.id.title)).setText(!items.get(position).first.equals("") ? items
                    .get(position).first : " - ");
            ((TextView) view.findViewById(R.id.subtitle)).setText("type: " + items.get(position).second);
            return view;
        }
        
    }
}
