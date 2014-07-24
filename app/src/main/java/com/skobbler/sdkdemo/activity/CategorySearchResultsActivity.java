package com.skobbler.sdkdemo.activity;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import android.widget.Toast;
import com.skobbler.ngx.SKCategories.SKPOIMainCategory;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.search.SKNearbySearchSettings;
import com.skobbler.ngx.search.SKSearchListener;
import com.skobbler.ngx.search.SKSearchManager;
import com.skobbler.ngx.search.SKSearchResult;
import com.skobbler.ngx.search.SKSearchStatus;
import com.skobbler.sdkdemo.R;


/**
 * Activity in which a nearby search for some main categories is performed
 * 
 * 
 * 
 */
public class CategorySearchResultsActivity extends Activity implements SKSearchListener {
    
    /**
     * The main categories for which the nearby search will be executed
     */
    private static final int[] mainCategories = new int[] {
            SKPOIMainCategory.SKPOI_MAIN_CATEGORY_ACCOMODATION.getValue(),
            SKPOIMainCategory.SKPOI_MAIN_CATEGORY_SERVICES.getValue(),
            SKPOIMainCategory.SKPOI_MAIN_CATEGORY_SHOPPING.getValue(),
            SKPOIMainCategory.SKPOI_MAIN_CATEGORY_LEISURE.getValue() };
    
    /**
     * The main category selected
     */
    private SKPOIMainCategory selectedMainCategory;
    
    private ListView listView;
    
    private TextView operationInProgressLabel;
    
    private ResultsListAdapter adapter;
    
    /**
     * Search results grouped by their main category field
     */
    private Map<SKPOIMainCategory, List<SKSearchResult>> results =
            new LinkedHashMap<SKPOIMainCategory, List<SKSearchResult>>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        
        operationInProgressLabel = (TextView) findViewById(R.id.label_operation_in_progress);
        listView = (ListView) findViewById(R.id.list_view);
        operationInProgressLabel.setText(getResources().getString(R.string.searching));
        
        startSearch();
    }
    
    /**
     * Initiates a nearby search with the specified categories
     */
    private void startSearch() {
        // get a search manager object on which the search listener is specified
        SKSearchManager searchManager = new SKSearchManager(this);
        // get a search object
        SKNearbySearchSettings searchObject = new SKNearbySearchSettings();
        // set nearby search center and radius
        searchObject.setLocation(new SKCoordinate(13.387165, 52.516929));
        searchObject.setRadius(1500);
        // set the maximum number of search results to be returned
        searchObject.setSearchResultsNumber(300);
        // set the main categories for which to search
        searchObject.setSearchCategories(mainCategories);
        // set the search term
        searchObject.setSearchTerm("");
        // launch nearby search
        SKSearchStatus status = searchManager.nearbySearch(searchObject);
        if (status != SKSearchStatus.SK_SEARCH_NO_ERROR) {
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Build the search results map from the results of the search
     * @param searchResults
     */
    private void buildResultsMap(List<SKSearchResult> searchResults) {
        for (int mainCategory : mainCategories) {
            results.put(SKPOIMainCategory.forInt(mainCategory), new ArrayList<SKSearchResult>());
        }
        for (SKSearchResult result : searchResults) {
            results.get(result.getMainCategory()).add(result);
        }
    }
    
    @Override
    public void onReceivedSearchResults(final List<SKSearchResult> results) {
        buildResultsMap(results);
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                operationInProgressLabel.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                adapter = new ResultsListAdapter();
                listView.setAdapter(adapter);
                
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                        if (selectedMainCategory == null) {
                            selectedMainCategory = SKPOIMainCategory.forInt(mainCategories[position]);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        if (selectedMainCategory == null) {
            super.onBackPressed();
        } else {
            selectedMainCategory = null;
            adapter.notifyDataSetChanged();
        }
    }
    
    private class ResultsListAdapter extends BaseAdapter {
        
        @Override
        public int getCount() {
            if (selectedMainCategory == null) {
                return results.size();
            } else {
                return results.get(selectedMainCategory).size();
            }
        }
        
        @Override
        public Object getItem(int position) {
            if (selectedMainCategory == null) {
                return results.get(mainCategories[position]);
            } else {
                return results.get(selectedMainCategory).get(position);
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
            if (selectedMainCategory == null) {
                ((TextView) view.findViewById(R.id.title)).setText(SKPOIMainCategory.forInt(mainCategories[position])
                        .toString().replaceFirst(".*_", ""));
                ((TextView) view.findViewById(R.id.subtitle)).setText("number of POIs: "
                        + results.get(SKPOIMainCategory.forInt(mainCategories[position])).size());
            } else {
                SKSearchResult result = results.get(selectedMainCategory).get(position);
                ((TextView) view.findViewById(R.id.title)).setText(!result.getName().equals("") ? result.getName()
                        : " - ");
                ((TextView) view.findViewById(R.id.subtitle)).setText("type: "
                        + result.getCategory().toString().replaceAll(".*_", ""));
            }
            return view;
        }
    }
}
