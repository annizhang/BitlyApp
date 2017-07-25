package com.example.annizhang.bitlyapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.ExceptionLogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by heather on 7/24/17.
 * Endpoint: https://westcentralus.api.cognitive.microsoft.com/vision/v1.0
 *
 *  Key 1: c3045087ed7342bc84634a0bc16c58b0
 *
 *  Key 2: 62a432825d2a4826aef8fb401e067b67
 *
 *  MS Azure API Input requirements:
 Supported image formats: JPEG, PNG, GIF, BMP.
 Image file size must be less than 4MB.
 Image dimensions must be between 40 x 40 and
 3200 x 3200 pixels, and the image cannot be
 larger than 10 megapixels.
 */



/** ScanLink uses the device's camera app to take a picture,
    then that picture is saved to a file. The file is sent to
 MS Azure to parse the image for text. That text is then returned
 and parsed to get the URL (and date for calendar). */
@TargetApi(Build.VERSION_CODES.N)
public class ScanLink extends Activity {
    private String imageText; //
    private String linkText;
    private String mCurrentPhotoPath; //send to MS Azure
    private Context ctx;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            dispatchTakePictureIntent();
        }
        catch (IOException e){
            System.out.println("Error opening camera");
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        System.out.println("CURRENT PATH IN CREATED IMAGE IS: " + mCurrentPhotoPath);

//        // UPLOAD FILE TO S3
//
//        // get current date
//        // Tue, 25 Jul 2017 13:46:13 +0000
//        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
//        Date date = new Date();
//        String current_datetime = dateFormat.format(date);
//
//        // derive signature
//        byte[] aws_signature = new byte[]{};
//        try {
//            aws_signature = getSignatureKey(Constants.AWS_KEY, current_datetime, mCurrentPhotoPath);
//        }
//        catch (Exception e){
//
//        }
//        // create headers
//        final String authorization = "AWS" + " " + Constants.AWS_KEY + ":" + aws_signature;
//        System.out.println("AUTHORIZATION IS: " + authorization);
//        final long content_length = image.length();
//
//        // make request
//        final String endpoint = "https://" + Constants.BUCKET_NAME + ".s3.amazonaws.com/" + mCurrentPhotoPath;
//
//        final URL u = new URL(endpoint);
//
//        try {
//            // start thread
//            new Thread() {
//                @Override
//                public void run() {
//                    try {
//                        HttpsURLConnection connection = null;
//                        connection = (HttpsURLConnection) u.openConnection();
//                        connection.setRequestMethod("POST");
//
//                        //set the content length and authorization headers
//                        connection.setRequestProperty("Authorization", authorization);
//                        connection.setRequestProperty("Content-Length", Long.toString(content_length));
//
//                        connection.setAllowUserInteraction(false);
//
//                        //Connect to the server
//                        connection.connect();
//
//                        int status = connection.getResponseCode();
//                        Log.i("HTTP Client", "AWS HTTP status code : " + status);
//                        Log.i("HTTP Client", "AWS HTTP message: " + connection.getResponseMessage());
//                    }
//                    catch (Exception e) {
//                        System.out.println("Error Creating Connection for AWS upload" + e);
//                    }
//
//                }
//            }.start();// end thread
//        }
//        catch (Exception e){
//            System.out.println("ERROR UPLOADING FILE " + e);
//        }

        return image;

    }

    // UPLOAD FILE TO S3
    private static void uploadToS3Bucket(File image){
        try {

            String filePath  = image.getAbsolutePath();

            // get current date
            // Tue, 25 Jul 2017 13:46:13 +0000
            DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            Date date = new Date();
            String current_datetime = dateFormat.format(date);

            // derive signature
            byte[] aws_signature = new byte[]{};
            try {
                aws_signature = getSignatureKey(Constants.AWS_KEY, current_datetime, filePath);
            } catch (Exception e) {

            }
            // create headers
            final String authorization = "AWS" + " " + Constants.AWS_KEY + ":" + aws_signature;
            System.out.println("AUTHORIZATION IS: " + authorization);
            final long content_length = image.length();

            // make request
            final String endpoint = "https://" + Constants.BUCKET_NAME + ".s3.amazonaws.com/readonly" + filePath;

            final URL u = new URL(endpoint);

            try {
                // start thread
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            HttpsURLConnection connection = null;
                            connection = (HttpsURLConnection) u.openConnection();
                            connection.setRequestMethod("PUT");

                            //set the content length and authorization headers
                            connection.setRequestProperty("Authorization", authorization);
                            connection.setRequestProperty("Content-Length", Long.toString(content_length));
                            connection.setRequestProperty("Content-Type", "image/jpeg");

                            connection.setAllowUserInteraction(false);

                            //Connect to the server
                            connection.connect();

                            int status = connection.getResponseCode();
                            Log.i("HTTP Client", "AWS HTTP status code : " + status);
                            Log.i("HTTP Client", "AWS HTTP message: " + connection.getResponseMessage());
                        } catch (Exception e) {
                            System.out.println("Error Creating Connection for AWS upload" + e);
                        }

                    }
                }.start();// end thread
            } catch (Exception e) {
                System.out.println("ERROR UPLOADING FILE " + e);
            }
        }
        catch(Exception e){
            //
        }
    }

    private static byte[] HmacSHA256(String data, byte[] key) throws Exception {
        String algorithm="HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    private static byte[] getSignatureKey(String key, String dateStamp, String filePath) throws Exception {
        String method = "PUT\n";
        String mediaType = "image/jpeg\n";
        String current_datetime = dateStamp + "\n";
        String file_path = filePath;
        String string_to_sign = method + mediaType + current_datetime + file_path;
        byte[] kSecret = ("AWS4" + key).getBytes("UTF8");
        byte[] hashed_signature = HmacSHA256(string_to_sign, kSecret);
        return hashed_signature;
    }


    // call ms azure api & get back image text
    private String getTextFromImage() throws IOException {
        String api_endpoint = getString(R.string.api_endpoint);
        final String url_parameters = "?language=unk&detectOrientation=true";
        final String url = api_endpoint + url_parameters;
        String json = "{'url':'http://136.144.152.120/wp-content/uploads/2015/10/URL-FutureFest-2015-GB-poster.jpg'}";
        HttpsURLConnection connection = null;
        try {
            URL u = new URL(url);
            connection = (HttpsURLConnection) u.openConnection();
            connection.setRequestMethod("POST");

            //set the sending type and receiving type to json
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", getString(R.string.subscription_key));


            connection.setAllowUserInteraction(false);

            if (json != null) {
                //set the content length of the body
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                //send the json as body of the request
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(json.getBytes("UTF-8"));
                outputStream.close();
            }

            //Connect to the server
            connection.connect();

            int status = connection.getResponseCode();
            Log.i("HTTP Client", "HTTP status code : " + status);
            switch (status) {
                case 200:
                case 201:
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    bufferedReader.close();
                    Log.i("HTTP Client", "Received String : " + sb.toString());
                    //return received string
                    return getLink(sb.toString());
            }

        } catch (MalformedURLException ex) {
            Log.e("1 HTTP Client", "Error in http connection" + ex.toString());
        } catch (IOException ex) {
            Log.e("2 HTTP Client", "Error in http connection" + ex.toString());
        } catch (Exception ex) {
            Log.e("3 HTTP Client", "Error in http connection" + ex.toString());
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ex) {
                    Log.e("4 HTTP Client", "Error in http connection" + ex.toString());
                }
            }
        }
        return "";
    }

    private String getLink(String response){
        System.out.println("in getlink");
        try {
            JSONObject jsonRes = new JSONObject(response);
            JSONArray regions= (JSONArray)jsonRes.get("regions");
            System.out.println("regions: " + regions);
            return searchForLink(regions);
        } catch (JSONException e){
            System.out.println("json exception " + e);
        }
        return "";
    }

    //takes in REGIONS
    private String searchForLink(JSONArray arrResult){
        for (int i = 0; i < arrResult.length(); i++){
            System.out.println("region n." + i);
            try{
                JSONObject eachRegion  = (JSONObject) arrResult.get(i);
                JSONArray line = (JSONArray) eachRegion.get("lines");
                for (int j = 0; j < line.length(); j++){
                    System.out.println("line n." + j);
                    JSONObject eachLine = (JSONObject) line.get(j);
                    JSONArray words = (JSONArray) eachLine.get("words");
                    for (int k = 0; k < words.length(); k++){
                        System.out.println("word n." + k);
                        JSONObject word = (JSONObject) words.get(k);
                        String text = (String) word.get("text");
                        System.out.println("text " + text);
                        if (text.toLowerCase().contains("www")){
                            return text;
                        }
                    }
                }
            } catch (JSONException e){
                System.out.println("jsonexception in search " + e);

            }

        }
        return "";
    }

    // parse image text to get link & calendar date text
    private String parseImageText() {
        return "";
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            //Your code goes here
                            String scannedLink = "http://"+getTextFromImage().toLowerCase();

                            Intent myIntent = new Intent(ScanLink.this, CreateLink.class);
                            myIntent.putExtra("scanned_link", scannedLink); //Optional parameters
                            ScanLink.this.startActivity(myIntent);

                        } catch (Exception e) {
                            System.out.println("ERROR IN THREAD!!");
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();

            }
            catch (Exception e) {
                System.out.println("Error calling getTextFromImage");
            }
        }
    }

    public void dispatchTakePictureIntent() throws IOException{
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                if(photoFile != null){
                    uploadToS3Bucket(photoFile);
                }
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}


