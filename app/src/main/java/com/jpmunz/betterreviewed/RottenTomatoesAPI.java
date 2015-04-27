package com.jpmunz.betterreviewed;

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


public class RottenTomatoesAPI {

    public class RottenTomatoesAPIException extends Exception {}

    private String API_ROOT = "http://api.rottentomatoes.com/api/public/v1.0/";
    private String API_KEY = "k4armx9qg4x5rq4xx8us8m6g";

    private String makeRequest(String endpoint, HashMap<String,String> queryStringParams) throws RottenTomatoesAPIException {
        StringBuilder sb = new StringBuilder(API_ROOT + endpoint + ".json");
        sb.append("?apikey=" + API_KEY);

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

    public ArrayList<String> autoComplete(String input) throws RottenTomatoesAPIException {
        ArrayList<String> result = new ArrayList<String>();

        HashMap<String, String> queryStringParams = new HashMap<String, String>();

        try {
            queryStringParams.put("q", URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            throw new RottenTomatoesAPIException();
        }
        queryStringParams.put("page_limit", "5");

        result.add("the movie 1");
        result.add("ff");
        return result;
/*
        String response =  this.makeRequest("movies", queryStringParams);


        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray movies = jsonResponse.getJSONArray("movies");
            for(int i=0; i < movies.length(); i++){
                result.add(movies.getJSONObject(i).getString("title"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
        */
    }
}