package com.relylabs.neartag.Utils;

import com.amplitude.api.Amplitude;

import java.util.HashMap;
import java.util.WeakHashMap;

import org.json.JSONObject;


/**
 * Created by nagendra on 9/20/17.
 */

public class Logger {

    // params
    public static final String STATUS = "sttaus";
    public static final String RES = "res";
    public static final String THROWABLE = "throwable";
    public static final String JSON = "json";


    public static final String NEWS_FEED_FETCH_START = "NEWS_FEED_FETCH_START";
    public static final String NEWS_FEED_FETCH_SUCCESS = "NEWS_FEED_FETCH_SUCCESS";
    public static final String NEWS_FEED_FETCH_FAILED = "NEWS_FEED_FETCH_FAILED";

    public static final String NEWS_FEED_REFETCH_START = "NEWS_FEED_REFETCH_START";
    public static final String NEWS_FEED_REFETCH_SUCCESS = "NEWS_FEED_REFETCH_SUCCESS";
    public static final String NEWS_FEED_REFETCH_FAILED = "NEWS_FEED_REFETCH_FAILED";

    public static final String POST_CREATE_START = "POST_CREATE_START";
    public static final String POST_CREATE_SUCCESS = "POST_CREATE_SUCCESS";
    public static final String POST_CREATE_FAILED = "POST_CREATE_FAILED";


    public static void log(String eventName) {
        Amplitude.getInstance().logEvent(eventName);

    }

    public static void log(String eventName, WeakHashMap<String, String> arguments) {
        Amplitude.getInstance().logEvent(eventName, new JSONObject(arguments));
    }
}