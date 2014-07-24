package com.skobbler.sdkdemo.activity;


import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.skobbler.ngx.SKCategories.SKPOICategory;
import com.skobbler.ngx.SKCategories.SKPOIMainCategory;
import com.skobbler.ngx.util.SKUtils;
import com.skobbler.sdkdemo.R;


public class POICategoriesListActivity extends Activity {
    
    private ListView listView;
    
    private POICategoryListAdapter adapter;
    
    private List<POICategoryListItem> listItems;
    
    private List<Integer> selectedCategories = new ArrayList<Integer>();
    
    private static class POICategoryListItem {
        
        private boolean isMainCategory;
        
        private String name;
        
        private int id;
        
        public POICategoryListItem(boolean isMainCategory, String name, int id) {
            super();
            this.isMainCategory = isMainCategory;
            this.name = name;
            this.id = id;
        }
        
        public String toString() {
            return "[isMainCategory=" + isMainCategory + ", name=" + name + ", id=" + id + "]";
        }
    }
    
    private static List<POICategoryListItem> getListItems() {
        List<POICategoryListItem> listItems = new ArrayList<POICategoryListItem>();
        for (SKPOIMainCategory mainCategory : SKPOIMainCategory.values()) {
            listItems
                    .add(new POICategoryListItem(true, mainCategory.toString().replace("SKPOI_MAIN_CATEGORY_", ""), -1));
            for (int categoryId : SKUtils.getSubcategoriesForCategory(mainCategory.getValue())) {
                listItems.add(new POICategoryListItem(false,
                        SKUtils.getMainCategoryForCategory(categoryId).getNames()[0].toUpperCase().replace("_", " "),
                        categoryId));
            }
        }
        return listItems;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        
        findViewById(R.id.label_operation_in_progress).setVisibility(View.GONE);
        
        listItems = getListItems();
        
        listView = (ListView) findViewById(R.id.list_view);
        listView.setVisibility(View.VISIBLE);
        
        adapter = new POICategoryListAdapter();
        listView.setAdapter(adapter);
        
        Toast.makeText(this, "Select the desired POI categories for heat map display", Toast.LENGTH_SHORT).show();
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                POICategoryListItem selectedItem = listItems.get(position);
                if (selectedItem.id > 0) {
                    if (selectedCategories.contains(selectedItem.id)) {
                        selectedCategories.remove(Integer.valueOf(selectedItem.id));
                        view.setBackgroundColor(getResources().getColor(R.color.white));
                    } else {
                        selectedCategories.add(selectedItem.id);
                        view.setBackgroundColor(getResources().getColor(R.color.selected));
                    }
                    
                    Button showButton = (Button) findViewById(R.id.show_heat_map);
                    if (selectedCategories.isEmpty()) {
                        showButton.setVisibility(View.GONE);
                    } else {
                        showButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
    
    public void onClick(View v) {
        if (v.getId() == R.id.show_heat_map) {
            SKPOICategory[] categories = new SKPOICategory[selectedCategories.size()];
            for (int i = 0; i < selectedCategories.size(); i++) {
                categories[i] = SKPOICategory.forInt(selectedCategories.get(i));
            }
            MapActivity.heatMapCategories = categories;
            finish();
        }
    }
    
    private class POICategoryListAdapter extends BaseAdapter {
        
        @Override
        public int getCount() {
            return listItems.size();
        }
        
        @Override
        public Object getItem(int position) {
            return listItems.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return 0;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = null;
            if (convertView == null) {
                view = new TextView(POICategoriesListActivity.this);
            } else {
                view = (TextView) convertView;
            }
            
            POICategoryListItem item = listItems.get(position);
            
            view.setText("  " + item.name);
            if (item.isMainCategory) {
                view.setTextAppearance(POICategoriesListActivity.this, R.style.menu_options_group_style);
                view.setBackgroundColor(getResources().getColor(R.color.grey_options_group));
            } else {
                view.setTextAppearance(POICategoriesListActivity.this, R.style.menu_options_style);
                if (!selectedCategories.contains(item.id)) {
                    view.setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    view.setBackgroundColor(getResources().getColor(R.color.selected));
                }
            }
            return view;
        }
    }
}
