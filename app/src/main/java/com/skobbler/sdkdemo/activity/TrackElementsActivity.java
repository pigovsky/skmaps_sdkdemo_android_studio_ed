package com.skobbler.sdkdemo.activity;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.skobbler.ngx.tracks.SKTrackElement;
import com.skobbler.ngx.tracks.SKTrackElementType;
import com.skobbler.ngx.tracks.SKTracksFile;
import com.skobbler.ngx.tracks.SKTracksPoint;
import com.skobbler.sdkdemo.R;
import com.skobbler.sdkdemo.application.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TrackElementsActivity extends Activity {
    
    private static SKTracksFile loadedFile;
    
    public static SKTrackElement selectedTrackElement;
    
    public static boolean routeCalculationRequested;
    
    private ListView listView;
    
    private TrackElementsListAdapter adapter;
    
    private Map<Integer, List<Object>> elementsPerLevel = new HashMap<Integer, List<Object>>();
    
    private App app;
    
    private int currentLevel;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        findViewById(R.id.label_operation_in_progress).setVisibility(View.GONE);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setVisibility(View.VISIBLE);
        app = App.getInstance();
        if (loadedFile == null) {
            loadedFile = SKTracksFile.loadAtPath(app.getMapResourcesDirPath() + "GPXTracks/foxboro.gpx");
        }
        initialize();
    }
    
    private List<Object> getChildrenForCollectionElement(SKTrackElement parent) {
        List<Object> children = new ArrayList<Object>();
        for (SKTrackElement childElement : parent.getChildElements()) {
            if (childElement.getType().equals(SKTrackElementType.COLLECTION)) {
                children.add(childElement);
            }
        }
        children.addAll(parent.getPointsOnTrackElement());
        return children;
    }
    
    private void changeLevel(int newLevel, SKTrackElement parent) {
        if (newLevel > currentLevel) {
            elementsPerLevel.put(newLevel, getChildrenForCollectionElement(parent));
        }
        currentLevel = newLevel;
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
    }
    
    private void initialize() {
        elementsPerLevel.put(currentLevel, getChildrenForCollectionElement(loadedFile.getRootTrackElement()));
        adapter = new TrackElementsListAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                if (elementsPerLevel.get(currentLevel).get(pos) instanceof SKTrackElement) {
                    changeLevel(currentLevel + 1, (SKTrackElement) elementsPerLevel.get(currentLevel).get(pos));
                }
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        if (currentLevel == 0) {
            super.onBackPressed();
        } else {
            changeLevel(currentLevel - 1, null);
        }
    }
    
    private class TrackElementsListAdapter extends BaseAdapter {
        
        @Override
        public int getCount() {
            return elementsPerLevel.get(currentLevel).size();
        }
        
        @Override
        public Object getItem(int position) {
            return elementsPerLevel.get(currentLevel).get(position);
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
                view = inflater.inflate(R.layout.layout_track_element_list_item, null);
            } else {
                view = convertView;
            }
            Button drawButton = (Button) view.findViewById(R.id.draw_button);
            Button routeButton = (Button) view.findViewById(R.id.route_button);
            TextView text = (TextView) view.findViewById(R.id.label_list_item);
            Object item = elementsPerLevel.get(currentLevel).get(position);
            if (item instanceof SKTracksPoint) {
                drawButton.setVisibility(View.GONE);
                routeButton.setVisibility(View.GONE);
                view.findViewById(R.id.indicator_children_available).setVisibility(View.GONE);
                final SKTracksPoint point = (SKTracksPoint) item;
                text.setText("POINT\n(" + point.getLatitude() + ", " + point.getLongitude() + ")");
            } else if (item instanceof SKTrackElement) {
                drawButton.setVisibility(View.VISIBLE);
                routeButton.setVisibility(View.VISIBLE);
                view.findViewById(R.id.indicator_children_available).setVisibility(View.VISIBLE);
                final SKTrackElement trackElement = (SKTrackElement) item;
                String name = trackElement.getName();
                if (name == null || name.equals("")) {
                    text.setText(trackElement.getGPXElementType().toString());
                } else {
                    text.setText(name);
                }
                drawButton.setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        routeCalculationRequested = false;
                        selectedTrackElement = trackElement;
                        TrackElementsActivity.this.finish();
                    }
                });
                routeButton.setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        routeCalculationRequested = true;
                        selectedTrackElement = trackElement;
                        TrackElementsActivity.this.finish();
                    }
                });
            }
            return view;
        }
    }
}