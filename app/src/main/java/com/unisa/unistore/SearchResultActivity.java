package com.unisa.unistore;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by utente pc on 17/06/2015.
 */
public class SearchResultActivity extends Activity {


        @Override
        public void onCreate(Bundle savedInstanceState) {

            handleIntent(getIntent());
        }

        @Override
        protected void onNewIntent(Intent intent) {

            handleIntent(intent);
        }

        private void handleIntent(Intent intent) {

            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                //use the query to search your data somehow
            }
        }


}
