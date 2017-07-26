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
import com.google.gson.stream.JsonReader;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Region;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
import java.util.List;
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
    private static Context ctx;
    private File imageFile;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ctx = this.getApplicationContext();
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

        return image;
    }

    // UPLOAD FILE TO S3
    private static void uploadToS3Bucket(File image) {
        try {
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    ctx,
                    Constants.COGNITO_POOL_ID, // Identity pool ID
                    Constants.COGNITO_POOL_REGION // Region
            );

            // Create an S3 client
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

            TransferUtility transferUtility = new TransferUtility(s3, ctx);

            String file_name = image.getName();

            System.out.println("IMAGE NAME: " + image.getName());

            TransferObserver observer = transferUtility.upload(
                    Constants.BUCKET_NAME,     /* The bucket to upload to */
                    "readonly/" + file_name,    /* The key for the uploaded object */
                    image       /* The file where the data to upload exists */
            );
        }
        catch (Exception e){
            System.out.println("Error uploading to s3 bucket: " + e);
        }
    }

    // call ms azure api & get back image text
    private String getTextFromImage(String fileName) throws IOException {
        String api_endpoint = getString(R.string.api_endpoint);
        final String url_parameters = "?language=unk&detectOrientation=true";
        final String url = api_endpoint + url_parameters;
<<<<<<< HEAD

        // create aws file name; will need to pass in file name
        String aws_file_name = Constants.BUCKET_LOCATION + fileName;
        String json = "{'url':" + aws_file_name + "'}";

        //String json = "{'url':'http://136.144.152.120/wp-content/uploads/2015/10/URL-FutureFest-2015-GB-poster.jpg'}";
=======
        String json = "{'url':'http://www.savebay.org/file/ICC-POSTER-WHALE-with-URL.compressed-page-001.jpg'}";
>>>>>>> 718c89a... stream and get link
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
                    try{
                        JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
                        Gson gson = new GsonBuilder().create();
                        reader.beginObject();
                        while(reader.hasNext()){
                            String name = reader.nextName();
                            if (name.equals("regions")){
                                reader.beginArray();
                                while(reader.hasNext()){
                                    final Region region = gson.fromJson(reader, Region.class);
                                    String foundLink = searchForLink(region);
                                    if (foundLink != ""){
                                        return foundLink;
                                    }
                                }
                                reader.endArray();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        reader.close();
                    } catch (UnsupportedEncodingException ex){
                        System.out.println("unsupported encoding exception");
                    } catch (IOException ex){
                        System.out.println("io exception");
                    }
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
                        if (text.toLowerCase().contains("www") || text.toLowerCase().contains(".com")){
                            return text;
                        }
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
            uploadToS3Bucket(imageFile);
            try {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            //Your code goes here
                            String scannedLink;
                            String possible_url = getTextFromImage(imageFile.getName());
                            if(possible_url != "") {
                                scannedLink = "http://" + possible_url.toLowerCase();
                            }else {
                                scannedLink = "whoops, no url found";
                            }

                            Intent myIntent = new Intent(ScanLink.this, CreateLink.class);
                            myIntent.putExtra("scanned_link", scannedLink); //Optional parameters
                            ScanLink.this.startActivity(myIntent);

                        } catch (Exception e) {
                            System.out.println("ERROR IN THREAD: " + e);
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
            //File photoFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (imageFile.exists()) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}


