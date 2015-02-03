package com.parasohjelmistoprojekti.bexit;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleMap googleMap;
        LatLng myPosition;

        //Get and set up google map object
        MapFragment fm = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        googleMap = fm.getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng arg0) {
                //Make toast of the location where user touched
                Log.d("arg0", arg0.latitude + "-" + arg0.longitude);
                String toast = "Lat: " + arg0.latitude + " Lng: " + arg0.longitude;
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
            }
        });

        //Get location manager and location object of user
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

        if(location!=null) {
            //get location of user
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            myPosition = new LatLng(latitude, longitude);

            //Zoom to last known location of user
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(myPosition, 10);
            googleMap.animateCamera(yourLocation);

            LatLng base = new LatLng(65.062188,25.457940);
            for(int i = 0; i < 5; i++){
                //calculate start point (up-left) and end point (down-right) for square
                LatLng start = calculateOffset(base,i*500,i*500);
                LatLng end = calculateOffset(start,500,500);

                //Create points for polyline
                PolylineOptions rectOptions = new PolylineOptions()
                        .add(start)
                        .add(new LatLng(start.latitude, end.longitude))  // North of the previous point, but at the same longitude
                        .add(end)  // Same latitude, and 30km to the west
                        .add(new LatLng(end.latitude, start.longitude))  // Same longitude, and 16km to the south
                        .add(start); // Closes the polyline.

                // Get back the mutable Polyline
                Polyline polyline = googleMap.addPolyline(rectOptions);
            }




        }
    }

    private LatLng calculateOffset(LatLng original, int latDiff, int lonDiff){
        //This function calculates new point for latlng object when difference is given meters
        //Position, decimal degrees
        double lat = original.latitude;
        double lon = original.longitude;

        //Earth’s radius, sphere
        double R=6378137;

        //offsets in meters
        int dn = latDiff;
        int de = lonDiff;

        //Coordinate offsets in radians
        double dLat = dn/R;
        double dLon = de/(R*Math.cos(Math.PI*lat/180));

        //OffsetPosition, decimal degrees
        double latO = lat + dLat * 180/Math.PI;
        double lonO = lon + dLon * 180/Math.PI;
        LatLng newLatLng = new LatLng(latO,lonO);
        return newLatLng;

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
