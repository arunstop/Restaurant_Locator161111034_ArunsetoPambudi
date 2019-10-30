package com.arunseto.utsa.restaurant_locator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {


    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.layout);

        Button btnMaps = findViewById(R.id.btnMaps);
        Button btnAbout = findViewById(R.id.btnAbout);
        Button btnHelp = findViewById(R.id.btnHelp);

        final AboutFragment aboutF = new AboutFragment();
        final HelpFragment helpF = new HelpFragment();

        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRestaurants();
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutF.show(getSupportFragmentManager().beginTransaction().addToBackStack("1"), "Dialog Fragment");
            }
        });

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpF.show(getSupportFragmentManager().beginTransaction().addToBackStack("1"), "Dialog Fragment");
            }
        });
    }



    @SuppressLint("MissingPermission")
    public void startRestaurants() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(
                    String.valueOf(lm.getBestProvider(new Criteria(), true)).toString(),
                    1000,
                    0,
                    this
                    );
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Double longitude = location.getLongitude();
            Double latitude = location.getLatitude();
            String longit = Double.toString(longitude);
            String lat = Double.toString(latitude);
            intent.putExtra("long", longit);
            intent.putExtra("lat", lat);
            startActivity(intent);
            Double lo =  Double.parseDouble(intent.getStringExtra("long"));
            Double la = Double.parseDouble(intent.getStringExtra("lat"));
            Toast.makeText(this, lo+", "+la, Toast.LENGTH_SHORT).show();
        }
    }

    public void showRestaurants(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, show restaurants
            Snackbar.make(mLayout,
                    "Location permission available. Show restaurants.",
                    Snackbar.LENGTH_SHORT).show();
            startRestaurants();
        } else {
            // Permission is missing and must be requested.
            requestLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for location permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start preview Activity.
                Snackbar.make(mLayout, "Location permission granted. Showing restaurants.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                startRestaurants();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, "Location permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(mLayout, "Location access is required to display restaurants near you.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                }
            }).show();

        } else {
            Snackbar.make(mLayout,
                    "Permission is not available. Requesting location permission.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
