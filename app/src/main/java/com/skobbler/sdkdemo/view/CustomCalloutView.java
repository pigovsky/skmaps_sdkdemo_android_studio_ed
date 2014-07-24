package com.skobbler.sdkdemo.view;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.skobbler.ngx.map.SKCalloutView;
import com.skobbler.sdkdemo.R;


/**
 * Custom map popup
 * 
 * 
 */
public class CustomCalloutView extends SKCalloutView {
    
    public CustomCalloutView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected View getContainedView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_popup, null);
        setTailColor(Color.parseColor("#ffffffd0"));
        return view;
    }
}