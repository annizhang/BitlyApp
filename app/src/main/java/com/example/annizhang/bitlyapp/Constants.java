package com.example.annizhang.bitlyapp;

import com.amazonaws.regions.Regions;

/**
 * Created by annizhang on 7/25/17.
 */

public class Constants {
    public static String ACCESSCODE = "";
    public static String COGNITO_POOL_ID = "us-east-1:12264668-e435-4f52-a4b0-34072aa3a426";
    public static Regions COGNITO_POOL_REGION = Regions.US_EAST_1;
    public static String BUCKET_NAME = "hackweekapp";
    public static String BUCKET_LOCATION = "https://s3-us-west-2.amazonaws.com/hackweekapp/readonly/";

    public static final String DEFAULT_FONT = "fonts/ProximaNovaCond-Semibold.otf";

    public static String URL_REGEXP = "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" +
            "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
            "|mil|biz|info|mobi|name|aero|jobs|museum" +
            "|travel|[a-z]{2}))(:[\\d]{1,5})?" +
            "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" +
            "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
            "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
            "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b";

}
