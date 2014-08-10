package com.jpmunz.betterreviewed;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
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

        URI url = null;
        try {
            url = new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new RottenTomatoesAPIException();
        }

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        try {
            return client.execute(request, responseHandler);
        } catch (IOException e) {
            throw new RottenTomatoesAPIException();
        }
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

        String response =  this.makeRequest("movies", queryStringParams);

        /*
        suggest = new ArrayList<String>();
        JSONArray jArray = new JSONArray(data);
        for(int i=0;i<jArray.getJSONArray(1).length();i++){
            String SuggestKey = jArray.getJSONArray(1).getString(i);
            suggest.add(SuggestKey);
        }
        */

        return result;
    }
}