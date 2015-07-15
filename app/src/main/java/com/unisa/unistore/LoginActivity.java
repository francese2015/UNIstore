/*
 *  Copyright (c) 2014, Parse, LLC. All rights reserved.
 *
 *  You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 *  copy, modify, and distribute this software in source code or binary form for use
 *  in connection with the web services and APIs provided by Parse.
 *
 *  As with any software that integrates with the Parse platform, your use of
 *  this software is subject to the Parse Terms of Service
 *  [https://www.parse.com/about/terms]. This copyright notice shall be
 *  included in all copies or substantial portions of the software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.unisa.unistore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseInstallation;
import com.parse.ParseUser;

import ui.ParseLoginBuilder;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Shows the user profile. This simple activity can function regardless of whether the user
 * is currently logged in.
 */
public class LoginActivity extends Activity {
    private static final int LOGIN_REQUEST = 0;

    private TextView titleTextView;
    private TextView emailTextView;
    private TextView nameTextView;
    private Button loginOrLogoutButton;
    private Button noLoginButton;

    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        titleTextView = (TextView) findViewById(R.id.profile_title);
        emailTextView = (TextView) findViewById(R.id.profile_email);
        nameTextView = (TextView) findViewById(R.id.profile_name);
        loginOrLogoutButton = (Button) findViewById(R.id.login_or_logout_button);
        noLoginButton = (Button) findViewById(R.id.no_login_button);
        titleTextView.setText(R.string.profile_title_logged_in);

        loginOrLogoutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userAuthenticated()) {
                    // User clicked to log out.
                    ParseUser.logOut();
                    currentUser = null;
                    showProfileLoggedOut();
                } else {
                    // User clicked to log in.
                    ParseLoginBuilder loginBuilder = new ParseLoginBuilder(
                            LoginActivity.this);
                    startActivityForResult(loginBuilder.build(), LOGIN_REQUEST);
                }
            }
        });

        noLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                open(v);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = ParseUser.getCurrentUser();
        if(userAuthenticated()) {
            //showProfileLoggedIn();
            launchMainActivity();
        } else {
            showProfileLoggedOut();
        }
    }

    private boolean userAuthenticated() {
        if(currentUser != null)
            return true;

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == LOGIN_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                currentUser = ParseUser.getCurrentUser();



                // Associate the device with a user
                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                if(installation.get("user") == null || !installation.get("user").equals(currentUser.getObjectId())) {
                    installation.put("user", currentUser.getObjectId());
                    installation.saveEventually(); //Se sono arrivato qui significa che ho la connessione attivata
                }

                if(userAuthenticated())
                    launchMainActivity();
                else
                    showProfileLoggedOut();
            }
        }
    }

    private void launchMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * Shows the profile of the given user.
     */
    private void showProfileLoggedIn() {
        titleTextView.setText(R.string.profile_title_logged_in);
        emailTextView.setText(currentUser.getEmail());
        String fullName = currentUser.getString("name");
        if (fullName != null) {
            nameTextView.setText(fullName);
        }
        loginOrLogoutButton.setText(R.string.profile_logout_button_label);
        noLoginButton.setVisibility(View.GONE);
    }

    /**
     * Show a message asking the user to log in, toggle login/logout button text.
     */
    private void showProfileLoggedOut() {
        titleTextView.setText(R.string.profile_title_logged_out);
        emailTextView.setText("");
        nameTextView.setText("");
        loginOrLogoutButton.setText(R.string.profile_login_button_label);
        noLoginButton.setVisibility(View.VISIBLE);
    }

    public void open(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.skip_message) + " " +
                getString(R.string.app_name));

        alertDialogBuilder.setPositiveButton(R.string.skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                ParseLoginBuilder loginBuilder = new ParseLoginBuilder(
                        LoginActivity.this);
                launchMainActivity();
            }
        });

        alertDialogBuilder.setNegativeButton(R.string.no_skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
