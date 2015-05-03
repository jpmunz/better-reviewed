package com.jpmunz.betterreviewed;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RottenTomatoesAPI {

    public class RottenTomatoesAPIException extends Exception {
        RottenTomatoesAPIException() {
        }

        RottenTomatoesAPIException(String message) {
            super(message);
        }
    }

    // Not using their real api since I can't get my key activated
    //private final static String API_ROOT = "http://api.rottentomatoes.com/api/public/v1.0/";
    //private final static String API_KEY = "k4armx9qg4x5rq4xx8us8m6g";

    private final static String SEARCH_ROOT = "http://www.rottentomatoes.com/search/json/";
    private final static String MOVIE_ROOT = "http://www.rottentomatoes.com/m/";
    private final static HashMap<String, ArrayList<String>> autoCompleteCache = new HashMap<String, ArrayList<String>>();
    private final static HashMap<String, String> nameToId = new HashMap<String, String>();

    private final static Pattern RATING_PATTERN = Pattern.compile(".*\"(\\d+)% of critics liked it\".*\"(\\d+)% of users liked it\".*", Pattern.DOTALL);


    private String makeRequest(String endpoint) throws RottenTomatoesAPIException {
        return makeRequest(endpoint, new HashMap<String, String>());
    }

    private String makeRequest(String endpoint, HashMap<String,String> queryStringParams) throws RottenTomatoesAPIException {
        //StringBuilder sb = new StringBuilder(API_ROOT + endpoint + ".json");
        //sb.append("?apikey=" + API_KEY);
        StringBuilder sb = new StringBuilder(endpoint + '?');

        Iterator<String> keySetIterator = queryStringParams.keySet().iterator();
        while(keySetIterator.hasNext()){
            String key = keySetIterator.next();
            sb.append("&" + key + "=" + queryStringParams.get(key));
        }

        URL url = null;
        try {
            url = new URL(sb.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RottenTomatoesAPIException();
        }

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RottenTomatoesAPIException();
        } finally {
            urlConnection.disconnect();
        }
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    public JSONObject getRatings(String movie) throws RottenTomatoesAPIException {
        if (!nameToId.containsKey(movie)) {
            throw new RottenTomatoesAPIException("No id mapping for: " + movie);
        }
        String url = MOVIE_ROOT + nameToId.get(movie);
        String response =  this.makeRequest(url);

        Matcher m = RATING_PATTERN.matcher(response);

        JSONObject result = new JSONObject();

        if (m.matches()) {
            try {
                result.put("critic", Integer.parseInt(m.group(1)));
                result.put("user", Integer.parseInt(m.group(2)));
            } catch (JSONException e) {
                e.printStackTrace();
                throw new RottenTomatoesAPIException("Could not parse response from: " + url);
            }
        } else {
            try {
                result.put("critic", 0);
                result.put("user", 0);
            } catch (JSONException e) {
                e.printStackTrace();
                throw new RottenTomatoesAPIException("Could not parse response from: " + url);
            }
        }
        return result;
    }

    public ArrayList<String> autoComplete(String input) throws RottenTomatoesAPIException {
        if (autoCompleteCache.containsKey(input)) {
            Log.v(BetterReviewedApp.LOG_TAG, "cache hit");
            return autoCompleteCache.get(input);
        }

        ArrayList<String> result = new ArrayList<String>();
        HashMap<String, String> queryStringParams = new HashMap<String, String>();

        try {
            queryStringParams.put("q", URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            throw new RottenTomatoesAPIException();
        }
        queryStringParams.put("catCount", "5");

        String response =  this.makeRequest(SEARCH_ROOT, queryStringParams);

        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray movies = jsonResponse.getJSONArray("movies");
            for(int i=0; i < movies.length(); i++){
                JSONObject movie = movies.getJSONObject(i);
                String displayName = movie.getString("name") + " (" + movie.getString("year") + ")";
                nameToId.put(displayName, movie.getString("vanity"));
                result.add(displayName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v(BetterReviewedApp.LOG_TAG, "cache miss");

        autoCompleteCache.put(input, result);
        return result;
    }
}