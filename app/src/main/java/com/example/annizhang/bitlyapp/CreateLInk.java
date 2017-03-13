package com.example.annizhang.bitlyapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;


public class CreateLink extends AppCompatActivity {

    public static String ACCESSCODE = "user access code after log in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_link);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        ACCESSCODE = intent.getStringExtra(MainActivity.ACCESSCODE);

        TabHost thisTab = (TabHost) findViewById(R.id.tabhost);

        thisTab.setup();

        TabHost.TabSpec createTab = thisTab.newTabSpec("create");
        createTab.setContent(R.id.create_link);
        createTab.setIndicator("Create Link");
        thisTab.addTab(createTab);

        TabHost.TabSpec myLinksTab = thisTab.newTabSpec("mylinks");
        myLinksTab.setContent(R.id.my_links);
        myLinksTab.setIndicator("My Links");
        thisTab.addTab(myLinksTab);

        Button shortenButton = (Button) findViewById(R.id.button_makelink);
        shortenButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                shortenLink(v);
            }
        });

        Button copyButton = (Button) findViewById(R.id.button_copy);
        copyButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                final TextView shortLink = (TextView) findViewById(R.id.resultLink);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("new link", shortLink.getText());
                clipboard.setPrimaryClip(clip);
            }
        });

    }

    public void shortenLink(View view){
        //call the bitly edit link endpoint to create a shorter bitly link
        EditText editLink = (EditText) findViewById(R.id.editLink);

        //remember to do url encoding on this
        String longLink = editLink.getText().toString();

        EditText linkTitle = (EditText) findViewById(R.id.linkTitle);
        String title = linkTitle.getText().toString();
        EditText linkNote = (EditText) findViewById(R.id.linkNote);
        String note = linkNote.getText().toString();
        final TextView mTextView = (TextView) findViewById(R.id.resultLink);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api-ssl.bitly.com" + "/v3/user/link_save?access_token=" + ACCESSCODE + "&longUrl=" + longLink +
                "&title=" + title + "&note="+note;
        //final TextView shorterUrl = (TextView) findViewById(R.id.resultLink);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        JSONObject newUrl;
                        try {
                            newUrl = new JSONObject(response);
                            mTextView.setText(newUrl.getJSONObject("data").getJSONObject("link_save").getString("link"));
                            Button copyButton = (Button) findViewById(R.id.button_copy);
                            copyButton.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void copyLink(View view){
        //use this to copy link to clipboard
    }

}
