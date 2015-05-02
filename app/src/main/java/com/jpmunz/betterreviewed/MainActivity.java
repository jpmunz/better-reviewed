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

    private static final int AUTO_COMPLETE_THRESHOLD = 5;

    /*
    TODO

        - setting menu should allow configuration of user/critic review weighting

        -implement compareRatings
            - show a loading indicator on the rhs
            - show a warning indicator if the call failed
            - show a checkmark next to the higher rated movie
            - activate a ratings search for the 2 given results

        - Bugs
            - 2 results for the notebook?

         http://makovkastar.github.io/blog/2014/04/12/android-autocompletetextview-with-suggestions-from-a-web-service/
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupMovieInput(R.id.movie1_autoComplete, R.id.movie1_autoComplete_progressBar, R.id.movie2_autoComplete);
        setupMovieInput(R.id.movie2_autoComplete, R.id.movie2_autoComplete_progressBar, R.id.movie1_autoComplete);
    }

    protected void setupMovieInput(int inputId, int progressBarId, final int otherInputId) {
        RottenTomatoesAutoCompleteTextView movieAutoComplete = (RottenTomatoesAutoCompleteTextView) findViewById(inputId);
        movieAutoComplete.setAdapter(new RottenTomatoesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));

        movieAutoComplete.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
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

        movieAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                nextMovieInput(otherInputId);
            }
        });

        movieAutoComplete.setThreshold(AUTO_COMPLETE_THRESHOLD);
        movieAutoComplete.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(progressBarId));
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
        AutoCompleteTextView movie1 = (AutoCompleteTextView) findViewById(R.id.movie1_autoComplete);
        AutoCompleteTextView movie2 = (AutoCompleteTextView) findViewById(R.id.movie2_autoComplete);

        Log.v(BetterReviewedApp.LOG_TAG, "comparing " +movie1.getText() + " and " + movie2.getText());
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
