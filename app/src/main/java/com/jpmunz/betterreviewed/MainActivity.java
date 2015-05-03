package com.jpmunz.betterreviewed;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends Activity {
    private static final int AUTO_COMPLETE_THRESHOLD = 1;
    private SharedPreferences settings;

    private RottenTomatoesAPI api;

    private AutoCompleteTextView movie1;
    private AutoCompleteTextView movie2;
    private ImageView movie1Success;
    private ImageView movie2Success;
    private ProgressBar compareRatingsProgress;
    private CompareRatings mCompareRatings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        api = new RottenTomatoesAPI();

        setContentView(R.layout.activity_main);
        setupMovieInput(R.id.movie1_autoComplete, R.id.movie1_autoComplete_progressBar, R.id.movie2_autoComplete);
        setupMovieInput(R.id.movie2_autoComplete, R.id.movie2_autoComplete_progressBar, R.id.movie1_autoComplete);

        movie1 = (AutoCompleteTextView) findViewById(R.id.movie1_autoComplete);
        movie2 = (AutoCompleteTextView) findViewById(R.id.movie2_autoComplete);
        compareRatingsProgress = (ProgressBar) findViewById(R.id.compare_ratings_progress);
        movie1Success = (ImageView) findViewById(R.id.movie1_success);
        movie2Success = (ImageView) findViewById(R.id.movie2_success);

        Button reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compareRatingsProgress.setVisibility(View.GONE);
                movie1Success.setVisibility(View.GONE);
                movie2Success.setVisibility(View.GONE);
                movie1.clearFocus();
                movie2.clearFocus();
                movie1.setEnabled(true);
                movie2.setEnabled(true);
                movie1.setText("");
                movie2.setText("");

                if (mCompareRatings != null) {
                    mCompareRatings.cancel(true);
                }
            }
        });
    }

    protected void setupMovieInput(int inputId, int progressBarId, final int otherInputId) {
        RottenTomatoesAutoCompleteTextView movieAutoComplete = (RottenTomatoesAutoCompleteTextView) findViewById(inputId);
        movieAutoComplete.setAdapter(new RottenTomatoesAutoCompleteAdapter(this, R.layout.autocomplete_list_item, api));

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

        movieAutoComplete.setValidator(new ForceSelection(movieAutoComplete));
        movieAutoComplete.setThreshold(AUTO_COMPLETE_THRESHOLD);
        movieAutoComplete.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(progressBarId));
    }

    class ForceSelection implements AutoCompleteTextView.Validator {
        private AutoCompleteTextView mAutoComplete;

        public ForceSelection(AutoCompleteTextView autoComplete) {
            mAutoComplete = autoComplete;
        }

        @Override
        public boolean isValid(CharSequence charSequence) {
            return ((RottenTomatoesAutoCompleteAdapter) mAutoComplete.getAdapter()).hasValue(charSequence.toString());
        }

        @Override
        public CharSequence fixText(CharSequence charSequence) {
            RottenTomatoesAutoCompleteAdapter adapter = ((RottenTomatoesAutoCompleteAdapter) mAutoComplete.getAdapter());

            if (adapter.getCount() > 0) {
                return adapter.getItem(0);
            } else {
                return charSequence;
            }
        }
    }

    protected void nextMovieInput(int inputId) {
        AutoCompleteTextView movieAutoComplete = (AutoCompleteTextView) findViewById(inputId);

        if (movieAutoComplete.getText().length() == 0) {
            movieAutoComplete.requestFocus();
        } else {
            mCompareRatings = new CompareRatings();
            mCompareRatings.execute(movie1.getText().toString(), movie2.getText().toString());
        }
    }

    private class CompareRatings extends AsyncTask<String, Void, Integer> {

        protected void onPreExecute() {
            compareRatingsProgress.setVisibility(View.VISIBLE);
            movie1Success.setVisibility(View.GONE);
            movie2Success.setVisibility(View.GONE);
            movie1.clearFocus();
            movie2.clearFocus();
            movie1.setEnabled(false);
            movie2.setEnabled(false);
        }

        protected Integer doInBackground(String... movieNames) {
            int criticWeight = Integer.parseInt(settings.getString("pref_critic_weight", "50"));
            int userWeight = Integer.parseInt(settings.getString("pref_user_weight", "50"));

            JSONObject result1;
            JSONObject result2;
            float rating1;
            float rating2;

            try {
                result1 = api.getRatings(movie1.getText().toString());
                result2 = api.getRatings(movie2.getText().toString());
            } catch (RottenTomatoesAPI.RottenTomatoesAPIException e) {
                Log.v(BetterReviewedApp.LOG_TAG, e.getMessage());
                return -1;
            }

            try {
                rating1 = (result1.getInt("critic") * criticWeight) + (result1.getInt("user") * userWeight);
                rating2 = (result2.getInt("critic") * criticWeight) + (result2.getInt("user") * userWeight);
            } catch (JSONException e) {
                e.printStackTrace();
                return -1;
            }

            if (rating1 > rating2) {
                return 0;
            } else if (rating2 > rating1) {
                return 1;
            } else {
                return 2;
            }
        }

        protected void onPostExecute(Integer winner) {
            compareRatingsProgress.setVisibility(View.GONE);
            movie1.setEnabled(true);
            movie2.setEnabled(true);

            if (winner == 0) {
                movie1Success.setVisibility(View.VISIBLE);
            } else if (winner == 1) {
                movie2Success.setVisibility(View.VISIBLE);
            } else if (winner == 2) {
                movie1Success.setVisibility(View.VISIBLE);
                movie2Success.setVisibility(View.VISIBLE);
            }
        }
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
