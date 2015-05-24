package com.unisa.unistore;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Bangers/Bangers.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // Add your initialization code here
        Parse.initialize(this, getString(R.string.parse_app_id),
                getString(R.string.parse_client_key));

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));
    }
}
