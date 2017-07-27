package com.example.annizhang.bitlyapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.annizhang.bitlyapp.R.id.parent;


public class CreateLink extends AppCompatActivity{

    public static String ACCESSCODE = "user access code after log in";
    public static String ACCESSCODE1 = "user access code after log in";
    public static final String allLinks = "all the links from link_history";
    public ArrayList<MyLink> linkHistory;
    //public Intent linksIntent;
    ListView linksList;
    TextView mTextView;
    String title;
    Button createEventButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_link);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String scannedLink = intent.getStringExtra("scanned_link");
        if (scannedLink != null && scannedLink != ""){
            EditText editLink = (EditText) findViewById(R.id.editLink);
            editLink.setText(scannedLink);
        }

        ACCESSCODE = intent.getStringExtra(MainActivity.ACCESSCODE);
        System.out.println("ACCESSCODE IS: " + ACCESSCODE);

        linksList = (ListView) findViewById(R.id.listView);

        TabHost thisTab = (TabHost) findViewById(R.id.tabhost);
        thisTab.setOnTabChangedListener(
                new TabHost.OnTabChangeListener(){
                        @Override
                        public void onTabChanged(String tabId) {
                            if(tabId.equals("create")) {
                                //in create tab
                            }
                            else if(tabId.equals("mylinks")) {
                                //call bitly link history api
                                getLinks();
                            }
                            else if(tabId.equals("myStats")) {
                                getStats();
                            }
                        }
                });

        thisTab.setup();

        TabHost.TabSpec createTab = thisTab.newTabSpec("create");
        createTab.setContent(R.id.create_link);
        createTab.setIndicator("Create Link");
        thisTab.addTab(createTab);

        TabHost.TabSpec myLinksTab = thisTab.newTabSpec("mylinks");
        myLinksTab.setContent(R.id.my_links);
        myLinksTab.setIndicator("My Links");
        thisTab.addTab(myLinksTab);

        TabHost.TabSpec statsTab = thisTab.newTabSpec("myStats");
        statsTab.setContent(R.id.my_Stats);
        statsTab.setIndicator("My Stats");
        thisTab.addTab(statsTab);

        Button shortenButton = (Button) findViewById(R.id.button_makelink);
        shortenButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                shortenLink(v);
            }
        });

        createEventButton = (Button) findViewById(R.id.button_addevent);
        final Intent calendarIntent = new Intent(this, AddToCalendar.class);
        createEventButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                System.out.println("Pressed calendar button");
                System.out.println("BITLINK IS: " + mTextView.getText().toString());

                Bundle bundle = new Bundle();
                bundle.putString("bitlink", mTextView.getText().toString());
                bundle.putString("title", title);
                calendarIntent.putExtras(bundle);

                startActivity(calendarIntent);
            }
        });

        Button scanLinkButton = (Button) findViewById(R.id.getFromImage);
        final Intent cameraIntent = new Intent(this, ScanLink.class);
        scanLinkButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            startActivity(cameraIntent);
            }
        });
    }

    public void shortenLink(View view){
        //call the bitly edit link endpoint to create a shorter bitly link
        EditText editLink = (EditText) findViewById(R.id.editLink);

        final String longLink = editLink.getText().toString();

        EditText linkTitle = (EditText) findViewById(R.id.linkTitle);
        title = linkTitle.getText().toString();

        System.out.println("TITLE BEFORE EXTRACTOR IS: " + title);

        EditText linkNote = (EditText) findViewById(R.id.linkNote);
        String note = linkNote.getText().toString();
        mTextView = (TextView) findViewById(R.id.resultLink);

        RequestQueue queue = Volley.newRequestQueue(this);
        System.out.println("CreateLink accesscode: " + Constants.ACCESSCODE);
        String url = "https://api-ssl.bitly.com" + "/v3/user/link_save?access_token=" + Constants.ACCESSCODE + "&longUrl=" + longLink + "&title=" + title +
                "&note" + note;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        JSONObject newUrl;
                        try {
                            System.out.println("response: " + response);
                            newUrl = new JSONObject(response);

                            mTextView.setText(newUrl.getJSONObject("data").getJSONObject("link_save").getString("link"));
                            createEventButton.setVisibility(View.VISIBLE);
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

        // addTextChangedListener
        mTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(title.matches("")) {
                    System.out.println("TRYING TO GET TITLE");
                    // start thread
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                title = TitleExtractor.getPageTitle(longLink);
                                System.out.println("TITLE IN EXTRACTOR IS: " + title);
                            }
                            catch(IOException e) {
                                System.out.println("Error getting title: " + e);
                            }
                            // end thread
                        }
                    });
                    thread.start();
                }
                // end listener
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void getLinks() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api-ssl.bitly.com" + "/v3/user/link_history?access_token=" + Constants.ACCESSCODE;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            linkHistory = new ArrayList<MyLink>();
                            JSONObject newUrl;
                            newUrl = new JSONObject(response);
                            JSONArray oldlinks = newUrl.getJSONObject("data").getJSONArray("link_history");
                            for (int i = 0; i < oldlinks.length(); i++) {

                                MyLink eachHist = new MyLink(oldlinks.getJSONObject(i));
                                linkHistory.add(eachHist);
                                addToListView();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    public void getStats() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api-ssl.bitly.com" + "/v3/user/clicks?access_token=" + Constants.ACCESSCODE;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject newUrl;
                            newUrl = new JSONObject(response);
                            int days = newUrl.getJSONObject("data").getInt("days");
                            JSONArray clicks = newUrl.getJSONObject("data").getJSONArray("clicks");
                            DataPoint[] clickData = new DataPoint[days];
                            GraphView graph = (GraphView) findViewById(R.id.graph);
                            for (int i = 0; i < days; i++) {
                                try{
                                    clickData[i] = new DataPoint(i, clicks.getJSONObject(days-1-i).getInt("clicks"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(clickData);
                            graph.addSeries(series);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    private void addToListView(){
        ArrayAdapter<MyLink> adapter = new MyLinkListAdapter();
        linksList.setAdapter(adapter);
    }


    private class MyLinkListAdapter extends ArrayAdapter<MyLink> {
        public MyLinkListAdapter() {
            super(CreateLink.this, R.layout.link_list_view, linkHistory);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.link_list_view, parent, false);
            }
            MyLink currentLink = linkHistory.get(position);
            String longTitle = currentLink.title;
            TextView title = (TextView) view.findViewById(R.id.linkTitle);
            if (longTitle.length() > 20){
                longTitle = longTitle.substring(0,19) + "...";
            }
            title.setText(longTitle);
            TextView shortLink = (TextView) view.findViewById(R.id.shortLink);
            shortLink.setText("short: " + currentLink.link);
            String oldLink = currentLink.long_url;
            if (oldLink.length() > 35){
                oldLink = oldLink.substring(0, 34) + "...";
            }
            TextView longLink = (TextView) view.findViewById(R.id.longLink);
            longLink.setText("long: " + oldLink);
            return view;
        }
    }



}
