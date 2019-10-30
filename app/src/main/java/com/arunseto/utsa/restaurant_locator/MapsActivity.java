package com.arunseto.utsa.restaurant_locator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String API_KEY = "AIzaSyBO7_U7r1oST2upR26wkjwLQfYSMbAogQ4";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
    private static final String TYPE_SEARCH = "/nearbysearch";
    private static final String OUT_JSON = "/json?";
    private static final String LOG_TAG = "ListRest";
    private GoogleMap mMap;
    private LinearLayout llItem;
    private String longitude, latitude;
    private Intent intent;
    private Double lat, lng;
    private int radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Initialized
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        llItem = findViewById(R.id.llItem);


        // Meng-apply Map
        mapFragment.getMapAsync(this);

        intent = getIntent();
        longitude = intent.getStringExtra("long");
        latitude = intent.getStringExtra("lat");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        lat = Double.parseDouble(latitude);
        lng = Double.parseDouble(longitude);
        radius = 1000;

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding

            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        final LatLng[] myLoc = {new LatLng(1, 1)};
        mMap.setMyLocationEnabled(true);
//        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//            @Override
//            public void onMyLocationChange(Location location) {
//                myLoc[0] = new LatLng(location.getLatitude(), location.getLongitude());
//                mMap.addCircle(new CircleOptions().center(myLoc[0]));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc[0], 15));
//            }
//        });

        mMap.addCircle(new CircleOptions().center(new LatLng(lat, lng)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 18));

        mMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest poi) {

            }
        });

        mMap.getUiSettings().setMapToolbarEnabled(true);

        // Add a marker in Sydney and move the camera


        showItems(lat, lng, radius);
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    public void showItems(final double lat, final double lng, int radius) {
        llItem.removeAllViews();
        View child;
        LinearLayout llListItem,llItemSub;
        TextView tvItemName,tvItemRating,tvItemDistance,tvItemTime,tvItemPrice,tvItemStatus;


        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_SEARCH);
            sb.append(OUT_JSON);
            sb.append("location=" + lat + "," + lng);
            sb.append("&radius=" + radius);
            sb.append("&type=restaurant");
            sb.append("&key=" + API_KEY);

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Toast.makeText(this, e + "", Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Error processing Places API URL", e);
        } catch (IOException e) {
            Toast.makeText(this, e + "", Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Error connecting to Places API", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("results");

            double restaurantLat,restaurantLng,restaurantRating,distance,time;
            int restaurantPrice;
            String restaurantName;
            Boolean restaurantStatus;

            NumberFormat formatter = new DecimalFormat("#0.00");

            // Extract the descriptions from the results
            for (int i = 0; i <= predsJsonArray.length(); i++) {
                child = getLayoutInflater().inflate(R.layout.template_restaurant, null);
                llListItem = child.findViewById(R.id.llListItem);
                tvItemName = child.findViewById(R.id.tvItemName);
                tvItemRating = child.findViewById(R.id.tvItemRating);
                tvItemDistance = child.findViewById(R.id.tvItemDistance);
                tvItemTime = child.findViewById(R.id.tvItemTime);
                tvItemPrice = child.findViewById(R.id.tvItemPrice);
                tvItemStatus = child.findViewById(R.id.tvItemStatus);

                // mengambil latitude
                restaurantLat = predsJsonArray.getJSONObject(i)
                        .getJSONObject("geometry")
                        .getJSONObject("location")
                        .getDouble("lat");
                restaurantLng = predsJsonArray.getJSONObject(i)
                        .getJSONObject("geometry")
                        .getJSONObject("location")
                        .getDouble("lng");
                restaurantName = predsJsonArray.getJSONObject(i).getString("name");
//                restaurantRating = predsJsonArray.getJSONObject(i).getDouble("rating");
                restaurantRating = 5.0;
                distance = CalculationByDistance(new LatLng(lat, lng), new LatLng(restaurantLat, restaurantLng));
                time = distance*17;
                restaurantPrice = 1;
//                restaurantStatus = predsJsonArray.getJSONObject(i)
//                        .getJSONObject("opening_hours")
//                        .getBoolean("open_now");



                // memberi label pada list
                tvItemName.setText(restaurantName);
                tvItemRating.setText("-");
                tvItemDistance.setText(formatter.format(distance) + " km");
                tvItemTime.setText(formatter.format(time)+" menit");
                tvItemPrice.setText("");
                for (int price=1;price<=restaurantPrice;price++){
                    tvItemPrice.append("$");
                }
                tvItemStatus.setText((true == true ? "Open": "Closed"));

                final double finalLng = restaurantLng;
                final double finalLat = restaurantLat;
                final String finalRestaurantName = restaurantName;

                llListItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMap.clear();
                        mMap.addCircle(new CircleOptions().center(new LatLng(lat, lng)));
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(finalLat, finalLng), 18);
                        mMap.animateCamera(cameraUpdate);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(finalLat, finalLng)).title(finalRestaurantName)).showInfoWindow();
                    }
                });


                // menandai lokasi di map
//                mMap.addCircle(new CircleOptions().center(new LatLng(restaurantLat, restaurantLng)));


                llItem.addView(child);

            }
        } catch (JSONException e) {
//            Toast.makeText(this, e + "s", Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Error processing JSON results", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}