package com.example.annizhang.bitlyapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.ExceptionLogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

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
        System.out.printf("IMAGE GOES TO: %s", mCurrentPhotoPath);
        return image;
    }

    // call ms azure api & get back image text
    private String getTextFromImage(String filePath) throws IOException {

        // Convert image to byte array
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos); //bm is the bitmap object; was 100 for highest image quality
        final byte[] encodedImage = baos.toByteArray();
        //String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        try
        {
            String api_endpoint = getString(R.string.api_endpoint);
            final String url_parameters = "?language=unk&detectOrientation=true";
            final URL obj = new URL(api_endpoint);

            System.out.println("URL OBJECT IS: " + obj);

            final HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
            System.out.println("\nCreated connection\n");

            new Thread(){
                @Override
                public void run(){
                    try {


                        System.out.println("Inside thread");

                        conn.setDoOutput(true);
                        conn.setInstanceFollowRedirects(false);
                        conn.setRequestMethod("POST");
                        conn.setUseCaches(false);

                        //add request headers
                        //conn.setRequestProperty("Content-Type", "application/octet-stream");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Ocp-Apim-Subscription-Key", getString(R.string.subscription_key));

                        System.out.println("Added headers to connection");


                        // write body to output stream
                        int len = encodedImage.length;
                        final DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                        try {
                            wr.write(encodedImage, 0, encodedImage.length);
                        }
                        catch (Exception e){
                            System.out.println("Error writing bytes" + e);
                        }

                        System.out.println("Wrote request body");

                        // Send post request
                        wr.writeBytes(url_parameters);
                        wr.flush();

                        System.out.println("Closed DataOutputStream");

                        int response_code = conn.getResponseCode();
                        System.out.printf("Response Code is: %d\n", response_code);

                        String response_message = conn.getResponseMessage();
                        System.out.println("Response message is: \n" + response_message);

                        wr.close();

                        // Response body
                    }
                    catch (Exception e){
                        System.out.println("Exception with threads is: " + e);
                    }
                }
            }.start();

        }
        catch (Exception e)
        {
            System.out.println("Error doing something in getTextFromImage: " + e);
        }
        return imageText;
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
                getTextFromImage(mCurrentPhotoPath);
            }
            catch (IOException e) {
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


