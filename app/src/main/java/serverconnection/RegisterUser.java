package serverconnection;

import android.os.AsyncTask;

import com.parasohjelmistoprojekti.bexit.MainActivity;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import entities.MapSquare;

/**
 * Created by Esa on 10.3.2015.
 */
public class RegisterUser extends AsyncTask<String, Integer, JSONObject> {

    private String IP = "http://ec2-52-28-46-121.eu-central-1.compute.amazonaws.com:3000/users/userlist";

    protected JSONObject doInBackground(String... input) {

        HttpClient httpClient = new DefaultHttpClient();

        try {
            HttpPost request = new HttpPost(IP);
            request.setHeader("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            JSONObject keyArg = new JSONObject();
            keyArg.put("username", input[0]);
            keyArg.put("email", input[1]);
            keyArg.put("password", input[2]);

            //request.setEntity(keyArg);

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("username", input[0]));
            pairs.add(new BasicNameValuePair("email", input[1]));
            pairs.add(new BasicNameValuePair("password", input[2]));
            request.setEntity(new UrlEncodedFormEntity(pairs));

            request.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));


            HttpResponse response = httpClient.execute(request);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String json = reader.readLine();
            System.out.println(json);
            JSONTokener tokener = new JSONTokener(json);
            JSONObject jsonObject = new JSONObject(tokener);
            return jsonObject;
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
