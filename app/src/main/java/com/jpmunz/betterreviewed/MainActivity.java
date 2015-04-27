package com.jpmunz.betterreviewed;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.util.ArrayList;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    /*
    TODO


        http://makovkastar.github.io/blog/2014/04/12/android-autocompletetextview-with-suggestions-from-a-web-service/
        - loading indicator in autocomplete while retrieving
        - add a delayed autocomplete to buffer network calls
        - set threshold for autocomplete higher (5)

        
        -implement compareRatings
            - show a loading indicator on the rhs
            - show a warning indicator if the call failed
            - show a checkmark next to the higher rated movie
            - activate a ratings search for the 2 given results

        - setting menu should allow configuration of user/critic review weighting


        - Bugs
            - autocomplete suggestions should be as long as the longest result
            - should be able to click anywhere on the suggestion
            - 2 results for the notebook?
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupMovieInput(R.id.movie1_autocomplete, R.id.movie2_autocomplete);
        setupMovieInput(R.id.movie2_autocomplete, R.id.movie1_autocomplete);
    }

    protected void setupMovieInput(int inputId, final int otherInputId) {
        AutoCompleteTextView movie1AutoComplete = (AutoCompleteTextView) findViewById(inputId);
        movie1AutoComplete.setAdapter(new RottenTomatoesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));

        movie1AutoComplete.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL
                        && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    nextMovieInput(otherInputId);
                    return true;
                }

                return false;
            }
        });

        movie1AutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                nextMovieInput(otherInputId);
            }
        });
    }

    protected void nextMovieInput(int inputId) {
        AutoCompleteTextView movie1AutoComplete = (AutoCompleteTextView) findViewById(inputId);

        if (movie1AutoComplete.getText().length() == 0) {
            movie1AutoComplete.requestFocus();
        } else {
            compareRatings();
        }
    }

    protected void compareRatings() {
        AutoCompleteTextView movie1 = (AutoCompleteTextView) findViewById(R.id.movie1_autocomplete);
        AutoCompleteTextView movie2 = (AutoCompleteTextView) findViewById(R.id.movie2_autocomplete);

        Log.v(TAG, "comparing " +movie1.getText() + " and " + movie2.getText());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
