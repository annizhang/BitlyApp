package com.example.annizhang.bitlyapp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by annizhang on 3/13/17.
 */

public class MyLink {
    String title;
    String long_url;
    String link;

    public MyLink(JSONObject rawLinkObj){
        try {
            this.title = rawLinkObj.getString("title");
            this.long_url = rawLinkObj.getString("long_url");
            this.link = rawLinkObj.getString("link");
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

}
