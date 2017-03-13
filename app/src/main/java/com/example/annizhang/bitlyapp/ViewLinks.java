package com.example.annizhang.bitlyapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by annizhang on 3/13/17.
 */

public class ViewLinks extends ListActivity {

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems = new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;

    public static String allLinks = "from createLink tab";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_link);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        allLinks = intent.getStringExtra(CreateLink.allLinks);
        JSONObject newUrl;
        try {
            newUrl = new JSONObject(allLinks);
            JSONArray oldlinks = newUrl.getJSONObject("data").getJSONArray("link_history");
//            JSONObject firstLink = oldlinks.getJSONObject(0);
            for (int i = 0; i < oldlinks.length(); i++) {
                JSONObject aLink = oldlinks.getJSONObject(i);
                String toAdd = aLink.getString("title") + "\n" + aLink.getString("long_url") + "\n" +
                        aLink.getString("link");
                listItems.add(toAdd);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);
    }

//    //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
//    public void addItems(View v) {
//        adapter.notifyDataSetChanged();
//    }
}
