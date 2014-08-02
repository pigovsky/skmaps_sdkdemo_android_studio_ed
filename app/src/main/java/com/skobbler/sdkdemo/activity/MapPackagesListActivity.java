package com.skobbler.sdkdemo.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.skobbler.ngx.packages.SKPackageManager;
import com.skobbler.sdkdemo.R;
import com.skobbler.sdkdemo.application.App;
import com.skobbler.sdkdemo.model.DownloadPackage;
import com.skobbler.sdkdemo.util.MapDataParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Activity which displays map packages
 * 
 * 
 */
public class MapPackagesListActivity extends Activity {
    
    private ListView listView;
    
    private App application;
    
    /**
     * Packages currently shown in list
     */
    private List<DownloadPackage> currentPackages;
    
    private MapPackageListAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView = (ListView) findViewById(R.id.list_view);
        application = App.getInstance();
        
        if (application.getPackageMap() != null) {
            // map packages are already available
            currentPackages = searchByParentCode(null);
            initializeList();
        } else {
            // map packages need to be obtained from parsing the Maps.xml file
            new Thread() {
                
                public void run() {
                    // get a parser object to parse the Maps.xml file
                    MapDataParser parser =
                            new MapDataParser(SKPackageManager.getInstance().getMapsXMLPathForCurrentVersion());
                    // do the parsing
                    parser.parse();
                    // after parsing Maps.xml cache the download packages
                    application.setPackageMap(parser.getPackageMap());
                    // after parsing display the highest level download packages
                    currentPackages = searchByParentCode(null);
                    runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            initializeList();
                        }
                    });
                }
            }.start();
        }
    }
    
    /**
     * Populate list with current packages
     */
    private void initializeList() {
        findViewById(R.id.label_operation_in_progress).setVisibility(View.GONE);
        adapter = new MapPackageListAdapter();
        listView.setAdapter(adapter);
        listView.setVisibility(View.VISIBLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                List<DownloadPackage> childPackages = searchByParentCode(currentPackages.get(position).getCode());
                if (childPackages.size() > 0) {
                    currentPackages = searchByParentCode(currentPackages.get(position).getCode());
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
    
    private class MapPackageListAdapter extends BaseAdapter {
        
        @Override
        public int getCount() {
            return currentPackages.size();
        }
        
        @Override
        public Object getItem(int position) {
            return currentPackages.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return 0;
        }
        
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.layout_package_list_item, null);
            } else {
                view = convertView;
            }
            final DownloadPackage currentPackage = currentPackages.get(position);
            Button downloadButton = (Button) view.findViewById(R.id.download_button);
            // countries and US states should be downloadable
            boolean downloadable =
                    (currentPackage.getType().equals("country") || currentPackage.getType().equals("state"))
                            && !currentPackage.getCode().equals("US");
            if (downloadable) {
                downloadButton.setVisibility(View.VISIBLE);
                view.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MapPackagesListActivity.this, DownloadActivity.class);
                        intent.putExtra("packageCode", currentPackage.getCode());
                        startActivity(intent);
                    }
                });
            } else {
                downloadButton.setVisibility(View.GONE);
            }
            TextView hasChildrenIndicator = (TextView) view.findViewById(R.id.indicator_children_available);
            if (currentPackage.getChildrenCodes().isEmpty()) {
                hasChildrenIndicator.setVisibility(View.INVISIBLE);
            } else {
                hasChildrenIndicator.setVisibility(View.VISIBLE);
            }
            ((TextView) view.findViewById(R.id.label_list_item)).setText(currentPackage.getName());
            return view;
        }
    }
    
    @Override
    public void onBackPressed() {
        boolean shouldClose = true;
        String grandparentCode = null;
        String parentCode = null;
        if (!currentPackages.isEmpty()) {
            parentCode = currentPackages.get(0).getParentCode(); 
        }
        if (parentCode != null) {
            shouldClose = false;
            grandparentCode = application.getPackageMap().get(parentCode).getParentCode();
        }
        if (shouldClose) {
            super.onBackPressed();
        } else {
            // go one level higher in the map packages hierarchy
            currentPackages = searchByParentCode(grandparentCode);
            adapter.notifyDataSetChanged();
            DownloadPackage parentPackage = application.getPackageMap().get(parentCode);
            listView.setSelection(currentPackages.indexOf(parentPackage));
        }
    }
    
    /**
     * Gets a list of download packages having the given parent code
     * @param parentCode
     * @return
     */
    private List<DownloadPackage> searchByParentCode(String parentCode) {
        Collection<DownloadPackage> packages = application.getPackageMap().values();
        List<DownloadPackage> results = new ArrayList<DownloadPackage>();
        for (DownloadPackage pack : packages) {
            if (parentCode == null) {
                if (pack.getParentCode() == null) {
                    results.add(pack);
                }
            } else if (parentCode.equals(pack.getParentCode())) {
                results.add(pack);
            }
        }
        return results;
    }
}
