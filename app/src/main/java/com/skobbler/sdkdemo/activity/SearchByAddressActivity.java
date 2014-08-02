package com.skobbler.sdkdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.skobbler.ngx.SKCoordinate;
import com.skobbler.sdkdemo.R;
import com.skobbler.sdkdemo.application.App;
import com.skobbler.sdkdemo.util.ISKCoordinateFound;
import com.skobbler.sdkdemo.util.SearchTask;

public class SearchByAddressActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);
        findViewById(R.id.buttonSearchByAddress).setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_address, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        findViewById(R.id.progressbarSearchByAddress).setVisibility(View.VISIBLE);

        String addressToSearch =
            ((EditText) findViewById(R.id.edittextSearchAddress)).getText().toString();

        new SearchTask(new ISKCoordinateFound() {
            @Override
            public void coordinateFound(SKCoordinate location) {
                if (location == null) {
                    Toast.makeText(SearchByAddressActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                    return;
                }
                findViewById(R.id.progressbarSearchByAddress).setVisibility(View.GONE);
                App.getInstance().goTo(location);
                finish();

            }
        }).execute(addressToSearch);

    }


}
