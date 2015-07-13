package com.unisa.unistore;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

public class SplashScreenActivity extends Activity {

    private static final String TAG = "SplashScreen";
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    public static final String PREFS_NAME = "PrefsFile";
    private View vLogo;
    private SharedPreferences settings;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        settings = getSharedPreferences(PREFS_NAME, 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                vLogo = findViewById(R.id.splash_img);
                AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(SplashScreenActivity.this,
                        R.animator.fab_anim);
                set.setTarget(vLogo);
                set.start();

                Slide slideExitTransition = new Slide(Gravity.BOTTOM);
                getWindow().setExitTransition(slideExitTransition);
                //slideExitTransition.excludeTarget(R.id.imgLogo, true);

                // vLogo.animate().setDuration(3000).setStartDelay(0).scaleX(1).scaleY(1).start();

                // Restore preferences
                boolean alreadyLaunched = settings.getBoolean("alreadyLaunched", false);


                if(alreadyLaunched) {
                    launchLoginActivity();
                    Log.d(TAG, "onCreate()/La splash screen è stata già lanciata");
                } else {
                    Log.d(TAG, "onCreate()/La splash screen non è ancora stata lanciata");
                    new Handler().postDelayed(new Runnable() {

                        /*
                         * Showing splash screen with a timer. This will be useful when you
                         * want to show case your app logo / company
                         */
                        @Override
                        public void run() {
                            // This method will be executed once the timer is over
                            // Start your app main activity
                            launchLoginActivity();
                        }
                    }, SPLASH_TIME_OUT);
                }
            }
        }).run();
    }

    private void launchLoginActivity() {
        markSplashScreenAsAlreadyLaunched(true, settings);

        Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);

        //ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this, Pair.create(vLogo, "fab"));

        //startActivity(i,transitionActivityOptions.toBundle());
        startActivity(i);

        // close this activity
        finish();
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        markSplashScreenAsAlreadyLaunched(true, settings);
    }

    public static void markSplashScreenAsAlreadyLaunched(final boolean alreadyLaunched, final SharedPreferences settings) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("alreadyLaunched", alreadyLaunched);

                // Commit the edits!
                editor.commit();
            }
        }).run();
    }
}