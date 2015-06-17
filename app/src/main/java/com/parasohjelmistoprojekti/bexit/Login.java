package com.parasohjelmistoprojekti.bexit;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import serverconnection.LoginUser;
import serverconnection.RegisterUser;

/**
 * Created by Esa on 10.3.2015.
 */
public class Login extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        final EditText userNameField = (EditText)findViewById(R.id.username_edittext);
        final EditText password = (EditText)findViewById(R.id.password_edittext);

        Button signUpButton = (Button)findViewById(R.id.login_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject reply = new LoginUser().execute(userNameField.getText().toString(), password.getText().toString()).get();
                    if (reply.getInt("error")==0){
                        System.out.println("Succesful login");

                        SharedPreferences sharedpreferences = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        System.out.println("The id: "+reply.getString("id"));
                        System.out.println("----------------------");
                        System.out.println("----------------------");

                        System.out.println("----------------------");
                        System.out.println("----------------------");
                        System.out.println("----------------------");
                        System.out.println("----------------------");
                        System.out.println("----------------------");
                        System.out.println("----------------------");

                        editor.putString("hash", reply.getString("id"));
                        editor.putString("username", userNameField.getText().toString());
                        if(sharedpreferences.getString("nortwestLat",null)!= null) {
                            editor.putString("nortwestLat", reply.getString("northwestLat"));
                            editor.putString("northwestLon", reply.getString("northwestLon"));
                        }

                        editor.putInt("soldiers", reply.getInt("soldiers"));
                        editor.putInt("money", reply.getInt("money"));

                        editor.commit();

                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else{
                        System.out.println("Unsuccesful");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
