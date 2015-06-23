package com.parasohjelmistoprojekti.bexit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import entities.MapSquare;
import serverconnection.ClaimSquare;
import serverconnection.CreateBase;
import serverconnection.DestroyBase;
import serverconnection.GetSquares;
import serverconnection.LootSquare;
import utilities.SHA1hasher;


public class MainActivity extends ActionBarActivity {

    public static String hashedImei;
    public static String username;

    private TextView moneyText;
    private TextView soldiersText;

    private GoogleMap googleMap;
    private ArrayList<MapSquare> currentSquares;

    private double baseNorthwestLat;
    private double baseNorthwestLon;

    private boolean baseExistsAlready;

    private Button createBaseButton;
    private Button lootSquareButton;

    private MyLocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentSquares = new ArrayList<MapSquare>();

        final SharedPreferences sharedpreferences = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        hashedImei = sharedpreferences.getString("hash", "");
        username = sharedpreferences.getString("username","");

        baseNorthwestLat = 0;
        baseNorthwestLon = 0;

        if(sharedpreferences.getString("northwestLat",null)!=null && sharedpreferences.getString("northwestLat",null).equals("181.0")==false){
            System.out.println("Base found");
            baseExistsAlready = true;
            baseNorthwestLat = Double.parseDouble(sharedpreferences.getString("northwestLat",null));
            baseNorthwestLon = Double.parseDouble(sharedpreferences.getString("northwestLon",null));
        }
        else{
            baseExistsAlready = false;
        }

        //ROUNDING TESTS
            BigDecimal bd = new BigDecimal(10.473892);
            bd = bd.setScale(3, RoundingMode.HALF_UP);
            System.out.println(bd.doubleValue());

            long factor = (long) Math.pow(10,3);
            double latitudeTest = -32.847984 * factor;
            long tmp = (long) latitudeTest;
            latitudeTest = (double) tmp / factor;
            System.out.println(latitudeTest);

            factor = (long) Math.pow(10,3);
            double longitudeTest = 32.5478392 * factor;
            tmp = (long) longitudeTest;
            longitudeTest = (double) tmp / factor;
            System.out.println(longitudeTest);

            bd = new BigDecimal(-53.48927);
            bd = bd.setScale(3, RoundingMode.HALF_UP);
            System.out.println(bd.doubleValue());


        //Set up location listener
        mLocationListener = new MyLocationListener();
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, 10, mLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000, 10, mLocationListener);

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

                mLocationListener.getLocation();

                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                int squareIndex =  checkIfInsideCurrentSquares(arg0);
                if(squareIndex != -1){
                    try {
                        new ClaimSquare().execute(currentSquares.get(squareIndex)).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        moneyText = (TextView)findViewById(R.id.money_text);
        moneyText.setText(Integer.toString(sharedpreferences.getInt("money",-1))+" money");

        soldiersText = (TextView)findViewById(R.id.soldiers_text);
        soldiersText.setText(Integer.toString(sharedpreferences.getInt("soldiers",-1))+" soldiers");

        Button refreshButton = (Button)findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    System.out.println(mLocationListener.location.getLongitude());
                    currentSquares = new GetSquares().execute(mLocationListener.location,mLocationListener.exactLocation).get();
                    drawMap(currentSquares);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Not located yet!", Toast.LENGTH_LONG).show();
                }

            }
        });

        createBaseButton = (Button)findViewById(R.id.create_base);
        lootSquareButton = (Button)findViewById(R.id.loot_area);

        changeCreateBaseButton();

        lootSquareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject lootresult = new LootSquare().execute(mLocationListener.location,mLocationListener.exactLocation).get();

                    int newMoney = sharedpreferences.getInt("money", -1) + lootresult.getInt("lootedgold");
                    sharedpreferences.edit().putInt("money", newMoney).commit();
                    moneyText.setText(Integer.toString(newMoney)+ " money");

                    int newSoldiers = sharedpreferences.getInt("soldiers", -1)-lootresult.getInt("lostsoldiers");
                    sharedpreferences.edit().putInt("soldiers",newSoldiers).commit();
                    soldiersText.setText(Integer.toString(newSoldiers)+" soldiers");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

            /*
            LatLng base = new LatLng(65.085473,25.384426);
            LatLng start;
            int i = 0;
            int j = 0;
            while(true){
                while(true){
                    //calculate start point (up-left) and end point (down-right) for square
                    start = calculateOffset(base, i * -500, j * 500);
                    if(start.longitude > 25.557289)
                        break;
                    LatLng end = calculateOffset(start, 500, 500);
                    System.out.println("northwestlon "+start.longitude+ " northwestlan "+start.latitude + " southeastlon "+end.longitude+ " southwestlan "+end.latitude);

                    //Create points for polyline
                    PolygonOptions rectOptions = new PolygonOptions()
                            .add(start)
                            .add(new LatLng(start.latitude, end.longitude))  // North of the previous point, but at the same longitude
                            .add(end)  // Same latitude, and 30km to the west
                            .add(new LatLng(end.latitude, start.longitude))  // Same longitude, and 16km to the south
                            .add(start)
                            .strokeWidth(1.0f)
                            .fillColor(Color.parseColor("#5500FF00"));

                    // Get back the mutable Polyline
                    Polygon polyline = googleMap.addPolygon(rectOptions);
                    j++;
                }
                j = 0;
                if(start.latitude < 64.972166)
                    break;
                i++;
            }
            */
        }


        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                //SEND CURRENT LOCATION TO GET SQUARES
                try {
                    System.out.println(mLocationListener.location.getLongitude());
                    currentSquares = new GetSquares().execute(mLocationListener.location,mLocationListener.exactLocation).get();
                    drawMap(currentSquares);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Not located yet!", Toast.LENGTH_LONG).show();
                }

                handler.postDelayed(this, 1000*5);
            }
        };

        handler.post(r);


    }

    private void changeCreateBaseButton(){
        if(baseExistsAlready){
            lootSquareButton.setEnabled(true);

            createBaseButton.setText("Destroy base");
            createBaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        new DestroyBase().execute(mLocationListener.location, mLocationListener.exactLocation).get();
                        baseExistsAlready = false;
                        baseNorthwestLat = 181;
                        baseNorthwestLon = 181;
                        Toast.makeText(MainActivity.this, "Base destroyed!", Toast.LENGTH_LONG);
                        changeCreateBaseButton();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else{
            lootSquareButton.setEnabled(false);

            createBaseButton.setText("Create base");
            createBaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONObject baseCreateObject = new CreateBase().execute(mLocationListener.location,mLocationListener.exactLocation).get();
                        baseExistsAlready = true;
                        baseNorthwestLat = mLocationListener.location.getLatitude();
                        baseNorthwestLon = mLocationListener.location.getLongitude();
                        Toast.makeText(MainActivity.this, "Base created!", Toast.LENGTH_SHORT);
                        changeCreateBaseButton();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
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


    private void drawMap(ArrayList<MapSquare> squares){
        googleMap.clear();

        if(baseExistsAlready) {

            System.out.println("Draw base: " + baseNorthwestLat + " " + baseNorthwestLon);
            PolygonOptions baserectOptions = new PolygonOptions()
                    .add(new LatLng(baseNorthwestLat, baseNorthwestLon))
                    .add(new LatLng(baseNorthwestLat, baseNorthwestLon + 0.001))  // North of the previous point, but at the same longitude
                    .add(new LatLng(baseNorthwestLat + 0.001, baseNorthwestLon + 0.001))  // Same latitude, and 30km to the west
                    .add(new LatLng(baseNorthwestLat + 0.001, baseNorthwestLon))  // Same longitude, and 16km to the south
                    .add(new LatLng(baseNorthwestLat, baseNorthwestLon))
                    .strokeColor(Color.BLACK)
                    .strokeWidth(1.0f)
                    .fillColor(Color.parseColor("#FF6666"))
                    .visible(true);

            Polygon basepolyline = googleMap.addPolygon(baserectOptions);
        }

        for(int i = 0; i < squares.size(); i++){
            System.out.println("Drawing Square");
            //Create points for polyline
            PolygonOptions rectOptions = new PolygonOptions()
                    .add(new LatLng(squares.get(i).getNorthWestLat(),squares.get(i).getNorthWestLon()))
                    .add(new LatLng(squares.get(i).getNorthWestLat(),squares.get(i).getSouthEastLon()))  // North of the previous point, but at the same longitude
                    .add(new LatLng(squares.get(i).getSouthEastLat(),squares.get(i).getSouthEastLon()))  // Same latitude, and 30km to the west
                    .add(new LatLng(squares.get(i).getSouthEastLat(),squares.get(i).getNorthWestLon()))  // Same longitude, and 16km to the south
                    .add(new LatLng(squares.get(i).getNorthWestLat(),squares.get(i).getNorthWestLon()))
                    .strokeColor(Color.BLACK)
                    .strokeWidth(1.0f)
                    .visible(true);
            System.out.println(squares.get(i).getNorthWestLat());
            System.out.println(squares.get(i).getNorthWestLon());
            System.out.println(squares.get(i).getSouthEastLat());
            System.out.println(squares.get(i).getSouthEastLon());
            if(squares.get(i).getType().equals("forest")){
                rectOptions.fillColor(Color.parseColor("#5500FF00"));
            }
            else             if(squares.get(i).getType().equals("building")){
                rectOptions.fillColor(Color.parseColor("#5533FF00"));
            }
            else            if(squares.get(i).getType().equals("parkinglot")){
                rectOptions.fillColor(Color.parseColor("#55000000"));
            }
            else            if(squares.get(i).getType().equals("hospital")){
                rectOptions.fillColor(Color.parseColor("#55FF0000"));
            }
            else            if(squares.get(i).getType().equals("water")){
                rectOptions.fillColor(Color.parseColor("#550000FF"));
            }
            else            if(squares.get(i).getType().equals("otherPlace")){
                rectOptions.fillColor(Color.parseColor("#55aaFFaa"));
            }

            // Get back the mutable Polyline
            Polygon polyline = googleMap.addPolygon(rectOptions);
            System.out.println("Drew to the map");
        }

    }

    private int checkIfInsideCurrentSquares(LatLng touchPosition){

        for(int i = 0; i < currentSquares.size(); i++){
            if(touchPosition.latitude > currentSquares.get(i).getNorthWestLat() && touchPosition.longitude > currentSquares.get(i).getNorthWestLon())
                if(touchPosition.longitude < currentSquares.get(i).getSouthEastLat() && touchPosition.longitude < currentSquares.get(i).getSouthEastLon()){
                    System.out.println("Inside square "+i);
                    return i;
                }
        }

        return -1;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.registration:
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }


    private final class MyLocationListener implements LocationListener {

        public Location location;
        public Location exactLocation;

        @Override
        public void onLocationChanged(Location locFromGps) {
            // called when the listener is notified with a location update from the GPS
            System.out.println("NEW LOCATIONS");

            location = locFromGps;
            exactLocation = locFromGps;

            if(location.getLatitude()>0){
                BigDecimal bd = new BigDecimal(location.getLatitude());
                bd = bd.setScale(3, RoundingMode.HALF_UP);
                location.setLatitude(bd.doubleValue());
            }
            else{
                System.out.println(location.getLatitude());
                long factor = (long) Math.pow(10,3);
                double latitude = location.getLatitude() * factor;
                long tmp = (long) latitude;
                latitude = (double) tmp / factor;
                System.out.println(latitude);
                location.setLatitude(latitude);
            }

            if(location.getLongitude()>0){
                System.out.println(location.getLongitude());
                long factor = (long) Math.pow(10,3);
                double longitude = location.getLongitude() * factor;
                long tmp = (long) longitude;
                longitude = (double) tmp / factor;
                System.out.println(longitude);
                location.setLongitude(longitude);
            }
            else{
                BigDecimal bd = new BigDecimal(location.getLongitude());
                bd = bd.setScale(3, RoundingMode.HALF_UP);
                location.setLongitude(bd.doubleValue());
            }

            /*//ROUND LATITUDE DOWN to either 0 or 5
            if(latString.substring(2).equals("1")||latString.substring(2).equals("2")||latString.substring(2).equals("3")||latString.substring(2).equals("4")){
                //replace old
                latString = latString.substring(0,2) + "0";
            }
            else if(latString.substring(2).equals("6")||latString.substring(2).equals("7")||latString.substring(2).equals("8")||latString.substring(2).equals("9")){
                latString = latString.substring(0,2) + "5";
            }

            //On northern hemisphere (lon > 0) longitude needs to be rounded up, away from zero, towatds north pole
            if(location.getLongitude() > 0){
                if(lonString.substring(2).equals("1")||lonString.substring(2).equals("2")||lonString.substring(2).equals("3")||lonString.substring(2).equals("4")){
                    String lastdecimal = lonString.substring(2);
                    String adder = "0.00"+ Integer.toString(5 - Integer.parseInt(lastdecimal));
                    double result =  location.getLongitude() + Double.parseDouble(adder);
                    lonString = Double.toString(result);
                }
                else if(lonString.substring(2).equals("6")||lonString.substring(2).equals("7")||lonString.substring(2).equals("8")||lonString.substring(2).equals("9")){
                    String lastdecimal = lonString.substring(2);
                    String adder = "0.00"+ Integer.toString(10 - Integer.parseInt(lastdecimal));
                    double result =  location.getLongitude() + Double.parseDouble(adder);
                    lonString = Double.toString(result);
                }
            }

            //On southern hemisphere longituide needs to be rounded down, towards zero, towards north pole
            else{
                if(lonString.substring(2).equals("1")||lonString.substring(2).equals("2")||lonString.substring(2).equals("3")||lonString.substring(2).equals("4")){
                    String lastdecimal = lonString.substring(2);
                    String subtracter = "0.00"+ Integer.toString(Integer.parseInt(lastdecimal));
                    double result =  location.getLongitude() - Double.parseDouble(subtracter);
                    lonString = Double.toString(result);
                }
                else if(lonString.substring(2).equals("6")||lonString.substring(2).equals("7")||lonString.substring(2).equals("8")||lonString.substring(2).equals("9")){
                    String lastdecimal = lonString.substring(2);
                    String subtracter = "0.00"+ Integer.toString(5 + Integer.parseInt(lastdecimal));
                    double result =  location.getLongitude() + Double.parseDouble(subtracter);
                    lonString = Double.toString(result);
                }
            }
*/
            System.out.println(location.getLatitude());
            System.out.println(location.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
            // called when the GPS provider is turned off (user turning off the GPS on the phone)
        }

        @Override
        public void onProviderEnabled(String provider) {
            // called when the GPS provider is turned on (user turning on the GPS on the phone)
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // called when the status of the GPS provider changes
        }

        public Location getLocation() {
            return location;
        }

    }


}
