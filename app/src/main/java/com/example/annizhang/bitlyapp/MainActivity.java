package com.example.annizhang.bitlyapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import android.util.Base64;


public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** called when send button is pressed */
    public void sendMessage(View view) {
        //do something when button is pressed
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText1);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void login(View view) {
        final EditText name = (EditText)findViewById(R.id.editText1);
        final EditText pass = (EditText)findViewById(R.id.editText2);
        final String username = name.getText().toString();
        final String password = pass.getText().toString();
        final TextView mTextView = (TextView) findViewById(R.id.textView2);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api-ssl.bitly.com/oauth/access_token";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                 new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mTextView.setText("Response is: " + response);
                        String accessCode = response;
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){
                        mTextView.setText("That didn't work!");
                    }
                }){
                    protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("username", username);
                        params.put("password", password);
//                        String loginData = username +  ":" + password;
//                        byte[] data = loginData.getBytes();
//                        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
//                        params.put("Authorization", "Basic " + base64);
                        return params;
                    };

                    public Map<String, String> getHeaders(){
                        Map<String, String> headers = new HashMap<String, String>();
                        String loginData = username +  ":" + password;
                        byte[] data = loginData.getBytes();
                        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                        headers.put("Authorization", "Basic " + base64);
//                      params.put("username", username);
//                      params.put("password", password);
                        return headers;
                    };
        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
