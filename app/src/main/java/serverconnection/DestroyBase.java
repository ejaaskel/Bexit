package serverconnection;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;

import com.parasohjelmistoprojekti.bexit.MainActivity;

import entities.MapSquare;

/**
 * Created by Esa on 3.2.2015.
 */
public class DestroyBase extends AsyncTask<Location, Integer, String> {

    private String IP = "http://ec2-52-28-147-181.eu-central-1.compute.amazonaws.com:3000/users/base/";

    protected String doInBackground(Location... locations) {

        HttpClient httpClient = new DefaultHttpClient();

        try {
            String IPtoUse = IP.concat(MainActivity.hashedImei);
            HttpDelete request = new HttpDelete(IPtoUse);
            request.setHeader("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");

            HttpResponse response = httpClient.execute(request);
            System.out.println(response.getEntity().toString());
            return "Yay";
        }catch (Exception ex) {
            // handle exception here
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
                        Double.parseDouble(jsonObject.getString("northwestLon")),
                        Double.parseDouble(jsonObject.getString("northwestLat")),
                        Double.parseDouble(jsonObject.getString("southeastLon")),
                        Double.parseDouble(jsonObject.getString("southeastLat")),
                        jsonObject.getString("ownerHash"));
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
