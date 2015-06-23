package serverconnection;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;

import com.parasohjelmistoprojekti.bexit.MainActivity;

import entities.MapSquare;

/**
 * Created by Esa on 3.2.2015.
 */
public class GetSquares extends AsyncTask<Location, Integer, ArrayList<MapSquare>> {

    private String IP = "http://ec2-52-28-147-181.eu-central-1.compute.amazonaws.com:3000/locations/getlocation";

    protected ArrayList<MapSquare> doInBackground(Location... locations) {

        HttpClient httpClient = new DefaultHttpClient();

        try {
            HttpPost request = new HttpPost(IP);

            request.setHeader("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("northwestLat", Double.toString(locations[0].getLatitude())));
            pairs.add(new BasicNameValuePair("northwestLon", Double.toString(locations[0].getLongitude())));
            pairs.add(new BasicNameValuePair("exactNorthwestLat", Double.toString(locations[1].getLatitude())));
            pairs.add(new BasicNameValuePair("exactNorthwestLon", Double.toString(locations[1].getLongitude())));

            request.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));



            HttpResponse response = httpClient.execute(request);
            return parseResult(response);
        }catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return null;


    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(Long result) {
    }

    private ArrayList<MapSquare> parseResult(HttpResponse response){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String json = reader.readLine();
            System.out.println(json);
            JSONTokener tokener = new JSONTokener(json);
            JSONArray jsonArray = new JSONArray(tokener);
            ArrayList<MapSquare> parsedSquares = new ArrayList<MapSquare>();
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MapSquare mapSquare = new MapSquare(jsonObject.getString("_id"),
                        Double.parseDouble(jsonObject.getString("northwestLat")),
                        Double.parseDouble(jsonObject.getString("northwestLon")),
                        Double.parseDouble(jsonObject.getString("southeastLat")),
                        Double.parseDouble(jsonObject.getString("southeastLon")),
                        jsonObject.getString("typeOfLocation"));
                parsedSquares.add(mapSquare);
            }
            return parsedSquares;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
