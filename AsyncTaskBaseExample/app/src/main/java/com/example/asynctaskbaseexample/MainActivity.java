package com.example.asynctaskbaseexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AsyncResponse {

    String jsonString;
    String apiURL = "http://www.somaku.com/posts";
    ArrayList<String> items = new ArrayList<String>();
    ArrayAdapter<String> mArrayAdapter;
    ProgressBar progressHorizontalMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressHorizontalMain = findViewById(R.id.progress_horizontal_main); /* Call Async task after initializing it. */

        AsyncBaseClass process = new AsyncBaseClass(); /* initialize AsyncTask Class */
        process.delegate = this;  /* setting delegate to Main activity instance */
        process.execute(apiURL); /* pass desired url */

        ListView listView = findViewById(R.id.content);
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, items);
        listView.setAdapter(mArrayAdapter);

    }

    @Override
    public void processFinish(JSONArray output) {
        /* Converting JSONArray into a ArrayList*/
        for(int i=0;i<output.length();i++) {
            try {
                items.add(output.getJSONObject(i).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void processPending(Boolean status) {
        if (status)progressHorizontalMain.setVisibility(View.VISIBLE);
        else progressHorizontalMain.setVisibility(View.GONE);
    }

    public void nextActivity(View view) {
        Intent intent = new Intent(this,AnotherActivity.class);
        startActivity(intent);
        finish();
    }
}
