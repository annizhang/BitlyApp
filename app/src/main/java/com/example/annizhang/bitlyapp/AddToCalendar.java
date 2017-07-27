package com.example.annizhang.bitlyapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;

import java.io.IOException;

/**
 * Created by heather on 7/26/17.
 */

@TargetApi(Build.VERSION_CODES.N)
public class AddToCalendar extends Activity {
    // TODO: Need to get link to pass into description; could possibly populate title & date as well
    String title;
    String description;
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = "Test title";
        try {
            Bundle bundle = getIntent().getExtras();
            String bitlink = bundle.getString("bitlink");
            title = bundle.getString("title");

            description = "Click here for event details: " + bitlink;
        } catch (NullPointerException e){
            description = "";
        }
        try {
            ctx = this.getApplicationContext();
            dispatchCreateEventIntent();
        }
        catch (Exception e){
            System.out.println("Error creating calendar event");
        }
    }

    public void dispatchCreateEventIntent() {
        Intent createEventIntent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description);
        startActivity(createEventIntent);
    }

}
