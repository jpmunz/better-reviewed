package com.jpmunz.betterreviewed;

import android.app.Application;
import java.util.ArrayList;
import java.util.HashMap;

public class BetterReviewedApp extends Application {

    public static final String LOG_TAG = "better_reviewed";
    private static BetterReviewedApp instance = null;

    /**
     * Convenient accessor, saves having to call and cast getApplicationContext()
     */
    public static BetterReviewedApp getInstance() {
        checkInstance();
        return instance;
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //provide an instance for our static accessors
        instance = this;
    }
}