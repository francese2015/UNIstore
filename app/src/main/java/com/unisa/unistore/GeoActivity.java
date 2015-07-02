package com.unisa.unistore;

/**
 * Created by utente pc on 02/07/2015.
 */
import android.app.Activity;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

public class GeoActivity extends Activity
{
    private String providerId = LocationManager.GPS_PROVIDER;
    private Geocoder geo = null;
    private LocationManager locationManager=null;
    private static final int MIN_DIST=20;
    private static final int MIN_PERIOD=30000;

    private LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }
        @Override
        public void onProviderEnabled(String provider)
        {
            // attivo GPS su dispositivo
            updateText(R.id.enabled, "TRUE");
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            // disattivo GPS su dispositivo
            updateText(R.id.enabled, "FALSE");
        }
        @Override
        public void onLocationChanged(Location location)
        {
            updateGUI(location);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        geo=new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location!=null)
            updateGUI(location);
        if (locationManager!=null && locationManager.isProviderEnabled(providerId))
            updateText(R.id.enabled, "TRUE");
        else
            updateText(R.id.enabled, "FALSE");
        locationManager.requestLocationUpdates(providerId, MIN_PERIOD,MIN_DIST, locationListener);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (locationManager!=null && locationManager.isProviderEnabled(providerId))
            locationManager.removeUpdates(locationListener);
    }
    private void updateGUI(Location location)
    {
        Date timestamp = new Date(location.getTime());
        updateText(R.id.timestamp, timestamp.toString());
        double latitude = location.getLatitude();
        updateText(R.id.latitude, String.valueOf(latitude));
        double longitude = location.getLongitude();
        updateText(R.id.longitude, String.valueOf(longitude));
        new AddressSolver().execute(location);
    }

    private void updateText(int id, String text)
    {
        TextView textView = (TextView) findViewById(id);
        textView.setText(text);
    }
}