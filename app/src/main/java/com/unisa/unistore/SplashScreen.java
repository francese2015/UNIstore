package com.unisa.unistore;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;

public class SplashScreen extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    private View vLogo;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        vLogo = findViewById(R.id.imgLogo);
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(this,
                R.animator.fab_anim);
        set.setTarget(vLogo);
        set.start();

        Slide slideExitTransition = new Slide(Gravity.BOTTOM);
        getWindow().setExitTransition(slideExitTransition);
        //slideExitTransition.excludeTarget(R.id.imgLogo, true);

        getWindow().setExitTransition(slideExitTransition);

       // vLogo.animate().setDuration(3000).setStartDelay(0).scaleX(1).scaleY(1).start();

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, MainActivity.class);

                //ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this, Pair.create(vLogo, "fab"));

                //startActivity(i,transitionActivityOptions.toBundle());
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}